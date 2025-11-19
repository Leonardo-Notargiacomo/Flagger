import {authenticate} from '@loopback/authentication';
import {inject} from '@loopback/core';
import {
  Count,
  CountSchema,
  Filter,
  FilterExcludingWhere,
  repository,
  Where,
} from '@loopback/repository';
import {
  post,
  param,
  get,
  getModelSchemaRef,
  patch,
  del,
  requestBody,
  response,
  HttpErrors,
} from '@loopback/rest';
import {SecurityBindings, UserProfile} from '@loopback/security';
import {Challenge, UserChallenge} from '../models';
import {ChallengeRepository, UserChallengeRepository} from '../repositories';
import {ChallengeService} from '../services';

export class ChallengeController {
  constructor(
    @repository(ChallengeRepository)
    public challengeRepository: ChallengeRepository,
    @repository(UserChallengeRepository)
    public userChallengeRepository: UserChallengeRepository,
    @inject('services.ChallengeService')
    public challengeService: ChallengeService,
  ) {}

  // ============ USER ENDPOINTS ============

  /**
   * Get the current challenge status for the authenticated user
   */
  @authenticate('jwt')
  @get('/challenges/status')
  @response(200, {
    description: 'Challenge status for the current user',
    content: {
      'application/json': {
        schema: {
          type: 'object',
          properties: {
            hasActiveChallenge: {type: 'boolean'},
            activeChallenge: {type: 'object'},
            isOnCooldown: {type: 'boolean'},
            cooldownEndsAt: {type: 'string'},
            availableChallenges: {
              type: 'array',
              items: getModelSchemaRef(Challenge),
            },
          },
        },
      },
    },
  })
  async getChallengeStatus(
    @inject(SecurityBindings.USER) currentUser: UserProfile,
  ): Promise<object> {
    const userId = parseInt(currentUser.id);
    return this.challengeService.getUserChallengeStatus(userId);
  }

  /**
   * Get 3 random available challenge cards
   */
  @authenticate('jwt')
  @get('/challenges/available')
  @response(200, {
    description: 'Array of available challenges',
    content: {'application/json': {schema: {type: 'array', items: getModelSchemaRef(Challenge)}}},
  })
  async getAvailableChallenges(
    @inject(SecurityBindings.USER) currentUser: UserProfile,
  ): Promise<Challenge[]> {
    const userId = parseInt(currentUser.id);
    return this.challengeService.getAvailableChallenges(userId);
  }

  /**
   * Select and activate a challenge
   */
  @authenticate('jwt')
  @post('/challenges/{id}/select')
  @response(200, {
    description: 'Challenge activated successfully',
    content: {
      'application/json': {
        schema: {
          type: 'object',
          properties: {
            userChallenge: getModelSchemaRef(UserChallenge),
            challenge: getModelSchemaRef(Challenge),
            cooldownEndsAt: {type: 'string'},
          },
        },
      },
    },
  })
  async selectChallenge(
    @param.path.number('id') challengeId: number,
    @inject(SecurityBindings.USER) currentUser: UserProfile,
  ): Promise<object> {
    const userId = parseInt(currentUser.id);
    return this.challengeService.selectChallenge(userId, challengeId);
  }

  /**
   * Get the active challenge for the authenticated user
   */
  @authenticate('jwt')
  @get('/challenges/active')
  @response(200, {
    description: 'Active challenge with details',
    content: {
      'application/json': {
        schema: {
          type: 'object',
          properties: {
            userChallenge: getModelSchemaRef(UserChallenge, {includeRelations: true}),
            timeRemaining: {
              type: 'object',
              properties: {
                hours: {type: 'number'},
                minutes: {type: 'number'},
                expired: {type: 'boolean'},
              },
            },
          },
        },
      },
    },
  })
  async getActiveChallenge(
    @inject(SecurityBindings.USER) currentUser: UserProfile,
  ): Promise<{userChallenge: UserChallenge | null; timeRemaining?: object}> {
    const userId = parseInt(currentUser.id);

    // Check and expire old challenges first
    await this.challengeService.checkAndExpireChallenges(userId);

    const userChallenge = await this.userChallengeRepository.findOne({
      where: {userId, status: 'active'},
      include: [{relation: 'challenge'}],
    });

    if (!userChallenge) {
      return {userChallenge: null};
    }

    // Calculate time remaining
    const now = new Date();
    let timeRemaining: {hours: number; minutes: number; expired: boolean} | undefined;

    if (userChallenge.expiresAt) {
      const msRemaining = userChallenge.expiresAt.getTime() - now.getTime();
      if (msRemaining > 0) {
        const hoursRemaining = Math.floor(msRemaining / (1000 * 60 * 60));
        const minutesRemaining = Math.floor((msRemaining % (1000 * 60 * 60)) / (1000 * 60));
        timeRemaining = {
          hours: hoursRemaining,
          minutes: minutesRemaining,
          expired: false,
        };
      } else {
        timeRemaining = {
          hours: 0,
          minutes: 0,
          expired: true,
        };
      }
    }

    return {
      userChallenge,
      timeRemaining,
    };
  }

  /**
   * Get all user challenges (history)
   */
  @authenticate('jwt')
  @get('/challenges/history')
  @response(200, {
    description: 'Array of user challenges',
    content: {
      'application/json': {
        schema: {type: 'array', items: getModelSchemaRef(UserChallenge, {includeRelations: true})},
      },
    },
  })
  async getChallengeHistory(
    @inject(SecurityBindings.USER) currentUser: UserProfile,
  ): Promise<UserChallenge[]> {
    const userId = parseInt(currentUser.id);
    return this.userChallengeRepository.find({
      where: {userId},
      include: [{relation: 'challenge'}],
      order: ['activatedAt DESC'],
    });
  }

  /**
   * Manually check if active challenge is completed (useful for testing)
   */
  @authenticate('jwt')
  @post('/challenges/check-completion')
  @response(200, {
    description: 'Check completion result',
    content: {
      'application/json': {
        schema: {
          type: 'object',
          properties: {
            completed: {type: 'boolean'},
            badge: {type: 'object'},
          },
        },
      },
    },
  })
  async checkCompletion(
    @inject(SecurityBindings.USER) currentUser: UserProfile,
  ): Promise<{completed: boolean; badge?: object}> {
    const userId = parseInt(currentUser.id);
    const badge = await this.challengeService.checkChallengeCompletion(userId);
    return {
      completed: badge !== null,
      badge: badge ?? undefined,
    };
  }

  // ============ ADMIN ENDPOINTS ============

  /**
   * Create a new challenge (admin only)
   */
  @post('/challenges')
  @response(200, {
    description: 'Challenge model instance',
    content: {'application/json': {schema: getModelSchemaRef(Challenge)}},
  })
  async create(
    @requestBody({
      content: {
        'application/json': {
          schema: getModelSchemaRef(Challenge, {
            title: 'NewChallenge',
            exclude: ['id'],
          }),
        },
      },
    })
    challenge: Omit<Challenge, 'id'>,
  ): Promise<Challenge> {
    return this.challengeRepository.create(challenge);
  }

  /**
   * Get count of challenges
   */
  @get('/challenges/count')
  @response(200, {
    description: 'Challenge model count',
    content: {'application/json': {schema: CountSchema}},
  })
  async count(@param.where(Challenge) where?: Where<Challenge>): Promise<Count> {
    return this.challengeRepository.count(where);
  }

  /**
   * Get all challenges (with filters)
   */
  @get('/challenges')
  @response(200, {
    description: 'Array of Challenge model instances',
    content: {
      'application/json': {
        schema: {
          type: 'array',
          items: getModelSchemaRef(Challenge, {includeRelations: true}),
        },
      },
    },
  })
  async find(@param.filter(Challenge) filter?: Filter<Challenge>): Promise<Challenge[]> {
    return this.challengeRepository.find(filter);
  }

  /**
   * Update all challenges matching a filter
   */
  @patch('/challenges')
  @response(200, {
    description: 'Challenge PATCH success count',
    content: {'application/json': {schema: CountSchema}},
  })
  async updateAll(
    @requestBody({
      content: {
        'application/json': {
          schema: getModelSchemaRef(Challenge, {partial: true}),
        },
      },
    })
    challenge: Challenge,
    @param.where(Challenge) where?: Where<Challenge>,
  ): Promise<Count> {
    return this.challengeRepository.updateAll(challenge, where);
  }

  /**
   * Get a challenge by ID
   */
  @get('/challenges/{id}')
  @response(200, {
    description: 'Challenge model instance',
    content: {
      'application/json': {
        schema: getModelSchemaRef(Challenge, {includeRelations: true}),
      },
    },
  })
  async findById(
    @param.path.number('id') id: number,
    @param.filter(Challenge, {exclude: 'where'}) filter?: FilterExcludingWhere<Challenge>,
  ): Promise<Challenge> {
    return this.challengeRepository.findById(id, filter);
  }

  /**
   * Update a challenge by ID
   */
  @patch('/challenges/{id}')
  @response(204, {
    description: 'Challenge PATCH success',
  })
  async updateById(
    @param.path.number('id') id: number,
    @requestBody({
      content: {
        'application/json': {
          schema: getModelSchemaRef(Challenge, {partial: true}),
        },
      },
    })
    challenge: Challenge,
  ): Promise<void> {
    await this.challengeRepository.updateById(id, challenge);
  }

  /**
   * Delete a challenge by ID
   */
  @del('/challenges/{id}')
  @response(204, {
    description: 'Challenge DELETE success',
  })
  async deleteById(@param.path.number('id') id: number): Promise<void> {
    await this.challengeRepository.deleteById(id);
  }
}

