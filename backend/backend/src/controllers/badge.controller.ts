import {get, param, response} from '@loopback/rest';
import {repository} from '@loopback/repository';
import {BadgeRepository, UserBadgeRepository} from '../repositories';

export class BadgeController {
  constructor(
    @repository(BadgeRepository)
    public badgeRepository: BadgeRepository,
    @repository(UserBadgeRepository)
    public userBadgeRepository: UserBadgeRepository,
  ) {}

  /**
   * GET /api/badges
   * Get all available badges in the system
   */
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

      // Map badges to include unlock status
      const unlockedIds = new Set(userBadges.map(ub => ub.badgeId));
      console.log(`[BadgeController] Unlocked badge IDs: ${Array.from(unlockedIds).join(', ')}`);

      const badgesWithStatus = allBadges.map(badge => ({
        ...badge,
        isUnlocked: unlockedIds.has(badge.id!),
        unlockedAt: userBadges.find(ub => ub.badgeId === badge.id)?.unlockedAt,
      }));

      const result = {
        badges: badgesWithStatus,
        totalBadges: allBadges.length,
        earnedBadges: userBadges.length,
      };

      console.log(`[BadgeController] Returning: ${result.earnedBadges}/${result.totalBadges} badges earned`);
      return result;
    } catch (error) {
      console.error(`[BadgeController] Error in getUserBadges for userId ${userId}:`, error);
      throw error;
    }
  }
}
