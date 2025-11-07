import {Entity, model, property} from '@loopback/repository';

@model()
export class UserStreak extends Entity {
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
    default: 0,
  })
  currentStreak?: number;

  @property({
    type: 'number',
    default: 0,
  })
  longestStreak?: number;

  @property({
    type: 'date',
  })
  lastActivityDate?: Date;

  @property({
    type: 'date',
    default: () => new Date(),
  })
  updatedAt?: Date;


  constructor(data?: Partial<UserStreak>) {
    super(data);
  }
}

export interface UserStreakRelations {
  // describe navigational properties here
}

export type UserStreakWithRelations = UserStreak & UserStreakRelations;
