import {inject} from '@loopback/core';
import {DefaultCrudRepository} from '@loopback/repository';
import {DbDataSource} from '../datasources';
import {NotificationHistory, NotificationHistoryRelations} from '../models';

export class NotificationHistoryRepository extends DefaultCrudRepository<
  NotificationHistory,
  typeof NotificationHistory.prototype.id,
  NotificationHistoryRelations
> {
  constructor(
    @inject('datasources.db') dataSource: DbDataSource,
  ) {
    super(NotificationHistory, dataSource);
  }
}
