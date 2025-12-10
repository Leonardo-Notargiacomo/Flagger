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
    default: "#1A0000",
  })
  background?: string;

  @property({
    type: 'string',
    default: "❤️",

  })
  emoji?: string;
  @property({
    type: 'string',
    default: "#FF3131",

  })
  border?: string;
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
