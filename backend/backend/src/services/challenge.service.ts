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


  async checkAndExpireChallenges(userId: number): Promise<void> {
    const now = new Date();
    const activeChallenge = await this.userChallengeRepository.findOne({
      where: {userId, status: 'active'},
    });

    if (activeChallenge && activeChallenge.expiresAt && activeChallenge.expiresAt < now) {
      activeChallenge.status = 'expired';
      await this.userChallengeRepository.update(activeChallenge);
      console.log(`Challenge ${activeChallenge.challengeId} expired for user ${userId}`);
    }
  }


  async getUserChallengeStatus(userId: number): Promise<ChallengeStatus> {
    const now = new Date();

    await this.checkAndExpireChallenges(userId);

    const activeChallenge = await this.userChallengeRepository.findOne({
      where: {userId, status: 'active'},
      include: [{relation: 'challenge'}],
    });

    if (activeChallenge) {
      if (activeChallenge.expiresAt && activeChallenge.expiresAt < now) {
        activeChallenge.status = 'expired';
        await this.userChallengeRepository.update(activeChallenge);

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

    return {
      userChallenge,
      challenge,
      cooldownEndsAt,
    };
  }


  async checkChallengeCompletion(userId: number): Promise<Badge | null> {
    // First, check and expire any old challenges
    await this.checkAndExpireChallenges(userId);

    const activeUserChallenge = await this.userChallengeRepository.findOne({
      where: {userId, status: 'active'},
      include: [{relation: 'challenge'}],
    });

    if (!activeUserChallenge) {
      return null;
    }

    // Check if challenge has expired (shouldn't happen after checkAndExpireChallenges, but safety check)
    const now = new Date();
    if (activeUserChallenge.expiresAt && activeUserChallenge.expiresAt < now) {
      activeUserChallenge.status = 'expired';
      await this.userChallengeRepository.update(activeUserChallenge);
      return null;
    }

    const challenge = (activeUserChallenge as UserChallenge & {challenge: Challenge}).challenge;
    const isCompleted = await this.evaluateChallengeCondition(userId, challenge, activeUserChallenge);

    if (isCompleted) {
      // Mark as completed but keep cooldown intact
      activeUserChallenge.status = 'completed';
      activeUserChallenge.completedAt = new Date();
      await this.userChallengeRepository.update(activeUserChallenge);

      // Award the badge
      const badge = await this.awardChallengeBadge(userId, challenge.rewardBadgeId);

      return badge;
    }

    return null;
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
   * Get available challenges without cooldown check (internal use)
   */
private async getAvailableChallengesWithoutCooldownCheck(userId: number): Promise<Challenge[]> {
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
  return shuffled.slice(0, Math.min(3, shuffled.length));
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
