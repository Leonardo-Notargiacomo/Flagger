import {inject, Getter} from '@loopback/core';
import {DefaultCrudRepository, repository, BelongsToAccessor} from '@loopback/repository';
import {DbDataSource} from '../datasources';
import {Flag, FlagRelations, GoUser} from '../models';
import {GoUserRepository} from './go-user.repository';

export class FlagRepository extends DefaultCrudRepository<
  Flag,
  typeof Flag.prototype.id,
  FlagRelations
> {

  public readonly user: BelongsToAccessor<GoUser, typeof Flag.prototype.id>;

  constructor(
    @inject('datasources.db') dataSource: DbDataSource, @repository.getter('GoUserRepository') protected goUserRepositoryGetter: Getter<GoUserRepository>,
  ) {
    super(Flag, dataSource);
    this.user = this.createBelongsToAccessorFor('user', goUserRepositoryGetter,);
  }
}
