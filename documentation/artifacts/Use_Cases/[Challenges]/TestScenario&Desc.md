## Challenge Use Case

### Use Case: As a user, I want to earn/complete challenges for my exploration activity so that I can track and celebrate my achievements.
**Actor:** User 

**Description:** A user completes challenges automatically by completing explorations and maintaining streaks within the system. These challenges serve as recognition and motivation for the user's progress.

**Precondition:** 1: The user is logged into their account. 2: The user has access to the exploration and challenges features of the system.

**Scenario:**
1. The user navigates his way to the challenges section.
2. The System displays 3 randomly selected challenges.
3. The user completes an exploration or performs the required actions to finish a challenge (e.g., visiting a set amount of locations).
4. The system records the exploration event or challenge action and updates the user's completion count and streak data.
5. The system automatically checks the chosen challenge's unlock criteria for the user.
6. The system verifies if any challenge requirements have been met (e.g., reached 5 explorations, maintained a 3-day streak).
7. The system awards the corresponding challenge(s)/challenge badge to the user's profile with timestamp.
8. The system logs a notification for the newly earned/completed challenge(s).
9. The user navigates to the challenges screen to view their challenge collection.
10. The system displays all challenges (locked and unlocked) with their unlock status, categories, and earned challenge count.
11. The user selects a challenge to view detailed information about it.

**Result:** The user successfully completes challenge(s) for reaching exploration milestones, which are displayed in their challenge collection with completion timestamps and progress tracking.

**Exception:**
- Step 4a. Exploration/challenge logging fails: The system notifies the user that the exploration or challenge action could not be recorded and suggests retrying.
- Step 5b. Challenge verification fails: The system logs the error internally and continues normal operation without awarding challenges.
- Step 7c. Challenge awarding fails: The system logs the error and attempts to award the challenge during the next exploration event.
- Step 10d. Challenge loading fails: The system displays an error message with a retry button to reload the challenge collection.

**Extensions:**
- Step 11a User views challenge details: 
  1. The user taps on a challenge in the challenge collection grid.
  2. The system displays a detail dialog showing the challenge name, description, category, icon, and completion date (if completed).
  3. For locked challenges, the system shows the requirements needed to unlock the challenge (e.g., "Complete 10 explorations").
  4. The user dismisses the dialog to return to the challenge collection.
  5. Return to main scenario step 10.
- Step 9a User checks challenge progress:
  1. The user views the challenge screen header showing their progress.
  2. The system displays completed challenges count and total available challenges (e.g., "3 / 9 challenges completed").
  3. The system shows a visual progress indicator.
  4. Return to main scenario step 9.
- Step 10a User filters challenges by category:
  1. The user views challenges organized by categories (Beginner, Explorer, Expert, Legend, Streak).
  2. The system groups challenges visually to show achievement progression.
  3. Continue to step 10.

---

