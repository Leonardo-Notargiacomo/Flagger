import {Entity, model, property} from '@loopback/repository';

@model()
export class Goer extends Entity {

  constructor(data?: Partial<Goer>) {
    super(data);
  }
}

export interface GoerRelations {
  // describe navigational properties here
}

export type GoerWithRelations = Goer & GoerRelations;
