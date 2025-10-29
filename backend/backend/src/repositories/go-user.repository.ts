import {Getter, inject} from '@loopback/core';
import {
  DefaultCrudRepository,
  HasOneRepositoryFactory, repository
} from '@loopback/repository';
import {DbDataSource} from '../datasources';
import {GoUser, GoUserRelations, GoUserCredentials} from '../models';
import {User, UserCredentials} from "@loopback/authentication-jwt/src/models";
import {GoUserCredentialsRepository} from "./go-user-credentials.repository";

export class GoUserRepository extends DefaultCrudRepository<
  GoUser,
  typeof GoUser.prototype.id,
  GoUserRelations
> {
  public readonly userCredentials: HasOneRepositoryFactory<
    UserCredentials,
    typeof GoUser.prototype.id
  >;

  public readonly goUserCredentials: HasOneRepositoryFactory<GoUserCredentials, typeof GoUser.prototype.id>;

  public readonly gUCToGU: HasOneRepositoryFactory<GoUserCredentials, typeof GoUser.prototype.id>;

  constructor(
    @inject('datasources.db') dataSource: DbDataSource,
    @repository.getter('UserCredentialsRepository')
    protected userCredentialsRepositoryGetter: Getter<GoUserCredentialsRepository>, @repository.getter('GoUserCredentialsRepository') protected goUserCredentialsRepositoryGetter: Getter<GoUserCredentialsRepository>,
  ) {
    super(GoUser, dataSource);
    this.gUCToGU = this.createHasOneRepositoryFactoryFor('gUCToGU', goUserCredentialsRepositoryGetter);
    this.registerInclusionResolver('gUCToGU', this.gUCToGU.inclusionResolver);
    this.goUserCredentials = this.createHasOneRepositoryFactoryFor('goUserCredentials', goUserCredentialsRepositoryGetter);
    this.registerInclusionResolver('goUserCredentials', this.goUserCredentials.inclusionResolver);
    this.goUserCredentials = this.createHasOneRepositoryFactoryFor(
      'userCredentials',
      userCredentialsRepositoryGetter,
    );
    this.registerInclusionResolver(
      'userCredentials',
      this.userCredentials.inclusionResolver,
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
