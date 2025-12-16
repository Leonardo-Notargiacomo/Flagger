import {Entity, model, property} from '@loopback/repository';

@model()
export class Badge extends Entity {
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
  })
  category: string;

  @property({
    type: 'object',
    required: true,
    postgresql: {
      dataType: 'jsonb',
    },
  })
  unlockCriteria: {
    type: 'exploration_count' | 'streak';
    threshold: number;
  }

  @property({
    type: 'boolean',
    default: false,
  })
  isChallengeBadge?: boolean;

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


  constructor(data?: Partial<Badge>) {
    super(data);
  }
}

export interface BadgeRelations {
  // describe navigational properties here
}

export type BadgeWithRelations = Badge & BadgeRelations;
