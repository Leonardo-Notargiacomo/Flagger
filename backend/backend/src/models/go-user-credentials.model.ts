import {belongsTo, Entity, model, property} from '@loopback/repository';
import {GoUser} from "./go-user.model";

@model()
export class GoUserCredentials extends Entity {
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
  password: string;

  @belongsTo(() => GoUser)
  goUserId: number;

  constructor(data?: Partial<GoUserCredentials>) {
    super(data);
  }
}

export interface GoUserCredentialsRelations {
  goUser?: GoUser;
}

export type GoUserCredentialsWithRelations = GoUserCredentials & GoUserCredentialsRelations;
