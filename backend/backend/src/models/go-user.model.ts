import {Entity, hasOne, model, property} from '@loopback/repository';
import {GoUserCredentials} from './go-user-credentials.model';
import {UserCustomFlag} from './user-custom-flag.model';

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
    required: true,
    index: {
      unique: true,
    },
  })
  userName: string;

  @property({
    type: 'string',
    required: true,
    index: {
      unique: true,
    },
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

  @hasOne(() => UserCustomFlag)
  userCustomFlag: UserCustomFlag;

  constructor(data?: Partial<GoUser>) {
    super(data);
  }
}

export interface GoUserRelations {
  goUserCredentials?: GoUserCredentials;
}

export type GoUserWithRelations = GoUser & GoUserRelations;
