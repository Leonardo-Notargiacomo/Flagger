import {get, del, param, response} from '@loopback/rest';
import {repository} from '@loopback/repository';
import {BadgeRepository, UserBadgeRepository, ExplorationEventRepository, UserStreakRepository, UserChallengeRepository} from '../repositories';
import {Badge} from '../models';

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
  ) {}

  @get('/api/badges')
  @response(200, {
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

      // Get active challenge to calculate progress for challenge badges
      const activeChallenge = await this.userChallengeRepository.findOne({
        where: {userId, status: 'active'},
        include: [{relation: 'challenge'}],
      });

      console.log(`[BadgeController] Active challenge: ${activeChallenge ? `Challenge ID ${activeChallenge.challengeId}` : 'None'}`);

      // Pre-calculate challenge exploration count once if there's an active challenge
      let challengeExplorationCount = 0;
      if (activeChallenge) {
        const challengeStart = activeChallenge.activatedAt;
        const now = new Date();
        const result = await this.explorationEventRepository.count({
          userId,
          completedAt: {between: [challengeStart, now]},
        });
        challengeExplorationCount = result.count;
        console.log(`[BadgeController] Challenge exploration count: ${challengeExplorationCount}`);
      }

      // Map badges to include unlock status
      const unlockedIds = new Set(userBadges.map(ub => ub.badgeId));
      console.log(`[BadgeController] Unlocked badge IDs: ${Array.from(unlockedIds).join(', ')}`);

      const badgesWithStatus = allBadges.map((badge: Badge) => {
        // Calculate progress based on badge criteria
        let currentProgress = 0;
        let maxProgress = badge.unlockCriteria.threshold;

        // Challenge badges: only show progress when the respective challenge is active
        if (badge.isChallengeBadge) {
          // If badge is already unlocked, show full progress
          if (unlockedIds.has(badge.id!)) {
            currentProgress = maxProgress;
          }
          // If there's an active challenge and it matches this badge's reward
          else if (activeChallenge && activeChallenge.challenge?.rewardBadgeId === badge.id) {
            // Use pre-calculated challenge exploration count
            if (badge.unlockCriteria.type === 'exploration_count') {
              currentProgress = Math.min(challengeExplorationCount, maxProgress);
            } else if (badge.unlockCriteria.type === 'streak') {
              // For streak challenges, use current streak
              currentProgress = Math.min(currentStreak, maxProgress);
            }
          }
          // If no active challenge or different challenge is active, progress stays 0
        } else {
          // Regular badges: use total stats
          if (badge.unlockCriteria.type === 'exploration_count') {
            currentProgress = Math.min(explorationCount.count, maxProgress);
          } else if (badge.unlockCriteria.type === 'streak') {
            currentProgress = Math.min(currentStreak, maxProgress);
          }
        }

        return {
          ...badge,
          isUnlocked: unlockedIds.has(badge.id!),
          unlockedAt: userBadges.find(ub => ub.badgeId === badge.id)?.unlockedAt,
          currentProgress,
          maxProgress,
        };
      });

      const result = {
        badges: badgesWithStatus,
        earnedBadges: userBadges.length,
        totalBadges: allBadges.length,
      };

      console.log(`[BadgeController] Returning: ${result.earnedBadges}/${result.totalBadges} badges earned`);
      return result;
    } catch (error) {
      console.error('[BadgeController] Error in getUserBadges:', error);
      throw error;
    }
  }

  /**
   * DELETE /api/users/{userId}/badges
   * Delete all badges for a specific user
   */
  @del('/api/users/{userId}/badges')
  @response(200, {
    description: 'User badges deleted successfully',
  })
  async deleteUserBadges(
    @param.path.number('userId') userId: number,
  ) {
    console.log(`[BadgeController] deleteUserBadges() called for userId: ${userId}`);
    try {
      const deletedBadges = await this.userBadgeRepository.deleteAll({userId});
      console.log(`[BadgeController] Deleted ${deletedBadges.count} badge(s) for user ${userId}`);

      return {
        success: true,
        message: `Deleted ${deletedBadges.count} badge(s) for user ${userId}`,
        deletedCount: deletedBadges.count,
      };
    } catch (error) {
      console.error(`[BadgeController] Error deleting badges for user ${userId}:`, error);
      throw error;
    }
  }
}
