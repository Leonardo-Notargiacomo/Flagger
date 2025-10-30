import {
  repository,
} from '@loopback/repository';
import {
  param,
  get,
  getModelSchemaRef,
} from '@loopback/rest';
import {
  Flag,
  GoUser,
} from '../models';
import {FlagRepository} from '../repositories';

export class FlagGoUserController {
  constructor(
    @repository(FlagRepository)
    public flagRepository: FlagRepository,
  ) { }

  @get('/flags/{id}/go-user', {
    responses: {
      '200': {
        description: 'GoUser belonging to Flag',
        content: {
          'application/json': {
            schema: getModelSchemaRef(GoUser),
          },
        },
      },
    },
  })
  async getGoUser(
    @param.path.number('id') id: typeof Flag.prototype.id,
  ): Promise<GoUser> {
    return this.flagRepository.user(id);
  }
}
