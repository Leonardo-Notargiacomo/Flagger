import {Entity, model, property, belongsTo} from '@loopback/repository';
import {GoUser} from './go-user.model';

@model()
export class UserCustomFlag extends Entity {
  @property({
    type: 'number',
    id: true,
    generated: true,
  })
  id?: number;

  @property({
    type: 'string',
    default: "Color.RED",
  })
  background?: string;

  @property({
    type: 'string',
  })
  emoji?: string;

  @belongsTo(() => GoUser)
  goUserId: number;

  constructor(data?: Partial<UserCustomFlag>) {
    super(data);
  }
}

export interface UserCustomFlagRelations {
  // describe navigational properties here
}

export type UserCustomFlagWithRelations = UserCustomFlag & UserCustomFlagRelations;
