import {inject, Getter} from '@loopback/core';
import {DefaultCrudRepository, repository, BelongsToAccessor} from '@loopback/repository';
import {DbDataSource} from '../datasources';
import {UserCustomFlag, UserCustomFlagRelations, GoUser} from '../models';
import {GoUserRepository} from './go-user.repository';

export class UserCustomFlagRepository extends DefaultCrudRepository<
  UserCustomFlag,
  typeof UserCustomFlag.prototype.id,
  UserCustomFlagRelations
> {

  public readonly goUser: BelongsToAccessor<GoUser, typeof UserCustomFlag.prototype.id>;

  constructor(
    @inject('datasources.db') dataSource: DbDataSource, @repository.getter('GoUserRepository') protected goUserRepositoryGetter: Getter<GoUserRepository>,
  ) {
    super(UserCustomFlag, dataSource);
    this.goUser = this.createBelongsToAccessorFor('goUser', goUserRepositoryGetter,);
    this.registerInclusionResolver('goUser', this.goUser.inclusionResolver);
  }
}
