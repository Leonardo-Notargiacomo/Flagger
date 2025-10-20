import {Entity, model, property} from '@loopback/repository';

@model()
export class OwnerCredentials extends Entity {

  constructor(data?: Partial<OwnerCredentials>) {
    super(data);
  }
}

export interface OwnerCredentialsRelations {
  // describe navigational properties here
}

export type OwnerCredentialsWithRelations = OwnerCredentials & OwnerCredentialsRelations;
