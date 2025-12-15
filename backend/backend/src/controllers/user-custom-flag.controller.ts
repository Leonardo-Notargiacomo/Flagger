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
  put,
  del,
  requestBody,
  response,
} from '@loopback/rest';
import {UserCustomFlag} from '../models';
import {UserCustomFlagRepository} from '../repositories';
import { authenticate } from '@loopback/authentication';
@authenticate('jwt')

export class UserCustomFlagController {
  constructor(
    @repository(UserCustomFlagRepository)
    public userCustomFlagRepository : UserCustomFlagRepository,
  ) {}

  @post('/user-custom-flags')
  @response(200, {
    description: 'UserCustomFlag model instance',
    content: {'application/json': {schema: getModelSchemaRef(UserCustomFlag)}},
  })
  async create(
    @requestBody({
      content: {
        'application/json': {
          schema: getModelSchemaRef(UserCustomFlag, {
            title: 'NewUserCustomFlag',
            exclude: ['id'],
          }),
        },
      },
    })
    userCustomFlag: Omit<UserCustomFlag, 'id'>,
  ): Promise<UserCustomFlag> {
    return this.userCustomFlagRepository.create(userCustomFlag);
  }

  @get('/user-custom-flags/count')
  @response(200, {
    description: 'UserCustomFlag model count',
    content: {'application/json': {schema: CountSchema}},
  })
  async count(
    @param.where(UserCustomFlag) where?: Where<UserCustomFlag>,
  ): Promise<Count> {
    return this.userCustomFlagRepository.count(where);
  }

  @get('/user-custom-flags')
  @response(200, {
    description: 'Array of UserCustomFlag model instances',
    content: {
      'application/json': {
        schema: {
          type: 'array',
          items: getModelSchemaRef(UserCustomFlag, {includeRelations: true}),
        },
      },
    },
  })
  async find(
    @param.filter(UserCustomFlag) filter?: Filter<UserCustomFlag>,
  ): Promise<UserCustomFlag[]> {
    return this.userCustomFlagRepository.find(filter);
  }

  @patch('/user-custom-flags')
  @response(200, {
    description: 'UserCustomFlag PATCH success count',
    content: {'application/json': {schema: CountSchema}},
  })
  async updateAll(
    @requestBody({
      content: {
        'application/json': {
          schema: getModelSchemaRef(UserCustomFlag, {partial: true}),
        },
      },
    })
    userCustomFlag: UserCustomFlag,
    @param.where(UserCustomFlag) where?: Where<UserCustomFlag>,
  ): Promise<Count> {
    return this.userCustomFlagRepository.updateAll(userCustomFlag, where);
  }

  @get('/user-custom-flags/{id}')
  @response(200, {
    description: 'UserCustomFlag model instance',
    content: {
      'application/json': {
        schema: getModelSchemaRef(UserCustomFlag, {includeRelations: true}),
      },
    },
  })
  async findById(
    @param.path.number('id') id: number,
    @param.filter(UserCustomFlag, {exclude: 'where'}) filter?: FilterExcludingWhere<UserCustomFlag>
  ): Promise<UserCustomFlag> {
    return this.userCustomFlagRepository.findById(id, filter);
  }

  @patch('/user-custom-flags/{id}')
  @response(204, {
    description: 'UserCustomFlag PATCH success',
  })
  async updateById(
    @param.path.number('id') id: number,
    @requestBody({
      content: {
        'application/json': {
          schema: getModelSchemaRef(UserCustomFlag, {partial: true}),
        },
      },
    })
    userCustomFlag: UserCustomFlag,
  ): Promise<void> {
    await this.userCustomFlagRepository.updateById(id, userCustomFlag);
  }

  @put('/user-custom-flags/{id}')
  @response(204, {
    description: 'UserCustomFlag PUT success',
  })
  async replaceById(
    @param.path.number('id') id: number,
    @requestBody() userCustomFlag: UserCustomFlag,
  ): Promise<void> {
    await this.userCustomFlagRepository.replaceById(id, userCustomFlag);
  }

  @del('/user-custom-flags/{id}')
  @response(204, {
    description: 'UserCustomFlag DELETE success',
  })
  async deleteById(@param.path.number('id') id: number): Promise<void> {
    await this.userCustomFlagRepository.deleteById(id);
  }
  @get('/user-custom-flag/{goUserId}')
@response(200, {
  description: 'UserCustomFlag by goUserId',
  content: {
    'application/json': {
      schema: getModelSchemaRef(UserCustomFlag, {includeRelations: true}),
    },
  },
})
async findByGoUserId(
  @param.path.number('goUserId') goUserId: number,
): Promise<UserCustomFlag | null> {
  return this.userCustomFlagRepository.findOne({
    where: {goUserId: goUserId},
  });
}
}
