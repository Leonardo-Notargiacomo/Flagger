import {Entity, model, property, belongsTo} from '@loopback/repository';
import {GoUser} from './go-user.model';

@model({
  settings: {
    postgresql: {
      table: 'friendships',
    },
    indexes: {
      uniqueFriendship: {
        keys: {userId: 1, friendId: 1},
        options: {unique: true},
      },
    },
  },
})
export class Friendship extends Entity {
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
  userId: number;

  @property({
    type: 'number',
    required: true,
  })
  friendId: number;

  @property({
    type: 'date',
    defaultFn: 'now',
  })
  createdAt?: Date;

  @belongsTo(() => GoUser, {name: 'user'})
  userRelationId: number;

  @belongsTo(() => GoUser, {name: 'friend'})
  friendRelationId: number;

  constructor(data?: Partial<Friendship>) {
    super(data);

    // Validation: prevent self-friendship
    if (data?.userId && data?.friendId && data.userId === data.friendId) {
      throw new Error('Cannot create friendship with yourself');
    }
  }
}

export interface FriendshipRelations {
  user?: GoUser;
  friend?: GoUser;
}

export type FriendshipWithRelations = Friendship & FriendshipRelations;
