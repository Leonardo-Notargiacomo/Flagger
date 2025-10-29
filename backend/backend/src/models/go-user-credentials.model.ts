import {belongsTo, Entity, model, property} from '@loopback/repository';
import {GoUser} from "./go-user.model";

@model()
export class GoUserCredentials extends Entity {
  @property({
    type: 'string',
    id: true,
    generated: true,
  })
  id?: string;

  @property({
    type: 'string',
    required: true,
  })
  password: string;

  @property({
    type: 'number',
    required: 'true'
  })
  goUserId: number;

  constructor(data?: Partial<GoUserCredentials>) {
    super(data);
  }
}

export interface GoUserCredentialsRelations {
  // describe navigational properties here
}

export type GoUserCredentialsWithRelations = GoUserCredentials & GoUserCredentialsRelations;
