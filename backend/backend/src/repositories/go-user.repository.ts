import {inject} from '@loopback/core';
import {DefaultCrudRepository} from '@loopback/repository';
import {DbDataSource} from '../datasources';
import {GoUser, GoUserRelations} from '../models';

export class GoUserRepository extends DefaultCrudRepository<
  GoUser,
  typeof GoUser.prototype.id,
  GoUserRelations
> {
  constructor(
    @inject('datasources.db') dataSource: DbDataSource,
  ) {
    super(GoUser, dataSource);
  }
}
