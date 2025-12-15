import {inject, Getter} from '@loopback/core';
import {DefaultCrudRepository, BelongsToAccessor, repository} from '@loopback/repository';
import {DbDataSource} from '../datasources';
import {UserChallenge, UserChallengeRelations, Challenge} from '../models';
import {ChallengeRepository} from './challenge.repository';

export class UserChallengeRepository extends DefaultCrudRepository<
  UserChallenge,
  typeof UserChallenge.prototype.id,
  UserChallengeRelations
> {
  public readonly challenge: BelongsToAccessor<Challenge, typeof UserChallenge.prototype.id>;

  constructor(
    @inject('datasources.db') dataSource: DbDataSource,
    @repository.getter('ChallengeRepository')
    protected challengeRepositoryGetter: Getter<ChallengeRepository>,
  ) {
    super(UserChallenge, dataSource);
    this.challenge = this.createBelongsToAccessorFor('challenge', challengeRepositoryGetter);
    this.registerInclusionResolver('challenge', this.challenge.inclusionResolver);
  }
}

