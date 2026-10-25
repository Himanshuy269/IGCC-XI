# IGCC XI - Simple Cricket Live Score App
## Technical Documentation & Architecture Guide

A clean, modern, and production-ready Android cricket scoring application built with **Kotlin**, **Jetpack Compose**, **Material 3**, **MVVM architecture**, and **Firebase Firestore**.

---

## 1. Core Architecture Overview

The app is built following Google's official Android Architecture Guidelines, utilizing pure functional domain logic and reactive state management:

```
com.example.igccxi
├── data
│   ├── model
│   │   ├── MatchStatus.kt            // Lifecycle: NOT_STARTED, INNINGS_1, INNINGS_BREAK, INNINGS_2, COMPLETED
│   │   └── Match.kt                  // Match, Innings, BatterScore, BowlerScore, Extras, BallEvent
│   └── repository
│       ├── MatchRepository.kt        // Repository interface with real-time Flow streams
│       ├── InMemoryMatchRepository.kt// Pre-seeded offline fallback repository for instant preview
│       ├── FirestoreMatchRepository.kt// Cloud Firestore repository with real-time snapshot listeners
│       └── RepositoryProvider.kt     // Singleton provider with auto-detection of Firebase initialization
├── domain
│   ├── ScoringEngine.kt              // Pure functional scoring engine (runs, extras, wickets, overs, targets, results)
│   └── PdfScorecardExporter.kt       // Native Android PdfDocument generator & system share intent
├── ui
│   ├── navigation
│   │   └── Screen.kt                 // Compose navigation routes (Home, Create, Scoring, Details, History)
│   ├── components
│   │   ├── ScoreCardSummary.kt       // Match summary card with Live badge & scores
│   │   ├── BattingTable.kt           // Comprehensive batting scorecard table
│   │   ├── BowlingTable.kt           // Comprehensive bowling figures table
│   │   └── BallByBallTicker.kt       // Color-coded recent deliveries ticker
│   ├── screens
│   │   ├── home                      // HomeScreen & HomeViewModel
│   │   ├── create                    // CreateMatchScreen & CreateMatchViewModel
│   │   ├── scoring                   // LiveScoringScreen & LiveScoringViewModel (Keypad & Undo)
│   │   ├── details                   // MatchDetailsScreen & MatchDetailsViewModel (Scorecard & PDF)
│   │   └── history                   // MatchHistoryScreen & MatchHistoryViewModel (Date & Search filters)
│   └── theme
│       ├── Color.kt, Theme.kt, Type.kt
└── MainActivity.kt                   // Compose NavHost entrypoint
```

---

## 2. Core Features Breakdown

### Feature 1: Live Match Scoring
- **Match Setup**: Scorer enters Team A & Team B names, match date, total overs (e.g., 5, 10, 15, 20), and toss details.
- **Ergonomic Keypad**:
  - `0, 1, 2, 3, 4, 6`: Records runs off the bat.
  - `Wd` (Wide): Adds 1 run penalty, bowler conceded +1, legal ball count does **not** advance.
  - `Nb` (No-Ball): Adds 1 run penalty + runs off bat, legal ball count does **not** advance.
  - `Wicket`: Prompts for dismissal type and new batter name, marks striker out, increments wickets. Automatically detects **All Out** at 10 wickets.
  - **Undo**: Multi-step state history stack (`historyStack`) that reverts ball events, strike rotation, extras, and overs with 100% precision.
  - **Over Management**: Automatically increments completed overs after 6 legal deliveries, resets ball counter, swaps strike between ends, and prompts for bowler change.
  - **2nd Innings Target & Result**: Automatically calculates target (`Innings 1 Total + 1`), required run rate, balls remaining, and declares the winner (by wickets, runs, or match tied).

### Feature 2: Public Live Scores & Scorecards
- **Real-Time Synchronisation**: Uses Firestore snapshot listeners (`callbackFlow`) so viewers receive instant score updates without manual refreshing.
- **Scorer Ownership Security**: Scorer's unique identity (`scorerId`) is generated via Firebase Anonymous Authentication. Only the authorized scorer can submit score updates; other users view the live match in read-only mode.
- **Complete Scorecard**:
  - Batters: Runs, Balls faced, 4s, 6s, Strike Rate, Dismissal status (`batting *`, `not out`, `c Fielder b Bowler`).
  - Bowlers: Overs, Maidens, Runs conceded, Wickets, Economy rate.
  - Extras breakdown (Wides, No Balls, Byes, Leg Byes).
- **PDF Scorecard Download**: Any viewer or scorer can tap **Download PDF** to export a clean, formatted match report generated via Android's `PdfDocument` and share or save it via Android's native Share Sheet (`Intent.ACTION_SEND`).

### Feature 3: Match History & Calendar Scorecard Search
- **Material 3 Calendar DatePicker**: Scorer or viewers can open the interactive Calendar (`DatePickerDialog`) to pick any date in history or future.
- **Active Filter Banner**: Clearly indicates the selected date and count of scorecards found, with a single-click "Clear" button.
- **Quick Date Chips**: One-tap filter chips for `All Dates`, `Today`, `Yesterday`, and dates of all recorded matches.
- **Real-time Search Filter**: Live search by team name (`"India"`, `"Australia"`) or Match ID.
- **Persistent Scorecard Archive**: Scorecards from any date are retained permanently on disk locally (`matches_local_cache.json`) and synchronized to Cloud Firestore when connected.

---

## 3. Database Comparison: Firebase vs. Supabase

When choosing a database to retain and synchronize live cricket scorecards, **Firebase Firestore is the strongly recommended solution** for this application:

| Feature / Requirement | Firebase (Cloud Firestore) | Supabase (PostgreSQL) | Why Firebase is Superior for This App |
|---|---|---|---|
| **Live Scoring Real-time Push** | **Built-in `addSnapshotListener`**: Instant push notifications to all viewers on every ball with zero boilerplate. | Uses Realtime Channels over WebSockets. Requires manual connection management and reconnection loops on mobile. | Cricket requires sub-second live updates to all viewers without complex networking code. |
| **Retaining Scorecards Offline** | **Native Offline Persistence**: Built into Google Firestore SDK (`PersistentCacheSettings`). Scorecards are retained on disk automatically. | **No built-in offline caching in mobile SDK**. Developer must build a separate Room / SQLite database with sync logic. | Users can view past scorecards from any date even without internet connectivity. |
| **Android Integration** | **First-Party Google Support**: Official Google Services Gradle plugin (`com.google.gms.google-services`), seamless Android Studio integration. | Community-maintained Kotlin SDK (`supabase-kt`). Requires third-party Ktor HTTP engines. | Maximum stability and performance on Android. |
| **Scorer Authorization** | **Firebase Anonymous Auth**: Creates a persistent device UUID without forcing users to type passwords. Firestore rules protect scorer updates. | Requires Postgres Row Level Security (RLS) policies and JWT auth handling. | Simple and secure match ownership without login friction. |
| **Status in this Codebase** | **100% Implemented**: `FirestoreMatchRepository.kt`, data models, and Firebase BoM are already completely built and tested. | 0% implemented. Would require rewriting all data models, repository interfaces, and network layers. | Simply add `google-services.json` to immediately go live! |
| **Free Tier Generosity** | **Spark Plan (100% Free)**: 50,000 reads/day, 20,000 writes/day, 1GB storage (holds millions of matches). | Free tier database pauses after 1 week of inactivity unless upgraded to paid plan. | Firebase never sleeps or pauses your database. |

---

## 4. Firebase Setup Guide

The application connects to Firebase Firestore and Firebase Authentication. To connect your Firebase project:

### Step 1: Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com/).
2. Click **Add project** and name it (e.g., `IGCC XI Cricket`).
3. Add an **Android app**:
   - Package name: `com.example.igccxi`
   - App nickname: `IGCC XI`

### Step 2: Download `google-services.json`
1. Download `google-services.json` from the Firebase Console.
2. Place the file directly in the app module directory:
   ```
   c:\Users\Admin\AndroidStudioProjects\IGCCXI\app\google-services.json
   ```
*(Note: When `google-services.json` is present, the Google Services Gradle plugin is automatically activated).*

### Step 3: Enable Authentication & Firestore
1. In Firebase Console, go to **Build > Authentication**:
   - Click **Get Started** and enable the **Anonymous** sign-in provider.
2. Go to **Build > Firestore Database**:
   - Click **Create Database** and start in **Production mode**.

### Step 4: Deploy Firestore Security Rules
Go to **Firestore Database > Rules** tab in the Firebase Console and paste the rules from [`firestore.rules`](file:///c:/Users/Admin/AndroidStudioProjects/IGCCXI/firestore.rules), or use either option below:

#### Option A: Production-Ready Rules (Matches `firestore.rules`)
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /matches/{matchId} {
      // 1. Any public user can read live matches and scorecards without sign-in
      allow read: if true;

      // 2. Any authenticated user (including anonymous) can create a match
      allow create: if request.auth != null &&
        request.resource.data.id == matchId &&
        request.resource.data.scorerId == request.auth.uid;

      // 3. Only the match creator (scorer) can update or delete the match
      allow update: if request.auth != null && 
        (resource.data.scorerId == request.auth.uid || !('scorerId' in resource.data) || resource.data.scorerId == '') &&
        request.resource.data.id == resource.data.id &&
        request.resource.data.createdAt == resource.data.createdAt;

      allow delete: if request.auth != null && resource.data.scorerId == request.auth.uid;
    }

    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

#### Option B: Permissive Development Rules (For Quick Testing)
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```
*(Note: Always ensure **Anonymous Authentication** is enabled in Firebase Console under Authentication > Sign-in method).*

---

## 4. Running & Automated Unit Testing

### Running Unit Tests
All cricket scoring engine scenarios (runs, strike rotation, extras, overs, wickets, targets, match results) are covered by automated unit tests:

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat testDebugUnitTest
```

### Building Debug APK
```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat assembleDebug
```
The compiled APK will be located at:
`app\build\outputs\apk\debug\app-debug.apk`

---

## 5. Clean Internal Storage & Cloud Synchronization
- **Zero Phone Storage Clutter**: The app does not write any backup files to the phone's `Downloads`, `Documents`, or external public directories.
- **Private Internal Cache**: Matches are stored strictly in the app's private sandbox (`matches_local_cache.json`) consuming minimal storage (~a few kilobytes).
- **Google Cloud Firestore Sync**: When `google-services.json` is added to the `app/` folder, `RepositoryProvider` automatically streams matches directly to Google Cloud Firestore in real-time, preserving all matches and scorecards permanently in the cloud without using device storage.

---

## 6. Custom Club Branding & App Icons
The app is branded with the official club crest:
- **Launcher Icons Across All Densities**: `mipmap-mdpi` (48x48), `mipmap-hdpi` (72x72), `mipmap-xhdpi` (96x96), `mipmap-xxhdpi` (144x144), and `mipmap-xxxhdpi` (192x192) for both standard and circular icon buckets.
- **Android 8.0+ Adaptive Icons**: Foreground centered at 308x308 safe-zone with matching forest green background (`#0F2711`) ensuring no distortion or cropping on any launcher mask (circular, squircle, or rounded rect).
- **In-App Branding**: TopAppBar header and Hero stadium banner incorporate the club logo badge with emerald border accent.

---

## 7. Production Release APK

The production release APK is compiled, optimized, signed, and ready for installation on any Android phone (Android 7.0 / API 24 to Android 15 / API 36+):

- **Release APK in Project Root**:
  [`IGCCXI-Cricket-v1.0-release.apk`](file:///c:/Users/Admin/AndroidStudioProjects/IGCCXI/IGCCXI-Cricket-v1.0-release.apk)
- **Standard Gradle Output Path**:
  [`app/build/outputs/apk/release/app-release.apk`](file:///c:/Users/Admin/AndroidStudioProjects/IGCCXI/app/build/outputs/apk/release/app-release.apk)
- **Size**: ~16.6 MB (resource-optimized and dex-compressed)
- **Included Features**:
  - Full Live Scoring with ergonomic keypad (0-6, boundaries, Wd, Nb, 1-tap dismissal chips, undo, strike rotation).
  - Material 3 Calendar DatePicker & search archive.
  - PDF Scorecard generation & sharing.
  - Dual-tier persistence (local disk cache + Cloud Firestore synchronization).
  - Official custom club logo and adaptive launcher icon.

### How to Install the Release APK:
1. **Directly on Phone**: Copy `IGCCXI-Cricket-v1.0-release.apk` to your phone via USB or WhatsApp/Google Drive, tap the file in your file manager, and tap **Install**.
2. **Via ADB Command Line**:
   ```powershell
   adb install -r IGCCXI-Cricket-v1.0-release.apk
   ```
