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
  GoUserCredentials,
} from '../models';
import {GoUserRepository} from '../repositories';

export class GoUserGoUserCredentialsController {
  constructor(
    @repository(GoUserRepository) protected goUserRepository: GoUserRepository,
  ) { }

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
  async get(
    @param.path.number('id') id: number,
    @param.query.object('filter') filter?: Filter<GoUserCredentials>,
  ): Promise<GoUserCredentials> {
    return this.goUserRepository.gUCToGU(id).get(filter);
  }

  @post('/go-users/{id}/go-user-credentials', {
    responses: {
      '200': {
        description: 'GoUser model instance',
        content: {'application/json': {schema: getModelSchemaRef(GoUserCredentials)}},
      },
    },
  })
  async create(
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
    return this.goUserRepository.gUCToGU(id).create(goUserCredentials);
  }

  @patch('/go-users/{id}/go-user-credentials', {
    responses: {
      '200': {
        description: 'GoUser.GoUserCredentials PATCH success count',
        content: {'application/json': {schema: CountSchema}},
      },
    },
  })
  async patch(
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
    return this.goUserRepository.gUCToGU(id).patch(goUserCredentials, where);
  }

  @del('/go-users/{id}/go-user-credentials', {
    responses: {
      '200': {
        description: 'GoUser.GoUserCredentials DELETE success count',
        content: {'application/json': {schema: CountSchema}},
      },
    },
  })
  async delete(
    @param.path.number('id') id: number,
    @param.query.object('where', getWhereSchemaFor(GoUserCredentials)) where?: Where<GoUserCredentials>,
  ): Promise<Count> {
    return this.goUserRepository.gUCToGU(id).delete(where);
  }
}
