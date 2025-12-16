import {post, get, del, param, requestBody, response} from '@loopback/rest';
import {repository} from '@loopback/repository';
import {ExplorationEventRepository, UserStreakRepository, UserBadgeRepository} from '../repositories';
import {UserBadgeWithRelations} from '../models';
import {inject} from '@loopback/core';
import {BadgeUnlockService} from '../services/badge-unlock.service';
import {StreakCalculatorService} from '../services/streak-calculator.service';
import {ChallengeService} from '../services/challenge.service';

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
    @inject('services.ChallengeService')
    public challengeService: ChallengeService,
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

    // 4. Check for challenge completion
    const challengeResult = await this.challengeService.checkChallengeCompletion(userId);

    // 5. Return response with exploration confirmation and any new badges
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
      challengeCompleted: challengeResult.completed,
      challengeBadge: challengeResult.badge ? {
        id: challengeResult.badge.id,
        name: challengeResult.badge.name,
        description: challengeResult.badge.description,
        iconUrl: challengeResult.badge.iconUrl,
      } : undefined,
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
  console.log(`[ExplorationController] getUserStats() called for userId: ${userId}`);

  try {
    console.log('[ExplorationController] Fetching exploration count');
    const explorationCount = await this.explorationEventRepository.count({userId});
    console.log(`[ExplorationController] Found ${explorationCount.count} explorations`);

    console.log('[ExplorationController] Fetching user streak');
    const userStreak = await this.userStreakRepository.findOne({
      where: {userId},
    });
    console.log(`[ExplorationController] Current streak: ${userStreak?.currentStreak ?? 0}, Longest: ${userStreak?.longestStreak ?? 0}`);

    console.log('[ExplorationController] Fetching unlocked badges');
    const unlockedBadges = await this.userBadgeRepository.find({
      where: {userId},
      include: [{relation: 'badge'}],
    }) as UserBadgeWithRelations[];
    console.log(`[ExplorationController] Found ${unlockedBadges.length} unlocked badges`);

    const result = {
      totalExplorations: explorationCount.count,
      currentStreak: userStreak?.currentStreak ?? 0,
      longestStreak: userStreak?.longestStreak ?? 0,
      badges: unlockedBadges.map(ub => ({
        id: ub.badgeId,
        name: ub.badge?.name,
        description: ub.badge?.description,
        unlockedAt: ub.unlockedAt,
      })),
    };

    console.log('[ExplorationController] Returning user stats:', JSON.stringify(result, null, 2));
    return result;
  } catch (error) {
    console.error(`[ExplorationController] Error in getUserStats for userId ${userId}:`, error);
    throw error;
  }
}

  /**
   * DELETE /api/users/{userId}/test-data
   * Delete all test data for a user (badges, explorations, streak)
   * WARNING: This deletes all progress for the user. Use only for testing!
   */
  @del('/api/users/{userId}/test-data')
  @response(200, {
    description: 'User test data deleted successfully',
  })
  async deleteUserTestData(
    @param.path.number('userId') userId: number,
  ) {
    console.log(`[ExplorationController] Deleting test data for user ${userId}...`);

    try {
      // 1. Delete user badges
      console.log('[ExplorationController] Deleting user badges...');
      const deletedBadges = await this.userBadgeRepository.deleteAll({userId});
      console.log(`[ExplorationController] Deleted ${deletedBadges.count} badge(s)`);

      // 2. Delete exploration events
      console.log('[ExplorationController] Deleting exploration events...');
      const deletedExplorations = await this.explorationEventRepository.deleteAll({userId});
      console.log(`[ExplorationController] Deleted ${deletedExplorations.count} exploration(s)`);

      // 3. Delete user streak
      console.log('[ExplorationController] Deleting user streak...');
      const deletedStreaks = await this.userStreakRepository.deleteAll({userId});
      console.log(`[ExplorationController] Deleted ${deletedStreaks.count} streak record(s)`);

      // 4. Verify clean state
      const remainingBadges = await this.userBadgeRepository.count({userId});
      const remainingExplorations = await this.explorationEventRepository.count({userId});
      const remainingStreaks = await this.userStreakRepository.count({userId});

      const result = {
        success: true,
        message: `All test data deleted for user ${userId}`,
        deleted: {
          badges: deletedBadges.count,
          explorations: deletedExplorations.count,
          streaks: deletedStreaks.count,
        },
        remaining: {
          badges: remainingBadges.count,
          explorations: remainingExplorations.count,
          streaks: remainingStreaks.count,
        },
      };

      console.log('[ExplorationController] Cleanup result:', JSON.stringify(result, null, 2));
      return result;
    } catch (error) {
      console.error(`[ExplorationController] Error deleting test data for user ${userId}:`, error);
      throw error;
    }
  }
}
