import {inject} from '@loopback/core';
import {DefaultCrudRepository} from '@loopback/repository';
import {DbDataSource} from '../datasources';
import {GoUserCredentials, GoUserCredentialsRelations} from '../models';

export class GoUserCredentialsRepository extends DefaultCrudRepository<
  GoUserCredentials,
  typeof GoUserCredentials.prototype.id,
  GoUserCredentialsRelations
> {
  constructor(
    @inject('datasources.db') dataSource: DbDataSource,
  ) {
    super(GoUserCredentials, dataSource);
  }
}
