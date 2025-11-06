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
    const {toUserId} = request;

    // 1. Check if toUser exists
    const toUser = await this.goUserRepository.findById(toUserId).catch(() => {
      throw new HttpErrors.NotFound(`User with id ${toUserId} not found`);
    });

    // Prevent sending request to yourself
    if (fromUserId === toUserId) {
      throw new HttpErrors.BadRequest('Cannot send friend request to yourself');
    }

    // 2. Check if already friends
    const alreadyFriends = await this.friendshipRepository.friendshipExists(fromUserId, toUserId);
    if (alreadyFriends) {
      throw new HttpErrors.BadRequest('You are already friends with this user');
    }

    // 3. Check if request already exists (in either direction)
    const existingRequests = await this.friendRequestRepository.find({
      where: {
        or: [
          {fromUserId, toUserId, status: 'PENDING'},
          {fromUserId: toUserId, toUserId: fromUserId, status: 'PENDING'},
        ],
      },
    });

    if (existingRequests.length > 0) {
      throw new HttpErrors.Conflict('A pending friend request already exists between these users');
    }

    // 4. Create friend request
    const friendRequest = await this.friendRequestRepository.create({
      fromUserId,
      toUserId,
      status: 'PENDING',
    });

    return friendRequest;
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

    // Get all requests where toUserId = current user and status = PENDING
    const requests = await this.friendRequestRepository.find({
      where: {
        toUserId: userId,
        status: 'PENDING',
      },
      include: [
        {relation: 'fromUser'},
        {relation: 'toUser'},
      ],
    });

    return requests;
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

    // Get all requests where fromUserId = current user
    const requests = await this.friendRequestRepository.find({
      where: {
        fromUserId: userId,
      },
      include: [
        {relation: 'fromUser'},
        {relation: 'toUser'},
      ],
    });

    return requests;
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

    // 1. Find the friend request
    const friendRequest = await this.friendRequestRepository.findById(requestId).catch(() => {
      throw new HttpErrors.NotFound('Friend request not found');
    });

    // 2. Verify current user is the recipient (toUserId)
    if (friendRequest.toUserId !== userId) {
      throw new HttpErrors.Forbidden('You can only accept friend requests sent to you');
    }

    // 3. Check status is PENDING
    if (friendRequest.status !== 'PENDING') {
      throw new HttpErrors.BadRequest(`Friend request is already ${friendRequest.status.toLowerCase()}`);
    }

    // 4. Update status to ACCEPTED
    await this.friendRequestRepository.updateById(requestId, {status: 'ACCEPTED'});
    friendRequest.status = 'ACCEPTED';

    // 5. Create 2 Friendship records (A→B and B→A)
    await this.friendshipRepository.create({
      userId: friendRequest.fromUserId,
      friendId: friendRequest.toUserId,
    });
    await this.friendshipRepository.create({
      userId: friendRequest.toUserId,
      friendId: friendRequest.fromUserId,
    });

    return {
      message: 'Friend request accepted successfully',
      friendRequest,
    };
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

    // 1. Find the friend request
    const friendRequest = await this.friendRequestRepository.findById(requestId).catch(() => {
      throw new HttpErrors.NotFound('Friend request not found');
    });

    // 2. Verify current user is the recipient
    if (friendRequest.toUserId !== userId) {
      throw new HttpErrors.Forbidden('You can only reject friend requests sent to you');
    }

    // Check status is PENDING
    if (friendRequest.status !== 'PENDING') {
      throw new HttpErrors.BadRequest(`Friend request is already ${friendRequest.status.toLowerCase()}`);
    }

    // 3. Update status to REJECTED
    await this.friendRequestRepository.updateById(requestId, {status: 'REJECTED'});

    return {
      message: 'Friend request rejected successfully',
    };
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

    // 1. Find the friend request
    const friendRequest = await this.friendRequestRepository.findById(requestId).catch(() => {
      throw new HttpErrors.NotFound('Friend request not found');
    });

    // 2. Verify current user is the sender (fromUserId)
    if (friendRequest.fromUserId !== userId) {
      throw new HttpErrors.Forbidden('You can only cancel friend requests you sent');
    }

    // 3. Delete the request
    await this.friendRequestRepository.deleteById(requestId);
  }
}
