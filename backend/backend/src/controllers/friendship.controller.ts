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
import {Friendship, GoUser, Flag} from '../models';
import {FriendshipRepository, GoUserRepository, FlagRepository} from '../repositories';

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

    // TODO: Implement logic
    // 1. Find all friendships where userId = current user
    // 2. Include friend user details (userName, email, bio, userImage)
    // 3. Return array with friend info
    throw new HttpErrors.NotImplemented('Get friends list not yet implemented');
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

    // TODO: Implement logic
    // 1. Verify friendship exists
    // 2. Delete both directions (userId→friendId and friendId→userId)
    // 3. Use friendshipRepository.deleteBidirectional()
    throw new HttpErrors.NotImplemented('Remove friend not yet implemented');
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

    // TODO: Implement logic
    // 1. Verify friendship exists between current user and friendId
    // 2. Get all flags where goUserId = friendId
    // 3. Return flags
    throw new HttpErrors.NotImplemented('Get friend flags not yet implemented');
  }
}
