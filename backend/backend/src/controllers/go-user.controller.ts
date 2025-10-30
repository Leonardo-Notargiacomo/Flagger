import {
  Count,
  CountSchema,
  Filter,
  FilterExcludingWhere, model, property,
  repository,
  Where,
} from '@loopback/repository';
import {
  post,
  param,
  get,
  getModelSchemaRef,
  patch,
  put,
  del,
  requestBody,
  response, SchemaObject,
} from '@loopback/rest';
import {GoUser} from '../models';
import {GoUserRepository} from '../repositories';
import {inject} from '@loopback/core';
import {
  TokenServiceBindings,
  UserServiceBindings,
} from '@loopback/authentication-jwt';
import {authenticate, TokenService} from '@loopback/authentication';
import {SecurityBindings, securityId, UserProfile} from '@loopback/security';
import {genSalt, hash} from 'bcryptjs';
import _ from 'lodash';
import {Credentials, MyUserService} from '../services';


@model()
export class NewUserRequest extends GoUser {
  @property({
    type: 'string',
    required: true,
  })
  password: string;
}

const CredentialsSchema: SchemaObject = {
  type: 'object',
  required: ['email', 'password'],
  properties: {
    email: {
      type: 'string',
      format: 'email',
    },
    password: {
      type: 'string',
      minLength: 8,
    },
  },
};

export const CredentialsRequestBody = {
  description: 'The input of login function',
  required: true,
  content: {
    'application/json': {schema: CredentialsSchema},
  },
};

export class GoUserController {
  constructor(
    @inject(TokenServiceBindings.TOKEN_SERVICE)
    public jwtService: TokenService,
    @inject(UserServiceBindings.USER_SERVICE)
    public userService: MyUserService,
    @inject(SecurityBindings.USER, {optional: true})
    public user: UserProfile, 
    @repository(GoUserRepository) protected userRepository: GoUserRepository,

    @repository(GoUserRepository)
    public goUserRepository : GoUserRepository,
  ) {}

  @post('/go-users')
  @response(200, {
    description: 'GoUser model instance',
    content: {'application/json': {schema: getModelSchemaRef(GoUser)}},
  })
  async create(
    @requestBody({
      content: {
        'application/json': {
          schema: getModelSchemaRef(GoUser, {
            title: 'NewGoUser',
            exclude: ['id'],
          }),
        },
      },
    })
    goUser: Omit<GoUser, 'id'>,
  ): Promise<GoUser> {
    return this.goUserRepository.create(goUser);
  }

  @get('/go-users/count')
  @response(200, {
    description: 'GoUser model count',
    content: {'application/json': {schema: CountSchema}},
  })
  async count(
    @param.where(GoUser) where?: Where<GoUser>,
  ): Promise<Count> {
    return this.goUserRepository.count(where);
  }

  @get('/go-users')
  @response(200, {
    description: 'Array of GoUser model instances',
    content: {
      'application/json': {
        schema: {
          type: 'array',
          items: getModelSchemaRef(GoUser, {includeRelations: true}),
        },
      },
    },
  })
  async find(
    @param.filter(GoUser) filter?: Filter<GoUser>,
  ): Promise<GoUser[]> {
    return this.goUserRepository.find(filter);
  }

  @patch('/go-users')
  @response(200, {
    description: 'GoUser PATCH success count',
    content: {'application/json': {schema: CountSchema}},
  })
  async updateAll(
    @requestBody({
      content: {
        'application/json': {
          schema: getModelSchemaRef(GoUser, {partial: true}),
        },
      },
    })
    goUser: GoUser,
    @param.where(GoUser) where?: Where<GoUser>,
  ): Promise<Count> {
    return this.goUserRepository.updateAll(goUser, where);
  }

  @get('/go-users/{id}')
  @response(200, {
    description: 'GoUser model instance',
    content: {
      'application/json': {
        schema: getModelSchemaRef(GoUser, {includeRelations: true}),
      },
    },
  })
  async findById(
    @param.path.number('id') id: number,
    @param.filter(GoUser, {exclude: 'where'}) filter?: FilterExcludingWhere<GoUser>
  ): Promise<GoUser> {
    return this.goUserRepository.findById(id, filter);
  }

  @patch('/go-users/{id}')
  @response(204, {
    description: 'GoUser PATCH success',
  })
  async updateById(
    @param.path.number('id') id: number,
    @requestBody({
      content: {
        'application/json': {
          schema: getModelSchemaRef(GoUser, {partial: true}),
        },
      },
    })
    goUser: GoUser,
  ): Promise<void> {
    await this.goUserRepository.updateById(id, goUser);
  }

  @put('/go-users/{id}')
  @response(204, {
    description: 'GoUser PUT success',
  })
  async replaceById(
    @param.path.number('id') id: number,
    @requestBody() goUser: GoUser,
  ): Promise<void> {
    await this.goUserRepository.replaceById(id, goUser);
  }

  @del('/go-users/{id}')
  @response(204, {
    description: 'GoUser DELETE success',
  })
  async deleteById(@param.path.number('id') id: number): Promise<void> {
    await this.goUserRepository.deleteById(id);
  }


 @post('/users/login', {
    responses: {
      '200': {
        description: 'Token',
        content: {
          'application/json': {
            schema: {
              type: 'object',
              properties: {
                token: {
                  type: 'string',
                },
              },
            },
          },
        },
      },
    },
  })
  async login(
    @requestBody(CredentialsRequestBody) credentials: Credentials,
  ): Promise<{token: string}> {
    // ensure the user exists, and the password is correct
    const user = await this.userService.verifyCredentials(credentials);
    // convert a User object into a UserProfile object (reduced set of properties)
    const userProfile = this.userService.convertToUserProfile(user);

    // create a JSON Web Token based on the user profile
    const token = await this.jwtService.generateToken(userProfile);
    return {token};
  }

  @authenticate('jwt')
  @get('/whoAmI', {
    responses: {
      '200': {
        description: 'Return current user',
        content: {
          'application/json': {
            schema: {
              type: 'string',
            },
          },
        },
      },
    },
  })
  async whoAmI(
    @inject(SecurityBindings.USER)
    currentUserProfile: UserProfile,
  ): Promise<string> {
    return currentUserProfile[securityId];
  }

  @post('/signup', {
    responses: {
      '200': {
        description: 'User',
        content: {
          'application/json': {
            schema: {
              'x-ts-type': GoUser,
            },
          },
        },
      },
    },
  })
  async signUp(
    @requestBody({
      content: {
        'application/json': {
          schema: getModelSchemaRef(NewUserRequest, {
            title: 'NewUser',
          }),
        },
      },
    })
    newUserRequest: NewUserRequest,
  ): Promise<GoUser> {
    const password = await hash(newUserRequest.password, await genSalt());
    const savedUser = await this.userRepository.create(
      _.omit(newUserRequest, 'password'),
    );

    await this.userRepository.goUserCredentials(savedUser.id).create({password});

    return savedUser;
  }
}

