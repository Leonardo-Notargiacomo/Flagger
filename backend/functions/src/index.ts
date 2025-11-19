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
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
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

setGlobalOptions({maxInstances: 10});
