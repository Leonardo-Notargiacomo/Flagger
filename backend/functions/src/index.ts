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
  "Ready to Discover?",
  "Let's Go Exploring!",
  "Your Next Adventure",
  "Explore Something New",
  "Adventure Time! 🌟",
  "The World is Calling",
  "Time for an Adventure",
  "Go Explore Today!",
  "Discovery Mode: ON",
  "Ready for Adventure?",
];

// Array of exploration messages
const messages = [
  "The world is waiting! Time to explore something new today 🌍",
  "Your next adventure is just around the corner...",
  "Discover something amazing in your neighborhood today!",
  "Don't let today's exploration opportunities pass you by!",
  "The best discoveries happen when you step outside 📍",
  "Your exploration streak is calling! Keep it going!",
  "What will you discover today? Let's find out!",
  "The world around you is full of surprises. Go explore!",
  "Time to add a new place to your explored list!",
  "Adventure mode: ON. Are you ready to explore?",
  "We noticed you haven't explored today... yet 😉",
  "Your local area has secrets waiting to be discovered!",
  "5 minutes of exploration = endless memories!",
];

// Runs every hour 9 AM - 5 PM, randomly sends ~2 notifications per day
// TESTING: Every 1 minute with 30% chance
// PRODUCTION: "0 9-17 * * *" (hourly 9 AM-5 PM) with 22% chance = ~2/day
export const sendDailyExplorationReminder = onSchedule({
  schedule: "*/1 * * * *", // Every 1 minute (for testing)
  // Production: schedule: "0 9-17 * * *" (every hour 9 AM - 5 PM)
  timeZone: "Europe/Amsterdam",
}, async (_event) => {
  // Random chance to send
  // Testing: 100% chance (every minute for testing)
  // Production: 22% chance across 9 hours = ~2 notifications per day
  const sendProbability = 1.0; // Change to 0.22 for production
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
