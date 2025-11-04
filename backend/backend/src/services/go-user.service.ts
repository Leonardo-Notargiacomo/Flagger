// Copyright IBM Corp. and LoopBack contributors 2020. All Rights Reserved.
// Node module: @loopback/authentication-jwt
// This file is licensed under the MIT License.
// License text available at https://opensource.org/licenses/MIT

import {UserService} from '@loopback/authentication';
import {repository} from '@loopback/repository';
import {HttpErrors} from '@loopback/rest';
import {securityId, UserProfile} from '@loopback/security';
import {compare} from 'bcryptjs';
import {GoUser, GoUserRelations} from '../models';
import {GoUserRepository} from '../repositories';

/**
 * A pre-defined type for user credentials. It assumes a user logs in
 * using the email and password. You can modify it if your app has different credential fields
 */
export type Credentials = {
  email: string;
  password: string;
};

export class MyUserService implements UserService<GoUser, Credentials> {
  constructor(
    @repository(GoUserRepository) public userRepository: GoUserRepository,
  ) {}

  async verifyCredentials(credentials: Credentials): Promise<GoUser> {
    const invalidCredentialsError = 'Invalid email or password.';
    console.log("Passed invalide Credentials");

    const foundUser = await this.userRepository.findOne({
      where: {email: credentials.email},
    });
        console.log("found User");

    if (!foundUser) {
      console.log("There is no user found!")
      throw new HttpErrors.Unauthorized(invalidCredentialsError);
    }

    const credentialsFound = await this.userRepository.findCredentials(
      foundUser.id,
    );
        console.log("found Credentials for User");

    if (!credentialsFound) {
      throw new HttpErrors.Unauthorized(invalidCredentialsError);
    }

    const passwordMatched = await compare(
      credentials.password,
      credentialsFound.password,
    );
        console.log("Credentials Passoword match");

    if (!passwordMatched) {
      throw new HttpErrors.Unauthorized(invalidCredentialsError);
    }

    return foundUser;
  }

  convertToUserProfile(user: GoUser): UserProfile {
    return {
      [securityId]: user.id!.toString(),
      name: user.userName,
      id: user.id,
      email: user.email,
    };
  }

  //function to find user by id
  async findUserById(id: number): Promise<GoUser & GoUserRelations> {
    const userNotfound = 'invalid User';
    const foundUser = await this.userRepository.findOne({
      where: {id: id},
    });

    if (!foundUser) {
      throw new HttpErrors.Unauthorized(userNotfound);
    }
    return foundUser;
  }
}
