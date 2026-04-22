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
  HttpErrors,
} from '@loopback/rest';
import {Flag} from '../models';
import {FlagRepository} from '../repositories';
import {authenticate} from '@loopback/authentication';
import {applyDefaultFlagFields, getFlagQueryTimeoutMs} from '../utils/flag-query';
import {withTimeout} from '../utils/with-timeout';

@authenticate('jwt') // <---- Apply the @authenticate decorator at the class level
export class FlagController {
  constructor(
    @repository(FlagRepository)
    public flagRepository : FlagRepository,
  ) {}

  /**
   * POST /flags
   * Create a new flag (location marker) for a user
   */
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

  /**
   * GET /flags/count
   * Return total count of flags, optionally filtered
   */
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

  /**
   * GET /flags
   * Get all flags, optionally filtered
   */
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

  /**
   * PATCH /flags
   * Bulk update flags matching a filter
   */
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

  /**
   * GET /flags/{id}
   * Get a single flag by ID
   */
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

  /**
   * PATCH /flags/{id}
   * Partially update a flag by ID
   */
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

  /**
   * PUT /flags/{id}
   * Full replace of a flag by ID
   */
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

  /**
   * DELETE /flags/{id}
   * Delete a flag by ID
   */
  @del('/flags/{id}')
  @response(204, {
    description: 'Flag DELETE success',
  })
  async deleteById(@param.path.number('id') id: number): Promise<void> {
    await this.flagRepository.deleteById(id);
  }

  /**
   * GET /flags/user/{userId}
   * Get all flags for a specific user, with timeout protection
   */
  @get('/flags/user/{userId}')
  @response(200, {
    description: 'Array of Flag model instances for a given user',
    content: {
      'application/json': {
        schema: {
          type: 'array',
          items: getModelSchemaRef(Flag, {includeRelations: true}),
        },
      },
    },
  })

  async findFlagsByUserId(
    @param.path.number('userId') userId: number,
    @param.filter(Flag) filter?: Filter<Flag>,
  ): Promise<Flag[]> {
    const flagsFilter = applyDefaultFlagFields({
      ...filter,
      where: {
        ...filter?.where,
        userId: userId,
      },
    });

    return withTimeout(
      this.flagRepository.find(flagsFilter),
      getFlagQueryTimeoutMs(),
      new HttpErrors.GatewayTimeout('No Flags flagged'),
    );
  }
}
