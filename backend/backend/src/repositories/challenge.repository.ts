import {inject} from '@loopback/core';
import {DefaultCrudRepository} from '@loopback/repository';
import {DbDataSource} from '../datasources';
import {Challenge, ChallengeRelations} from '../models';

export class ChallengeRepository extends DefaultCrudRepository<
  Challenge,
  typeof Challenge.prototype.id,
  ChallengeRelations
> {
  constructor(
    @inject('datasources.db') dataSource: DbDataSource,
  ) {
    super(Challenge, dataSource);
  }
}

