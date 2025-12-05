import {
  post,
  param,
  get,
  del,
  requestBody,
  response,
  HttpErrors,
} from '@loopback/rest';
import {repository} from '@loopback/repository';
import {FcmToken} from '../models';
import {FcmTokenRepository} from '../repositories';

export class FcmTokenController {
  constructor(
    @repository(FcmTokenRepository)
    public fcmTokenRepository: FcmTokenRepository,
  ) {}

  /**
   * POST /api/users/{userId}/fcm-token
   * Register or update FCM token for a user
   */
  @post('/api/users/{userId}/fcm-token')
  @response(200, {
    description: 'FCM token registered successfully',
    content: {
      'application/json': {
        schema: {
          type: 'object',
          properties: {
            success: {type: 'boolean'},
            message: {type: 'string'},
          },
        },
      },
    },
  })
  async registerToken(
    @param.path.number('userId') userId: number,
    @requestBody({
      content: {
        'application/json': {
          schema: {
            type: 'object',
            required: ['token'],
            properties: {
              token: {type: 'string'},
              platform: {type: 'string', default: 'android'},
            },
          },
        },
      },
    })
    tokenData: {
      token: string;
      platform?: string;
    },
  ) {
    console.log(`[FcmTokenController] Registering token for userId: ${userId}`);

    try {
      // Check if token already exists for this user
      const existingToken = await this.fcmTokenRepository.findOne({
        where: {
          userId,
          token: tokenData.token,
        },
      });

      if (existingToken) {
        // Update existing token
        await this.fcmTokenRepository.updateById(existingToken.id!, {
          lastUpdated: new Date(),
          isActive: true,
        });
        console.log(`[FcmTokenController] Token updated for userId: ${userId}`);
      } else {
        // Create new token
        await this.fcmTokenRepository.create({
          userId,
          token: tokenData.token,
          platform: tokenData.platform || 'android',
          isActive: true,
          createdAt: new Date(),
          lastUpdated: new Date(),
        });
        console.log(`[FcmTokenController] New token created for userId: ${userId}`);
      }

      return {
        success: true,
        message: 'FCM token registered successfully',
      };
    } catch (error) {
      console.error('[FcmTokenController] Error registering token:', error);
      throw new HttpErrors.InternalServerError('Failed to register FCM token');
    }
  }

  /**
   * GET /api/users/{userId}/fcm-tokens
   * Get all active FCM tokens for a user (multi-device support)
   */
  @get('/api/users/{userId}/fcm-tokens')
  @response(200, {
    description: 'List of active FCM tokens for user',
    content: {
      'application/json': {
        schema: {
          type: 'array',
          items: {type: 'object'},
        },
      },
    },
  })
  async getUserTokens(
    @param.path.number('userId') userId: number,
  ) {
    console.log(`[FcmTokenController] Getting tokens for userId: ${userId}`);

    try {
      const tokens = await this.fcmTokenRepository.find({
        where: {
          userId,
          isActive: true,
        },
        order: ['lastUpdated DESC'],
      });

      console.log(`[FcmTokenController] Found ${tokens.length} active tokens for userId: ${userId}`);
      return tokens;
    } catch (error) {
      console.error('[FcmTokenController] Error getting tokens:', error);
      throw new HttpErrors.InternalServerError('Failed to retrieve FCM tokens');
    }
  }

  /**
   * DELETE /api/users/{userId}/fcm-token
   * Remove/deactivate FCM token (e.g., on logout)
   */
  @del('/api/users/{userId}/fcm-token')
  @response(200, {
    description: 'FCM token deactivated successfully',
    content: {
      'application/json': {
        schema: {
          type: 'object',
          properties: {
            success: {type: 'boolean'},
            message: {type: 'string'},
          },
        },
      },
    },
  })
  async removeToken(
    @param.path.number('userId') userId: number,
    @requestBody({
      content: {
        'application/json': {
          schema: {
            type: 'object',
            required: ['token'],
            properties: {
              token: {type: 'string'},
            },
          },
        },
      },
    })
    tokenData: {
      token: string;
    },
  ) {
    console.log(`[FcmTokenController] Removing token for userId: ${userId}`);

    try {
      const token = await this.fcmTokenRepository.findOne({
        where: {
          userId,
          token: tokenData.token,
        },
      });

      if (token) {
        // Mark as inactive instead of deleting (for historical tracking)
        await this.fcmTokenRepository.updateById(token.id!, {
          isActive: false,
          lastUpdated: new Date(),
        });
        console.log(`[FcmTokenController] Token deactivated for userId: ${userId}`);
      } else {
        console.log(`[FcmTokenController] Token not found for userId: ${userId}`);
      }

      return {
        success: true,
        message: 'FCM token removed successfully',
      };
    } catch (error) {
      console.error('[FcmTokenController] Error removing token:', error);
      throw new HttpErrors.InternalServerError('Failed to remove FCM token');
    }
  }
}
