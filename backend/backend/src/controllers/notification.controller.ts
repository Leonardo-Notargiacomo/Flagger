import {
  get,
  post,
  param,
  requestBody,
  response,
  HttpErrors,
} from '@loopback/rest';
import {repository} from '@loopback/repository';
import {inject} from '@loopback/core';
import {NotificationHistoryRepository} from '../repositories';
import {
  NotificationTriggerService,
  NotificationTarget,
  NotificationContent,
} from '../services/notification-trigger.service';

export class NotificationController {
  constructor(
    @repository(NotificationHistoryRepository)
    public notificationHistoryRepository: NotificationHistoryRepository,
    @inject('services.NotificationTriggerService')
    public notificationTriggerService: NotificationTriggerService,
  ) {}

  /**
   * GET /api/notifications/targets/doing-well
   * Get list of users who should receive "doing well" notifications
   * Used by Firebase Functions
   */
  @get('/api/notifications/targets/doing-well')
  @response(200, {
    description: 'List of users who are doing well',
    content: {
      'application/json': {
        schema: {
          type: 'array',
          items: {type: 'object'},
        },
      },
    },
  })
  async getDoingWellTargets(): Promise<NotificationTarget[]> {
    console.log('[NotificationController] Getting doing well targets...');
    try {
      const targets = await this.notificationTriggerService.getUsersDoingWell();
      console.log(`[NotificationController] Found ${targets.length} doing well targets`);
      return targets;
    } catch (error) {
      console.error('[NotificationController] Error getting doing well targets:', error);
      throw new HttpErrors.InternalServerError('Failed to get notification targets');
    }
  }

  /**
   * GET /api/notifications/targets/skipping
   * Get list of users who should receive "skipping" notifications
   * Used by Firebase Functions
   */
  @get('/api/notifications/targets/skipping')
  @response(200, {
    description: 'List of users who are skipping',
    content: {
      'application/json': {
        schema: {
          type: 'array',
          items: {type: 'object'},
        },
      },
    },
  })
  async getSkippingTargets(): Promise<NotificationTarget[]> {
    console.log('[NotificationController] Getting skipping targets...');
    try {
      const targets = await this.notificationTriggerService.getUsersSkipping();
      console.log(`[NotificationController] Found ${targets.length} skipping targets`);
      return targets;
    } catch (error) {
      console.error('[NotificationController] Error getting skipping targets:', error);
      throw new HttpErrors.InternalServerError('Failed to get notification targets');
    }
  }

  /**
   * POST /api/notifications/message
   * Get personalized notification message for a user
   * Used by Firebase Functions
   */
  @post('/api/notifications/message')
  @response(200, {
    description: 'Personalized notification message',
    content: {
      'application/json': {
        schema: {type: 'object'},
      },
    },
  })
  async getPersonalizedMessage(
    @requestBody({
      content: {
        'application/json': {
          schema: {
            type: 'object',
            required: ['userId', 'reason', 'context'],
            properties: {
              userId: {type: 'number'},
              reason: {type: 'string'},
              context: {type: 'object'},
            },
          },
        },
      },
    })
    data: {
      userId: number;
      reason: string;
      context: any;
    },
  ): Promise<NotificationContent> {
    console.log(`[NotificationController] Getting message for user ${data.userId}, reason: ${data.reason}`);
    try {
      const message = await this.notificationTriggerService.getPersonalizedMessage(
        data.userId,
        data.reason,
        data.context,
      );
      return message;
    } catch (error) {
      console.error('[NotificationController] Error getting personalized message:', error);
      throw new HttpErrors.InternalServerError('Failed to generate notification message');
    }
  }

  /**
   * POST /api/notifications/mark-sent
   * Mark notification as sent in history
   * Used by Firebase Functions after sending notification
   */
  @post('/api/notifications/mark-sent')
  @response(200, {
    description: 'Notification marked as sent',
    content: {
      'application/json': {
        schema: {
          type: 'object',
          properties: {
            success: {type: 'boolean'},
          },
        },
      },
    },
  })
  async markNotificationSent(
    @requestBody({
      content: {
        'application/json': {
          schema: {
            type: 'object',
            required: ['userId', 'type', 'title', 'body'],
            properties: {
              userId: {type: 'number'},
              type: {type: 'string'},
              title: {type: 'string'},
              body: {type: 'string'},
            },
          },
        },
      },
    })
    data: {
      userId: number;
      type: string;
      title: string;
      body: string;
    },
  ) {
    console.log(`[NotificationController] Marking notification as sent for user ${data.userId}, type: ${data.type}`);
    try {
      await this.notificationHistoryRepository.create({
        userId: data.userId,
        notificationType: data.type,
        title: data.title,
        body: data.body,
        sentAt: new Date(),
        wasDismissed: false,
      });

      console.log('[NotificationController] Notification history created successfully');
      return {success: true};
    } catch (error) {
      console.error('[NotificationController] Error marking notification as sent:', error);
      throw new HttpErrors.InternalServerError('Failed to mark notification as sent');
    }
  }

  /**
   * POST /api/users/{userId}/notifications/{notificationId}/dismiss
   * Report that user dismissed a notification
   * Called from Android app
   */
  @post('/api/users/{userId}/notifications/{notificationId}/dismiss')
  @response(200, {
    description: 'Notification dismissal recorded',
    content: {
      'application/json': {
        schema: {
          type: 'object',
          properties: {
            success: {type: 'boolean'},
          },
        },
      },
    },
  })
  async reportDismissal(
    @param.path.number('userId') userId: number,
    @param.path.string('notificationId') notificationId: string,
  ) {
    console.log(`[NotificationController] Recording dismissal for user ${userId}, notification ${notificationId}`);
    try {
      // Try to find the notification by ID (if it's a database ID)
      const notificationDbId = parseInt(notificationId, 10);

      if (!isNaN(notificationDbId)) {
        const notification = await this.notificationHistoryRepository.findById(notificationDbId);

        if (notification && notification.userId === userId) {
          await this.notificationHistoryRepository.updateById(notificationDbId, {
            wasDismissed: true,
            dismissedAt: new Date(),
          });
          console.log('[NotificationController] Dismissal recorded successfully');
        } else {
          console.log('[NotificationController] Notification not found or userId mismatch');
        }
      } else {
        console.log('[NotificationController] Invalid notification ID format');
      }

      return {success: true};
    } catch (error) {
      console.error('[NotificationController] Error recording dismissal:', error);
      // Don't throw error - we don't want client failures for dismissal tracking
      return {success: false};
    }
  }
}
