import {inject} from '@loopback/core';
import {DefaultCrudRepository} from '@loopback/repository';
import {DbDataSource} from '../datasources';
import {FcmToken, FcmTokenRelations} from '../models';

export class FcmTokenRepository extends DefaultCrudRepository<
  FcmToken,
  typeof FcmToken.prototype.id,
  FcmTokenRelations
> {
  constructor(
    @inject('datasources.db') dataSource: DbDataSource,
  ) {
    super(FcmToken, dataSource);
  }
}
