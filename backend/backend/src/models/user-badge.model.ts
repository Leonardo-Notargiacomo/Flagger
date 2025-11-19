import {Entity, model, property, belongsTo} from '@loopback/repository';
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

  @belongsTo(() => Badge)
  badgeId: number;

  @property({
    type: 'date',
    default: () => new Date(),
  })
  unlockedAt?: Date;

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
  badge?: Badge;
}

export type UserBadgeWithRelations = UserBadge & UserBadgeRelations;
