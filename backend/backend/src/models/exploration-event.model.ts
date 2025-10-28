import {Entity, model, property} from '@loopback/repository';

@model()
export class ExplorationEvent extends Entity {
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
    type: 'string',
  })
  LocationName?: string;

  @property({
    type: 'number',
  })
  latitude?: number;

  @property({
    type: 'number',
  })
  longitude?: number;

  @property({
    type: 'date',
    default: () => new Date(),
  })
  completedAt?: Date;

  @property({
    type: 'string',
  })
  notes?: string;

  @property({
    type: 'string',
    default: 'verified',
  })
  verificationStatus?: string;


  constructor(data?: Partial<ExplorationEvent>) {
    super(data);
  }
}

export interface ExplorationEventRelations {
  // describe navigational properties here
}

export type ExplorationEventWithRelations = ExplorationEvent & ExplorationEventRelations;
