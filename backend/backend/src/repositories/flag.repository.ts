import {inject} from '@loopback/core';
import {DefaultCrudRepository} from '@loopback/repository';
import {DbDataSource} from '../datasources';
import {Flag, FlagRelations} from '../models';

export class FlagRepository extends DefaultCrudRepository<
  Flag,
  typeof Flag.prototype.ID,
  FlagRelations
> {
  constructor(
    @inject('datasources.db') dataSource: DbDataSource,
  ) {
    super(Flag, dataSource);
  }
}
