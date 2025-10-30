import {Entity, hasOne, model, property} from '@loopback/repository';
import {GoUserCredentials} from './go-user-credentials.model';

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
  })
  userImage?: number;

  @property({
    type: 'string',
    default: 'Hi! Welcome and be part of my Journey',
  })
  bio?: string;

  @hasOne(() => GoUserCredentials, {keyTo: 'goUserId'})
  goUserCredentials: GoUserCredentials;

  constructor(data?: Partial<GoUser>) {
    super(data);
  }
}

export interface GoUserRelations {
  goUserCredentials?: GoUserCredentials;
}

export type GoUserWithRelations = GoUser & GoUserRelations;
