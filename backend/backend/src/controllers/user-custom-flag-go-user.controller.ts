import {
  repository,
} from '@loopback/repository';
import {
  param,
  get,
  getModelSchemaRef,
} from '@loopback/rest';
import {
  UserCustomFlag,
  GoUser,
} from '../models';
import {UserCustomFlagRepository} from '../repositories';

export class UserCustomFlagGoUserController {
  constructor(
    @repository(UserCustomFlagRepository)
    public userCustomFlagRepository: UserCustomFlagRepository,
  ) { }

  @get('/user-custom-flags/{id}/go-user', {
    responses: {
      '200': {
        description: 'GoUser belonging to UserCustomFlag',
        content: {
          'application/json': {
            schema: getModelSchemaRef(GoUser),
          },
        },
      },
    },
  })
  async getGoUser(
    @param.path.number('id') id: typeof UserCustomFlag.prototype.id,
  ): Promise<GoUser> {
    return this.userCustomFlagRepository.goUser(id);
  }
}
