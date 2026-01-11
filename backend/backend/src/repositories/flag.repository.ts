import {inject, Getter} from '@loopback/core';
import {DefaultCrudRepository, repository, BelongsToAccessor, HasManyRepositoryFactory} from '@loopback/repository';
import {DbDataSource} from '../datasources';
import {Flag, FlagRelations, GoUser, Review} from '../models';
import {GoUserRepository} from './go-user.repository';
import {ReviewRepository} from './review.repository';

export class FlagRepository extends DefaultCrudRepository<
  Flag,
  typeof Flag.prototype.id,
  FlagRelations
> {

  public readonly user: BelongsToAccessor<GoUser, typeof Flag.prototype.id>;

  public readonly reviews: HasManyRepositoryFactory<Review, typeof Flag.prototype.id>;

  constructor(
    @inject('datasources.db') dataSource: DbDataSource, @repository.getter('GoUserRepository') protected goUserRepositoryGetter: Getter<GoUserRepository>, @repository.getter('ReviewRepository') protected reviewRepositoryGetter: Getter<ReviewRepository>,
  ) {
    super(Flag, dataSource);
    this.reviews = this.createHasManyRepositoryFactoryFor('reviews', reviewRepositoryGetter,);
    this.registerInclusionResolver('reviews', this.reviews.inclusionResolver);
    this.user = this.createBelongsToAccessorFor('user', goUserRepositoryGetter,);
  }
}
