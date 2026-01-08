import {authenticate} from '@loopback/authentication';
import {inject} from '@loopback/core';
import {repository} from '@loopback/repository';
import {
  get,
  del,
  param,
  response,
  getModelSchemaRef,
  HttpErrors,
} from '@loopback/rest';
import {SecurityBindings, UserProfile} from '@loopback/security';
import {GoUser, Flag} from '../models';
import {FriendshipRepository, GoUserRepository, FlagRepository} from '../repositories';
import {applyDefaultFlagFields, getFlagQueryTimeoutMs} from '../utils/flag-query';
import {withTimeout} from '../utils/with-timeout';

@authenticate('jwt')
export class FriendshipController {
  constructor(
    @repository(FriendshipRepository)
    public friendshipRepository: FriendshipRepository,
    @repository(GoUserRepository)
    public goUserRepository: GoUserRepository,
    @repository(FlagRepository)
    public flagRepository: FlagRepository,
  ) {}

  /**
   * GET /friends
   * Get list of current user's friends with user details
   */
  @get('/friends')
  @response(200, {
    description: 'Array of friends with user details',
    content: {
      'application/json': {
        schema: {
          type: 'array',
          items: {
            type: 'object',
            properties: {
              id: {type: 'number'},
              userId: {type: 'number'},
              friendId: {type: 'number'},
              createdAt: {type: 'string', format: 'date-time'},
              friendDetails: getModelSchemaRef(GoUser),
            },
          },
        },
      },
    },
  })
  async getFriends(
    @inject(SecurityBindings.USER) currentUser: UserProfile,
  ): Promise<object[]> {
    const userId = parseInt(currentUser.id);

    // 1. Find all friendships where userId = current user
    const friendships = await this.friendshipRepository.find({
      where: {userId},
      include: [{relation: 'friend'}],
    });

    // 2 & 3. Return array with friend info (userName, email, bio, userImage)
    return friendships.map(friendship => ({
      id: friendship.id,
      userId: friendship.userId,
      friendId: friendship.friendId,
      createdAt: friendship.createdAt,
      friendDetails: friendship.friend,
    }));
  }

  /**
   * DELETE /friends/{friendId}
   * Remove a friend (deletes both A→B and B→A records)
   */
  @del('/friends/{friendId}')
  @response(204, {
    description: 'Friend removed successfully',
  })
  async removeFriend(
    @inject(SecurityBindings.USER) currentUser: UserProfile,
    @param.path.number('friendId') friendId: number,
  ): Promise<void> {
    const userId = parseInt(currentUser.id);

    // 1. Verify friendship exists
    const exists = await this.friendshipRepository.friendshipExists(userId, friendId);
    if (!exists) {
      throw new HttpErrors.NotFound('Friendship not found');
    }

    // 2 & 3. Delete both directions using helper method
    await this.friendshipRepository.deleteBidirectional(userId, friendId);
  }

  /**
   * GET /friends/{friendId}/flags
   * Get a specific friend's location flags (progress)
   */
  @get('/friends/{friendId}/flags')
  @response(200, {
    description: "Friend's flags/progress",
    content: {
      'application/json': {
        schema: {
          type: 'array',
          items: getModelSchemaRef(Flag),
        },
      },
    },
  })
  async getFriendFlags(
    @inject(SecurityBindings.USER) currentUser: UserProfile,
    @param.path.number('friendId') friendId: number,
  ): Promise<Flag[]> {
    const userId = parseInt(currentUser.id);

    // 1. Verify friendship exists between current user and friendId
    const exists = await this.friendshipRepository.friendshipExists(userId, friendId);
    if (!exists) {
      throw new HttpErrors.Forbidden('You can only view flags of your friends');
    }

    // 2. Get all flags where userId = friendId
    const flagsFilter = applyDefaultFlagFields({
      where: {userId: friendId},
    });

    const flags = await withTimeout(
      this.flagRepository.find(flagsFilter),
      getFlagQueryTimeoutMs(),
      new HttpErrors.GatewayTimeout('No Flags flagged'),
    );

    // 3. Return flags
    return flags;
  }
}
