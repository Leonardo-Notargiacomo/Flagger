/**
 * Import function triggers from their respective submodules:
 *
 * import {onCall} from "firebase-functions/v2/https";
 * import {onDocumentWritten} from "firebase-functions/v2/firestore";
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

import functions, {setGlobalOptions} from "firebase-functions";


import admin from "firebase-admin";
admin.initializeApp();

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

// Function runs every day at 6 PM (18:00)
// Cron format: "0 18 * * *" = At 18:00 every day
exports.sendDailyExplorationReminder = functions.pubsub
  .schedule("0 18 * * *")
  .timeZone("Europe/Amsterdam") // Change to your timezone
  .onRun(async (_context) => {
    // Pick a random message
    const message = messages[Math.floor(Math.random() * messages.length)];

    // Send to all users subscribed to the topic
    const payload = {
      notification: {
        title: "Time to Explore!",
        body: message,
        icon: "ic_launcher", // Your app icon
      },
      data: {
        type: "daily_reminder",
        timestamp: Date.now().toString(),
      },
    };

    try {
      const response = await admin.messaging().sendToTopic(
        "daily_exploration_reminders",
        payload
      );
      console.log("Successfully sent daily reminder:", response);
      return null;
    } catch (error) {
      console.error("Error sending notification:", error);
      throw error;
    }
  });

setGlobalOptions({maxInstances: 10});
