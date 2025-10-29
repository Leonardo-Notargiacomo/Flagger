import {Entity, model, property} from '@loopback/repository';

@model()
export class GoUser extends Entity {
  @property({
    type: 'number',
    id: true,
    generated: true,
  })
  id?: number;

  @property({
    type: 'string',
    default: 'User',
  })
  userName?: string;

  @property({
    type: 'string',
    required: true,
  })
  email: string;

  @property({
    type: 'number',
    default: 'Blank',
  })
  userImage?: number;

  @property({
    type: 'string',
    default: 'Hi! Welcome and be part of my Journey',
  })
  bio?: string;

//  @hasOne(() => GoUserCredentials)
  //gUCToGU: GoUserCredentials;

  constructor(data?: Partial<GoUser>) {
    super(data);
  }
}

export interface GoUserRelations {
  // describe navigational properties here
}

export type GoUserWithRelations = GoUser & GoUserRelations;
