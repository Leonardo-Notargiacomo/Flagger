import {
  Count,
  CountSchema,
  Filter,
  FilterExcludingWhere,
  model,
  property,
  repository,
  Where,
} from '@loopback/repository';
import {
  del,
  get,
  getModelSchemaRef,
  HttpErrors,
  param,
  patch,
  post,
  put,
  requestBody,
  response,
  SchemaObject,
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
import fs from 'fs';
import path from 'path';


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


 @post('/login', {
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
    console.log("Starting login");
    // ensure the user exists, and the password is correct
    const user = await this.userService.verifyCredentials(credentials);
    console.log("Verifying Credentials passed");
    // convert a User object into a UserProfile object (reduced set of properties)
    const userProfile = this.userService.convertToUserProfile(user);
    console.log("It converted to a User Profile");
    // create a JSON Web Token based on the user profile
    const token = await this.jwtService.generateToken(userProfile);
    console.log("Passed the JWT services");
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
    try {
      const password = await hash(newUserRequest.password, await genSalt());
      const savedUser = await this.userRepository.create(
        _.omit(newUserRequest, 'password'),
      );

      await this.userRepository.goUserCredentials(savedUser.id).create({password});

      return savedUser;
    } catch (error: any) {
      // Check if it's a PostgreSQL unique constraint violation
      if (error.code === '23505') {
        // PostgreSQL error code for unique constraint violation
        const constraintName = error.constraint || '';

        if (constraintName.includes('username')) {
          throw new HttpErrors.Conflict('This username is already taken. Please choose a different username.');
        } else if (constraintName.includes('email')) {
          throw new HttpErrors.Conflict('This email is already registered. Please use a different email or try logging in.');
        } else {
          throw new HttpErrors.Conflict('A user with this username or email already exists.');
        }
      }

      // If it's not a duplicate error, rethrow the original error
      throw error;
    }
  }

  @get('/go-users/{id}/is-admin', {
    responses: {
      '200': {
        description: 'Returns whether the user is an admin',
        content: {
          'application/json': {
            schema: {
              type: 'boolean',
            },
          },
        },
      },
    },
  })
  async isAdmin(
      @param.path.number('id') id: number,
  ): Promise<boolean> {
      const user = await this.goUserRepository.findById(id);
      if (!user) {
      throw new HttpErrors.NotFound(`User with id ${id} not found`);
    }
    return user.isAdmin;
  }

  @get('/go-users/filter-bio')
  @response(200, {
    description: 'Filtered Users',
    content: {
      'application/json': {
        schema: {
          type: 'array',
          items: getModelSchemaRef(GoUser, {includeRelations: true}),
        },
      },
    },
  })
  async filterUsersBio(){
    const users = await this.goUserRepository.find();

    const filePath = path.join(__dirname, '../../src/CsvFiles/profanity_en.csv');
    const data = fs.readFileSync(filePath, 'utf8');

    // Split by lines, skip header, extract first column (the profane word)
    const lines = data.split('\n').slice(1); // Skip header row
    const profaneWords = lines
      .map(line => line.split(',')[0]?.trim().toLowerCase())
      .filter(word => word); // Remove empty entries

    const filteredUsers = users.filter(user => {
      if(!user.bio) return false;
      const bioWords = user.bio.toLowerCase().split(/\s+/); // Split by any whitespace
      return profaneWords.some(profaneWord => bioWords.includes(profaneWord));
    });

    return filteredUsers;
  }
}

