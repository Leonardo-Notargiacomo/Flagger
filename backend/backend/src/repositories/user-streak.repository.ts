import {inject} from '@loopback/core';
import {DefaultCrudRepository} from '@loopback/repository';
import {DbDataSource} from '../datasources';
import {UserStreak, UserStreakRelations} from '../models';

export class UserStreakRepository extends DefaultCrudRepository<
  UserStreak,
  typeof UserStreak.prototype.id,
  UserStreakRelations
> {
  constructor(
    @inject('datasources.db') dataSource: DbDataSource,
  ) {
    super(UserStreak, dataSource);
  }
}
