import {Getter, inject} from '@loopback/core';
import {
  DefaultCrudRepository,
  HasOneRepositoryFactory,
  repository
} from '@loopback/repository';
import {DbDataSource} from '../datasources';
import {GoUser, GoUserRelations, GoUserCredentials, UserCustomFlag} from '../models';
import {GoUserCredentialsRepository} from './go-user-credentials.repository';
import {UserCustomFlagRepository} from './user-custom-flag.repository';

export class GoUserRepository extends DefaultCrudRepository<
  GoUser,
  typeof GoUser.prototype.id,
  GoUserRelations
> {
  public readonly goUserCredentials: HasOneRepositoryFactory<
    GoUserCredentials,
    typeof GoUser.prototype.id
  >;

  public readonly userCustomFlag: HasOneRepositoryFactory<UserCustomFlag, typeof GoUser.prototype.id>;

  constructor(
    @inject('datasources.db') dataSource: DbDataSource,
    @repository.getter('GoUserCredentialsRepository')
    protected goUserCredentialsRepositoryGetter: Getter<GoUserCredentialsRepository>, @repository.getter('UserCustomFlagRepository') protected userCustomFlagRepositoryGetter: Getter<UserCustomFlagRepository>,
  ) {
    super(GoUser, dataSource);
    this.userCustomFlag = this.createHasOneRepositoryFactoryFor('userCustomFlag', userCustomFlagRepositoryGetter);
    this.registerInclusionResolver('userCustomFlag', this.userCustomFlag.inclusionResolver);
    this.goUserCredentials = this.createHasOneRepositoryFactoryFor(
      'goUserCredentials',
      goUserCredentialsRepositoryGetter,
    );
    this.registerInclusionResolver(
      'goUserCredentials',
      this.goUserCredentials.inclusionResolver,
    );
  }
  async findCredentials(
    userId: typeof GoUser.prototype.id,
  ): Promise<GoUserCredentials | undefined> {
    return this.goUserCredentials(userId)
      .get()
      .catch(err => {
        if (err.code === 'ENTITY_NOT_FOUND') return undefined;
        throw err;
      });
  }
}
