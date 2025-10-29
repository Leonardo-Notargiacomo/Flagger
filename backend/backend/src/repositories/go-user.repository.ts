import {Getter, inject} from '@loopback/core';
import {
  DefaultCrudRepository,
  HasOneRepositoryFactory,
  repository
} from '@loopback/repository';
import {DbDataSource} from '../datasources';
import {GoUser, GoUserRelations, GoUserCredentials} from '../models';
import {GoUserCredentialsRepository} from './go-user-credentials.repository';

export class GoUserRepository extends DefaultCrudRepository<
  GoUser,
  typeof GoUser.prototype.id,
  GoUserRelations
> {
  public readonly goUserCredentials: HasOneRepositoryFactory<
    GoUserCredentials,
    typeof GoUser.prototype.id
  >;

  constructor(
    @inject('datasources.db') dataSource: DbDataSource,
    @repository.getter('GoUserCredentialsRepository')
    protected goUserCredentialsRepositoryGetter: Getter<GoUserCredentialsRepository>,
  ) {
    super(GoUser, dataSource);
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
