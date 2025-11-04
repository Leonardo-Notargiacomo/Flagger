import {post, get, param, requestBody, response} from '@loopback/rest';
import {repository} from '@loopback/repository';
import {ExplorationEventRepository, UserStreakRepository, UserBadgeRepository} from '../repositories';
import {UserBadgeWithRelations} from '../models';
import {inject} from '@loopback/core';
import {BadgeUnlockService} from '../services/badge-unlock.service';
import {StreakCalculatorService} from '../services/streak-calculator.service';

export class ExplorationController {
  constructor(
    @repository(ExplorationEventRepository)
    public explorationEventRepository: ExplorationEventRepository,
    @repository(UserStreakRepository)
    public userStreakRepository: UserStreakRepository,
    @repository(UserBadgeRepository)
    public userBadgeRepository: UserBadgeRepository,
    @inject('services.BadgeUnlockService')
    public badgeUnlockService: BadgeUnlockService,
    @inject('services.StreakCalculatorService')
    public streakCalculatorService: StreakCalculatorService,
  ) {}

  /**
   * POST /api/users/{userId}/explorations
   * Log a new exploration event (triggers badge checks)
   */
  @post('/api/users/{userId}/explorations')
  @response(200, {
    description: 'Exploration logged successfully',
  })
  async logExploration(
    @param.path.number('userId') userId: number,
    @requestBody({
      content: {
        'application/json': {
          schema: {
            type: 'object',
            properties: {
              locationName: {type: 'string'},
              latitude: {type: 'number'},
              longitude: {type: 'number'},
              notes: {type: 'string'},
            },
          },
        },
      },
    })
    explorationData: {
      locationName?: string;
      latitude?: number;
      longitude?: number;
      notes?: string;
    },
  ) {
    // 1. Save exploration event
    const event = await this.explorationEventRepository.create({
      userId,
      ...explorationData,
      completedAt: new Date(),
    });

    // 2. Update user's streak
    const updatedStreak = await this.streakCalculatorService.updateStreak(userId);

    // 3. Check for newly unlocked badges
    const newBadges = await this.badgeUnlockService.checkAndUnlockBadges(userId);

    // 4. Return response with exploration confirmation and any new badges
    return {
      success: true,
      event,
      streak: {
        current: updatedStreak.currentStreak,
        longest: updatedStreak.longestStreak,
      },
      newBadges: newBadges.map(b => ({
        id: b.id,
        name: b.name,
        description: b.description,
        iconUrl: b.iconUrl,
      })),
    };
  }

  /**
   * GET /api/users/{userId}/stats
   * Get user's exploration statistics
   */
@get('/api/users/{userId}/stats')
@response(200, {
  description: 'User exploration statistics',
})
async getUserStats(
  @param.path.number('userId') userId: number,
) {
  const explorationCount = await this.explorationEventRepository.count({userId});
  const userStreak = await this.userStreakRepository.findOne({
    where: {userId},
  });

  // ADD THESE LINES
  const unlockedBadges = await this.userBadgeRepository.find({
    where: {userId},
    include: [{relation: 'badge'}],
  }) as UserBadgeWithRelations[];

  return {
    totalExplorations: explorationCount.count,
    currentStreak: userStreak?.currentStreak ?? 0,
    longestStreak: userStreak?.longestStreak ?? 0,
    badges: unlockedBadges.map(ub => ({ // ADD THIS
      id: ub.badgeId,
      name: ub.badge?.name,
      description: ub.badge?.description,
      unlockedAt: ub.unlockedAt,
    })),
  };
}
}
