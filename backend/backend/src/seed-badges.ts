import {BackendApplication} from './application';
import {BadgeRepository, UserBadgeRepository} from './repositories';

export async function seedBadges() {
  console.log('🌱 Starting badge seeding...');

  const app = new BackendApplication();
  await app.boot();
  await app.start();

  const badgeRepo = await app.getRepository(BadgeRepository);
  const userBadgeRepo = await app.getRepository(UserBadgeRepository);

  // Sample badges to seed
  const badges = [
    {
      name: 'First Steps',
      description: 'Complete your first exploration',
      iconUrl: '🎯',
      category: 'Beginner',
      unlockCriteria: {type: 'exploration_count' as const, threshold: 1},
      displayOrder: 1,
    },
    {
      name: 'Explorer',
      description: 'Complete 5 explorations',
      iconUrl: '🗺️',
      category: 'Explorer',
      unlockCriteria: {type: 'exploration_count' as const, threshold: 5},
      displayOrder: 2,
    },
    {
      name: 'Adventurer',
      description: 'Complete 10 explorations',
      iconUrl: '🏔️',
      category: 'Explorer',
      unlockCriteria: {type: 'exploration_count' as const, threshold: 10},
      displayOrder: 3,
    },
    {
      name: 'Pathfinder',
      description: 'Complete 25 explorations',
      iconUrl: '🧭',
      category: 'Explorer',
      unlockCriteria: {type: 'exploration_count' as const, threshold: 25},
      displayOrder: 4,
    },
    {
      name: 'World Traveler',
      description: 'Complete 50 explorations',
      iconUrl: '🌍',
      category: 'Expert',
      unlockCriteria: {type: 'exploration_count' as const, threshold: 50},
      displayOrder: 5,
    },
    {
      name: 'Legend',
      description: 'Complete 100 explorations',
      iconUrl: '👑',
      category: 'Legend',
      unlockCriteria: {type: 'exploration_count' as const, threshold: 100},
      displayOrder: 6,
    },
    {
      name: 'Daily Dedication',
      description: 'Maintain a 3-day streak',
      iconUrl: '🔥',
      category: 'Streak',
      unlockCriteria: {type: 'streak' as const, threshold: 3},
      displayOrder: 7,
    },
    {
      name: 'Week Warrior',
      description: 'Maintain a 7-day streak',
      iconUrl: '⚡',
      category: 'Streak',
      unlockCriteria: {type: 'streak' as const, threshold: 7},
      displayOrder: 8,
    },
    {
      name: 'Consistency King',
      description: 'Maintain a 30-day streak',
      iconUrl: '💎',
      category: 'Streak',
      unlockCriteria: {type: 'streak' as const, threshold: 30},
      displayOrder: 9,
    },
  ];

  // Check if badges already exist
  const existingBadges = await badgeRepo.find();

  if (existingBadges.length > 0) {
    console.log(`ℹ️  Found ${existingBadges.length} existing badges. Skipping badge creation.`);
  } else {
    console.log('📝 Creating badges...');
    for (const badge of badges) {
      await badgeRepo.create(badge);
    }
    console.log(`✅ Created ${badges.length} badges!`);
  }

  // Unlock first 3 badges for user ID 1 as test data
  console.log('🔍 Checking user badges for user ID 1...');
  const existingUserBadges = await userBadgeRepo.find({where: {userId: 1}});

  if (existingUserBadges.length > 0) {
    console.log(`ℹ️  User 1 already has ${existingUserBadges.length} badges. Skipping.`);
  } else {
    console.log('🎁 Unlocking test badges for user ID 1...');
    const allBadges = await badgeRepo.find({order: ['displayOrder ASC']});

    // Unlock first 3 badges for testing
    for (let i = 0; i < Math.min(3, allBadges.length); i++) {
      await userBadgeRepo.create({
        userId: 1,
        badgeId: allBadges[i].id!,
        unlockedAt: new Date(),
        notificationSent: false,
      });
    }
    console.log(`✅ Unlocked ${Math.min(3, allBadges.length)} badges for user 1!`);
  }

  await app.stop();
  console.log('🎉 Badge seeding complete!');
  process.exit(0);
}

seedBadges().catch(err => {
  console.error('❌ Error seeding badges:', err);
  process.exit(1);
});
