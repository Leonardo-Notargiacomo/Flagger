import {
  Count,
  CountSchema,
  Filter,
  repository,
  Where,
} from '@loopback/repository';
import {inject} from '@loopback/core';
import {
  del,
  get,
  getModelSchemaRef,
  getWhereSchemaFor,
  HttpErrors,
  param,
  patch,
  post,
  requestBody,
} from '@loopback/rest';
import {authenticate} from '@loopback/authentication';
import {SecurityBindings, securityId, UserProfile} from '@loopback/security';
import {
  GoUser,
  GoUserCredentials,
} from '../models';
import {GoUserRepository} from '../repositories';

export class GoUserGoUserCredentialsController {
  constructor(
    @repository(GoUserRepository) protected goUserRepository: GoUserRepository,
  ) { }

  private getAuthenticatedUserId(currentUserProfile: UserProfile): number {
    const rawId = currentUserProfile[securityId];
    const userId = Number(rawId);
    if (!rawId || Number.isNaN(userId)) {
      throw new HttpErrors.Unauthorized('Invalid user profile.');
    }
    return userId;
  }

  @get('/go-users/{id}/go-user-credentials', {
    responses: {
      '200': {
        description: 'GoUser has one GoUserCredentials',
        content: {
          'application/json': {
            schema: getModelSchemaRef(GoUserCredentials),
          },
        },
      },
    },
  })
  @authenticate('jwt')
  async get(
    @inject(SecurityBindings.USER) currentUserProfile: UserProfile,
    @param.path.number('id') id: number,
    @param.query.object('filter') filter?: Filter<GoUserCredentials>,
  ): Promise<GoUserCredentials> {
    const currentUserId = this.getAuthenticatedUserId(currentUserProfile);
    if (currentUserId !== id) {
      throw new HttpErrors.Forbidden(
        'You can only access your own credentials.',
      );
    }
    return this.goUserRepository.goUserCredentials(id).get(filter);
  }

  @post('/go-users/{id}/go-user-credentials', {
    responses: {
      '200': {
        description: 'GoUser model instance',
        content: {'application/json': {schema: getModelSchemaRef(GoUserCredentials)}},
      },
    },
  })
  @authenticate('jwt')
  async create(
    @inject(SecurityBindings.USER) currentUserProfile: UserProfile,
    @param.path.number('id') id: typeof GoUser.prototype.id,
    @requestBody({
      content: {
        'application/json': {
          schema: getModelSchemaRef(GoUserCredentials, {
            title: 'NewGoUserCredentialsInGoUser',
            exclude: ['id'],
            optional: ['goUserId']
          }),
        },
      },
    }) goUserCredentials: Omit<GoUserCredentials, 'id'>,
  ): Promise<GoUserCredentials> {
    const currentUserId = this.getAuthenticatedUserId(currentUserProfile);
    if (currentUserId !== id) {
      throw new HttpErrors.Forbidden(
        'You can only create credentials for your own user.',
      );
    }
    return this.goUserRepository.goUserCredentials(id).create(goUserCredentials);
  }

  @patch('/go-users/{id}/go-user-credentials', {
    responses: {
      '200': {
        description: 'GoUser.GoUserCredentials PATCH success count',
        content: {'application/json': {schema: CountSchema}},
      },
    },
  })
  @authenticate('jwt')
  async patch(
    @inject(SecurityBindings.USER) currentUserProfile: UserProfile,
    @param.path.number('id') id: number,
    @requestBody({
      content: {
        'application/json': {
          schema: getModelSchemaRef(GoUserCredentials, {partial: true}),
        },
      },
    })
    goUserCredentials: Partial<GoUserCredentials>,
    @param.query.object('where', getWhereSchemaFor(GoUserCredentials)) where?: Where<GoUserCredentials>,
  ): Promise<Count> {
    const currentUserId = this.getAuthenticatedUserId(currentUserProfile);
    if (currentUserId !== id) {
      throw new HttpErrors.Forbidden(
        'You can only update your own credentials.',
      );
    }

    if ('password' in goUserCredentials) {
      throw new HttpErrors.BadRequest(
        'Use PATCH /go-users/me/password or PATCH /go-users/{id}/password to update passwords.',
      );
    }
    if ('goUserId' in goUserCredentials) {
      throw new HttpErrors.BadRequest('goUserId cannot be modified.');
    }
    return this.goUserRepository.goUserCredentials(id).patch(goUserCredentials, where);
  }

  @del('/go-users/{id}/go-user-credentials', {
    responses: {
      '200': {
        description: 'GoUser.GoUserCredentials DELETE success count',
        content: {'application/json': {schema: CountSchema}},
      },
    },
  })
  @authenticate('jwt')
  async delete(
    @inject(SecurityBindings.USER) currentUserProfile: UserProfile,
    @param.path.number('id') id: number,
    @param.query.object('where', getWhereSchemaFor(GoUserCredentials)) where?: Where<GoUserCredentials>,
  ): Promise<Count> {
    const currentUserId = this.getAuthenticatedUserId(currentUserProfile);
    if (currentUserId !== id) {
      throw new HttpErrors.Forbidden(
        'You can only delete your own credentials.',
      );
    }
    return this.goUserRepository.goUserCredentials(id).delete(where);
  }
}
