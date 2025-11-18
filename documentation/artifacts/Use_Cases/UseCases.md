# Use Cases

This document outlines the use cases for the project. Each use case describes a specific interaction between a user and the system to achieve a goal.

---

## Template for New Use Cases

### Use Case: [Use Case Name]
**Actor:** [Role/Person who initiates the use case]

**Description:** [Brief description of what the actor wants to accomplish]

**Precondition:** [What must be true before this use case can start]

**Scenario:**
1. [Step 1 - Actor action or system response]
2. [Step 2 - System displays/responds]
3. [Step 3 - Actor performs action]
   
**Result:** [What is accomplished when the use case completes successfully]

**Exception:**
- [Step]a. [Condition]: [System response and actor guidance]
- [Step]b. [Another exception condition]: [System response]

**Extensions:**
- [Step]a [Optional flow description]
  1. [Sub-step 1]
  2. [Sub-step 2]
  3. [Return to main scenario step]
- n/a [if no extensions apply]

**link:** [link to the folder with artifacts]

---

### Use Case: Receive Daily Notifications
**Actor:** User

**Description:** The user wants to receive daily notifications to stay motivated in exploring the world

**Precondition:** 
- The user has granted notification permissions to the application
- The user is logged in

**Scenario:**
1. User opens the application and allows to receive notifications
2. System triggers the daily notifications
3. System creates a notification with appropriate title, message, and icon
4. System displays the notification in the device's notification tray
5. User views the notification on their device
6. User taps on the notification
7. System opens the application to the relevant screen/content
   
**Result:** The user successfully receives and views the daily notification, and can access related content by tapping the notification.

**Exception:**
- 4a. User has disabled notifications in device settings: Notification is not displayed, system logs the attempt
- 4b. Device is in Do Not Disturb mode: Notification is queued and displayed when mode is disabled

**Extensions:**
- n/a

---

### Use Case: Earn Badges for Exploration Activity
**Actor:** User

**Description:** The user earns badges automatically by completing explorations and maintaining streaks. Badges serve as recognition for achievements.

**Precondition:** The user is logged into their account and has access to exploration features.

**Scenario:**
1. User completes an exploration by logging a location visit.
2. System records the exploration event and updates exploration count and streak data.
3. System automatically checks all badge unlock criteria.
4. System verifies if badge requirements are met (e.g., 5 explorations, 3-day streak).
5. System awards corresponding badge(s) to user's profile with timestamp.
6. System logs notification for newly earned badge(s).
7. User navigates to badge screen to view collection.
8. System displays all badges (locked/unlocked) with status and progress.
9. User selects a badge to view detailed information.

**Result:** User earns badge(s) for milestones, displayed in collection with unlock timestamps and progress tracking.

**Exception:**
- 2a. Exploration logging fails: System notifies user to retry.
- 3b. Badge verification fails: System logs error internally and continues.
- 5c. Badge awarding fails: System retries during next exploration event.
- 8d. Badge loading fails: System displays error with retry button.

**Extensions:**
- 9a User views badge details: System shows detail dialog with name, description, category, icon, unlock date, and requirements for locked badges.
- 7a User checks progress: System displays earned badge count and progress indicator (e.g., "3 / 9 badges earned").

**Link:** [Full use case details](/documentation/artifacts/Use_Cases/[Badge]/TestScenario&Desc.md)

---

### Use Case: changing user information
**Actor:** User

**Description:** The user wants to update their personal information in the system.

**Precondition:** The user is logged into their account.

**Scenario:**
1. User navigates to the "Profile" section.
2. System displays the user's current information.
3. User changes the info they want to change
4. system updates the information and confirms the changes.

**Result:** users info has changed.

**Link:** [readme for further info](/documentation/artifacts/Use_Cases/change_user_info/README.md)
