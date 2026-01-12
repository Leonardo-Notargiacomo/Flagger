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
import {authenticate} from '@loopback/authentication';
import {inject} from '@loopback/core';
import {SecurityBindings, UserProfile} from '@loopback/security';
import {FcmToken} from '../models';
import {FcmTokenRepository} from '../repositories';

@authenticate('jwt')
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
    @inject(SecurityBindings.USER) currentUser: UserProfile,
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
    const authenticatedUserId = parseInt(currentUser.id);

    // Authorization: Users can only register tokens for themselves
    if (authenticatedUserId !== userId) {
      throw new HttpErrors.Forbidden('Cannot register FCM token for another user');
    }

    console.log(`[FcmTokenController] Registering token for userId: ${userId}`);

    try {
      // Try to create new token first (optimistic approach)
      // If it already exists, the unique constraint will prevent duplicate
      try {
        await this.fcmTokenRepository.create({
          userId,
          token: tokenData.token,
          platform: tokenData.platform || 'android',
          isActive: true,
          createdAt: new Date(),
          lastUpdated: new Date(),
        });
        console.log(`[FcmTokenController] New token created for userId: ${userId}`);
      } catch (createError: any) {
        // Check if error is due to unique constraint violation
        if (createError.code === '23505' || createError.message?.includes('duplicate key')) {
          // Token already exists, update it instead
          const existingToken = await this.fcmTokenRepository.findOne({
            where: {
              userId,
              token: tokenData.token,
            },
          });

          if (existingToken) {
            await this.fcmTokenRepository.updateById(existingToken.id!, {
              lastUpdated: new Date(),
              isActive: true,
              platform: tokenData.platform || existingToken.platform,
            });
            console.log(`[FcmTokenController] Token updated for userId: ${userId}`);
          } else {
            // This shouldn't happen, but handle it gracefully
            throw createError;
          }
        } else {
          // Different error, re-throw
          throw createError;
        }
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
    @inject(SecurityBindings.USER) currentUser: UserProfile,
    @param.path.number('userId') userId: number,
  ) {
    const authenticatedUserId = parseInt(currentUser.id);

    // Authorization: Users can only get their own tokens
    if (authenticatedUserId !== userId) {
      throw new HttpErrors.Forbidden('Cannot access another user\'s FCM tokens');
    }

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
    @inject(SecurityBindings.USER) currentUser: UserProfile,
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
    const authenticatedUserId = parseInt(currentUser.id);

    // Authorization: Users can only remove their own tokens
    if (authenticatedUserId !== userId) {
      throw new HttpErrors.Forbidden('Cannot remove another user\'s FCM token');
    }

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
