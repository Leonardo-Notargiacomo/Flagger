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
import {Flag} from '../models';
import {FlagRepository} from '../repositories';

export class FlaggerController {
  constructor(
    @repository(FlagRepository)
    public flagRepository : FlagRepository,
  ) {}

  @post('/flags')
  @response(200, {
    description: 'Flag model instance',
    content: {'application/json': {schema: getModelSchemaRef(Flag)}},
  })
  async create(
    @requestBody({
      content: {
        'application/json': {
          schema: getModelSchemaRef(Flag, {
            title: 'NewFlag',
            exclude: ['id'],
          }),
        },
      },
    })
    flag: Omit<Flag, 'id'>,
  ): Promise<Flag> {
    return this.flagRepository.create(flag);
  }

  @get('/flags/count')
  @response(200, {
    description: 'Flag model count',
    content: {'application/json': {schema: CountSchema}},
  })
  async count(
    @param.where(Flag) where?: Where<Flag>,
  ): Promise<Count> {
    return this.flagRepository.count(where);
  }

  @get('/flags')
  @response(200, {
    description: 'Array of Flag model instances',
    content: {
      'application/json': {
        schema: {
          type: 'array',
          items: getModelSchemaRef(Flag, {includeRelations: true}),
        },
      },
    },
  })
  async find(
    @param.filter(Flag) filter?: Filter<Flag>,
  ): Promise<Flag[]> {
    return this.flagRepository.find(filter);
  }

  @patch('/flags')
  @response(200, {
    description: 'Flag PATCH success count',
    content: {'application/json': {schema: CountSchema}},
  })
  async updateAll(
    @requestBody({
      content: {
        'application/json': {
          schema: getModelSchemaRef(Flag, {partial: true}),
        },
      },
    })
    flag: Flag,
    @param.where(Flag) where?: Where<Flag>,
  ): Promise<Count> {
    return this.flagRepository.updateAll(flag, where);
  }

  @get('/flags/{id}')
  @response(200, {
    description: 'Flag model instance',
    content: {
      'application/json': {
        schema: getModelSchemaRef(Flag, {includeRelations: true}),
      },
    },
  })
  async findById(
    @param.path.number('id') id: number,
    @param.filter(Flag, {exclude: 'where'}) filter?: FilterExcludingWhere<Flag>
  ): Promise<Flag> {
    return this.flagRepository.findById(id, filter);
  }

  @patch('/flags/{id}')
  @response(204, {
    description: 'Flag PATCH success',
  })
  async updateById(
    @param.path.number('id') id: number,
    @requestBody({
      content: {
        'application/json': {
          schema: getModelSchemaRef(Flag, {partial: true}),
        },
      },
    })
    flag: Flag,
  ): Promise<void> {
    await this.flagRepository.updateById(id, flag);
  }

  @put('/flags/{id}')
  @response(204, {
    description: 'Flag PUT success',
  })
  async replaceById(
    @param.path.number('id') id: number,
    @requestBody() flag: Flag,
  ): Promise<void> {
    await this.flagRepository.replaceById(id, flag);
  }

  @del('/flags/{id}')
  @response(204, {
    description: 'Flag DELETE success',
  })
  async deleteById(@param.path.number('id') id: number): Promise<void> {
    await this.flagRepository.deleteById(id);
  }
}
