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
        name: 'Nature Enjoyer',
        description: 'Complete 250 explorations. You really enjoy the outdoors!',
        iconUrl: '🗿',
        category: 'Chad',
        unlockCriteria: {type: 'exploration_count' as const, threshold: 250},
        displayOrder: 7,
    },
    {
      name: 'Daily Dedication',
      description: 'Maintain a 3-day streak',
      iconUrl: '🔥',
      category: 'Streak',
      unlockCriteria: {type: 'streak' as const, threshold: 3},
      displayOrder: 8,
    },
    {
      name: 'Week Warrior',
      description: 'Maintain a 7-day streak',
      iconUrl: '⚡',
      category: 'Streak',
      unlockCriteria: {type: 'streak' as const, threshold: 7},
      displayOrder: 9,
    },
    {
      name: 'Consistency King',
      description: 'Maintain a 30-day streak',
      iconUrl: '💎',
      category: 'Streak',
      unlockCriteria: {type: 'streak' as const, threshold: 30},
      displayOrder: 10,
    },
  ];

  // Check if badges already exist
  const existingBadges = await badgeRepo.find();

  if (existingBadges.length > 0) {
    console.log(`ℹ️  Found ${existingBadges.length} existing badges. Updating if needed...`);

    // Update or create badges
    for (const badge of badges) {
      const existing = existingBadges.find(b => b.name === badge.name);
      if (existing) {
        // Update existing badge
        await badgeRepo.updateById(existing.id!, badge);
        console.log(`✏️  Updated badge: ${badge.name}`);
      } else {
        // Create new badge
        await badgeRepo.create(badge);
        console.log(`➕ Created new badge: ${badge.name}`);
      }
    }
    console.log(`✅ Processed ${badges.length} badges!`);
  } else {
    console.log('📝 Creating badges...');
    for (const badge of badges) {
      await badgeRepo.create(badge);
    }
    console.log(`✅ Created ${badges.length} badges!`);
  }

  // Note: Removed auto-unlocking badges for user 1 to properly test badge unlock functionality
  console.log('ℹ️  Users start with 0 badges - badges unlock through exploration!')

  await app.stop();
  console.log('🎉 Badge seeding complete!');
  process.exit(0);
}

seedBadges().catch(err => {
  console.error('❌ Error seeding badges:', err);
  process.exit(1);
});
