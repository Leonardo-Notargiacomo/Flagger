import {inject, Getter} from '@loopback/core';
import {DefaultCrudRepository, repository, BelongsToAccessor} from '@loopback/repository';
import {DbDataSource} from '../datasources';
import {Friendship, FriendshipRelations, GoUser} from '../models';
import {GoUserRepository} from './go-user.repository';

export class FriendshipRepository extends DefaultCrudRepository<
  Friendship,
  typeof Friendship.prototype.id,
  FriendshipRelations
> {
  public readonly user: BelongsToAccessor<GoUser, typeof Friendship.prototype.id>;
  public readonly friend: BelongsToAccessor<GoUser, typeof Friendship.prototype.id>;

  constructor(
    @inject('datasources.db') dataSource: DbDataSource,
    @repository.getter('GoUserRepository')
    protected goUserRepositoryGetter: Getter<GoUserRepository>,
  ) {
    super(Friendship, dataSource);

    this.user = this.createBelongsToAccessorFor('user', goUserRepositoryGetter);
    this.registerInclusionResolver('user', this.user.inclusionResolver);

    this.friend = this.createBelongsToAccessorFor('friend', goUserRepositoryGetter);
    this.registerInclusionResolver('friend', this.friend.inclusionResolver);
  }

  /**
   * Helper method to delete bidirectional friendship
   * When removing a friend, both A→B and B→A should be deleted
   */
  async deleteBidirectional(userId: number, friendId: number): Promise<void> {
    // Delete both directions
    await this.deleteAll({
      or: [
        {userId: userId, friendId: friendId},
        {userId: friendId, friendId: userId},
      ],
    });
  }

  /**
   * Helper method to check if friendship exists (either direction)
   */
  async friendshipExists(userId: number, friendId: number): Promise<boolean> {
    const count = await this.count({
      or: [
        {userId: userId, friendId: friendId},
        {userId: friendId, friendId: userId},
      ],
    });
    return count.count > 0;
  }
}
