import {authenticate} from '@loopback/authentication';
import {inject} from '@loopback/core';
import {repository} from '@loopback/repository';
import {
  post,
  get,
  patch,
  del,
  param,
  requestBody,
  response,
  getModelSchemaRef,
  HttpErrors,
} from '@loopback/rest';
import {SecurityBindings, UserProfile} from '@loopback/security';
import {FriendRequest, Friendship} from '../models';
import {FriendRequestRepository, FriendshipRepository, GoUserRepository} from '../repositories';

@authenticate('jwt')
export class FriendRequestController {
  constructor(
    @repository(FriendRequestRepository)
    public friendRequestRepository: FriendRequestRepository,
    @repository(FriendshipRepository)
    public friendshipRepository: FriendshipRepository,
    @repository(GoUserRepository)
    public goUserRepository: GoUserRepository,
  ) {}

  /**
   * POST /friend-requests
   * Send a friend request to another user
   */
  @post('/friend-requests')
  @response(201, {
    description: 'Friend request created successfully',
    content: {'application/json': {schema: getModelSchemaRef(FriendRequest)}},
  })
  async sendFriendRequest(
    @inject(SecurityBindings.USER) currentUser: UserProfile,
    @requestBody({
      content: {
        'application/json': {
          schema: {
            type: 'object',
            required: ['toUserId'],
            properties: {
              toUserId: {type: 'number'},
            },
          },
        },
      },
    })
    request: {toUserId: number},
  ): Promise<FriendRequest> {
    const fromUserId = parseInt(currentUser.id);

    // TODO: Implement logic
    // 1. Check if toUser exists
    // 2. Check if already friends
    // 3. Check if request already exists
    // 4. Create friend request
    throw new HttpErrors.NotImplemented('Friend request sending not yet implemented');
  }

  /**
   * GET /friend-requests/received
   * Get all incoming friend requests for the current user
   */
  @get('/friend-requests/received')
  @response(200, {
    description: 'Array of received friend requests',
    content: {
      'application/json': {
        schema: {
          type: 'array',
          items: getModelSchemaRef(FriendRequest, {includeRelations: true}),
        },
      },
    },
  })
  async getReceivedRequests(
    @inject(SecurityBindings.USER) currentUser: UserProfile,
  ): Promise<FriendRequest[]> {
    const userId = parseInt(currentUser.id);

    // TODO: Implement logic
    // Get all requests where toUserId = current user and status = PENDING
    throw new HttpErrors.NotImplemented('Get received requests not yet implemented');
  }

  /**
   * GET /friend-requests/sent
   * Get all outgoing friend requests sent by the current user
   */
  @get('/friend-requests/sent')
  @response(200, {
    description: 'Array of sent friend requests',
    content: {
      'application/json': {
        schema: {
          type: 'array',
          items: getModelSchemaRef(FriendRequest, {includeRelations: true}),
        },
      },
    },
  })
  async getSentRequests(
    @inject(SecurityBindings.USER) currentUser: UserProfile,
  ): Promise<FriendRequest[]> {
    const userId = parseInt(currentUser.id);

    // TODO: Implement logic
    // Get all requests where fromUserId = current user
    throw new HttpErrors.NotImplemented('Get sent requests not yet implemented');
  }

  /**
   * PATCH /friend-requests/{id}/accept
   * Accept a friend request and create bidirectional friendship
   */
  @patch('/friend-requests/{id}/accept')
  @response(200, {
    description: 'Friend request accepted',
    content: {
      'application/json': {
        schema: {
          type: 'object',
          properties: {
            message: {type: 'string'},
            friendRequest: getModelSchemaRef(FriendRequest),
          },
        },
      },
    },
  })
  async acceptFriendRequest(
    @inject(SecurityBindings.USER) currentUser: UserProfile,
    @param.path.number('id') requestId: number,
  ): Promise<{message: string; friendRequest: FriendRequest}> {
    const userId = parseInt(currentUser.id);

    // TODO: Implement logic
    // 1. Find the friend request
    // 2. Verify current user is the recipient (toUserId)
    // 3. Check status is PENDING
    // 4. Update status to ACCEPTED
    // 5. Create 2 Friendship records (A→B and B→A)
    throw new HttpErrors.NotImplemented('Accept friend request not yet implemented');
  }

  /**
   * PATCH /friend-requests/{id}/reject
   * Reject a friend request
   */
  @patch('/friend-requests/{id}/reject')
  @response(200, {
    description: 'Friend request rejected',
    content: {
      'application/json': {
        schema: {
          type: 'object',
          properties: {
            message: {type: 'string'},
          },
        },
      },
    },
  })
  async rejectFriendRequest(
    @inject(SecurityBindings.USER) currentUser: UserProfile,
    @param.path.number('id') requestId: number,
  ): Promise<{message: string}> {
    const userId = parseInt(currentUser.id);

    // TODO: Implement logic
    // 1. Find the friend request
    // 2. Verify current user is the recipient
    // 3. Update status to REJECTED
    throw new HttpErrors.NotImplemented('Reject friend request not yet implemented');
  }

  /**
   * DELETE /friend-requests/{id}
   * Cancel a sent friend request
   */
  @del('/friend-requests/{id}')
  @response(204, {
    description: 'Friend request cancelled successfully',
  })
  async cancelFriendRequest(
    @inject(SecurityBindings.USER) currentUser: UserProfile,
    @param.path.number('id') requestId: number,
  ): Promise<void> {
    const userId = parseInt(currentUser.id);

    // TODO: Implement logic
    // 1. Find the friend request
    // 2. Verify current user is the sender (fromUserId)
    // 3. Delete the request
    throw new HttpErrors.NotImplemented('Cancel friend request not yet implemented');
  }
}
