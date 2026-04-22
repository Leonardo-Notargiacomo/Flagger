import {injectable, BindingScope} from '@loopback/core';
import {repository} from '@loopback/repository';
import {HttpErrors} from '@loopback/rest';
import {
  ChallengeRepository,
  UserChallengeRepository,
  ExplorationEventRepository,
  UserStreakRepository,
  BadgeRepository,
  UserBadgeRepository,
} from '../repositories';
import {Challenge, UserChallenge, Badge} from '../models';

export interface ChallengeCard {
  challenge: Challenge;
  progress?: {
    current: number;
    target: number;
  };
}

export interface ChallengeSelectionResult {
  userChallenge: UserChallenge;
  challenge: Challenge;
  cooldownEndsAt: Date;
}

export interface ChallengeStatus {
  hasActiveChallenge: boolean;
  activeChallenge?: UserChallenge & {challenge: Challenge};
  isOnCooldown: boolean;
  cooldownEndsAt?: Date;
  availableChallenges?: Challenge[];
}

@injectable({scope: BindingScope.TRANSIENT})
export class ChallengeService {
  // Cache for user's available challenges (userId -> {challenges, expiresAt})
  private static challengeCache: Map<number, {challenges: Challenge[], expiresAt: Date}> = new Map();

  constructor(
    @repository(ChallengeRepository)
    public challengeRepository: ChallengeRepository,
    @repository(UserChallengeRepository)
    public userChallengeRepository: UserChallengeRepository,
    @repository(ExplorationEventRepository)
    public explorationEventRepository: ExplorationEventRepository,
    @repository(UserStreakRepository)
    public userStreakRepository: UserStreakRepository,
    @repository(BadgeRepository)
    public badgeRepository: BadgeRepository,
    @repository(UserBadgeRepository)
    public userBadgeRepository: UserBadgeRepository,
  ) {}

  /**
   * Get 3 random challenge cards for a user
   * Excludes challenges the user has already completed
   */
  async getAvailableChallenges(userId: number): Promise<Challenge[]> {
    // Check if user is on cooldown
    const status = await this.getUserChallengeStatus(userId);
    if (status.isOnCooldown) {
      throw new HttpErrors.Forbidden(
        `You are on cooldown. Please wait until ${status.cooldownEndsAt?.toISOString()}`
      );
    }

    const completedUserChallenges = await this.userChallengeRepository.find({
      where: {userId, status: 'completed'},
    });
    const completedChallengeIds = completedUserChallenges.map(uc => uc.challengeId);

    const allChallenges = await this.challengeRepository.find({
      where: {
        isActive: true,
        id: completedChallengeIds.length > 0 ? {nin: completedChallengeIds} : undefined,
      },
    });

    if (allChallenges.length === 0) {
      return [];
    }

    const shuffled = this.shuffleArray(allChallenges);
    return shuffled.slice(0, Math.min(3, shuffled.length));
  }


  /**
   * Check if user's active challenge has passed its expiry time and mark it expired
   */
  async checkAndExpireChallenges(userId: number): Promise<void> {
    const now = new Date();
    const activeChallenge = await this.userChallengeRepository.findOne({
      where: {userId, status: 'active'},
    });

    if (activeChallenge && activeChallenge.expiresAt && activeChallenge.expiresAt < now) {
      // Update status without navigational properties
      await this.userChallengeRepository.updateById(activeChallenge.id, {
        status: 'expired'
      });
      console.log(`Challenge ${activeChallenge.challengeId} expired for user ${userId}`);
    }
  }


  /**
   * Get full challenge status for a user:
   * - If active challenge exists → return it
   * - If expired → mark expired, return cooldown
   * - If on cooldown → return cooldown end time
   * - Otherwise → return available challenges to pick from
   */
  async getUserChallengeStatus(userId: number): Promise<ChallengeStatus> {
    const now = new Date();

    await this.checkAndExpireChallenges(userId);

    const activeChallenge = await this.userChallengeRepository.findOne({
      where: {userId, status: 'active'},
      include: [{relation: 'challenge'}],
    });

    if (activeChallenge) {
      if (activeChallenge.expiresAt && activeChallenge.expiresAt < now) {
        // Update status without navigational properties
        await this.userChallengeRepository.updateById(activeChallenge.id, {
          status: 'expired'
        });

        // Set cooldown
        return {
          hasActiveChallenge: false,
          isOnCooldown: true,
          cooldownEndsAt: activeChallenge.cooldownEndsAt,
        };
      }

      return {
        hasActiveChallenge: true,
        activeChallenge: activeChallenge as UserChallenge & {challenge: Challenge},
        isOnCooldown: false,
      };
    }

    // Check for cooldown from most recent challenge
    const recentChallenge = await this.userChallengeRepository.findOne({
      where: {userId},
      order: ['activatedAt DESC'],
    });

    if (recentChallenge?.cooldownEndsAt && recentChallenge.cooldownEndsAt > now) {
      return {
        hasActiveChallenge: false,
        isOnCooldown: true,
        cooldownEndsAt: recentChallenge.cooldownEndsAt,
      };
    }

    // No active challenge and no cooldown
    const availableChallenges = await this.getAvailableChallengesWithoutCooldownCheck(userId);
    return {
      hasActiveChallenge: false,
      isOnCooldown: false,
      availableChallenges,
    };
  }

  /**
   * Select a challenge for the user:
   * - Guards: no cooldown, no active challenge, challenge is active, not already completed
   * - Sets expiresAt (24h default, custom for streak type)
   * - Sets cooldownEndsAt so user must wait after completion/expiry
   * - Initializes progressData based on challenge type
   */
  async selectChallenge(userId: number, challengeId: number): Promise<ChallengeSelectionResult> {
    const status = await this.getUserChallengeStatus(userId);
    if (status.isOnCooldown) {
      throw new HttpErrors.Forbidden('You are on cooldown. Please wait before selecting a new challenge.');
    }
    if (status.hasActiveChallenge) {
      throw new HttpErrors.BadRequest('You already have an active challenge.');
    }

    // Get the challenge
    const challenge = await this.challengeRepository.findById(challengeId);
    if (!challenge.isActive) {
      throw new HttpErrors.BadRequest('This challenge is not available.');
    }

    // Check if user already completed this challenge
    const existingCompletion = await this.userChallengeRepository.findOne({
      where: {userId, challengeId, status: 'completed'},
    });
    if (existingCompletion) {
      throw new HttpErrors.BadRequest('You have already completed this challenge.');
    }

    const now = new Date();
    const cooldownEndsAt = new Date(now.getTime() + (challenge.cooldownHours ?? 24) * 60 * 60 * 1000);

    let expiresAt: Date | undefined;
    if (challenge.conditionType === 'streak') {
      // Streak challenges can have custom expiration or no expiration
      expiresAt = challenge.expirationHours
        ? new Date(now.getTime() + challenge.expirationHours * 60 * 60 * 1000)
        : undefined;
    } else {
      // All other challenges expire in 24 hours (or custom if specified)
      const expirationTime = challenge.expirationHours ?? 24;
      expiresAt = new Date(now.getTime() + expirationTime * 60 * 60 * 1000);
    }

    const userChallenge = await this.userChallengeRepository.create({
      userId,
      challengeId,
      status: 'active',
      activatedAt: now,
      cooldownEndsAt,
      expiresAt,
      progressData: this.initializeProgressData(challenge),
    });

    // Clear the challenges cache so user gets new options after cooldown
    ChallengeService.challengeCache.delete(userId);
    console.log(`Cleared challenge cache for user ${userId} after selecting challenge`);

    return {
      userChallenge,
      challenge,
      cooldownEndsAt,
    };
  }


  /**
   * Check if user's active challenge condition is now met:
   * - Expires stale challenges first
   * - Updates progressData with latest counts
   * - Evaluates condition (exploration_count, time_based, streak)
   * - If met: marks completed, awards badge if configured
   */
  async checkChallengeCompletion(userId: number): Promise<{completed: boolean, badge: Badge | null}> {
    // First, check and expire any old challenges
    await this.checkAndExpireChallenges(userId);

    const activeUserChallenge = await this.userChallengeRepository.findOne({
      where: {userId, status: 'active'},
      include: [{relation: 'challenge'}],
    });

    if (!activeUserChallenge) {
      return {completed: false, badge: null};
    }

    // Check if challenge has expired (shouldn't happen after checkAndExpireChallenges, but safety check)
    const now = new Date();
    if (activeUserChallenge.expiresAt && activeUserChallenge.expiresAt < now) {
      await this.userChallengeRepository.updateById(activeUserChallenge.id, {
        status: 'expired'
      });
      return {completed: false, badge: null};
    }

    const challenge = (activeUserChallenge as UserChallenge & {challenge: Challenge}).challenge;

    // Update progress data before checking completion
    const updatedProgressData = await this.updateProgressData(userId, challenge, activeUserChallenge);
    if (updatedProgressData) {
      await this.userChallengeRepository.updateById(activeUserChallenge.id, {
        progressData: updatedProgressData
      });
    }

    const isCompleted = await this.evaluateChallengeCondition(userId, challenge, activeUserChallenge);

    if (isCompleted) {
      // Mark as completed but keep cooldown intact (without navigational properties)
      await this.userChallengeRepository.updateById(activeUserChallenge.id, {
        status: 'completed',
        completedAt: new Date()
      });

      // Award the badge if there is one
      let badge: Badge | null = null;
      if (challenge.rewardBadgeId) {
        badge = await this.awardChallengeBadge(userId, challenge.rewardBadgeId);
      }

      return {completed: true, badge};
    }

    return {completed: false, badge: null};
  }

  /**
   * Award a badge to a user
   */
  private async awardChallengeBadge(userId: number, badgeId: number): Promise<Badge> {
    // Check if user already has this badge
    const existing = await this.userBadgeRepository.findOne({
      where: {userId, badgeId},
    });

    if (!existing) {
      await this.userBadgeRepository.create({
        userId,
        badgeId,
        unlockedAt: new Date(),
        notificationSent: false,
      });
    }

    const badge = await this.badgeRepository.findById(badgeId);
    return badge;
  }

  /**
   * Evaluate if the challenge condition is met
   * - exploration_count: counts exploration events within activation window
   * - time_based: checks if any exploration happened at the target UTC hour
   * - streak: checks if user's current streak meets the target days
   */
  protected async evaluateChallengeCondition(
    userId: number,
    challenge: Challenge,
    userChallenge: UserChallenge,
  ): Promise<boolean> {
    if (userChallenge.status !== 'active') {
      return false;
    }

    const window = this.resolveEvaluationWindow(userChallenge, new Date());
    if (!window) {
      return false;
    }

    switch (challenge.conditionType) {
      case 'exploration_count': {
        const count = await this.explorationEventRepository.count({
          userId,
          completedAt: {between: [window.start, window.end]},
        });
        const target = challenge.conditionParams.count ?? 0;
        return count.count >= target;
      }

      case 'time_based': {
        const targetHourRaw = challenge.conditionParams.hour ?? 0;
        const normalizedTargetHour = ((targetHourRaw % 24) + 24) % 24;
        const explorations = await this.explorationEventRepository.find({
          where: {
            userId,
            completedAt: {between: [window.start, window.end]},
          },
        });

        return explorations.some(exp => {
          const completedAt = exp.completedAt ? new Date(exp.completedAt) : undefined;
          if (!completedAt) {
            return false;
          }
          return completedAt.getUTCHours() === normalizedTargetHour;
        });
      }

      case 'streak': {
        const userStreak = await this.userStreakRepository.findOne({where: {userId}});
        const targetDays = challenge.conditionParams.days ?? 0;
        return (userStreak?.currentStreak ?? 0) >= targetDays;
      }


      default:
        return false;
    }
  }

  /**
   * Resolve the time window for evaluating a challenge:
   * start = activatedAt, end = min(expiresAt, now)
   * Returns null if activatedAt is missing or window is invalid
   */
  private resolveEvaluationWindow(
    userChallenge: UserChallenge,
    referenceDate: Date = new Date(),
  ): {start: Date; end: Date} | null {
    if (!userChallenge.activatedAt) {
      return null;
    }

    const start = new Date(userChallenge.activatedAt);
    const rawEnd = userChallenge.expiresAt ? new Date(userChallenge.expiresAt) : referenceDate;
    const end = rawEnd > referenceDate ? referenceDate : rawEnd;

    if (end < start) {
      return null;
    }

    return {start, end};
  }

  /**
   * Initialize progress data based on challenge type
   */
  private initializeProgressData(challenge: Challenge): object {
    switch (challenge.conditionType) {
      case 'exploration_count':
        return {
          currentCount: 0,
          targetCount: challenge.conditionParams.count,
        };
      case 'time_based':
        return {
          targetHour: challenge.conditionParams.hour,
          completed: false,
        };
      case 'streak':
        return {
          currentStreak: 0,
          targetStreak: challenge.conditionParams.days,
        };
      default:
        return {};
    }
  }

  /**
   * Update progress data with actual current progress
   */
  private async updateProgressData(
    userId: number,
    challenge: Challenge,
    userChallenge: UserChallenge,
  ): Promise<object | null> {
    const window = this.resolveEvaluationWindow(userChallenge, new Date());
    if (!window) {
      return null;
    }

    switch (challenge.conditionType) {
      case 'exploration_count': {
        const count = await this.explorationEventRepository.count({
          userId,
          completedAt: {between: [window.start, window.end]},
        });
        return {
          currentCount: count.count,
          targetCount: challenge.conditionParams.count,
        };
      }
      case 'streak': {
        const userStreak = await this.userStreakRepository.findOne({where: {userId}});
        return {
          currentStreak: userStreak?.currentStreak ?? 0,
          targetStreak: challenge.conditionParams.days,
        };
      }
      case 'time_based': {
        // Time-based challenges don't have incremental progress
        return null;
      }
      default:
        return null;
    }
  }

  /**
   * Get available challenges without cooldown check (internal use)
   * Caches the selection for 24 hours
   */
private async getAvailableChallengesWithoutCooldownCheck(userId: number): Promise<Challenge[]> {
  const now = new Date();

  // Check cache first
  const cached = ChallengeService.challengeCache.get(userId);
  if (cached && cached.expiresAt > now) {
    console.log(`Returning cached challenges for user ${userId}, expires at ${cached.expiresAt}`);
    return cached.challenges;
  }

  // Cache miss or expired, fetch new challenges
  const completedUserChallenges = await this.userChallengeRepository.find({
    where: {userId, status: 'completed'},
  });
  const completedChallengeIds = completedUserChallenges.map(uc => uc.challengeId);

  const allChallenges = await this.challengeRepository.find({
    where: {
      isActive: true,
      id: completedChallengeIds.length > 0 ? {nin: completedChallengeIds} : undefined,
    },
  });

  const shuffled = this.shuffleArray(allChallenges);
  const selectedChallenges = shuffled.slice(0, Math.min(3, shuffled.length));

  // Cache for 24 hours
  const expiresAt = new Date(now.getTime() + 24 * 60 * 60 * 1000);
  ChallengeService.challengeCache.set(userId, {
    challenges: selectedChallenges,
    expiresAt
  });

  console.log(`Cached new challenges for user ${userId}, expires at ${expiresAt}`);
  return selectedChallenges;
}


  /**
   * Shuffle array utility
   */
  private shuffleArray<T>(array: T[]): T[] {
    const shuffled = [...array];
    for (let i = shuffled.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
    }
    return shuffled;
  }

}
