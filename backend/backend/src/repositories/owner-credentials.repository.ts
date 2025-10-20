import {inject} from '@loopback/core';
import {DefaultCrudRepository} from '@loopback/repository';
import {DbDataSource} from '../datasources';
import {OwnerCredentials, OwnerCredentialsRelations} from '../models';

export class OwnerCredentialsRepository extends DefaultCrudRepository<
  OwnerCredentials,
  typeof OwnerCredentials.prototype.id,
  OwnerCredentialsRelations
> {
  constructor(
    @inject('datasources.db') dataSource: DbDataSource,
  ) {
    super(OwnerCredentials, dataSource);
  }
}
