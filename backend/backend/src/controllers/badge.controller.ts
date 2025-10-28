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
    return this.badgeRepository.find({
      order: ['displayOrder ASC', 'id ASC'],
    });
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
    // Get user's unlocked badges with badge details
    const userBadges = await this.userBadgeRepository.find({
      where: {userId},
      include: ['badge'],
      order: ['unlockedAt DESC'],
    });

    // Get all badges to show locked ones
    const allBadges = await this.badgeRepository.find({
      order: ['displayOrder ASC'],
    });

    // Map badges to include unlock status
    const unlockedIds = new Set(userBadges.map(ub => ub.badgeId));

    const badgesWithStatus = allBadges.map(badge => ({
      ...badge,
      isUnlocked: unlockedIds.has(badge.id!),
      unlockedAt: userBadges.find(ub => ub.badgeId === badge.id)?.unlockedAt,
    }));

    return {
      badges: badgesWithStatus,
      totalBadges: allBadges.length,
      earnedBadges: userBadges.length,
    };
  }
}
