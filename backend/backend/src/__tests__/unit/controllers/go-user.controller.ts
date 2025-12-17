import {expect} from '@loopback/testlab';
import {GoUserController} from '../../../controllers';
import {GoUserRepository} from '../../../repositories';
import {GoUser} from '../../../models';
import sinon from 'sinon';

describe('filterUsersBio', () => {
  let controller: GoUserController;
  let repo: sinon.SinonStubbedInstance<GoUserRepository>;

  beforeEach(() => {
    repo = sinon.createStubInstance(GoUserRepository);
    controller = new GoUserController(
      {verifyToken: sinon.stub(), generateToken: sinon.stub()} as unknown as GoUserController['jwtService'],
      {verifyCredentials: sinon.stub(), convertToUserProfile: sinon.stub()} as unknown as GoUserController['userService'],
      {id: '1', name: 'test'} as unknown as GoUserController['user'],
      repo,
      repo,
    );
  });

  afterEach(() => sinon.restore());

  it('filters profane words', async () => {
    repo.find.resolves([
      new GoUser({id: 1, email: 'a@test.com', bio: 'arse', isAdmin: false}),
      new GoUser({id: 2, email: 'b@test.com', bio: 'clean', isAdmin: false}),
    ]);

    const result = await controller.filterUsersBio();

    expect(result.length).to.equal(1);
    expect(result[0].id).to.equal(1);
  });

  it('handles clean bios', async () => {
    repo.find.resolves([
      new GoUser({id: 1, email: 'a@test.com', bio: 'clean', isAdmin: false}),
    ]);

    expect((await controller.filterUsersBio()).length).to.equal(0);
  });

  it('ignores users without bio', async () => {
    repo.find.resolves([
      new GoUser({id: 1, email: 'a@test.com', bio: undefined, isAdmin: false}),
    ]);

    expect((await controller.filterUsersBio()).length).to.equal(0);
  });  

  it('detects uppercase', async () => {
    repo.find.resolves([
      new GoUser({id: 1, email: 'a@test.com', bio: 'NIGGA', isAdmin: false}),
    ]);

    expect((await controller.filterUsersBio()).length).to.equal(1);
  });

  it('detects mixed case', async () => {
    repo.find.resolves([
      new GoUser({id: 1, email: 'a@test.com', bio: 'ArSe', isAdmin: false}),
    ]);

    expect((await controller.filterUsersBio()).length).to.equal(1);
  });
});

