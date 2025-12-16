import {injectable, BindingScope} from '@loopback/core';
import {repository} from '@loopback/repository';
import {BadgeRepository, UserBadgeRepository, ExplorationEventRepository, UserStreakRepository} from '../repositories';
import {Badge, UserBadge} from '../models';
// TODO: Firebase service is in backend/functions, needs to be integrated properly
// import {FirebaseService} from './firebase.service';

@injectable({scope: BindingScope.TRANSIENT})
export class BadgeUnlockService {
  constructor(
    @repository(BadgeRepository)
    public badgeRepository: BadgeRepository,
    @repository(UserBadgeRepository)
    public userBadgeRepository: UserBadgeRepository,
    @repository(ExplorationEventRepository)
    public explorationEventRepository: ExplorationEventRepository,
    @repository(UserStreakRepository)
    public userStreakRepository: UserStreakRepository,
    // TODO: Re-enable Firebase service once integrated
    // @inject('services.FirebaseService')
    // public firebaseService: FirebaseService,
  ) {}

  /**
   * Check all badge criteria for a user and unlock eligible badges
   * @param userId - The user ID to check badges for
   * @returns Array of newly unlocked badges
   */
  async checkAndUnlockBadges(userId: number): Promise<Badge[]> {
    const allBadges = await this.badgeRepository.find();

    const userBadges = await this.userBadgeRepository.find({
      where: {userId}
    });
    const unlockedBadgeIds = userBadges.map(ub => ub.badgeId);

    const explorationCount = await this.explorationEventRepository.count({userId});
    const userStreak = await this.userStreakRepository.findOne({where: {userId}});
    const currentStreak = userStreak?.currentStreak ?? 0;

    const newlyUnlocked: Badge[] = [];

    for (const badge of allBadges) {
      // Challenge-specific badges are awarded exclusively through challenges
      if (badge.isChallengeBadge) {
        continue;
      }

      if (unlockedBadgeIds.includes(badge.id!)) {
        continue;
      }

      // Check unlock criteria
      const isUnlocked = this.checkCriteria(
        badge.unlockCriteria,
        explorationCount.count,
        currentStreak
      );

      if (isUnlocked) {
        // Award a new badge
        await this.userBadgeRepository.create({
          userId,
          badgeId: badge.id!,
          unlockedAt: new Date(),
          notificationSent: false,
        });

        newlyUnlocked.push(badge);

        // Send notification asynchronously
        this.sendBadgeNotification(userId, badge).catch(err => {
          console.error('Failed to send badge notification:', err);
        });
      }
    }

    return newlyUnlocked;
  }

  /**
   * Check if criteria is met
   */
  private checkCriteria(
    criteria: {type: string; threshold: number},
    explorationCount: number,
    currentStreak: number
  ): boolean {
    switch (criteria.type) {
      case 'exploration_count':
        return explorationCount >= criteria.threshold;
      case 'streak':
        return currentStreak >= criteria.threshold;
      default:
        return false;
    }
  }

  /**
   * Send push notification via Firebase
   * TODO: Re-enable once Firebase service is integrated
   */
  private async sendBadgeNotification(userId: number, badge: Badge): Promise<void> {
    try {
      // TODO: Uncomment once Firebase service is available
      // await this.firebaseService.sendToUser(userId, {
      //   notification: {
      //     title: '🏆 Badge Unlocked!',
      //     body: `You've earned the ${badge.name} badge!`,
      //   },
      //   data: {
      //     type: 'badge_unlock',
      //     badgeId: badge.id!.toString(),
      //     badgeName: badge.name,
      //   },
      // });

      console.log(`📢 Badge notification would be sent to user ${userId}: ${badge.name}`);

      // Mark notification as sent
      await this.userBadgeRepository.updateAll(
        {notificationSent: true},
        {userId, badgeId: badge.id!}
      );
    } catch (error) {
      console.error('Badge notification logging failed:', error);
      // Don't throw - notification failure shouldn't break badge unlocking
    }
  }
}
