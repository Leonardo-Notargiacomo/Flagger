import {inject} from '@loopback/core';
import {DefaultCrudRepository} from '@loopback/repository';
import {DbDataSource} from '../datasources';
import {ExplorationEvent, ExplorationEventRelations} from '../models';

export class ExplorationEventRepository extends DefaultCrudRepository<
  ExplorationEvent,
  typeof ExplorationEvent.prototype.id,
  ExplorationEventRelations
> {
  constructor(
    @inject('datasources.db') dataSource: DbDataSource,
  ) {
    super(ExplorationEvent, dataSource);
  }
}
