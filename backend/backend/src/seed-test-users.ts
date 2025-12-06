import {BackendApplication} from './application';
import {
  GoUserRepository,
  FcmTokenRepository,
  UserStreakRepository,
  ExplorationEventRepository,
  NotificationHistoryRepository,
} from './repositories';

/**
 * Seeds test users with various behaviors to test personalized notifications
 *
 * Creates 5 test users:
 * 1. High Streak User (7 days) - triggers "doing well" notification
 * 2. Active Explorer (3 explorations today) - triggers "doing well" notification
 * 3. Inactive User (5 days no activity) - triggers "skipping" notification
 * 4. Broken Streak User - triggers "skipping" notification
 * 5. Frequent Dismisser - triggers "skipping" notification
 */
export async function seedTestUsers() {
  console.log('🌱 Starting test user seeding...');

  // Only seed test users in non-production environments
  if (process.env.NODE_ENV === 'production' && !process.env.SEED_TEST_USERS) {
    console.log('⚠️  Skipping test user seeding in production environment');
    console.log('💡 Set SEED_TEST_USERS=true to enable test users in production');
    process.exit(0);
  }

  const app = new BackendApplication();
  await app.boot();
  await app.start();

  const userRepo = await app.getRepository(GoUserRepository);
  const fcmTokenRepo = await app.getRepository(FcmTokenRepository);
  const streakRepo = await app.getRepository(UserStreakRepository);
  const explorationRepo = await app.getRepository(ExplorationEventRepository);
  const notificationHistoryRepo = await app.getRepository(NotificationHistoryRepository);

  // Helper to calculate date offsets
  const daysAgo = (days: number) => {
    const date = new Date();
    date.setDate(date.getDate() - days);
    return date;
  };

  const hoursAgo = (hours: number) => {
    const date = new Date();
    date.setHours(date.getHours() - hours);
    return date;
  };

  // Check if test users already exist
  console.log('🔍 Checking for existing test users...');
  const existingUsers = await userRepo.find({
    where: {userName: {regexp: /^test_user_/i}},
  });

  if (existingUsers.length > 0) {
    console.log(`ℹ️  Found ${existingUsers.length} existing test users. Skipping creation.`);
    console.log('💡 To recreate test users, manually delete them first or set FORCE_SEED=true');
    await app.stop();
    process.exit(0);
  }

  // 1. HIGH STREAK USER (7 days) - should get "doing well" notification
  console.log('👤 Creating High Streak User...');
  const highStreakUser = await userRepo.create({
    userName: 'test_user_high_streak',
    email: 'high_streak@test.com',
    bio: 'Testing high streak notifications',
  });

  await fcmTokenRepo.create({
    userId: highStreakUser.id!,
    token: 'fake_fcm_token_high_streak_' + Date.now(),
    platform: 'android',
    isActive: true,
  });

  await streakRepo.create({
    userId: highStreakUser.id!,
    currentStreak: 7,
    longestStreak: 7,
    lastActivityDate: new Date(), // Active today
  });

  // Add exploration from today
  await explorationRepo.create({
    userId: highStreakUser.id!,
    locationName: 'Test Park',
    latitude: 52.0907,
    longitude: 5.1214,
    completedAt: hoursAgo(2),
    verificationStatus: 'verified',
  });

  console.log(`✅ Created user: ${highStreakUser.userName} (ID: ${highStreakUser.id})`);

  // 2. ACTIVE EXPLORER (3 explorations today) - should get "doing well" notification
  console.log('👤 Creating Active Explorer User...');
  const activeExplorerUser = await userRepo.create({
    userName: 'test_user_active_explorer',
    email: 'active_explorer@test.com',
    bio: 'Testing multiple explorations',
  });

  await fcmTokenRepo.create({
    userId: activeExplorerUser.id!,
    token: 'fake_fcm_token_active_explorer_' + Date.now(),
    platform: 'android',
    isActive: true,
  });

  await streakRepo.create({
    userId: activeExplorerUser.id!,
    currentStreak: 2,
    longestStreak: 3,
    lastActivityDate: new Date(),
  });

  // Add 3 explorations today
  for (let i = 0; i < 3; i++) {
    await explorationRepo.create({
      userId: activeExplorerUser.id!,
      locationName: `Test Location ${i + 1}`,
      latitude: 52.0907 + (i * 0.01),
      longitude: 5.1214 + (i * 0.01),
      completedAt: hoursAgo(8 - i * 2),
      verificationStatus: 'verified',
    });
  }

  console.log(`✅ Created user: ${activeExplorerUser.userName} (ID: ${activeExplorerUser.id})`);

  // 3. INACTIVE USER (5 days no activity) - should get "skipping" notification
  console.log('👤 Creating Inactive User...');
  const inactiveUser = await userRepo.create({
    userName: 'test_user_inactive',
    email: 'inactive@test.com',
    bio: 'Testing inactive notification',
  });

  await fcmTokenRepo.create({
    userId: inactiveUser.id!,
    token: 'fake_fcm_token_inactive_' + Date.now(),
    platform: 'android',
    isActive: true,
  });

  await streakRepo.create({
    userId: inactiveUser.id!,
    currentStreak: 0,
    longestStreak: 3,
    lastActivityDate: daysAgo(5), // Last active 5 days ago
  });

  // Add old exploration
  await explorationRepo.create({
    userId: inactiveUser.id!,
    locationName: 'Old Park',
    latitude: 52.0907,
    longitude: 5.1214,
    completedAt: daysAgo(5),
    verificationStatus: 'verified',
  });

  console.log(`✅ Created user: ${inactiveUser.userName} (ID: ${inactiveUser.id})`);

  // 4. BROKEN STREAK USER - should get "skipping" notification
  console.log('👤 Creating Broken Streak User...');
  const brokenStreakUser = await userRepo.create({
    userName: 'test_user_broken_streak',
    email: 'broken_streak@test.com',
    bio: 'Testing broken streak notification',
  });

  await fcmTokenRepo.create({
    userId: brokenStreakUser.id!,
    token: 'fake_fcm_token_broken_streak_' + Date.now(),
    platform: 'android',
    isActive: true,
  });

  await streakRepo.create({
    userId: brokenStreakUser.id!,
    currentStreak: 0, // Broken!
    longestStreak: 10, // Had a good streak before
    lastActivityDate: daysAgo(2),
  });

  console.log(`✅ Created user: ${brokenStreakUser.userName} (ID: ${brokenStreakUser.id})`);

  // 5. FREQUENT DISMISSER - should get "skipping" notification
  console.log('👤 Creating Frequent Dismisser User...');
  const dismisserUser = await userRepo.create({
    userName: 'test_user_dismisser',
    email: 'dismisser@test.com',
    bio: 'Testing frequent dismissal detection',
  });

  await fcmTokenRepo.create({
    userId: dismisserUser.id!,
    token: 'fake_fcm_token_dismisser_' + Date.now(),
    platform: 'android',
    isActive: true,
  });

  await streakRepo.create({
    userId: dismisserUser.id!,
    currentStreak: 1,
    longestStreak: 2,
    lastActivityDate: new Date(),
  });

  // Add 5 dismissed notifications in last 7 days
  for (let i = 0; i < 5; i++) {
    await notificationHistoryRepo.create({
      userId: dismisserUser.id!,
      notificationType: 'doing_well',
      title: 'Test Notification',
      body: 'Test body',
      sentAt: daysAgo(i + 1),
      wasDismissed: true,
      dismissedAt: daysAgo(i + 1),
    });
  }

  console.log(`✅ Created user: ${dismisserUser.userName} (ID: ${dismisserUser.id})`);

  console.log('\n📊 Summary:');
  console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
  console.log(`1. ${highStreakUser.userName} (ID: ${highStreakUser.id})`);
  console.log('   → Should trigger: "doing_well" (high_streak, 7 days)');
  console.log('');
  console.log(`2. ${activeExplorerUser.userName} (ID: ${activeExplorerUser.id})`);
  console.log('   → Should trigger: "doing_well" (multiple_explorations_today, 3)');
  console.log('');
  console.log(`3. ${inactiveUser.userName} (ID: ${inactiveUser.id})`);
  console.log('   → Should trigger: "skipping" (inactive_for_days, 5)');
  console.log('');
  console.log(`4. ${brokenStreakUser.userName} (ID: ${brokenStreakUser.id})`);
  console.log('   → Should trigger: "skipping" (streak_broken, was 10)');
  console.log('');
  console.log(`5. ${dismisserUser.userName} (ID: ${dismisserUser.id})`);
  console.log('   → Should trigger: "skipping" (frequent_dismissal, 5 in 7 days)');
  console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');

  await app.stop();
  console.log('\n🎉 Test user seeding complete!');
  console.log('\n📝 Next steps:');
  console.log('   1. Test the API: GET /api/notifications/targets/doing-well');
  console.log('   2. Test the API: GET /api/notifications/targets/skipping');
  console.log('   3. Generate message: POST /api/notifications/message');
  process.exit(0);
}

seedTestUsers().catch(err => {
  console.error('❌ Error seeding test users:', err);
  process.exit(1);
});
