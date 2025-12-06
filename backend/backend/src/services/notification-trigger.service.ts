import {injectable, BindingScope} from '@loopback/core';
import {repository} from '@loopback/repository';
import {
  FcmTokenRepository,
  NotificationHistoryRepository,
  UserStreakRepository,
  ExplorationEventRepository,
} from '../repositories';

export interface NotificationTarget {
  userId: number;
  fcmTokens: string[];
  reason: string;
  context: any;
}

export interface NotificationContent {
  title: string;
  body: string;
  type: string;
  data: Record<string, string>;
}

interface NotificationRules {
  minDaysBetween: number;
  maxPerMonth: number;
}

interface UserExplorationCount {
  userid: number;
  count: number;
}

interface UserDismissalCount {
  userid: number;
  dismisscount: number;
}

const NOTIFICATION_FREQUENCY_RULES: Record<string, NotificationRules> = {
  'doing_well': {
    minDaysBetween: 7, // Max once per week for high streak
    maxPerMonth: 4,
  },
  'skipping': {
    minDaysBetween: 2, // Max every 2 days
    maxPerMonth: 10,
  },
};

@injectable({scope: BindingScope.TRANSIENT})
export class NotificationTriggerService {
  constructor(
    @repository(FcmTokenRepository)
    public fcmTokenRepository: FcmTokenRepository,
    @repository(NotificationHistoryRepository)
    public notificationHistoryRepository: NotificationHistoryRepository,
    @repository(UserStreakRepository)
    public userStreakRepository: UserStreakRepository,
    @repository(ExplorationEventRepository)
    public explorationEventRepository: ExplorationEventRepository,
  ) {}

  /**
   * Get users who are "doing well" and deserve positive reinforcement
   */
  async getUsersDoingWell(): Promise<NotificationTarget[]> {
    const targets: NotificationTarget[] = [];

    // 1. High streak (5+ days)
    const highStreakUsers = await this.userStreakRepository.find({
      where: {
        currentStreak: {gte: 5},
      },
    });

    for (const streak of highStreakUsers) {
      const shouldSend = await this.shouldSendNotification(streak.userId, 'doing_well');

      if (shouldSend) {
        const tokens = await this.getActiveTokens(streak.userId);

        if (tokens.length > 0) {
          targets.push({
            userId: streak.userId,
            fcmTokens: tokens,
            reason: 'high_streak',
            context: {streak: streak.currentStreak},
          });
        }
      }
    }

    // 2. Multiple explorations per day (2+ today)
    const startOfDay = new Date();
    startOfDay.setHours(0, 0, 0, 0);

    const activeUsersResult = await this.explorationEventRepository.execute(
      `SELECT userid, COUNT(*) as count
       FROM explorationevent
       WHERE completedat >= $1
       GROUP BY userid
       HAVING COUNT(*) >= 2`,
      [startOfDay],
    ) as unknown as UserExplorationCount[];

    for (const user of activeUsersResult) {
      // Use different frequency for multiple explorations (every 3 days)
      const lastNotification = await this.getLastNotification(
        user.userid,
        'doing_well',
      );

      const daysSince = lastNotification
        ? this.daysSince(lastNotification.sentAt!)
        : 999;

      if (daysSince >= 3) {
        const tokens = await this.getActiveTokens(user.userid);

        if (tokens.length > 0) {
          targets.push({
            userId: user.userid,
            fcmTokens: tokens,
            reason: 'multiple_explorations_today',
            context: {explorationsToday: user.count},
          });
        }
      }
    }

    return targets;
  }

  /**
   * Get users who are "skipping" and need re-engagement
   */
  async getUsersSkipping(): Promise<NotificationTarget[]> {
    const targets: NotificationTarget[] = [];

    // 1. Not exploring for 3+ days
    const threeDaysAgo = new Date();
    threeDaysAgo.setDate(threeDaysAgo.getDate() - 3);

    const inactiveUsers = await this.userStreakRepository.find({
      where: {
        lastActivityDate: {lt: threeDaysAgo},
        currentStreak: {gt: 0}, // Had a streak, now inactive
      },
    });

    for (const user of inactiveUsers) {
      const shouldSend = await this.shouldSendNotification(user.userId, 'skipping');

      if (shouldSend) {
        const tokens = await this.getActiveTokens(user.userId);

        if (tokens.length > 0) {
          const daysSinceActivity = this.daysSince(user.lastActivityDate!);
          targets.push({
            userId: user.userId,
            fcmTokens: tokens,
            reason: 'inactive_for_days',
            context: {daysSinceActivity},
          });
        }
      }
    }

    // 2. Breaking their streak (current streak = 0, longest > 0)
    const brokenStreakUsers = await this.userStreakRepository.find({
      where: {
        currentStreak: 0,
        longestStreak: {gt: 0},
      },
    });

    for (const user of brokenStreakUsers) {
      // Check last notification specifically for broken streak (allow daily)
      const lastNotification = await this.getLastNotification(
        user.userId,
        'skipping',
      );

      const daysSince = lastNotification
        ? this.daysSince(lastNotification.sentAt!)
        : 999;

      if (daysSince >= 1) {
        const tokens = await this.getActiveTokens(user.userId);

        if (tokens.length > 0) {
          targets.push({
            userId: user.userId,
            fcmTokens: tokens,
            reason: 'streak_broken',
            context: {longestStreak: user.longestStreak},
          });
        }
      }
    }

    // 3. Dismissing notifications (3+ dismissals in last 7 days)
    const sevenDaysAgo = new Date();
    sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);

    const frequentDismissers = await this.notificationHistoryRepository.execute(
      `SELECT userid, COUNT(*) as dismisscount
       FROM notificationhistory
       WHERE wasdismissed = true
         AND dismissedat >= $1
       GROUP BY userid
       HAVING COUNT(*) >= 3`,
      [sevenDaysAgo],
    ) as unknown as UserDismissalCount[];

    for (const user of frequentDismissers) {
      // Send gentle "we miss you" message (allow every 5 days)
      const lastNotification = await this.getLastNotification(
        user.userid,
        'skipping',
      );

      const daysSince = lastNotification
        ? this.daysSince(lastNotification.sentAt!)
        : 999;

      if (daysSince >= 5) {
        const tokens = await this.getActiveTokens(user.userid);

        if (tokens.length > 0) {
          targets.push({
            userId: user.userid,
            fcmTokens: tokens,
            reason: 'frequent_dismissal',
            context: {dismissCount: user.dismisscount},
          });
        }
      }
    }

    return targets;
  }

  /**
   * Check if user should receive notification (spam prevention)
   */
  async shouldSendNotification(
    userId: number,
    type: string,
  ): Promise<boolean> {
    const rules = NOTIFICATION_FREQUENCY_RULES[type];
    if (!rules) return false;

    // Check last notification of this type
    const lastNotification = await this.getLastNotification(userId, type);

    if (lastNotification) {
      const daysSince = this.daysSince(lastNotification.sentAt!);
      if (daysSince < rules.minDaysBetween) {
        return false; // Too soon
      }
    }

    // Check monthly limit
    const thirtyDaysAgo = new Date();
    thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);

    const recentCount = await this.notificationHistoryRepository.count({
      userId,
      notificationType: type,
      sentAt: {gte: thirtyDaysAgo},
    });

    if (recentCount.count >= rules.maxPerMonth) {
      return false; // Monthly limit reached
    }

    return true;
  }

  /**
   * Generate personalized notification message based on user context
   */
  async getPersonalizedMessage(
    userId: number,
    reason: string,
    context: any,
  ): Promise<NotificationContent> {
    if (reason === 'high_streak') {
      return {
        title: `${context.streak} Day Streak! 🔥`,
        body: `You're on fire! Keep exploring to maintain your ${context.streak}-day streak!`,
        type: 'doing_well',
        data: {
          action: 'open_map',
          userId: userId.toString(),
          streak: context.streak.toString(),
        },
      };
    } else if (reason === 'multiple_explorations_today') {
      return {
        title: 'Super Explorer! 🌟',
        body: `${context.explorationsToday} explorations today! You're unstoppable!`,
        type: 'doing_well',
        data: {
          action: 'open_badges',
          userId: userId.toString(),
        },
      };
    } else if (reason === 'inactive_for_days') {
      const daysSince = context.daysSinceActivity;
      return {
        title: 'We Miss You! 👋',
        body: `It's been ${daysSince} days since your last exploration. Ready for a new adventure?`,
        type: 'skipping',
        data: {
          action: 'open_map',
          userId: userId.toString(),
        },
      };
    } else if (reason === 'streak_broken') {
      return {
        title: 'Start Fresh! 🌱',
        body: `Your ${context.longestStreak}-day streak was impressive! Let's start a new one today.`,
        type: 'skipping',
        data: {
          action: 'open_map',
          userId: userId.toString(),
        },
      };
    } else if (reason === 'frequent_dismissal') {
      return {
        title: 'Still There? 👀',
        body: "We'll give you space, but remember: new adventures await when you're ready!",
        type: 'skipping',
        data: {
          action: 'open_map',
          userId: userId.toString(),
        },
      };
    }

    // Fallback
    return {
      title: 'Time to Explore!',
      body: 'Discover something new today!',
      type: 'generic',
      data: {action: 'open_map'},
    };
  }

  /**
   * Get active FCM tokens for a user
   */
  private async getActiveTokens(userId: number): Promise<string[]> {
    const tokens = await this.fcmTokenRepository.find({
      where: {
        userId,
        isActive: true,
      },
    });

    return tokens.map(t => t.token);
  }

  /**
   * Get last notification of a specific type for a user
   */
  private async getLastNotification(
    userId: number,
    type: string,
  ): Promise<any | null> {
    const notifications = await this.notificationHistoryRepository.find({
      where: {
        userId,
        notificationType: type,
      },
      order: ['sentAt DESC'],
      limit: 1,
    });

    return notifications.length > 0 ? notifications[0] : null;
  }

  /**
   * Calculate days since a date
   */
  private daysSince(date: Date): number {
    const now = new Date();
    const diffTime = Math.abs(now.getTime() - new Date(date).getTime());
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays;
  }
}
