import {Entity, model, property} from '@loopback/repository';

@model()
export class GoUser extends Entity {

  constructor(data?: Partial<GoUser>) {
    super(data);
  }
}

export interface GoUserRelations {
  // describe navigational properties here
}

export type GoUserWithRelations = GoUser & GoUserRelations;
