import {
  repository,
} from '@loopback/repository';
import {
  param,
  get,
  getModelSchemaRef,
} from '@loopback/rest';
import {
  Review,
  Flag,
} from '../models';
import {ReviewRepository} from '../repositories';

export class ReviewFlagController {
  constructor(
    @repository(ReviewRepository)
    public reviewRepository: ReviewRepository,
  ) { }

  @get('/reviews/{id}/flag', {
    responses: {
      '200': {
        description: 'Flag belonging to Review',
        content: {
          'application/json': {
            schema: getModelSchemaRef(Flag),
          },
        },
      },
    },
  })
  async getFlag(
    @param.path.number('id') id: typeof Review.prototype.id,
  ): Promise<Flag> {
    return this.reviewRepository.flag(id);
  }
}
