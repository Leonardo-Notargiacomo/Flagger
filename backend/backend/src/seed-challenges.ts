import {BackendApplication} from './application';
import {BadgeRepository, ChallengeRepository} from './repositories';

/**
 * Seed initial challenges and their associated badges
 */
export async function seedChallenges(app: BackendApplication) {
  const badgeRepo = await app.getRepository(BadgeRepository);
  const challengeRepo = await app.getRepository(ChallengeRepository);

  console.log('🌱 Seeding challenge badges...');

  // Define challenge badges
  const challengeBadges = [
    {
      name: 'Explorer Novice',
      description: 'Complete 5 explorations in a single challenge',
      iconUrl: '🌲',
      category: 'challenge',
      unlockCriteria: {type: 'exploration_count' as const, threshold: 5},
      isChallengeBadge: true,
      displayOrder: 100,
    },
    {
      name: 'Explorer Adept',
      description: 'Complete 10 explorations in a single challenge',
      iconUrl: '🌳',
      category: 'challenge',
      unlockCriteria: {type: 'exploration_count' as const, threshold: 10},
      isChallengeBadge: true,
      displayOrder: 101,
    },
    {
      name: 'Explorer Master',
      description: 'Complete 20 explorations in a single challenge',
      iconUrl: '🌴',
      category: 'challenge',
      unlockCriteria: {type: 'exploration_count' as const, threshold: 20},
      isChallengeBadge: true,
      displayOrder: 102,
    },
    {
      name: 'Night Owl',
      description: 'Flag a place at 10 PM',
      iconUrl: '🦉',
      category: 'challenge',
      unlockCriteria: {type: 'exploration_count' as const, threshold: 1},
      isChallengeBadge: true,
      displayOrder: 103,
    },
    {
      name: 'Early Bird',
      description: 'Flag a place at 6 AM',
      iconUrl: '🐦',
      category: 'challenge',
      unlockCriteria: {type: 'exploration_count' as const, threshold: 1},
      isChallengeBadge: true,
      displayOrder: 104,
    },
    {
      name: 'Midnight Explorer',
      description: 'Flag a place at 3 AM',
      iconUrl: '🌙',
      category: 'challenge',
      unlockCriteria: {type: 'exploration_count' as const, threshold: 1},
      isChallengeBadge: true,
      displayOrder: 105,
    },
  ];

  // Create or update badges
  const badgeMap = new Map<string, number>();
  for (const badgeData of challengeBadges) {
    const existing = await badgeRepo.findOne({where: {name: badgeData.name}});
    if (existing) {
      console.log(`  ✓ Badge already exists: ${badgeData.name}`);
      badgeMap.set(badgeData.name, existing.id!);
    } else {
      const badge = await badgeRepo.create(badgeData);
      console.log(`  ✨ Created badge: ${badgeData.name}`);
      badgeMap.set(badgeData.name, badge.id!);
    }
  }

  console.log('🎯 Seeding challenges...');

  // Define challenges
  const challenges = [
    {
      name: '5 Explorations Challenge',
      description: 'Complete 5 explorations within 24 hours to earn the Explorer Novice badge',
      iconUrl: '🌲',
      conditionType: 'exploration_count' as const,
      conditionParams: {count: 5},
      rewardBadgeId: badgeMap.get('Explorer Novice')!,
      cooldownHours: 24,
      expirationHours: 24, // 24-hour expiration for exploration challenges
      isActive: true,
      difficulty: 'easy' as const,
      displayOrder: 0,
    },
    {
      name: '10 Explorations Challenge',
      description: 'Complete 10 explorations within 24 hours to earn the Explorer Adept badge',
      iconUrl: '🌳',
      conditionType: 'exploration_count' as const,
      conditionParams: {count: 10},
      rewardBadgeId: badgeMap.get('Explorer Adept')!,
      cooldownHours: 24,
      expirationHours: 24, // 24-hour expiration for exploration challenges
      isActive: true,
      difficulty: 'advanced' as const,
      displayOrder: 1,
    },
    {
      name: '20 Explorations Challenge',
      description: 'Complete 20 explorations within 24 hours to earn the Explorer Master badge',
      iconUrl: '🌴',
      conditionType: 'exploration_count' as const,
      conditionParams: {count: 20},
      rewardBadgeId: badgeMap.get('Explorer Master')!,
      cooldownHours: 24,
      expirationHours: 24, // 24-hour expiration for exploration challenges
      isActive: true,
      difficulty: 'chad' as const,
      displayOrder: 2,
    },
    {
      name: 'Night Owl Challenge',
      description: 'Flag a place at exactly 10 PM within 24 hours to earn the Night Owl badge',
      iconUrl: '🦉',
      conditionType: 'time_based' as const,
      conditionParams: {hour: 22}, // 10 PM
      rewardBadgeId: badgeMap.get('Night Owl')!,
      cooldownHours: 24,
      expirationHours: 24, // 24-hour expiration for time-based challenges
      isActive: true,
      difficulty: 'novice' as const,
      displayOrder: 3,
    },
    {
      name: 'Early Bird Challenge',
      description: 'Flag a place at exactly 6 AM within 24 hours to earn the Early Bird badge',
      iconUrl: '🐦',
      conditionType: 'time_based' as const,
      conditionParams: {hour: 6}, // 6 AM
      rewardBadgeId: badgeMap.get('Early Bird')!,
      cooldownHours: 24,
      expirationHours: 24, // 24-hour expiration for time-based challenges
      isActive: true,
      difficulty: 'expert' as const,
      displayOrder: 4,
    },
    {
      name: 'Midnight Explorer Challenge',
      description: 'Flag a place at exactly 3 AM within 24 hours to earn the Midnight Explorer badge',
      iconUrl: '🌙',
      conditionType: 'time_based' as const,
      conditionParams: {hour: 3}, // 3 AM
      rewardBadgeId: badgeMap.get('Midnight Explorer')!,
      cooldownHours: 24,
      expirationHours: 24, // 24-hour expiration for time-based challenges
      isActive: true,
      difficulty: 'expert' as const,
      displayOrder: 5,
    },
  ];

  // Create or update challenges
  for (const challengeData of challenges) {
    const existing = await challengeRepo.findOne({where: {name: challengeData.name}});
    if (existing) {
      console.log(`  ✓ Challenge already exists: ${challengeData.name}`);
    } else {
      await challengeRepo.create(challengeData);
      console.log(`  🎯 Created challenge: ${challengeData.name}`);
    }
  }

  console.log('✅ Challenge seeding completed!');
}

/**
 * Run the seeder if this file is executed directly
 */
async function main() {
  const app = new BackendApplication();
  await app.boot();
  await app.start();
  await seedChallenges(app);
  process.exit(0);
}

if (require.main === module) {
  main().catch(err => {
    console.error('Error seeding challenges:', err);
    process.exit(1);
  });
}

