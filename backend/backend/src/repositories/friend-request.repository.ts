import {inject, Getter} from '@loopback/core';
import {DefaultCrudRepository, repository, BelongsToAccessor} from '@loopback/repository';
import {DbDataSource} from '../datasources';
import {FriendRequest, FriendRequestRelations, GoUser} from '../models';
import {GoUserRepository} from './go-user.repository';

export class FriendRequestRepository extends DefaultCrudRepository<
  FriendRequest,
  typeof FriendRequest.prototype.id,
  FriendRequestRelations
> {
  public readonly fromUser: BelongsToAccessor<GoUser, typeof FriendRequest.prototype.id>;
  public readonly toUser: BelongsToAccessor<GoUser, typeof FriendRequest.prototype.id>;

  constructor(
    @inject('datasources.db') dataSource: DbDataSource,
    @repository.getter('GoUserRepository')
    protected goUserRepositoryGetter: Getter<GoUserRepository>,
  ) {
    super(FriendRequest, dataSource);

    this.fromUser = this.createBelongsToAccessorFor('fromUser', goUserRepositoryGetter);
    this.registerInclusionResolver('fromUser', this.fromUser.inclusionResolver);

    this.toUser = this.createBelongsToAccessorFor('toUser', goUserRepositoryGetter);
    this.registerInclusionResolver('toUser', this.toUser.inclusionResolver);
  }

  /**
   * Override create to add validation and update timestamp
   */
  async create(entity: Partial<FriendRequest>, options?: object): Promise<FriendRequest> {
    // Set updatedAt timestamp
    entity.updatedAt = new Date();
    return super.create(entity, options);
  }

  /**
   * Override updateById to update timestamp
   */
  async updateById(
    id: typeof FriendRequest.prototype.id,
    data: Partial<FriendRequest>,
    options?: object,
  ): Promise<void> {
    // Update the updatedAt timestamp
    data.updatedAt = new Date();
    return super.updateById(id, data, options);
  }
}
