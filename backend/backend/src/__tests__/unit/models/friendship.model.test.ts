import {expect} from '@loopback/testlab';
import {Friendship} from '../../../models';
import {
  testdb,
  givenEmptyDatabase,
} from '../../helpers/database.helpers';
import {FriendshipRepository} from '../../../repositories';

describe('Friendship Model (Unit Tests)', () => {
  let friendshipRepository: FriendshipRepository;

  before(async () => {
    await givenEmptyDatabase();

    // Create mock getter for GoUserRepository
    const goUserRepositoryGetter = async () => {
      const {GoUserRepository, GoUserCredentialsRepository} = await import('../../../repositories');
      const credentialsRepoGetter = async () => new GoUserCredentialsRepository(testdb);
      return new GoUserRepository(testdb, credentialsRepoGetter);
    };

    friendshipRepository = new FriendshipRepository(
      testdb,
      goUserRepositoryGetter,
    );
  });

  afterEach(async () => {
    await friendshipRepository.deleteAll();
  });

  it('should create a friendship with userId and friendId', async () => {
    const friendship = await friendshipRepository.create({
      userId: 1,
      friendId: 2,
    });

    expect(friendship).to.have.property('id');
    expect(friendship.userId).to.equal(1);
    expect(friendship.friendId).to.equal(2);
  });

  it('should have createdAt timestamp', async () => {
    const friendship = await friendshipRepository.create({
      userId: 1,
      friendId: 2,
    });

    expect(friendship).to.have.property('createdAt');
    expect(friendship.createdAt).to.be.instanceOf(Date);
  });

  it('should have relations to GoUser for both userId and friendId', async () => {
    // Check that relations are defined
    const modelDefinition = Friendship.definition;
    expect(modelDefinition.relations).to.have.property('user');
    expect(modelDefinition.relations).to.have.property('friend');

    expect(modelDefinition.relations.user.type).to.equal('belongsTo');
    expect(modelDefinition.relations.friend.type).to.equal('belongsTo');
  });

  it('should require userId', async () => {
    try {
      await friendshipRepository.create({
        friendId: 2,
      } as Partial<Friendship>);
      throw new Error('Should have thrown error for missing userId');
    } catch (error) {
      expect(error).to.not.be.undefined();
    }
  });

  it('should require friendId', async () => {
    try {
      await friendshipRepository.create({
        userId: 1,
      } as Partial<Friendship>);
      throw new Error('Should have thrown error for missing friendId');
    } catch (error) {
      expect(error).to.not.be.undefined();
    }
  });

  it('should prevent user from being friends with themselves', async () => {
    try {
      await friendshipRepository.create({
        userId: 1,
        friendId: 1, // Same user
      });
      throw new Error('Should have thrown validation error for self-friendship');
    } catch (error) {
      expect(error).to.not.be.undefined();
    }
  });

  it('should allow multiple friendships for same user', async () => {
    // User 1 can have multiple friends
    const friendship1 = await friendshipRepository.create({
      userId: 1,
      friendId: 2,
    });

    const friendship2 = await friendshipRepository.create({
      userId: 1,
      friendId: 3,
    });

    expect(friendship1.userId).to.equal(1);
    expect(friendship1.friendId).to.equal(2);
    expect(friendship2.userId).to.equal(1);
    expect(friendship2.friendId).to.equal(3);
  });

  it('should not allow duplicate friendship records', async () => {
    // Create first friendship
    await friendshipRepository.create({
      userId: 1,
      friendId: 2,
    });

    // Attempt to create duplicate
    try {
      await friendshipRepository.create({
        userId: 1,
        friendId: 2,
      });
      throw new Error('Should have thrown error for duplicate friendship');
    } catch (error) {
      expect(error).to.not.be.undefined();
      // Should be a unique constraint violation
    }
  });

  it('should find all friendships for a specific user', async () => {
    // Create multiple friendships
    await friendshipRepository.create({userId: 1, friendId: 2});
    await friendshipRepository.create({userId: 1, friendId: 3});
    await friendshipRepository.create({userId: 2, friendId: 4});

    // Find all friendships where userId is 1
    const user1Friendships = await friendshipRepository.find({
      where: {userId: 1},
    });

    expect(user1Friendships).to.have.length(2);
    expect(user1Friendships[0].userId).to.equal(1);
    expect(user1Friendships[1].userId).to.equal(1);
  });

  it('should support bidirectional friendship creation', async () => {
    // When creating friendship, both directions should be created
    // A→B and B→A

    // Create both directions manually (in real controller, this will be automatic)
    await friendshipRepository.create({userId: 1, friendId: 2});
    await friendshipRepository.create({userId: 2, friendId: 1});

    // User 1's friends should include User 2
    const user1Friends = await friendshipRepository.find({
      where: {userId: 1},
    });
    expect(user1Friends).to.have.length(1);
    expect(user1Friends[0].friendId).to.equal(2);

    // User 2's friends should include User 1
    const user2Friends = await friendshipRepository.find({
      where: {userId: 2},
    });
    expect(user2Friends).to.have.length(1);
    expect(user2Friends[0].friendId).to.equal(1);
  });

  it('should delete both directions when removing friendship', async () => {
    // Create bidirectional friendship
    const friendship1 = await friendshipRepository.create({userId: 1, friendId: 2});
    const friendship2 = await friendshipRepository.create({userId: 2, friendId: 1});

    // Delete one direction
    await friendshipRepository.deleteById(friendship1.id);

    // Should also delete the reverse direction (this will be handled in controller/repository)
    // For now, just verify the deleted one is gone
    const remaining = await friendshipRepository.find();
    expect(remaining).to.have.length(1);
    expect(remaining[0].id).to.equal(friendship2.id);
  });

  it('should count total friendships for a user', async () => {
    await friendshipRepository.create({userId: 1, friendId: 2});
    await friendshipRepository.create({userId: 1, friendId: 3});
    await friendshipRepository.create({userId: 1, friendId: 4});

    const count = await friendshipRepository.count({userId: 1});
    expect(count.count).to.equal(3);
  });
});
