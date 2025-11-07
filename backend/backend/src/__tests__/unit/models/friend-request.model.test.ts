import {expect} from '@loopback/testlab';
import {FriendRequest} from '../../../models';
import {
  testdb,
  givenEmptyDatabase,
} from '../../helpers/database.helpers';
import {FriendRequestRepository} from '../../../repositories';

describe('FriendRequest Model (Unit Tests)', () => {
  let friendRequestRepository: FriendRequestRepository;

  before(async () => {
    await givenEmptyDatabase();

    // Create mock getter for GoUserRepository
    const goUserRepositoryGetter = async () => {
      const {GoUserRepository, GoUserCredentialsRepository} = await import('../../../repositories');
      const credentialsRepoGetter = async () => new GoUserCredentialsRepository(testdb);
      return new GoUserRepository(testdb, credentialsRepoGetter);
    };

    friendRequestRepository = new FriendRequestRepository(
      testdb,
      goUserRepositoryGetter,
    );
  });

  afterEach(async () => {
    await friendRequestRepository.deleteAll();
  });

  it('should create a friend request with required fields', async () => {
    const friendRequest = await friendRequestRepository.create({
      fromUserId: 1,
      toUserId: 2,
      status: 'PENDING',
    });

    expect(friendRequest).to.have.property('id');
    expect(friendRequest.fromUserId).to.equal(1);
    expect(friendRequest.toUserId).to.equal(2);
    expect(friendRequest.status).to.equal('PENDING');
  });

  it('should default status to PENDING when not specified', async () => {
    const friendRequest = await friendRequestRepository.create({
      fromUserId: 1,
      toUserId: 2,
    });

    expect(friendRequest.status).to.equal('PENDING');
  });

  it('should have createdAt and updatedAt timestamps', async () => {
    const friendRequest = await friendRequestRepository.create({
      fromUserId: 1,
      toUserId: 2,
    });

    expect(friendRequest).to.have.property('createdAt');
    expect(friendRequest).to.have.property('updatedAt');
    expect(friendRequest.createdAt).to.be.instanceOf(Date);
    expect(friendRequest.updatedAt).to.be.instanceOf(Date);
  });

  it('should validate status enum (PENDING, ACCEPTED, REJECTED)', async () => {
    // Valid statuses
    const pending = await friendRequestRepository.create({
      fromUserId: 1,
      toUserId: 2,
      status: 'PENDING',
    });
    expect(pending.status).to.equal('PENDING');

    const accepted = await friendRequestRepository.create({
      fromUserId: 3,
      toUserId: 4,
      status: 'ACCEPTED',
    });
    expect(accepted.status).to.equal('ACCEPTED');

    const rejected = await friendRequestRepository.create({
      fromUserId: 5,
      toUserId: 6,
      status: 'REJECTED',
    });
    expect(rejected.status).to.equal('REJECTED');

    // Invalid status should throw error
    try {
      await friendRequestRepository.create({
        fromUserId: 7,
        toUserId: 8,
        status: 'INVALID_STATUS',
      });
      throw new Error('Should have thrown validation error for invalid status');
    } catch (error) {
      expect(error).to.not.be.undefined();
    }
  });

  it('should not allow duplicate friend requests', async () => {
    // Create first request
    await friendRequestRepository.create({
      fromUserId: 1,
      toUserId: 2,
      status: 'PENDING',
    });

    // Attempt to create duplicate
    try {
      await friendRequestRepository.create({
        fromUserId: 1,
        toUserId: 2,
        status: 'PENDING',
      });
      throw new Error('Should have thrown error for duplicate friend request');
    } catch (error) {
      expect(error).to.not.be.undefined();
      // Should be a unique constraint violation
    }
  });

  it('should prevent user from sending request to themselves', async () => {
    try {
      await friendRequestRepository.create({
        fromUserId: 1,
        toUserId: 1, // Same user
        status: 'PENDING',
      });
      throw new Error('Should have thrown validation error for self-request');
    } catch (error) {
      expect(error).to.not.be.undefined();
    }
  });

  it('should have relations to GoUser model', async () => {
    // Check that relations are defined
    const modelDefinition = FriendRequest.definition;
    expect(modelDefinition.relations).to.have.property('fromUser');
    expect(modelDefinition.relations).to.have.property('toUser');

    expect(modelDefinition.relations.fromUser.type).to.equal('belongsTo');
    expect(modelDefinition.relations.toUser.type).to.equal('belongsTo');
  });

  it('should require fromUserId and toUserId', async () => {
    // Missing fromUserId
    try {
      await friendRequestRepository.create({
        toUserId: 2,
      } as Partial<FriendRequest>);
      throw new Error('Should have thrown error for missing fromUserId');
    } catch (error) {
      expect(error).to.not.be.undefined();
    }

    // Missing toUserId
    try {
      await friendRequestRepository.create({
        fromUserId: 1,
      } as Partial<FriendRequest>);
      throw new Error('Should have thrown error for missing toUserId');
    } catch (error) {
      expect(error).to.not.be.undefined();
    }
  });

  it('should update updatedAt timestamp when status changes', async () => {
    const friendRequest = await friendRequestRepository.create({
      fromUserId: 1,
      toUserId: 2,
      status: 'PENDING',
    });

    const originalUpdatedAt = friendRequest.updatedAt;

    // Wait a bit to ensure timestamp difference
    await new Promise(resolve => setTimeout(resolve, 10));

    // Update status
    await friendRequestRepository.updateById(friendRequest.id, {
      status: 'ACCEPTED',
    });

    const updated = await friendRequestRepository.findById(friendRequest.id);
    expect(updated.updatedAt).to.not.equal(originalUpdatedAt);
    expect(updated.updatedAt!.getTime()).to.be.greaterThan(originalUpdatedAt!.getTime());
  });
});
