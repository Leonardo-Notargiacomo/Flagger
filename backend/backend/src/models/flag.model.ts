import {Entity, model, property} from '@loopback/repository';

@model()
export class Flag extends Entity {
  @property({
    type: 'string',
    required: true,
  })
  PlaceID: string;

  @property({
    type: 'number',
    id: true,
    generated: true,
  })
  ID?: number;


  constructor(data?: Partial<Flag>) {
    super(data);
  }
}

export interface FlagRelations {
  // describe navigational properties here
}

export type FlagWithRelations = Flag & FlagRelations;
