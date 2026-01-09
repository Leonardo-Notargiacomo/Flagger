import {BackendApplication} from './application';
import {DbDataSource} from './datasources';

/**
 * Adds critical performance indexes for the notification system
 *
 * Run this script with: npm run add-indexes
 *
 * These indexes are essential for:
 * - Fast exploration event queries by date
 * - Efficient notification history lookups
 * - Quick FCM token retrieval
 * - Optimized user streak queries
 */
export async function addNotificationIndexes() {
  console.log('🔧 Adding notification system indexes...');

  const app = new BackendApplication();
  await app.boot();
  await app.start();

  const dataSource = await app.get<DbDataSource>('datasources.db');
  const connector = dataSource.connector;

  if (!connector || !connector.execute) {
    throw new Error('Database connector not available');
  }

  // Helper to execute SQL
  const executeSql = async (sql: string, description: string) => {
    try {
      console.log(`  → ${description}...`);
      await connector.execute!(sql, []);
      console.log(`    ✓ Success`);
    } catch (error: any) {
      if (error.message?.includes('already exists')) {
        console.log(`    ℹ Already exists (skipping)`);
      } else {
        console.error(`    ✗ Error: ${error.message}`);
        throw error;
      }
    }
  };

  console.log('\n📊 Creating indexes for exploration events...');

  // Index for exploration event queries (date-based filtering)
  await executeSql(
    `CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_exploration_completed
     ON explorationevent(completedat DESC, userid)`,
    'Index for exploration date queries'
  );

  console.log('\n🔔 Creating indexes for notification history...');

  // Index for dismissal tracking (frequent dismisser detection)
  await executeSql(
    `CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_notification_dismissed
     ON notificationhistory(dismissedat DESC)
     WHERE wasdismissed = true`,
    'Partial index for dismissed notifications'
  );

  // Composite index for notification history lookups
  await executeSql(
    `CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_notification_history_lookup
     ON notificationhistory(userid, notificationtype, sentat DESC)`,
    'Composite index for notification queries'
  );

  console.log('\n📱 Creating indexes for FCM tokens...');

  // Unique constraint to prevent duplicate tokens (prevents race condition)
  await executeSql(
    `CREATE UNIQUE INDEX CONCURRENTLY IF NOT EXISTS idx_fcm_token_unique
     ON fcmtoken(userid, token)`,
    'Unique constraint on (userId, token)'
  );

  // Partial index for active FCM tokens only
  await executeSql(
    `CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_fcm_token_active
     ON fcmtoken(userid, isactive)
     WHERE isactive = true`,
    'Partial index for active FCM tokens'
  );

  console.log('\n🔥 Creating indexes for user streaks...');

  // Index for high streak queries (doing well notifications)
  await executeSql(
    `CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_user_streak_high
     ON userstreak(currentstreak DESC, userid)
     WHERE currentstreak >= 5`,
    'Partial index for high streaks'
  );

  // Index for broken streak queries (skipping notifications)
  await executeSql(
    `CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_user_streak_broken
     ON userstreak(longeststreak DESC, userid)
     WHERE currentstreak = 0 AND longeststreak > 0`,
    'Partial index for broken streaks'
  );

  // Index for inactive user queries
  await executeSql(
    `CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_user_streak_activity
     ON userstreak(lastactivitydate, currentstreak, userid)`,
    'Index for activity date queries'
  );

  console.log('\n✅ All indexes created successfully!');
  console.log('\n📈 Performance tips:');
  console.log('   - Run ANALYZE on these tables to update statistics');
  console.log('   - Monitor query performance with EXPLAIN ANALYZE');
  console.log('   - Consider VACUUM if tables are heavily updated');

  await app.stop();
  console.log('\n🎉 Index migration complete!\n');
  process.exit(0);
}

addNotificationIndexes().catch(err => {
  console.error('\n❌ Failed to add indexes:', err);
  process.exit(1);
});
