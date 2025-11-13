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
  // Using Material Symbols PNG URLs from Google Fonts CDN
  const badges = [
    {
      name: 'First Steps',
      description: 'Complete your first exploration',
      iconUrl: 'https://fonts.gstatic.com/s/i/short-term/release/materialsymbolsoutlined/stars/default/48px.svg',
      category: 'Beginner',
      unlockCriteria: {type: 'exploration_count' as const, threshold: 1},
      displayOrder: 1,
    },
    {
      name: 'Explorer',
      description: 'Complete 5 explorations',
      iconUrl: 'https://fonts.gstatic.com/s/i/short-term/release/materialsymbolsoutlined/explore/default/48px.svg',
      category: 'Explorer',
      unlockCriteria: {type: 'exploration_count' as const, threshold: 5},
      displayOrder: 2,
    },
    {
      name: 'Adventurer',
      description: 'Complete 10 explorations',
      iconUrl: 'https://fonts.gstatic.com/s/i/short-term/release/materialsymbolsoutlined/hiking/default/48px.svg',
      category: 'Explorer',
      unlockCriteria: {type: 'exploration_count' as const, threshold: 10},
      displayOrder: 3,
    },
    {
      name: 'Pathfinder',
      description: 'Complete 25 explorations',
      iconUrl: 'https://fonts.gstatic.com/s/i/short-term/release/materialsymbolsoutlined/map/default/48px.svg',
      category: 'Explorer',
      unlockCriteria: {type: 'exploration_count' as const, threshold: 25},
      displayOrder: 4,
    },
    {
      name: 'World Traveler',
      description: 'Complete 50 explorations',
      iconUrl: 'https://fonts.gstatic.com/s/i/short-term/release/materialsymbolsoutlined/travel_explore/default/48px.svg',
      category: 'Expert',
      unlockCriteria: {type: 'exploration_count' as const, threshold: 50},
      displayOrder: 5,
    },
    {
      name: 'Legend',
      description: 'Complete 100 explorations',
      iconUrl: 'https://fonts.gstatic.com/s/i/short-term/release/materialsymbolsoutlined/emoji_events/default/48px.svg',
      category: 'Legend',
      unlockCriteria: {type: 'exploration_count' as const, threshold: 100},
      displayOrder: 6,
    },
    {
      name: 'Daily Dedication',
      description: 'Maintain a 3-day streak',
      iconUrl: 'https://fonts.gstatic.com/s/i/short-term/release/materialsymbolsoutlined/local_fire_department/default/48px.svg',
      category: 'Streak',
      unlockCriteria: {type: 'streak' as const, threshold: 3},
      displayOrder: 7,
    },
    {
      name: 'Week Warrior',
      description: 'Maintain a 7-day streak',
      iconUrl: 'https://fonts.gstatic.com/s/i/short-term/release/materialsymbolsoutlined/bolt/default/48px.svg',
      category: 'Streak',
      unlockCriteria: {type: 'streak' as const, threshold: 7},
      displayOrder: 8,
    },
    {
      name: 'Consistency King',
      description: 'Maintain a 30-day streak',
      iconUrl: 'https://fonts.gstatic.com/s/i/short-term/release/materialsymbolsoutlined/diamond/default/48px.svg',
      category: 'Streak',
      unlockCriteria: {type: 'streak' as const, threshold: 30},
      displayOrder: 9,
    },
    // Example: Adding a new badge is this easy!
    // {
    //   name: 'Night Owl',
    //   description: 'Complete an exploration after 10 PM',
    //   iconUrl: 'https://fonts.gstatic.com/s/i/short-term/release/materialsymbolsoutlined/nightlight/default/48px.svg',
    //   category: 'Special',
    //   unlockCriteria: {type: 'exploration_count' as const, threshold: 1},
    //   displayOrder: 10,
    // },
  ];

  // Check if badges already exist
  const existingBadges = await badgeRepo.find();

  if (existingBadges.length > 0) {
    console.log(`ℹ️  Found ${existingBadges.length} existing badges. Updating icon URLs...`);
    for (const badge of badges) {
      const existing = existingBadges.find(b => b.name === badge.name);
      if (existing) {
        await badgeRepo.updateById(existing.id, {iconUrl: badge.iconUrl});
        console.log(`   Updated ${badge.name} icon URL`);
      } else {
        await badgeRepo.create(badge);
        console.log(`   Created new badge: ${badge.name}`);
      }
    }
    console.log(`✅ Updated ${existingBadges.length} badges!`);
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
