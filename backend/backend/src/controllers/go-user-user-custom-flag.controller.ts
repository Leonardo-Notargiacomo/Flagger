import {
  Count,
  CountSchema,
  Filter,
  repository,
  Where,
} from '@loopback/repository';
import {
  del,
  get,
  getModelSchemaRef,
  getWhereSchemaFor,
  param,
  patch,
  post,
  requestBody,
} from '@loopback/rest';
import {
  GoUser,
  UserCustomFlag,
} from '../models';
import {GoUserRepository} from '../repositories';

export class GoUserUserCustomFlagController {
  constructor(
    @repository(GoUserRepository) protected goUserRepository: GoUserRepository,
  ) { }

  @get('/go-users/{id}/user-custom-flag', {
    responses: {
      '200': {
        description: 'GoUser has one UserCustomFlag',
        content: {
          'application/json': {
            schema: getModelSchemaRef(UserCustomFlag),
          },
        },
      },
    },
  })
  async get(
    @param.path.number('id') id: number,
    @param.query.object('filter') filter?: Filter<UserCustomFlag>,
  ): Promise<UserCustomFlag> {
    return this.goUserRepository.userCustomFlag(id).get(filter);
  }

  @post('/go-users/{id}/user-custom-flag', {
    responses: {
      '200': {
        description: 'GoUser model instance',
        content: {'application/json': {schema: getModelSchemaRef(UserCustomFlag)}},
      },
    },
  })
  async create(
    @param.path.number('id') id: typeof GoUser.prototype.id,
    @requestBody({
      content: {
        'application/json': {
          schema: getModelSchemaRef(UserCustomFlag, {
            title: 'NewUserCustomFlagInGoUser',
            exclude: ['id'],
            optional: ['goUserId']
          }),
        },
      },
    }) userCustomFlag: Omit<UserCustomFlag, 'id'>,
  ): Promise<UserCustomFlag> {
    return this.goUserRepository.userCustomFlag(id).create(userCustomFlag);
  }

  @patch('/go-users/{id}/user-custom-flag', {
    responses: {
      '200': {
        description: 'GoUser.UserCustomFlag PATCH success count',
        content: {'application/json': {schema: CountSchema}},
      },
    },
  })
  async patch(
    @param.path.number('id') id: number,
    @requestBody({
      content: {
        'application/json': {
          schema: getModelSchemaRef(UserCustomFlag, {partial: true}),
        },
      },
    })
    userCustomFlag: Partial<UserCustomFlag>,
    @param.query.object('where', getWhereSchemaFor(UserCustomFlag)) where?: Where<UserCustomFlag>,
  ): Promise<Count> {
    return this.goUserRepository.userCustomFlag(id).patch(userCustomFlag, where);
  }

  @del('/go-users/{id}/user-custom-flag', {
    responses: {
      '200': {
        description: 'GoUser.UserCustomFlag DELETE success count',
        content: {'application/json': {schema: CountSchema}},
      },
    },
  })
  async delete(
    @param.path.number('id') id: number,
    @param.query.object('where', getWhereSchemaFor(UserCustomFlag)) where?: Where<UserCustomFlag>,
  ): Promise<Count> {
    return this.goUserRepository.userCustomFlag(id).delete(where);
  }
}
