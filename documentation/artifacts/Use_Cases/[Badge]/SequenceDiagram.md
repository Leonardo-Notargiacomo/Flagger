# Badge System Sequence Diagram

This sequence diagram illustrates the complete flow of badge earning, from logging an exploration to viewing badges in the UI.

## Main Flow: User Earns Badge Through Exploration

```mermaid
sequenceDiagram
    actor User
    participant App as Mobile App
    participant ExplCtrl as ExplorationController
    participant ExplRepo as ExplorationEventRepository
    participant StreakCalc as StreakCalculatorService
    participant StreakRepo as UserStreakRepository
    participant BadgeUnlock as BadgeUnlockService
    participant BadgeRepo as BadgeRepository
    participant UserBadgeRepo as UserBadgeRepository
    
    %% User logs exploration
    User->>App: Complete exploration at location
    App->>ExplCtrl: POST /api/users/{userId}/explorations
    Note over ExplCtrl: logExploration(userId, explorationData)
    
    %% Save exploration event
    ExplCtrl->>ExplRepo: create(exploration event)
    ExplRepo-->>ExplRepo: Insert into database
    ExplRepo-->>ExplCtrl: ExplorationEvent created
    
    %% Update user streak
    ExplCtrl->>StreakCalc: updateStreak(userId)
    StreakCalc->>StreakRepo: findOne({userId})
    StreakRepo-->>StreakCalc: UserStreak | null
    
    alt User has existing streak
        StreakCalc->>StreakCalc: Calculate consecutive days
        StreakCalc->>StreakRepo: updateById(id, {currentStreak, longestStreak})
    else First exploration
        StreakCalc->>StreakRepo: create({userId, currentStreak: 1})
    end
    
    StreakRepo-->>StreakCalc: Updated UserStreak
    StreakCalc-->>ExplCtrl: UserStreak with current/longest streak
    
    %% Check and unlock badges
    ExplCtrl->>BadgeUnlock: checkAndUnlockBadges(userId)
    
    BadgeUnlock->>BadgeRepo: find() - Get all badges
    BadgeRepo-->>BadgeUnlock: Badge[]
    
    BadgeUnlock->>UserBadgeRepo: find({where: {userId}})
    UserBadgeRepo-->>BadgeUnlock: UserBadge[] (already unlocked)
    
    BadgeUnlock->>ExplRepo: count({userId})
    ExplRepo-->>BadgeUnlock: {count: explorationCount}
    
    BadgeUnlock->>StreakRepo: findOne({where: {userId}})
    StreakRepo-->>BadgeUnlock: UserStreak with currentStreak
    
    loop For each badge
        BadgeUnlock->>BadgeUnlock: checkCriteria(badge.unlockCriteria, explorationCount, currentStreak)
        
        alt Badge criteria met AND not already unlocked
            BadgeUnlock->>UserBadgeRepo: create({userId, badgeId, unlockedAt})
            UserBadgeRepo-->>UserBadgeRepo: Insert user badge
            UserBadgeRepo-->>BadgeUnlock: UserBadge created
            
            BadgeUnlock->>BadgeUnlock: sendBadgeNotification(userId, badge)
            Note over BadgeUnlock: Console log notification
            
            BadgeUnlock->>UserBadgeRepo: updateAll({notificationSent: true})
            UserBadgeRepo-->>BadgeUnlock: Updated
        end
    end
    
    BadgeUnlock-->>ExplCtrl: Badge[] (newly unlocked badges)
    
    %% Return response to app
    ExplCtrl-->>App: {success, event, streak, newBadges}
    App-->>User: Show success + new badges earned
    
    alt New badges earned
        App->>User: Display badge unlock notification 🎉
    end
```

## Alternative Flow: User Views Badge Collection

```mermaid
sequenceDiagram
    actor User
    participant App as Mobile App
    participant BadgeCtrl as BadgeController
    participant BadgeRepo as BadgeRepository
    participant UserBadgeRepo as UserBadgeRepository
    
    User->>App: Navigate to Badge Screen
    App->>BadgeCtrl: GET /api/users/{userId}/badges
    Note over BadgeCtrl: getUserBadges(userId)
    
    %% Get user's unlocked badges
    BadgeCtrl->>UserBadgeRepo: find({where: {userId}, include: ['badge']})
    UserBadgeRepo-->>UserBadgeRepo: JOIN with Badge table
    UserBadgeRepo-->>BadgeCtrl: UserBadge[] with Badge details
    
    %% Get all available badges
    BadgeCtrl->>BadgeRepo: find({order: ['displayOrder ASC']})
    BadgeRepo-->>BadgeCtrl: Badge[] (all badges in system)
    
    %% Map badges with unlock status
    BadgeCtrl->>BadgeCtrl: Map badges with isUnlocked status
    Note over BadgeCtrl: Mark badges as locked/unlocked<br/>Add unlockedAt timestamp
    
    BadgeCtrl-->>App: {badges[], totalBadges, earnedBadges}
    
    App->>App: Display badge grid
    App-->>User: Show badge collection (locked/unlocked)
    
    %% User views badge details
    User->>App: Tap on badge
    App->>User: Show BadgeDetailDialog
    Note over App,User: Display: name, description,<br/>category, icon, unlock date<br/>or requirements if locked
```



## Key Interactions Summary

### 1. Exploration Logging Flow
- User completes exploration → App sends POST request
- System saves event → Updates streak → Checks badges
- Returns success with any newly unlocked badges
- Notifications are logged to console

### 2. Badge Checking Logic
- Retrieves all badges and user's unlocked badges
- Gets exploration count and current streak
- For each badge: checks if criteria met and not already unlocked
- Creates UserBadge entries for newly unlocked badges
- Logs notifications to console

### 3. Badge Viewing Flow
- App requests user's badges
- System retrieves all badges and marks unlock status
- Returns combined list with locked/unlocked indicators
- User can tap badges to view details

### 4. Badge Criteria Types
- **exploration_count**: Number of explorations completed
  - First Steps: 1, Explorer: 5, Adventurer: 10, Pathfinder: 25, World Traveler: 50, Legend: 100
- **streak**: Consecutive days with explorations
  - Daily Dedication: 3, Week Warrior: 7, Consistency King: 30


