import {
  Count,
  CountSchema,
  Filter,
  repository,
  Where,
} from '@loopback/repository';
import {
  del,
  get,
  getModelSchemaRef,
  getWhereSchemaFor,
  param,
  patch,
  post,
  requestBody,
} from '@loopback/rest';
import {
  Flag,
  Review,
} from '../models';
import {FlagRepository} from '../repositories';

export class FlagReviewController {
  constructor(
    @repository(FlagRepository) protected flagRepository: FlagRepository,
  ) { }

  @get('/flags/{id}/reviews', {
    responses: {
      '200': {
        description: 'Array of Flag has many Review',
        content: {
          'application/json': {
            schema: {type: 'array', items: getModelSchemaRef(Review)},
          },
        },
      },
    },
  })
  async find(
    @param.path.number('id') id: number,
    @param.query.object('filter') filter?: Filter<Review>,
  ): Promise<Review[]> {
    return this.flagRepository.reviews(id).find(filter);
  }

  @post('/flags/{id}/reviews', {
    responses: {
      '200': {
        description: 'Flag model instance',
        content: {'application/json': {schema: getModelSchemaRef(Review)}},
      },
    },
  })
  async create(
    @param.path.number('id') id: typeof Flag.prototype.id,
    @requestBody({
      content: {
        'application/json': {
          schema: getModelSchemaRef(Review, {
            title: 'NewReviewInFlag',
            exclude: ['id'],
            optional: ['flagId']
          }),
        },
      },
    }) review: Omit<Review, 'id'>,
  ): Promise<Review> {
    return this.flagRepository.reviews(id).create(review);
  }

  @patch('/flags/{id}/reviews', {
    responses: {
      '200': {
        description: 'Flag.Review PATCH success count',
        content: {'application/json': {schema: CountSchema}},
      },
    },
  })
  async patch(
    @param.path.number('id') id: number,
    @requestBody({
      content: {
        'application/json': {
          schema: getModelSchemaRef(Review, {partial: true}),
        },
      },
    })
    review: Partial<Review>,
    @param.query.object('where', getWhereSchemaFor(Review)) where?: Where<Review>,
  ): Promise<Count> {
    return this.flagRepository.reviews(id).patch(review, where);
  }

  @del('/flags/{id}/reviews', {
    responses: {
      '200': {
        description: 'Flag.Review DELETE success count',
        content: {'application/json': {schema: CountSchema}},
      },
    },
  })
  async delete(
    @param.path.number('id') id: number,
    @param.query.object('where', getWhereSchemaFor(Review)) where?: Where<Review>,
  ): Promise<Count> {
    return this.flagRepository.reviews(id).delete(where);
  }
}
