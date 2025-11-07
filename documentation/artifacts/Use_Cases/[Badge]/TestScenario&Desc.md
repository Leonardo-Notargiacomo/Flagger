## Template for New Use Cases

### Use Case: As a user, I want to earn badges for completing challenges so that I can see and celebrate my progress.
**Actor:** User 

**Description:** A user wants to earn badges by completing specific challenges within the system. These badges serve as a form of recognition and motivation for the user's achievements.

**Precondition:** 1: The user is logged into their account. 2: The user has access to the challenges section of the system.

**Scenario:**
1. The user navigates to the challenges section of the system.
2. The system displays a list of available challenges along with their descriptions and requirements.
3. The user automatically completes a challenge by fulfilling its requirements (e.g., completing a task, reaching a milestone).
4. The system verifies the completion of the challenge.
5. The system awards the corresponding badge to the user's profile.
6. The system notifies the user of the new badge earned.
7. The user views their profile to see the newly earned badge displayed.
8. The user shares their achievement on social media or within the system's community.

**Result:** The user successfully earns a badge for completing a challenge, which is displayed on their profile and can be shared with others.

**Exception:**
- Step 3a. Challenge requirements not met: The system informs the user that they have not met the requirements for the challenge and provides guidance on what needs to be done.
- Step 4b. System verification fails: The system notifies the user of an error during verification and suggests retrying or contacting support.
- Step 5c. Badge awarding fails: The system logs the error and informs the user that the badge could not be awarded at this time, advising them to try again later.

**Extensions:**
- Step 3a User requests help: 
  1. The user clicks on a "Help" or "More Info" link next to the challenge.
  2. The system displays additional information about the challenge requirements and tips for completion.
  3. Return to main scenario step 3.
- Step 6a User shares badge:
  1. The user clicks on a "Share" button next to the badge notification.
  2. The system provides options for sharing within the community.
  3. The user shares the badge.
  4. Return to main scenario step 7.

---