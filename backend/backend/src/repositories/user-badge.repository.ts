import {DefaultCrudRepository, repository, BelongsToAccessor} from '@loopback/repository';
import {UserBadge, Badge, User} from '../models';
import {DbDataSource} from '../datasources';
import {inject, Getter} from '@loopback/core';
import {BadgeRepository} from './badge.repository';
import {UserRepository} from './user.repository';

export class UserBadgeRepository extends DefaultCrudRepository<
  UserBadge,
  typeof UserBadge.prototype.id
> {
  public readonly badge: BelongsToAccessor<Badge, typeof UserBadge.prototype.id>;
  public readonly user: BelongsToAccessor<User, typeof UserBadge.prototype.id>;

  constructor(
    @inject('datasources.db') dataSource: DbDataSource,
    @repository.getter('BadgeRepository')
    protected badgeRepositoryGetter: Getter<BadgeRepository>,
    @repository.getter('UserRepository')
    protected userRepositoryGetter: Getter<UserRepository>,
  ) {
    super(UserBadge, dataSource);

    this.badge = this.createBelongsToAccessorFor('badge', badgeRepositoryGetter);
    this.registerInclusionResolver('badge', this.badge.inclusionResolver);

    this.user = this.createBelongsToAccessorFor('user', userRepositoryGetter);
    this.registerInclusionResolver('user', this.user.inclusionResolver);
  }
}
