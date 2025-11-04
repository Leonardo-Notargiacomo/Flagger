import {DefaultCrudRepository, repository, BelongsToAccessor} from '@loopback/repository';
import {UserBadge, Badge} from '../models';
import {DbDataSource} from '../datasources';
import {inject, Getter} from '@loopback/core';
import {BadgeRepository} from './badge.repository';

export class UserBadgeRepository extends DefaultCrudRepository<
  UserBadge,
  typeof UserBadge.prototype.id
> {
  public readonly badge: BelongsToAccessor<Badge, typeof UserBadge.prototype.id>;

  constructor(
    @inject('datasources.db') dataSource: DbDataSource,
    @repository.getter('BadgeRepository')
    protected badgeRepositoryGetter: Getter<BadgeRepository>,
  ) {
    super(UserBadge, dataSource);

    this.badge = this.createBelongsToAccessorFor('badge', badgeRepositoryGetter);
    this.registerInclusionResolver('badge', this.badge.inclusionResolver);
  }
}
