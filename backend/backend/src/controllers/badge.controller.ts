import {get, del, param, response} from '@loopback/rest';
import {repository} from '@loopback/repository';
import {BadgeRepository, UserBadgeRepository, ExplorationEventRepository, UserStreakRepository, UserChallengeRepository, ChallengeRepository} from '../repositories';
import {Badge, UserChallenge, Challenge} from '../models';

export class BadgeController {
  constructor(
    @repository(BadgeRepository)
    public badgeRepository: BadgeRepository,
    @repository(UserBadgeRepository)
    public userBadgeRepository: UserBadgeRepository,
    @repository(ExplorationEventRepository)
    public explorationEventRepository: ExplorationEventRepository,
    @repository(UserStreakRepository)
    public userStreakRepository: UserStreakRepository,
    @repository(UserChallengeRepository)
    public userChallengeRepository: UserChallengeRepository,
    @repository(ChallengeRepository)
    public challengeRepository: ChallengeRepository,
    description: 'List of all badges',
    content: {
      'application/json': {
        schema: {
          type: 'array',
          items: {type: 'object'},
        },
      },
    },
  })
  async getAllBadges() {
    console.log('[BadgeController] getAllBadges() called');
    try {
      const badges = await this.badgeRepository.find({
        order: ['displayOrder ASC', 'id ASC'],
      });
      console.log(`[BadgeController] Found ${badges.length} badges`);
      return badges;
    } catch (error) {
      console.error('[BadgeController] Error in getAllBadges:', error);
      throw error;
    }
  }

  /**
   * GET /api/users/{userId}/badges
   * Get badges earned by specific user
   */
  @get('/api/users/{userId}/badges')
  @response(200, {
    description: 'User badges with unlock timestamps',
  })
  async getUserBadges(
    @param.path.number('userId') userId: number,
  ) {
    console.log(`[BadgeController] getUserBadges() called for userId: ${userId}`);
    try {
      // Get user's unlocked badges with badge details
      console.log(`[BadgeController] Fetching user badges for userId ${userId}`);
      const userBadges = await this.userBadgeRepository.find({
        where: {userId},
        include: ['badge'],
        order: ['unlockedAt DESC'],
      });
      console.log(`[BadgeController] Found ${userBadges.length} user badges`);

      // Get all badges to show locked ones
      console.log('[BadgeController] Fetching all available badges');
      const allBadges = await this.badgeRepository.find({
        order: ['displayOrder ASC'],
      });
      console.log(`[BadgeController] Found ${allBadges.length} total badges`);

      // Get user's current stats for progress calculation
      const explorationCount = await this.explorationEventRepository.count({userId});
      const userStreak = await this.userStreakRepository.findOne({where: {userId}});
      const currentStreak = userStreak?.currentStreak ?? 0;

      console.log(`[BadgeController] User stats - Explorations: ${explorationCount.count}, Streak: ${currentStreak}`);

      // Map badges to include unlock status
      const unlockedIds = new Set(userBadges.map(ub => ub.badgeId));
      console.log(`[BadgeController] Unlocked badge IDs: ${Array.from(unlockedIds).join(', ')}`);

      // Get active challenge to calculate progress for challenge badges
      const now = new Date();
      const activeChallenge = await this.userChallengeRepository.findOne({
        where: {
          userId,
          status: 'active',
        },
        include: [{relation: 'challenge'}],
      });

      let activeChallengeData: {userChallenge: UserChallenge; challenge: Challenge; rewardBadgeId: number} | null = null;

      if (activeChallenge && activeChallenge.expiresAt && activeChallenge.expiresAt > now) {
        const challenge = (activeChallenge as UserChallenge & {challenge: Challenge}).challenge;
        activeChallengeData = {
          userChallenge: activeChallenge,
          challenge,
          rewardBadgeId: challenge.rewardBadgeId,
        };
        console.log(`[BadgeController] Active challenge found: ${challenge.name}, reward badge ID: ${challenge.rewardBadgeId}`);
      }

        earnedBadges: userBadges.length,
        totalBadges: allBadges.length,
      };

      const badgesWithStatus = await Promise.all(allBadges.map(async (badge: Badge) => {
      return result;
    } catch (error) {
        let requiresActiveChallenge = false;
      throw error;
        // Check if this is a challenge badge
        if (badge.isChallengeBadge) {
          requiresActiveChallenge = true;

          // Only calculate progress if this badge's challenge is currently active
          if (activeChallengeData && activeChallengeData.rewardBadgeId === badge.id) {
            const {userChallenge, challenge} = activeChallengeData;
            const window = {
              start: userChallenge.activatedAt,
              end: userChallenge.expiresAt && userChallenge.expiresAt < now ? userChallenge.expiresAt : now,
            };

            if (challenge.conditionType === 'exploration_count') {
              // Count explorations within the challenge window
              const count = await this.explorationEventRepository.count({
                userId,
                completedAt: {between: [window.start, window.end]},
              });
              currentProgress = Math.min(count.count, maxProgress);
              console.log(`[BadgeController] Challenge badge ${badge.name}: ${currentProgress}/${maxProgress}`);
            } else if (challenge.conditionType === 'time_based') {
              // Check if any exploration within the window matches the target hour
              const targetHour = challenge.conditionParams.hour ?? 0;
              const normalizedTargetHour = ((targetHour % 24) + 24) % 24;

              const explorations = await this.explorationEventRepository.find({
                where: {
                  userId,
                  completedAt: {between: [window.start, window.end]},
                },
              });

              const hasMatchingExploration = explorations.some(exp => {
                const completedAt = exp.completedAt ? new Date(exp.completedAt) : undefined;
                if (!completedAt) return false;
                return completedAt.getUTCHours() === normalizedTargetHour;
              });

              currentProgress = hasMatchingExploration ? 1 : 0;
              maxProgress = 1;
              console.log(`[BadgeController] Time-based challenge badge ${badge.name}: ${currentProgress}/${maxProgress}`);
            }
          } else {
            // Challenge badge but its challenge is not active - show 0 progress
            currentProgress = 0;
            console.log(`[BadgeController] Challenge badge ${badge.name} - no active challenge, progress: 0/${maxProgress}`);
          }
        } else {
          // Regular badge (not challenge-specific)
          if (badge.unlockCriteria.type === 'exploration_count') {
            currentProgress = Math.min(explorationCount.count, maxProgress);
          } else if (badge.unlockCriteria.type === 'streak') {
            currentProgress = Math.min(currentStreak, maxProgress);
          }
