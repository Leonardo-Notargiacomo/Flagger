# Friends Feature - Frontend Class Diagrams

---

## Diagram 1: UI Components Layer

```mermaid
classDiagram
    direction TB

    class FriendsScreen {
        <<Composable>>
        +navController: NavController
        +viewModel: FriendsViewModel
    }

    class FriendItem {
        <<Composable>>
        +friend: FriendListItem
        +onRemove: (Int) → Unit
        +onViewProfile: (Int) → Unit
    }

    class ReceivedRequestItem {
        <<Composable>>
        +request: FriendRequest
        +onAccept: (Int) → Unit
        +onReject: (Int) → Unit
    }

    class SentRequestItem {
        <<Composable>>
        +request: FriendRequest
        +onCancel: (Int) → Unit
    }

    class SearchUserItem {
        <<Composable>>
        +user: User
        +relationshipStatus: RelationshipStatus
        +onSendRequest: (Int) → Unit
        +onAcceptRequest: (Int) → Unit
    }

    FriendsScreen --> FriendItem : renders
    FriendsScreen --> ReceivedRequestItem : renders
    FriendsScreen --> SentRequestItem : renders
    FriendsScreen --> SearchUserItem : renders
```

## Diagram 2: ViewModel & State Management

```mermaid
classDiagram
    direction TB

    class FriendsViewModel {
        <<ViewModel>>
        -repository: FriendsRepository
        -userRepository: UserRepository
        -_uiState: MutableStateFlow~FriendsUiState~
        +uiState: StateFlow~FriendsUiState~
        +searchUsers(query: String)
        +loadFriends()
        +loadReceivedRequests()
        +loadSentRequests()
        +sendFriendRequest(toUserId: Int)
        +acceptFriendRequest(requestId: Int)
        +rejectFriendRequest(requestId: Int)
        +cancelFriendRequest(requestId: Int)
        +removeFriend(friendId: Int)
        +getRelationshipStatus(userId: Int)
    }

    class FriendsUiState {
        <<Data Class>>
        +friends: List~FriendListItem~
        +receivedRequests: List~FriendRequest~
        +sentRequests: List~FriendRequest~
        +searchResults: List~User~
        +isSearching: Boolean
        +isLoadingFriends: Boolean
        +currentOperation: FriendsOperation
        +error: String?
        +successMessage: String?
    }

    class FriendsOperation {
        <<Sealed Class>>
        +None
        +SendingRequest
        +AcceptingRequest
        +RejectingRequest
        +CancellingRequest
        +RemovingFriend
    }

    class RelationshipStatus {
        <<Enumeration>>
        NONE
        FRIENDS
        PENDING_SENT
        PENDING_RECEIVED
    }

    FriendsViewModel *-- FriendsUiState : manages state
    FriendsViewModel ..> FriendsOperation : uses
    FriendsViewModel ..> RelationshipStatus : determines
    FriendsUiState *-- FriendsOperation : contains
```

## Diagram 3: Data Layer - Repository & API

Shows data access layer components.

```mermaid
classDiagram
    direction TB

    class FriendsRepository {
        <<Class>>
        -api: FriendsApi
        +searchUsers(token, query): Result~List~User~~
        +sendFriendRequest(token, toUserId): Result~FriendRequest~
        +getReceivedRequests(token): Result~List~FriendRequest~~
        +getSentRequests(token): Result~List~FriendRequest~~
        +acceptFriendRequest(token, id): Result~Response~
        +rejectFriendRequest(token, id): Result~Response~
        +cancelFriendRequest(token, id): Result~Unit~
        +getFriends(token): Result~List~FriendListItem~~
        +removeFriend(token, friendId): Result~Unit~
        +getUserById(token, userId): Result~User~
        +getFriendFlags(token, friendId): Result~List~Flag~~
    }

    class FriendsApi {
        <<Interface - Retrofit>>
        +searchUsers(token, filter): Response~List~User~~
        +getUserById(token, userId): Response~User~
        +sendFriendRequest(token, body): Response~FriendRequest~
        +getReceivedRequests(token): Response~List~FriendRequest~~
        +getSentRequests(token): Response~List~FriendRequest~~
        +acceptFriendRequest(token, id): Response~Response~
        +rejectFriendRequest(token, id): Response~Response~
        +cancelFriendRequest(token, id): Response~Unit~
        +getFriends(token): Response~List~FriendListItem~~
        +removeFriend(token, friendId): Response~Unit~
        +getFriendFlags(token, friendId): Response~List~Flag~~
    }

    class ApiClient {
        <<Object - Singleton>>
        +retrofit: Retrofit
        +friendsApi: FriendsApi
        +create()
    }

    class UserRepository {
        <<Object - Singleton>>
        +userId: Int?
        +token: String?
    }

    FriendsRepository --> FriendsApi : uses
    ApiClient ..> FriendsApi : creates
    FriendsViewModel --> FriendsRepository : uses
    FriendsViewModel --> UserRepository : reads token/userId
```

## Diagram 4: Data Models

```mermaid
classDiagram
    direction TB

    class User {
        <<Data Class>>
        +id: Int
        +email: String?
        +userName: String?
        +bio: String?
        +userImage: String?
    }

    class FriendRequest {
        <<Data Class>>
        +id: Int?
        +fromUserId: Int
        +toUserId: Int
        +status: String
        +createdAt: String?
        +fromUser: User?
        +toUser: User?
    }

    class FriendListItem {
        <<Data Class>>
        +id: Int?
        +userId: Int
        +friendId: Int
        +friendDetails: User?
    }

    class Flag {
        <<Data Class>>
        +id: Int?
        +locationId: String
        +photoCode: String?
        +dateTaken: String
        +notification: Int
        +userId: Int
    }

    class SendFriendRequestBody {
        <<Data Class>>
        +toUserId: Int
    }

    class AcceptFriendRequestResponse {
        <<Data Class>>
        +message: String
    }

    class RejectFriendRequestResponse {
        <<Data Class>>
        +message: String
    }

    FriendRequest *-- User : contains fromUser
    FriendRequest *-- User : contains toUser
    FriendListItem *-- User : contains friendDetails
```