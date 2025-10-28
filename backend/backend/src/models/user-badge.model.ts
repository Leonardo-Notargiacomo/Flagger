import {Entity, model, property} from '@loopback/repository';
import {Badge} from './badge.model';

@model()
export class UserBadge extends Entity {
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
  badgeId: number;

  @property({
    type: 'date',
    default: () => new Date(),
  })
  unlockedAt?: string;

  @property({
    type: 'boolean',
    default: false,
  })
  notificationSent?: boolean;


  constructor(data?: Partial<UserBadge>) {
    super(data);
  }
}

export interface UserBadgeRelations {
  // describe navigational properties here
}

export type UserBadgeWithRelations = UserBadge & UserBadgeRelations;
