import {Entity, model, property, belongsTo, hasMany} from '@loopback/repository';
import {GoUser} from './go-user.model';
import {Review} from './review.model';

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
  location_id: string;

  @property({
    type: 'string',
    default: 'Blank',
  })
  photoCode?: string;

  @property({
    type: 'date',
    required: true,
  })
  dateTaken: Date;

  @property({
    type: 'number',
    required: true,
  })
  notification: number;

  @belongsTo(() => GoUser)
  userId: number;

  @hasMany(() => Review)
  reviews: Review[];

  constructor(data?: Partial<Flag>) {
    super(data);
  }
}

export interface FlagRelations {
  // describe navigational properties here
}

export type FlagWithRelations = Flag & FlagRelations;
