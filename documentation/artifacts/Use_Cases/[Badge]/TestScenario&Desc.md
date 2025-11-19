## Badge Use Case

### Use Case: As a user, I want to earn badges for my exploration activity so that I can track and celebrate my achievements.
**Actor:** User 

**Description:** A user earns badges automatically by completing explorations and maintaining streaks within the system. These badges serve as recognition and motivation for the user's progress. Badges are awarded based on predefined criteria such as total exploration count or consecutive day streaks.

**Precondition:** 1: The user is logged into their account. 2: The user has access to the exploration and badge features of the system.

**Scenario:**
1. The user completes an exploration by logging a new exploration event (location visit).
2. The system records the exploration event and updates the user's exploration count and streak data.
3. The system automatically checks all badge unlock criteria for the user.
4. The system verifies if any badge requirements have been met (e.g., reached 5 explorations, maintained a 3-day streak).
5. The system awards the corresponding badge(s) to the user's profile with timestamp.
6. The system logs a notification for the newly earned badge(s).
7. The user navigates to the badge screen to view their badge collection.
8. The system displays all badges (locked and unlocked) with their unlock status, categories, and earned badge count.
9. The user selects a badge to view detailed information about it.

**Result:** The user successfully earns badge(s) for reaching exploration milestones, which are displayed in their badge collection with unlock timestamps and progress tracking.

**Exception:**
- Step 2a. Exploration logging fails: The system notifies the user that the exploration could not be recorded and suggests retrying.
- Step 3b. Badge verification fails: The system logs the error internally and continues normal operation without awarding badges.
- Step 5c. Badge awarding fails: The system logs the error and attempts to award the badge during the next exploration event.
- Step 8d. Badge loading fails: The system displays an error message with a retry button to reload the badge collection.

**Extensions:**
- Step 9a User views badge details: 
  1. The user taps on a badge in the badge collection grid.
  2. The system displays a detail dialog showing the badge name, description, category, icon, and unlock date (if unlocked).
  3. For locked badges, the system shows the requirements needed to unlock the badge (e.g., "Complete 10 explorations").
  4. The user dismisses the dialog to return to the badge collection.
  5. Return to main scenario step 9.
- Step 7a User checks badge progress:
  1. The user views the badge screen header showing their progress.
  2. The system displays earned badges count and total available badges (e.g., "3 / 9 badges earned").
  3. The system shows a visual progress indicator.
  4. Return to main scenario step 8.
- Step 8a User filters badges by category:
  1. The user views badges organized by categories (Beginner, Explorer, Expert, Legend, Streak).
  2. The system groups badges visually to show achievement progression.
  3. Continue to step 9.

---