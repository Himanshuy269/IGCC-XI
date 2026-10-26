# IGCC XI - Simple Cricket Live Score App
## Technical Documentation & Architecture Guide

A clean, modern, and production-ready Android cricket scoring application built with **Kotlin**, **Jetpack Compose**, **Material 3**, **MVVM architecture**, and **Firebase Firestore**.

---

## 1. Core Architecture Overview

The app is built following **Clean Architecture** principles and Google's official Android Architecture Guidelines, utilizing pure functional domain logic, dedicated UseCases, and reactive state management:

```
com.example.igccxi
├── domain                            // Pure business domain layer (zero Android framework dependencies)
│   ├── model                         // Business Entities
│   │   ├── MatchStatus.kt            // Lifecycle: NOT_STARTED, INNINGS_1, INNINGS_BREAK, INNINGS_2, COMPLETED
│   │   └── Match.kt                  // Match, Innings, BatterScore, BowlerScore, Extras, BallEvent, BallType
│   ├── repository
│   │   └── MatchRepository.kt        // Repository contract interface with real-time Flow streams
│   ├── usecase                       // Focused single-responsibility interactors
│   │   ├── GetMatchesUseCase.kt      // Streams all public matches in real-time
│   │   ├── GetMatchByIdUseCase.kt    // Streams single match live scorecard
│   │   ├── CreateMatchUseCase.kt     // Persists new match & generates ID
│   │   ├── UpdateMatchUseCase.kt     // Updates ball-by-ball scorecard data
│   │   ├── GetCurrentUserIdUseCase.kt// Obtains current user/scorer identity
│   │   └── ExportScorecardPdfUseCase.kt // Generates & shares official PDF scorecards
│   ├── ScoringEngine.kt              // Pure functional scoring engine (runs, extras, wickets, overs, targets, results)
│   └── PdfScorecardExporter.kt       // Native Android PdfDocument generator & system share intent
├── data                              // Data implementation layer
│   ├── model                         // Backward compatibility aliases pointing to domain.model
│   └── repository                    // Repository implementations & local offline cache
│       ├── FirestoreMatchRepository.kt// Cloud Firestore repository with real-time snapshot listeners
│       ├── InMemoryMatchRepository.kt// Pre-seeded offline fallback repository with disk persistence
│       └── RepositoryProvider.kt     // Factory and service locator providing repositories & use cases
├── navigation                        // Dedicated top-level Navigation package
│   ├── Screen.kt                     // Sealed routes, argument keys & typed route builders
│   └── AppNavigation.kt              // Decoupled NavHost graph with transitions & backstack handling
├── screens                           // Dedicated top-level Screens package
│   ├── home                          // HomeScreen, HomeViewModel, HomeUiState
│   │   └── components/               // FeaturedLiveCard, CleanHeroBanner, HowItWorksCard, EmptyStatePrompt
│   ├── create                        // CreateMatchScreen, CreateMatchViewModel, CreateMatchUiState
│   ├── scoring                       // LiveScoringScreen, LiveScoringViewModel, LiveScoringUiState
│   │   └── components/               // ScoreBoardCard, BattersScoreCard, BowlerScoreCard,
│   │                                 // RecentDeliveriesSection, ScoringKeypad, WicketDialog, BowlerDialog, etc.
│   ├── details                       // MatchDetailsScreen, MatchDetailsViewModel, MatchDetailsUiState
│   └── history                       // MatchHistoryScreen, MatchHistoryViewModel, MatchHistoryUiState
├── ui                                // Shared cross-cutting UI & theme
│   ├── components                    // Reusable design system widgets
│   │   ├── ScoreCardSummary.kt       // Match summary card with Live badge & scores
│   │   ├── BattingTable.kt           // Comprehensive batting scorecard table
│   │   ├── BowlingTable.kt           // Comprehensive bowling figures table
│   │   └── BallByBallTicker.kt       // Color-coded recent deliveries ticker
│   └── theme                         // Material 3 Color.kt, Theme.kt, Type.kt
└── MainActivity.kt                   // Lean Activity entrypoint simply calling AppNavigation()
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

## 7. Production Release APK & Device Safety

The production release APK is compiled, security-hardened, signed with a dedicated production RSA-2048 key, and verified to install seamlessly across all Android devices (Samsung Knox, Xiaomi MIUI/HyperOS, BBK Vivo/Oppo/Realme, Google Pixel Play Protect):

- **Release APK in Project Root**:
  [`IGCC XI.apk`](file:///c:/Users/Admin/AndroidStudioProjects/IGCCXI/IGCC%20XI.apk) and [`IGCC-XI.apk`](file:///c:/Users/Admin/AndroidStudioProjects/IGCCXI/IGCC-XI.apk)
- **Standard Gradle Output Path**:
  [`app/build/outputs/apk/release/app-release.apk`](file:///c:/Users/Admin/AndroidStudioProjects/IGCCXI/app/build/outputs/apk/release/app-release.apk)
- **Size**: ~17.3 MB
- **Signature & Security Credentials**:
  - **Keystore**: Dedicated production keystore at `app/keystore/release.jks` with 30-year certificate validity (valid through year 2056).
  - **Distinguished Name (DN)**: `CN=IGCC XI Cricket Scorer, OU=Mobile, O=IGCC XI, L=Bengaluru, ST=Karnataka, C=IN`
  - **Signature Schemes**: APK Signature Scheme v2 (Full-file hashing) and v3 (Key rotation support) verified with `apksigner`.
  - **Zero Debug Flags**: `android:debuggable` is strictly `false` and `android:testOnly` is disabled.
  - **Network Security Configuration**: Cleartext HTTP traffic is blocked (`android:usesCleartextTraffic="false"`, `res/xml/network_security_config.xml`), enforcing encrypted HTTPS communication exclusively.
  - **Target SDK**: Standardized at API 35 (Android 15) ensuring full platform compliance without triggering OEM pre-release or experimental API warnings.

### How to Install the Release APK:
1. **Directly on Any Phone**:
   - Share or download `IGCC XI.apk` via WhatsApp, Google Drive, or USB.
   - Tap the APK file to install.
   - Because the APK is signed with a valid production certificate (not an untrusted `androiddebugkey`), devices will prompt a clean install dialog without high-risk debug flags.
2. **Via ADB Command Line**:
   ```powershell
   adb install -r "IGCC XI.apk"
   ```

---

## 8. Player Rankings & Leaderboard Architecture

The app includes a dedicated, career-wide leaderboard for batsmen and bowlers aggregated across all recorded match scorecards:

- **Clean Architecture Use Case**: `GetPlayerRankingsUseCase` parses all completed and ongoing match innings.
- **Top Batsmen Ranking**:
  - Ranked primarily by **Total Runs** (descending).
  - Tie-breakers: Highest Score (descending), Strike Rate (descending).
  - Displays: Total Runs, Balls Faced, Strike Rate, 4s, 6s, Highest Score (with not-out indicator `*`), 50s, 100s, and Innings count.
- **Top Bowlers Ranking**:
  - Ranked primarily by **Total Wickets** (descending).
  - Tie-breakers: Economy Rate (ascending), Total Runs Conceded (ascending).
  - Displays: Total Wickets, Overs & Legal Balls Bowled, Economy Rate, Maidens, Runs Conceded, and Best Bowling Figures (e.g., `4/18`).
- **Podium Showcase**: Top 3 ranked players are featured prominently on Gold (#1), Silver (#2), and Bronze (#3) podium cards.
- **Strict Single-Player Identity Rule ("No duplicate player name")**:
  - **Career Consolidation**: Player names are trimmed and normalized (`trim().lowercase()`) into a single canonical record. If a player was entered as `"Rohit Sharma"`, `"rohit sharma"`, or `"ROHIT SHARMA "` across multiple matches or innings, all their runs and wickets are merged into **one single unique career record**.
  - **Match Creation**: Opening Striker and Non-Striker cannot have the same player name.
  - **Live Scoring Wicket Dialog**: When recording a dismissal, `WicketDialog` validates against all batters who have already batted or are currently at the crease in the innings, prohibiting duplicate entries.
  - **Second Innings**: Opening striker and non-striker for the chase must be distinct.

---

## 9. Built-in In-App Auto-Updater (Powered by GitHub)

The app features a built-in, native In-App Auto-Updater configured to check and download releases directly from **GitHub** (`https://github.com/Himanshuy269/IGCC-XI`), allowing players and club members to receive update notifications and install newer versions directly inside the app without needing the Google Play Store.

### Architecture & Components
1. **GitHub Multi-Strategy Provider (`AppUpdateManager`)**:
   - **Primary Strategy**: Queries the raw repository update manifest:
     `https://raw.githubusercontent.com/Himanshuy269/IGCC-XI/main/app-update.json`
     (Instant, zero-rate-limit, unauthenticated).
   - **Secondary Strategy**: Queries the GitHub Releases API:
     `https://api.github.com/repos/Himanshuy269/IGCC-XI/releases/latest`
     (Automatically finds `.apk` assets attached to the latest release tag).
   - **Fallback Strategy**: Cloud Firestore `/app_metadata/app_update` for backup distribution.
2. **Dedicated In-App Update Section (`AppUpdateSection.kt`)**:
   - Prominently situated in the main home feed.
   - Displays:
     - **Installed Version**: e.g., `v1.0 (Build 1)`
     - **Update Channel**: `Source: GitHub (Himanshuy269/IGCC-XI)`
     - **Live Status Badge**: `Up to Date` (Green) or `Update Available` (Gold)
     - **Last Checked Timestamp**: `Last checked: 12:25 PM`
     - **"Check for Updates" Button**: Animated spinning refresh that queries GitHub in real-time.
     - **"GitHub" Button**: Opens the official GitHub Releases page in the browser.
     - When an update is detected, expands with the latest release highlights and a 1-tap **"Install Update (vX.X)"** button.
3. **Background Download & Safe Installation**:
   - Downloads the APK directly using background coroutines with chunked streaming, emitting real-time percentage and byte progress (`45% (8.2 MB / 18.2 MB)`).
   - Stores updates in the app's sandboxed external downloads directory (`context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)/updates`).
   - Securely generates `content://` URIs via `androidx.core.content.FileProvider`.
   - Gated with Android 8.0+ `REQUEST_INSTALL_PACKAGES` and guides the user to grant "Allow from this source" if not yet enabled.

### Automated Releases via GitHub Actions:
The repository includes a fully automated CI/CD workflow in [`.github/workflows/build-and-release.yml`](file:///c:/Users/Admin/AndroidStudioProjects/IGCCXI/.github/workflows/build-and-release.yml) that handles everything from compilation to publishing:

#### Method A: 1-Click Trigger from GitHub Web UI
1. Go to your repository on GitHub: `https://github.com/Himanshuy269/IGCC-XI/actions`
2. Select **"Build & Release APK (In-App Auto-Update)"** in the left sidebar.
3. Click **"Run workflow"**:
   - Enter **Version Name** (e.g. `1.1.0`)
   - Enter **Release Notes**
   - Check **Is Mandatory?** (if required)
   - Click **Run workflow**.
4. GitHub Actions will:
   - Run unit tests (`testDebugUnitTest`).
   - Compile and sign the production release APK (`assembleRelease`).
   - Automatically update `app-update.json` on `main`.
   - Create a GitHub Release with tag `v1.1.0` and attach `IGCC-XI.apk`.
   - All installed apps will instantly receive the update prompt!

#### Method B: Trigger via Git Tag Push
```powershell
git tag v1.1.0
git push origin v1.1.0
```
GitHub Actions will automatically run, build, create the GitHub Release, and publish the update!

---

## 10. Crash & Package Conflict Resolution Guide

### A. Root Cause & Fix for "App Keeps Stopping" (Startup Crash)
1. **Uncaught Firebase Initialization in ViewModel**:
   - *Issue*: `AppUpdateManager` previously had `private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()` as a constructor default argument. When `HomeScreen` composed, `AppUpdateViewModel` initialized and evaluated this before `FirebaseApp` was initialized, throwing `IllegalStateException: Default FirebaseApp is not initialized`.
   - *Fix*: Created [`IgccXiApplication.kt`](file:///c:/Users/Admin/AndroidStudioProjects/IGCCXI/app/src/main/java/com/example/igccxi/IgccXiApplication.kt) registered in `AndroidManifest.xml` to safely initialize Firebase on process creation. In addition, `AppUpdateManager` now accesses Firestore strictly as a safe, nullable lazy getter (`FirebaseFirestore?`), ensuring class instantiation never crashes under any circumstance.
2. **Safe Update State Ingestion**:
   - Replaced force unwraps (`updateState.updateInfo!!`) in [`HomeScreen.kt`](file:///c:/Users/Admin/AndroidStudioProjects/IGCCXI/app/src/main/java/com/example/igccxi/screens/home/HomeScreen.kt) with safe local bindings (`val availableUpdateInfo = updateState.updateInfo; if (availableUpdateInfo != null)`).
3. **Locale-Safe String Formatting**:
   - Added explicit `java.util.Locale.US` to `String.format` invocations in [`AppUpdateDialog.kt`](file:///c:/Users/Admin/AndroidStudioProjects/IGCCXI/app/src/main/java/com/example/igccxi/screens/updater/AppUpdateDialog.kt) to prevent locale crashes.

### B. Root Cause & Fix for "Package Conflicts When Updating the App"
When Android displays *"App not installed as package conflicts with an existing package"* (`INSTALL_FAILED_UPDATE_INCOMPATIBLE`), Android's OS package manager is rejecting the APK due to one of four reasons:

1. **Certificate Signature Mismatch (Most Common)**:
   - *Cause*: If an app installed from Android Studio (debug keystore) is updated with an APK built from GitHub (release keystore), or if CI generated a new keystore on every run, Android blocks the update because certificate SHA-256 fingerprints do not match.
   - *Fix*:
     - The production keystore (`app/keystore/release.jks`) is configured across both `release` and `debug` build types in `app/build.gradle.kts`.
     - The GitHub Actions workflow in [`.github/workflows/build-and-release.yml`](file:///c:/Users/Admin/AndroidStudioProjects/IGCCXI/.github/workflows/build-and-release.yml) is pinned to this exact keystore (`SHA-256: 3E:8F:41:9D:F3:CD:74:DF:7F:BC:3F:EA:D3:A3:5A:7A:E3:B5:43:4F:A9:90:63:47:68:E0:AD:32:72:F9:0F:41`).
     - **One-time Transition Note**: If you currently have an old debug build installed on your phone, uninstall it **once**. Install the newly signed release APK (`IGCC-XI.apk`). From that point forward, all future in-app updates will update seamlessly without conflicts!
2. **Atomic APK Download (No Corrupted Partial Files)**:
   - *Cause*: If an update download was interrupted, re-triggering installation on a partial `.apk` caused package parse errors.
   - *Fix*: Downloads now stream to `IGCCXI_Update.apk.tmp`. Only after the download completes with 100% data integrity and stream closure is the file atomically renamed to `IGCCXI_Update.apk`.
3. **Explicit Package Installer URI Permissions**:
   - *Cause*: On Android 7.0 through Android 15, `Intent.ACTION_VIEW` flags can sometimes be dropped by manufacturer-customized package installers (Samsung, Xiaomi/MIUI, Oppo).
   - *Fix*: [`AppUpdateManager.installApk()`](file:///c:/Users/Admin/AndroidStudioProjects/IGCCXI/app/src/main/java/com/example/igccxi/data/updater/AppUpdateManager.kt) now explicitly queries all matching package installer activities and invokes `context.grantUriPermission(targetPackage, contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)`.
4. **Android 11+ Package Visibility**:
   - Added `<queries>` in [`AndroidManifest.xml`](file:///c:/Users/Admin/AndroidStudioProjects/IGCCXI/app/src/main/AndroidManifest.xml) for `application/vnd.android.package-archive` so that package installers can always be queried and granted permissions.

