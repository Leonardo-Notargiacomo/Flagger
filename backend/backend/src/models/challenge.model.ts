import {Entity, model, property} from '@loopback/repository';

@model()
export class Challenge extends Entity {
  @property({
    type: 'number',
    id: true,
    generated: true,
  })
  id?: number;

  @property({
    type: 'string',
    required: true,
  })
  name: string;

  @property({
    type: 'string',
    required: true,
  })
  description: string;

  @property({
    type: 'string',
  })
  iconUrl?: string;

  @property({
    type: 'string',
    required: true,
    jsonSchema: {
      enum: ['exploration_count', 'time_based', 'streak'],
    },
  })
  conditionType: 'exploration_count' | 'time_based' | 'streak';

  @property({
    type: 'object',
    required: true,
    postgresql: {
      dataType: 'jsonb',
    },
  })
  conditionParams: {
    // For exploration_count: { count: number }
    // For time_based: { hour: number } (e.g., 22 for 10 PM)
    // For streak: { days: number }
    [key: string]: any;
  };

  @property({
    type: 'number',
    required: true,
  })
  rewardBadgeId: number;

  @property({
    type: 'number',
    default: 24,
  })
  cooldownHours?: number;

  @property({
    type: 'number',
  })
  expirationHours?: number;

  @property({
    type: 'boolean',
    default: false,
  })
  allowsExtension?: boolean;

  @property({
    type: 'boolean',
    default: true,
  })
  isActive?: boolean;

  @property({
    type: 'string',
    required: true,
    default: 'easy',
    jsonSchema: {
      enum: ['easy', 'novice', 'advanced', 'expert', 'chad'],
    },
  })
  difficulty: 'easy' | 'novice' | 'advanced' | 'expert' | 'chad';

  @property({
    type: 'number',
    default: 0,
  })
  displayOrder?: number;

  @property({
    type: 'date',
    default: () => new Date(),
  })
  createdAt?: Date;

  constructor(data?: Partial<Challenge>) {
    super(data);
  }
}

export interface ChallengeRelations {
  // describe navigational properties here
}

export type ChallengeWithRelations = Challenge & ChallengeRelations;

