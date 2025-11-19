import {Entity, model, property, belongsTo} from '@loopback/repository';
import {Challenge} from './challenge.model';

@model()
export class UserChallenge extends Entity {
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

  @belongsTo(() => Challenge)
  challengeId: number;

  @property({
    type: 'string',
    required: true,
    default: 'active',
    jsonSchema: {
      enum: ['active', 'completed', 'expired'],
    },
  })
  status: 'active' | 'completed' | 'expired';

  @property({
    type: 'date',
    required: true,
    default: () => new Date(),
  })
  activatedAt: Date;

  @property({
    type: 'date',
  })
  completedAt?: Date;

  @property({
    type: 'date',
  })
  expiresAt?: Date;

  @property({
    type: 'date',
  })
  cooldownEndsAt?: Date;

  @property({
    type: 'object',
    default: {},
    postgresql: {
      dataType: 'jsonb',
    },
  })
  progressData?: {
    // Track progress for the challenge
    // e.g., { explorationsCount: 3, targetCount: 5 }
    [key: string]: any;
  };

  @property({
    type: 'boolean',
    default: false,
  })
  notificationSent?: boolean;

  constructor(data?: Partial<UserChallenge>) {
    super(data);
  }
}

export interface UserChallengeRelations {
  challenge?: Challenge;
}

export type UserChallengeWithRelations = UserChallenge & UserChallengeRelations;

