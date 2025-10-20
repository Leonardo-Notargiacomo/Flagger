import {Entity, model, property} from '@loopback/repository';

@model()
export class Flag extends Entity {
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
  PlaceId: string;

  @property({
    type: 'string',
    required: true,
  })
  GoUId: string;


  constructor(data?: Partial<Flag>) {
    super(data);
  }
}

export interface FlagRelations {
  // describe navigational properties here
}

export type FlagWithRelations = Flag & FlagRelations;
