import {Entity, model, property, belongsTo} from '@loopback/repository';
import {GoUser} from './go-user.model';

@model({
  settings: {
    postgresql: {
      table: 'friend_requests',
    },
    indexes: {
      uniqueFriendRequest: {
        keys: {fromUserId: 1, toUserId: 1},
        options: {unique: true},
      },
    },
  },
})
export class FriendRequest extends Entity {
  @property({
    type: 'number',
    id: true,
    generated: true,
  })
  id?: number;

  @property({
    type: 'number',
    required: true,
  })
  fromUserId: number;

  @property({
    type: 'number',
    required: true,
  })
  toUserId: number;

  @property({
    type: 'string',
    required: true,
    default: 'PENDING',
    jsonSchema: {
      enum: ['PENDING', 'ACCEPTED', 'REJECTED'],
    },
  })
  status: string;

  @property({
    type: 'date',
    defaultFn: 'now',
  })
  createdAt?: Date;

  @property({
    type: 'date',
    defaultFn: 'now',
  })
  updatedAt?: Date;

  @belongsTo(() => GoUser, {name: 'fromUser'})
  fromUserRelationId: number;

  @belongsTo(() => GoUser, {name: 'toUser'})
  toUserRelationId: number;

  constructor(data?: Partial<FriendRequest>) {
    super(data);

    // Validation: prevent self-friendship
    if (data?.fromUserId && data?.toUserId && data.fromUserId === data.toUserId) {
      throw new Error('Cannot send friend request to yourself');
    }
  }
}

export interface FriendRequestRelations {
  fromUser?: GoUser;
  toUser?: GoUser;
}

export type FriendRequestWithRelations = FriendRequest & FriendRequestRelations;
