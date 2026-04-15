# Flagger — Location-Based Exploration App

Android app where users drop flags at real-world locations, explore places nearby,
and complete location-based challenges with friends. Built by a 6-person team at
Fontys University over one semester.

## What it does

- Drop and discover flags at GPS coordinates
- Explore nearby places via Google Places API
- Send and manage friend requests, view public profiles
- Complete location-based challenges with progress tracking
- Receive push notifications for friend activity and challenge updates
- Create custom flags with personalized icons and colors

## Architecture

```
frontend/    Kotlin + Jetpack Compose (Android)
backend/     LoopBack 4 (TypeScript) REST API — 18 controllers, 15 models
             PostgreSQL via LoopBack datasource
             Firebase Cloud Messaging for push notifications
```

## Tech stack

- Android: Kotlin, Jetpack Compose, MVVM, Retrofit, OkHttp, CameraX
- Backend: LoopBack 4 (TypeScript), PostgreSQL, Render (deployment)
- Auth: JWT
- Maps: Google Maps SDK, Google Places API
- Notifications: Firebase Cloud Messaging
- Infrastructure: Docker, GitHub Actions

## Running locally

**Backend**

```bash
cd backend/backend
cp .env.example .env   # fill in DATABASE_URL and Firebase credentials
npm install
npm start
```

**Android**

1. Open `frontend/` in Android Studio
2. Add your key to `frontend/local.properties`:
   ```
   MAPS_API_KEY=your_google_maps_api_key
   ```
3. Add your own `google-services.json` from Firebase Console to `frontend/app/`
4. Run on a device or emulator (minSdk 33)

## My role

Served as **Product Owner** — ran sprint planning, managed the backlog, and wrote
user story templates adopted across the team. Also shipped features as a developer:

- Built the full friends system: search, friend requests with conflict handling,
  relationship status tracking, and profile navigation
- Owned the onboarding flow end-to-end: animated screens, illustrations, and
  first-launch state management
- Integrated Google Places API in `MapRepository` for nearby place discovery
  and detail fetching
- Co-built the challenges system with a teammate: completion detection, 24h
  cooldown, auto-refresh every 30s, and backend endpoint fixes
- Implemented camera permission flow and custom flag creation (colors, icons)
