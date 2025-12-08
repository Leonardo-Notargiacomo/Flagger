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

    // 1. High streak (5+ days) - BATCH OPTIMIZED
    const highStreakUsers = await this.userStreakRepository.find({
      where: {
        currentStreak: {gte: 5},
      },
    });

    if (highStreakUsers.length > 0) {
      const userIds = highStreakUsers.map(s => s.userId);
      const eligibilityMap = await this.shouldSendNotificationBatch(
        userIds,
        'doing_well',
      );
      const eligibleUserIds = userIds.filter(
        id => eligibilityMap.get(id) === true,
      );
      const tokensMap = await this.getActiveTokensBatch(eligibleUserIds);

      for (const streak of highStreakUsers) {
        if (eligibilityMap.get(streak.userId)) {
          const tokens = tokensMap.get(streak.userId) || [];
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
    }

    // 2. Multiple explorations per day (2+ today) - BATCH OPTIMIZED
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

    if (activeUsersResult.length > 0) {
      const userIds = activeUsersResult.map(u => u.userid);
      const lastNotificationsMap = await this.getLastNotificationsBatch(
        userIds,
        'doing_well',
      );

      const eligibleUserIds: number[] = [];
      for (const user of activeUsersResult) {
        const lastNotification = lastNotificationsMap.get(user.userid);
        const daysSince = lastNotification
          ? this.daysSince(lastNotification.sentAt!)
          : 999;
        if (daysSince >= 3) {
          eligibleUserIds.push(user.userid);
        }
      }

      const tokensMap = await this.getActiveTokensBatch(eligibleUserIds);

      for (const user of activeUsersResult) {
        if (eligibleUserIds.includes(user.userid)) {
          const tokens = tokensMap.get(user.userid) || [];
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
    }

    return targets;
  }

  /**
   * Get users who are "skipping" and need re-engagement
   */
  async getUsersSkipping(): Promise<NotificationTarget[]> {
    const targets: NotificationTarget[] = [];

    // 1. Not exploring for 3+ days - BATCH OPTIMIZED
    const threeDaysAgo = new Date();
    threeDaysAgo.setDate(threeDaysAgo.getDate() - 3);

    const inactiveUsers = await this.userStreakRepository.find({
      where: {
        lastActivityDate: {lt: threeDaysAgo},
        currentStreak: {gt: 0}, // Had a streak, now inactive
      },
    });

    if (inactiveUsers.length > 0) {
      const userIds = inactiveUsers.map(u => u.userId);
      const eligibilityMap = await this.shouldSendNotificationBatch(userIds, 'skipping');
      const eligibleUserIds = userIds.filter(id => eligibilityMap.get(id) === true);
      const tokensMap = await this.getActiveTokensBatch(eligibleUserIds);

      for (const user of inactiveUsers) {
        if (eligibilityMap.get(user.userId)) {
          const tokens = tokensMap.get(user.userId) || [];
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
    }

    // 2. Breaking their streak (current streak = 0, longest > 0) - BATCH OPTIMIZED
    const brokenStreakUsers = await this.userStreakRepository.find({
      where: {
        currentStreak: 0,
        longestStreak: {gt: 0},
      },
    });

      for (const user of frequentDismissers) {
        if (eligibleUserIds.includes(user.userid)) {
          const tokens = tokensMap.get(user.userid) || [];
          if (tokens.length > 0) {
            targets.push({
              userId: user.userid,
              fcmTokens: tokens,
              reason: 'frequent_dismissal',
              context: {dismissCount: user.dismisscount},
            });
          }
    if (brokenStreakUsers.length > 0) {
      const userIds = brokenStreakUsers.map(u => u.userId);
      const lastNotificationsMap = await this.getLastNotificationsBatch(userIds, 'skipping');

      const eligibleUserIds: number[] = [];
      for (const user of brokenStreakUsers) {
        const lastNotification = lastNotificationsMap.get(user.userId);
        const daysSince = lastNotification ? this.daysSince(lastNotification.sentAt!) : 999;
        if (daysSince >= 1) {
          eligibleUserIds.push(user.userId);
        }
      }

      const tokensMap = await this.getActiveTokensBatch(eligibleUserIds);

      for (const user of brokenStreakUsers) {
        if (eligibleUserIds.includes(user.userId)) {
          const tokens = tokensMap.get(user.userId) || [];
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
    }

    // 3. Dismissing notifications (3+ dismissals in last 7 days) - BATCH OPTIMIZED
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

    if (frequentDismissers.length > 0) {
      const userIds = frequentDismissers.map(u => u.userid);
      const lastNotificationsMap = await this.getLastNotificationsBatch(userIds, 'skipping');

      const eligibleUserIds: number[] = [];
      for (const user of frequentDismissers) {
        const lastNotification = lastNotificationsMap.get(user.userid);
        const daysSince = lastNotification ? this.daysSince(lastNotification.sentAt!) : 999;
        if (daysSince >= 5) {
          eligibleUserIds.push(user.userid);
        }
      }

      const tokensMap = await this.getActiveTokensBatch(eligibleUserIds);

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

  /**
   * Batch fetch active FCM tokens for multiple users
   * Eliminates N+1 query problem when processing many users
   */
  private async getActiveTokensBatch(
    userIds: number[],
  ): Promise<Map<number, string[]>> {
    if (userIds.length === 0) return new Map();

    const tokens = await this.fcmTokenRepository.find({
      where: {
        userId: {inq: userIds},
        isActive: true,
      },
    });

    const tokenMap = new Map<number, string[]>();
    for (const token of tokens) {
      const existing = tokenMap.get(token.userId) || [];
      existing.push(token.token);
      tokenMap.set(token.userId, existing);
    }

    return tokenMap;
  }

  /**
   * Batch fetch last notifications for multiple users
   * Eliminates N+1 query problem when checking notification history
   */
  private async getLastNotificationsBatch(
    userIds: number[],
    type: string,
  ): Promise<Map<number, any>> {
    if (userIds.length === 0) return new Map();

    const notifications = await this.notificationHistoryRepository.find({
      where: {
        userId: {inq: userIds},
        notificationType: type,
      },
      order: ['sentAt DESC'],
    });

    const notificationMap = new Map<number, any>();
    for (const notif of notifications) {
      if (!notificationMap.has(notif.userId)) {
        notificationMap.set(notif.userId, notif);
      }
    }

    return notificationMap;
  }

  /**
   * Batch fetch notification counts for multiple users
   * Uses raw SQL for efficient counting with GROUP BY
   */
  private async getNotificationCountsBatch(
    userIds: number[],
    type: string,
    since: Date,
  ): Promise<Map<number, number>> {
    if (userIds.length === 0) return new Map();

    const results = await this.notificationHistoryRepository.execute(
      `SELECT userid, COUNT(*) as count
       FROM notificationhistory
       WHERE userid = ANY($1)
         AND notificationtype = $2
         AND sentat >= $3
       GROUP BY userid`,
      [userIds, type, since],
    ) as Array<{userid: number; count: string}>;

    const countMap = new Map<number, number>();
    for (const result of results) {
      countMap.set(result.userid, parseInt(result.count, 10));
    }

    return countMap;
  }

  /**
   * Batch version of shouldSendNotification for multiple users
   * Checks frequency rules for many users at once
   */
  private async shouldSendNotificationBatch(
    userIds: number[],
    type: string,
  ): Promise<Map<number, boolean>> {
    const rules = NOTIFICATION_FREQUENCY_RULES[type];
    if (!rules) {
      return new Map(userIds.map(id => [id, false]));
    }

    const lastNotifications = await this.getLastNotificationsBatch(
      userIds,
      type,
    );

    const thirtyDaysAgo = new Date();
    thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);
    const counts = await this.getNotificationCountsBatch(
      userIds,
      type,
      thirtyDaysAgo,
    );

    const resultMap = new Map<number, boolean>();
    for (const userId of userIds) {
      const lastNotif = lastNotifications.get(userId);
      if (lastNotif) {
        const daysSince = this.daysSince(lastNotif.sentAt);
        if (daysSince < rules.minDaysBetween) {
          resultMap.set(userId, false);
          continue;
        }
      }

      const count = counts.get(userId) || 0;
      if (count >= rules.maxPerMonth) {
        resultMap.set(userId, false);
        continue;
      }

      resultMap.set(userId, true);
    }

    return resultMap;
  }
}
