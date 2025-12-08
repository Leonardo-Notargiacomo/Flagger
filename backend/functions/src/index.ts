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

// Array of wisdom and inspiration titles
const titles = [
  "Explorer's Wisdom 🧭",
  "Daily Inspiration ✨",
  "Adventure Awaits 🗺️",
  "Travel Wisdom 🌍",
  "Exploration Tip 💡",
  "Did You Know? 🤔",
  "Words of Wanderlust 🌟",
  "Discovery Insight 🔍",
  "Explorer's Quote 📖",
  "Journey Inspiration 🚶",
];

// Array of inspirational messages, quotes, tips, and facts
const messages = [
  // Classic exploration quotes
  "The world is a book, and those who do not travel read only one page. " +
    "- Saint Augustine",
  "Not all those who wander are lost. - J.R.R. Tolkien",
  "Adventure is worthwhile in itself. - Amelia Earhart",
  "To travel is to live. - Hans Christian Andersen",
  "Life is either a daring adventure or nothing at all. " +
    "- Helen Keller",
  "The journey of a thousand miles begins with a single step. - Lao Tzu",
  "We travel not to escape life, but for life not to escape us. - Anonymous",
  "Jobs fill your pocket, but adventures fill your soul. - Jamie Lyn Beatty",

  // Exploration tips and wisdom
  "Pro tip: The best discoveries often happen within walking distance " +
    "of home. Explore your neighborhood today!",
  "Walking just 15-20 minutes in a new direction can reveal hidden gems " +
    "you never knew existed.",
  "Every street you haven't walked down is a mystery waiting to be solved.",
  "Your city has layers of history. Every building, every corner has " +
    "a story to tell.",
  "The best time to explore was yesterday. The second best time is now.",
  "Exploration isn't about distance traveled, it's about curiosity embraced.",
  "Today's routine path could become tomorrow's forgotten adventure. " +
    "Mix it up!",

  // Fun facts about exploration and discovery
  "Did you know? The average person walks past 36 murderers in their " +
    "lifetime... but also 36,000 interesting places!",
  "Fun fact: Walking in nature for just 20 minutes can boost your " +
    "creativity by 50%!",
  "Studies show that people who explore new places regularly are happier " +
    "and more creative.",
  "Ancient explorers navigated by stars. You have GPS. No excuses! ⭐",

  // Motivational and poetic
  "Every explorer was once a beginner who decided to take that first step.",
  "The world rewards the curious. What will you discover today?",
  "Your comfort zone is a beautiful place, but nothing ever grows there.",
  "Adventure is calling. Will you answer? 📞",
  "Somewhere, something incredible is waiting to be known. " +
    "- Carl Sagan",
  "The only impossible journey is the one you never begin. " +
    "- Tony Robbins",
];

// Sends inspirational exploration wisdom to all subscribed users
// Runs every hour 9 AM - 5 PM, randomly sends ~2 notifications per day
// PRODUCTION: Hourly checks with 22% probability = ~2 notifications/day
// TESTING: To make notifications every minute, change schedule to "* * * * *"
export const sendDailyExplorationReminder = onSchedule({
  schedule: "0 9-17 * * *", // Every hour 9 AM - 5 PM (Europe/Amsterdam)
  timeZone: "Europe/Amsterdam",
}, async () => {
  // PRODUCTION MODE: 22% chance each hour = ~2 notifications per day
  // Math: 9 hours (9 AM - 5 PM) × 22% = ~1.98 notifications/day
  // TESTING: Set sendProbability to 1.0 to guarantee notification every time
  const sendProbability = 0.22;
  const shouldSend = Math.random() < sendProbability;

  if (!shouldSend) {
    console.log("Skipping wisdom notification this time (random chance)");
    return;
  }

  // Pick random inspirational title and message
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
      type: "exploration_wisdom",
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
    console.log("Successfully sent exploration wisdom:", response);
    console.log(`Title: ${title}, Message: ${message}`);
  } catch (error) {
    console.error("Error sending wisdom notification:", error);
    throw error;
  }
});

// Personalized notifications based on user behavior
// Calls LoopBack API to get targets and sends personalized messages
export const sendPersonalizedNotifications = onSchedule({
  schedule: "0 9-17 * * *",
  timeZone: "Europe/Amsterdam",
}, async () => {
  // 22% chance each hour = ~2 notifications per day
  const sendProbability = 0.22;
  const shouldSend = Math.random() < sendProbability;

  if (!shouldSend) {
    console.log(
      "[Personalized] Skipping notification this time (random chance)"
    );
    return;
  }

  // Randomly choose notification type
  const notificationType =
    Math.random() > 0.5 ? "doing-well" : "skipping";
  console.log(`[Personalized] Fetching ${notificationType} targets...`);

  const backendUrl =
    "https://group-repository-2025-android-1-6of2.onrender.com";

  try {
    // Fetch notification targets from LoopBack API
    const targetsResponse = await fetch(
      `${backendUrl}/api/notifications/targets/${notificationType}`
    );

    if (!targetsResponse.ok) {
      console.error(
        "[Personalized] Failed to fetch targets: " +
        `${targetsResponse.status}`
      );
      return;
    }

    const targets = await targetsResponse.json();
    console.log(
      `[Personalized] Found ${targets.length} users for ` +
      `${notificationType} notifications`
    );

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
          console.error(
            "[Personalized] Failed to get message for " +
            `user ${target.userId}`
          );
          continue;
        }

        const message = await messageResponse.json();

        // Generate unique notification ID
        const randomPart = Math.random().toString(36).substr(2, 9);
        const notificationId = `notif_${Date.now()}_${randomPart}`;

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
            console.log(
              `[Personalized] Sent to user ${target.userId}: ` +
              `${fcmResponse}`
            );
            successCount++;
          } catch (fcmError: unknown) {
            const error = fcmError as Error;
            console.error(
              `[Personalized] FCM error for token ${token}:`,
              error.message
            );
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
      } catch (userError: unknown) {
        const error = userError as Error;
        console.error(
          "[Personalized] Error processing user " +
          `${target.userId}:`,
          error.message
        );
        errorCount++;
      }
    }

    console.log(
      `[Personalized] Summary: ${successCount} sent, ` +
      `${errorCount} errors`
    );
  } catch (error: unknown) {
    const err = error as Error;
    console.error("[Personalized] Fatal error:", err.message);
    throw error;
  }
});

setGlobalOptions({maxInstances: 10});
