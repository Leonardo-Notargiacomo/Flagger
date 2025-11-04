import {inject} from '@loopback/core';
import {DefaultCrudRepository} from '@loopback/repository';
import {DbDataSource} from '../datasources';
import {Badge, BadgeRelations} from '../models';

export class BadgeRepository extends DefaultCrudRepository<
  Badge,
  typeof Badge.prototype.id,
  BadgeRelations
> {
  constructor(
    @inject('datasources.db') dataSource: DbDataSource,
  ) {
    super(Badge, dataSource);
  }
}
