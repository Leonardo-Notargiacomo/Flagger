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

---

### Use Case: Receive Daily Notifications
**Actor:** User

**Description:** The user wants to receive daily notifications to stay motivated in exploring the world

**Precondition:** 
- The user has granted notification permissions to the application

**Scenario:**
1. User opens the application for the first time in the day
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
