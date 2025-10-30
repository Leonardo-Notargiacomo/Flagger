# UML Class Diagram Overview

This document describes the entity-relationship structure defined in the **ClassDiagram.drawio** file.  
It models a social application where users can interact, earn badges, complete challenges, and receive notifications.

---

## Entities and Attributes

### **User**
Represents an application user.

| Attribute | Type | Description |
|------------|------|-------------|
| id | int | Unique identifier for each user. |
| username | string | Display name of the user. |
| email | string | User's email address. |
| password | string | Encrypted password for authentication. |
| userImage | float | Reference or path to the user's profile image. |
| bio | string | Short description or biography of the user. |

---

### **Flag**
Represents a user’s flagged location or post record.

| Attribute | Type | Description |
|------------|------|-------------|
| userId | int | References the user who created the flag. |
| location_id | string | Identifier for the location (linked to Google API). |
| photoCode | string | Code or URL reference to the associated image. |
| dateTaken | DateTime | Timestamp of when the photo was taken. |
| notificationId | int | Links to the related notification. |

**Relationships:**
- Connected to **User** via `userId`.
- Connected to **GoogleAPI** via `location_id`.
- Connected to **Notification** via `notificationId`.

---

### **GoogleAPI**
Stores references to external Google API location identifiers.

| Attribute | Type | Description |
|------------|------|-------------|
| Location_Id | int | Unique location identifier. |

---

### **Badges**
Defines achievement or reward badges.

| Attribute | Type | Description |
|------------|------|-------------|
| id | int | Unique identifier for each badge. |
| name | string | Name of the badge. |
| nrOfVisits | int | Number of visits or actions required to earn it. |
| description | string | Description of the badge. |
| placeType | string | Type of location or activity associated with the badge. |
| notificationId | int | Related notification ID. |

---

### **Friend**
Represents friendship relationships between users.

| Attribute | Type | Description |
|------------|------|-------------|
| friendshipId | int | Unique identifier for the friendship. |
| UserId | int | References the `User` entity. |
| status | Enum (pending, accepted, blocked) | Current status of the friendship. |
| createdAt | DateTime | Timestamp of when the friendship was created. |

**Relationships:**
- Linked to **User** by `UserId`.

---

### **Challenge**
Represents a challenge that users can complete to earn rewards.

| Attribute | Type | Description |
|------------|------|-------------|
| id | int | Unique identifier for each challenge. |
| name | string | Name of the challenge. |
| description | string | Description of the challenge. |
| type | Enum (Special, Normal, Seasonal) | Type of challenge. |
| rewardedBadgeId | int | Links to a badge rewarded upon completion. |

**Relationships:**
- Connected to **Badges** through `rewardedBadgeId`.

---

### **Notification**
Represents messages or alerts sent to users.

| Attribute | Type | Description |
|------------|------|-------------|
| id | int | Unique notification identifier. |
| type | Enum (reminder, friend request, challenge, badge) | Type of notification. |
| msg | string | Notification message. |
| sentAt | DateTime | When the notification was sent. |
| isRead | Boolean | Indicates whether the notification was read. |

**Relationships:**
- Linked to **Badges** and **Flag** via `notificationId`.

---

## Relationships Summary

| From Entity | To Entity | Relationship Type | Based On |
|--------------|------------|------------------|-----------|
| User | Flag | One-to-Many | userId |
| User | Friend | One-to-Many | UserId |
| Flag | GoogleAPI | Many-to-One | location_id |
| Challenge | Badges | Many-to-One | rewardedBadgeId |
| Notification | Badges, Flag | One-to-Many | notificationId |

---