/**
 * Import function triggers from their respective submodules:
 *
 * import {onCall} from "firebase-functions/v2/https";
 * import {onDocumentWritten} from "firebase-functions/v2/firestore";
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

import {onSchedule} from "firebase-functions/v2/scheduler";
import {setGlobalOptions} from "firebase-functions/v2";
import * as admin from "firebase-admin";

admin.initializeApp();

// Array of exploration titles
const titles = [
  "Time to Explore!",
  "Adventure Awaits! 🗺️",
  "We Miss You...",
  "Still There?",
  "Hello? 👀",
  "Don't Make This Awkward",
  "We Need to Talk",
  "This Is Your Final Warning",
  "You've Left Us No Choice",
  "Deploy Guilt Trip? ✅",
  "The Consequences...",
];

// Array of exploration messages
const messages = [
  // Friendly reminders (days 1-2)
  "The world is waiting! Time to explore something new today 🌍",
  "Your next adventure is just around the corner...",
  "Discover something amazing in your neighborhood today!",

  // Getting personal (days 3-4)
  "We noticed you haven't explored today... yet 😉",
  "Your exploration streak is crying in the corner right now",
  "Remember when you used to explore? Those were good times...",
  "Your local area has secrets. Don't you want to know them?",

  // Passive aggressive (days 5-6)
  "I'm not mad. Just disappointed. 💔",
  "Go is sad. You wouldn't like Go when Go is sad.",
  "I see how it is. We're just not important to you anymore.",
  "Other users are exploring right now. Just saying. 👀",

  // Unhinged territory (day 7+)
  "I know where you live. Because you haven't explored anywhere else.",
  "Your plants are watered. Your pets fed. Exploration streak? Dead.",
  "We have a very particular set of skills... in guilt-tripping.",
  "Spanish or vanish? More like EXPLORE OR... we'll be sad 🥺",
  "5 minutes of exploration = not having to see this notification tomorrow",
  "Fine. Don't explore. See if we care. (We care very much please come back)",
  "Your ancestors didn't explore new lands for you to stay on the couch",
  "We're not angry, just... *checks notes* ...actually we're pretty angry",
];

// Runs every hour 9 AM - 5 PM, randomly sends ~2 notifications per day
// PRODUCTION: Hourly checks with 22% probability = ~2 notifications/day
// TESTING: To make notifications every minute, change schedule to "* * * * *"
export const sendDailyExplorationReminder = onSchedule({
  schedule: "0 9-17 * * *", // Every hour 9 AM - 5 PM (Europe/Amsterdam)
  timeZone: "Europe/Amsterdam",
}, async (_event) => {
  // PRODUCTION MODE: 22% chance each hour = ~2 notifications per day
  // Math: 9 hours (9 AM - 5 PM) × 22% = ~1.98 notifications/day
  // TESTING: Set sendProbability to 1.0 to guarantee notification every time
  const sendProbability = 0.22;
  const shouldSend = Math.random() < sendProbability;

  if (!shouldSend) {
    console.log("Skipping notification this time (random chance)");
    return;
  }

  // Pick random title and message
  const title = titles[Math.floor(Math.random() * titles.length)];
  const message = messages[Math.floor(Math.random() * messages.length)];

  // Send to all users subscribed to the topic
  const payload = {
    topic: "daily_exploration_reminders",
    notification: {
      title: title,
      body: message,
    },
    data: {
      type: "daily_reminder",
      timestamp: Date.now().toString(),
    },
    android: {
      priority: "high" as const, // High priority for Samsung/Android devices
      notification: {
        channelId: "explore_daily_reminders",
        priority: "max" as const, // Max priority for heads-up
        visibility: "public" as const, // Show on lock screen
        sound: "default",
        defaultSound: true,
        defaultVibrateTimings: true,
        defaultLightSettings: true,
      },
    },
  };

  try {
    const response = await admin.messaging().send(payload);
    console.log("Successfully sent daily reminder:", response);
    console.log(`Title: ${title}, Message: ${message}`);
  } catch (error) {
    console.error("Error sending notification:", error);
    throw error;
  }
});

// Personalized notifications based on user behavior
// Calls LoopBack API to get targets and sends personalized messages
export const sendPersonalizedNotifications = onSchedule({
  schedule: "0 9-17 * * *", // Every hour 9 AM - 5 PM (Europe/Amsterdam)
  timeZone: "Europe/Amsterdam",
}, async (_event) => {
  // 22% chance each hour = ~2 notifications per day
  const sendProbability = 0.22;
  const shouldSend = Math.random() < sendProbability;

  if (!shouldSend) {
    console.log("[Personalized] Skipping notification this time (random chance)");
    return;
  }

  // Randomly choose notification type
  const notificationType = Math.random() > 0.5 ? "doing-well" : "skipping";
  console.log(`[Personalized] Fetching ${notificationType} targets...`);

  const backendUrl = "https://group-repository-2025-android-1-6of2.onrender.com";

  try {
    // Fetch notification targets from LoopBack API
    const targetsResponse = await fetch(
      `${backendUrl}/api/notifications/targets/${notificationType}`
    );

    if (!targetsResponse.ok) {
      console.error(`[Personalized] Failed to fetch targets: ${targetsResponse.status}`);
      return;
    }

    const targets = await targetsResponse.json();
    console.log(`[Personalized] Found ${targets.length} users for ${notificationType} notifications`);

    if (targets.length === 0) {
      console.log("[Personalized] No eligible users found");
      return;
    }

    // Send personalized notifications to each target
    let successCount = 0;
    let errorCount = 0;

    for (const target of targets) {
      try {
        // Get personalized message for this user
        const messageResponse = await fetch(
          `${backendUrl}/api/notifications/message`,
          {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({
              userId: target.userId,
              reason: target.reason,
              context: target.context,
            }),
          }
        );

        if (!messageResponse.ok) {
          console.error(`[Personalized] Failed to get message for user ${target.userId}`);
          continue;
        }

        const message = await messageResponse.json();

        // Generate unique notification ID
        const notificationId = `notif_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;

        // Send to all user's devices
        for (const token of target.fcmTokens) {
          const payload = {
            token: token,
            notification: {
              title: message.title,
              body: message.body,
            },
            data: {
              type: message.type,
              timestamp: Date.now().toString(),
              notificationId: notificationId,
              action: message.data.action || "open_map",
              userId: target.userId.toString(),
            },
            android: {
              priority: "high" as const,
              notification: {
                channelId: "explore_daily_reminders",
                priority: "max" as const,
                visibility: "public" as const,
                sound: "default",
                defaultSound: true,
                defaultVibrateTimings: true,
                defaultLightSettings: true,
              },
            },
          };

          try {
            const fcmResponse = await admin.messaging().send(payload);
            console.log(`[Personalized] Sent to user ${target.userId}: ${fcmResponse}`);
            successCount++;
          } catch (fcmError: any) {
            console.error(`[Personalized] FCM error for token ${token}:`, fcmError.message);
            errorCount++;
            // TODO: Mark token as inactive if error is "invalid token"
          }
        }

        // Mark notification as sent in backend
        await fetch(
          `${backendUrl}/api/notifications/mark-sent`,
          {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({
              userId: target.userId,
              type: notificationType,
              title: message.title,
              body: message.body,
            }),
          }
        );
      } catch (userError: any) {
        console.error(`[Personalized] Error processing user ${target.userId}:`, userError.message);
        errorCount++;
      }
    }

    console.log(`[Personalized] Summary: ${successCount} sent, ${errorCount} errors`);
  } catch (error: any) {
    console.error("[Personalized] Fatal error:", error.message);
    throw error;
  }
});

setGlobalOptions({maxInstances: 10});
