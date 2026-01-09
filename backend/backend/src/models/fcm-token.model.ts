import {Entity, model, property} from '@loopback/repository';

@model({
  settings: {
    indexes: {
      uniqueUserToken: {
        keys: {
          userId: 1,
          token: 1,
        },
        options: {
          unique: true,
        },
      },
    },
  },
})
export class FcmToken extends Entity {
  @property({
    type: 'number',
    id: true,
    generated: true,
  })
  id?: number;

  @property({
    type: 'number',
    required: true,
    index: true,
  })
  userId: number;

  @property({
    type: 'string',
    required: true,
  })
  token: string;

  @property({
    type: 'string',
    default: 'android',
  })
  platform?: string;

  @property({
    type: 'date',
    default: () => new Date(),
  })
  createdAt?: Date;

  @property({
    type: 'date',
    default: () => new Date(),
  })
  lastUpdated?: Date;

  @property({
    type: 'boolean',
    default: true,
  })
  isActive?: boolean;

  constructor(data?: Partial<FcmToken>) {
    super(data);
  }
}

export interface FcmTokenRelations {
  // describe navigational properties here
}

export type FcmTokenWithRelations = FcmToken & FcmTokenRelations;
