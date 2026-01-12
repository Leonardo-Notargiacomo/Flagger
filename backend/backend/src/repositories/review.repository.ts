import {inject, Getter} from '@loopback/core';
import {DefaultCrudRepository, repository, BelongsToAccessor} from '@loopback/repository';
import {DbDataSource} from '../datasources';
import {Review, ReviewRelations, Flag} from '../models';
import {FlagRepository} from './flag.repository';

export class ReviewRepository extends DefaultCrudRepository<
  Review,
  typeof Review.prototype.id,
  ReviewRelations
> {

  public readonly flag: BelongsToAccessor<Flag, typeof Review.prototype.id>;

  constructor(
    @inject('datasources.db') dataSource: DbDataSource, @repository.getter('FlagRepository') protected flagRepositoryGetter: Getter<FlagRepository>,
  ) {
    super(Review, dataSource);
    this.flag = this.createBelongsToAccessorFor('flag', flagRepositoryGetter,);
    this.registerInclusionResolver('flag', this.flag.inclusionResolver);
  }
}
