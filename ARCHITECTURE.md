# IGCC XI — Architecture & Engineering Guide 🏏

Welcome to the **IGCC XI** codebase! This guide is written specifically to help any developer—especially junior engineers—understand how the application is built, why certain patterns were chosen, and how Kotlin Coroutines and Reactive Programming keep the app running silky smooth at 60/120 frames per second.

---

## 1. High-Level Architecture Overview

IGCC XI adheres strictly to **Clean Architecture** and Google's recommended **Modern Android Architecture (MVI / MVVM)** with **Unidirectional Data Flow (UDF)**.

```
       ┌────────────────────────────────────────────────────────┐
       │             PRESENTATION LAYER (UI)                    │
       │   Jetpack Compose Screens & Interactive Scorecards     │
       └─────────────────────────▲──────────────────────────────┘
                                 │ Observes StateFlow (UiState)
                                 │ Sends UI Events (clicks, inputs)
       ┌─────────────────────────▼──────────────────────────────┐
       │               VIEWMODEL LAYER (MVVM)                   │
       │    Exposes StateFlow, combines streams, handles state   │
       │     e.g., HomeViewModel, LiveScoringViewModel, etc.     │
       └─────────────────────────▲──────────────────────────────┘
                                 │ Calls Use Cases
                                 │ Observes Domain Flows
       ┌─────────────────────────▼──────────────────────────────┐
       │             DOMAIN LAYER (Business Logic)              │
       │  • Pure Kotlin Use Cases (e.g. GetPlayerRankingsUseCase)│
       │  • ScoringEngine: Gully cricket rules & ball scoring   │
       │  • Domain Models: Match, Innings, BatterScore, Bowler  │
       └─────────────────────────▲──────────────────────────────┘
                                 │ Repository Interfaces
                                 │ (MatchRepository, PlayerProfileRepo)
       ┌─────────────────────────▼──────────────────────────────┐
       │                 DATA LAYER (Data Sources)              │
       │  • Cloud Firestore (FirestoreMatchRepository)          │
       │  • Local JSON & File Storage (LocalPlayerProfileRepo)  │
       │  • Firebase Presence (FirestorePresenceRepository)     │
       └────────────────────────────────────────────────────────┘
```

### Why This Separation Matters:
1. **Separation of Concerns**: UI code only cares about drawing pixels and responding to clicks. Business rules (like how cricket runs are scored or how rankings are sorted) never touch Android UI widgets.
2. **Testability**: Pure business logic (e.g. `ScoringEngine` and `GetPlayerRankingsUseCase.calculateRankings`) does not depend on Android SDK classes and can be tested instantaneously with fast JVM unit tests.
3. **Maintainability**: If we replace Firebase with SQLite/Room or REST APIs tomorrow, we only change the `data` layer. Not a single line of Compose UI changes!

---

## 2. Reactive Programming & Kotlin Coroutines

Reactive programming means our application **reacts** to data changes automatically instead of constantly asking for updates.

### A. Kotlin `Flow` vs `StateFlow`
- **`Flow`**: A cold stream of data. It only begins producing values when a subscriber begins collecting it. Used in Repositories and Use Cases.
- **`StateFlow`**: A hot state holder that always has a current value (`.value`). It emits new states whenever data changes. Perfect for ViewModels exposing `UiState` to Jetpack Compose.

### B. Coroutine Dispatchers — Threading Made Simple
Android devices have one **Main Thread** that renders frames every 16ms (for 60Hz) or 8ms (for 120Hz). If you do heavy math, file reading, or network calls on the Main thread, the app will freeze or drop frames (called *jank* or *ANR*).

| Dispatcher | What it is for | Examples in IGCC XI |
| :--- | :--- | :--- |
| **`Dispatchers.Main`** | Updating UI, handling button clicks | Observing `uiState` in Jetpack Compose |
| **`Dispatchers.IO`** | Disk and Network I/O operations | Reading `player_profiles.json`, Firestore calls, PDF export |
| **`Dispatchers.Default`**| Heavy CPU calculations and data transformations | Calculating player rankings, filtering match history, sorting |

### C. The Magic of `.flowOn()`
When a flow produces or transforms data, `.flowOn(Dispatchers.Default)` specifies the background thread pool for upstream operations:

```kotlin
// In GetPlayerRankingsUseCase.kt:
operator fun invoke(): Flow<RankingsData> {
    return repository.observeMatches()
        .map { matches ->
            calculateRankings(matches) // Heavy math & sorting runs on Dispatchers.Default!
        }
        .flowOn(defaultDispatcher)
}
```

### D. Declarative UI State with `combine` and `stateIn`
Rather than updating states imperatively with mutable variables, ViewModels declare their state as a stream:

```kotlin
// In RankingsViewModel.kt:
val uiState: StateFlow<RankingsUiState> = combine(
    getPlayerRankingsUseCase(),
    _searchQuery,
    _selectedTab,
    _sortByBatsmen,
    _sortByBowlers
) { data, query, tab, sortBatsmen, sortBowlers ->
    // Pure function: transforms inputs into UI state!
    RankingsUiState(...)
}
    .flowOn(Dispatchers.Default) // Keeps UI silky smooth!
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000), // Automatically pauses when screen leaves!
        initialValue = RankingsUiState(isLoading = true)
    )
```

> **Why `SharingStarted.WhileSubscribed(5000)`?**
> When a user rotates the screen or navigates to another app, this stops background collection 5 seconds later. If they rotate back within 5 seconds, collection never stopped, preventing unnecessary re-fetching and conserving battery life.

---

## 3. Real-Time Cloud & Local Data Flow

### 1. `FirestoreMatchRepository` (`callbackFlow`)
Wraps Firebase's snapshot listener callbacks into an asynchronous Kotlin Flow:
- When a document is updated by a scorer, Firestore notifies the listener.
- `trySend(matches)` emits the updated list to subscribers.
- When the UI stops listening, `awaitClose { listenerRegistration.remove() }` safely detaches the listener to prevent memory leaks.

### 2. `FirestorePresenceRepository` (Active Online Users)
- Listens to active user heartbeats.
- Instead of making repetitive network calls every 30 seconds, it caches timestamps in memory and counts active users locally, saving Firestore quota and battery.

### 3. `LocalPlayerProfileRepository` (Player Profiles & Avatars)
- Player profiles, photos, and colors are cached in `filesDir/player_profiles.json`.
- File reads and writes are isolated to `Dispatchers.IO`.
- On startup, `seedFromMatches()` runs with `.first()` to populate profiles from match history in a single batch write.

---

## 4. The Gully Cricket Rules Engine (`ScoringEngine.kt`)

Street and gully cricket matches have unique real-world rules that IGCC XI natively supports:

1. **Common Player Slot (`CMN`)**:
   - In odd-numbered matches (e.g. 5 vs 6 players), a common player plays for **both teams**.
   - They bat in both innings and bowl for both teams.
   - Distinctive blue **`CMN`** badge rendered across all scorecards.
2. **"Boundary Paar" Out**:
   - In many local grounds, hitting the ball over a certain boundary (e.g., neighbor's terrace or road) is declared **Out** ("Boundary Paar").
   - Integrated alongside classic cricket outs: *Bowled*, *Caught*, *Run Out*, *Stumped*, *Hit Wicket*, *LBW*, and *Retired Hurt*.
3. **Single-Batter Rule**:
   - Gully cricket often allows the last remaining batter to continue batting alone ("last man standing").
4. **Dynamic Over Re-configuration**:
   - Overs can be adjusted mid-match if light fades or time is short, automatically recalculating targets and innings breaks.

---

## 5. Codebase Directory Structure

```
app/src/main/java/com/example/igccxi/
├── data/                         # Data Layer
│   ├── model/                    # Data transfer objects (DTOs)
│   ├── repository/               # Repository implementations
│   │   ├── FirestoreMatchRepository.kt
│   │   ├── FirestorePresenceRepository.kt
│   │   ├── LocalPlayerProfileRepository.kt
│   │   └── RepositoryProvider.kt # Service locator / DI entrypoint
│   └── updater/                  # In-app APK updater
│
├── domain/                       # Domain Layer (Pure Business Logic)
│   ├── model/                    # Domain entities (Match, Innings, BatterScore, etc.)
│   ├── repository/               # Repository interfaces
│   ├── usecase/                  # Single-responsibility Use Cases
│   │   ├── GetMatchesUseCase.kt
│   │   ├── GetPlayerRankingsUseCase.kt
│   │   ├── CreateMatchUseCase.kt
│   │   └── ExportScorecardPdfUseCase.kt
│   ├── ScoringEngine.kt          # Gully cricket ball-by-ball scoring engine
│   └── PdfScorecardExporter.kt   # Official scorecard PDF generator
│
├── navigation/                   # Navigation graph & routes
│   └── AppNavigation.kt
│
├── screens/                      # Presentation Layer (Compose Screens + ViewModels)
│   ├── create/                   # Match creation & player selection
│   ├── details/                  # Completed match scorecard & PDF sharing
│   ├── history/                  # Scorecard archive, search & date filtering
│   ├── home/                     # Live matches & dashboard
│   ├── rankings/                 # Batsman & Bowler official leaderboards
│   └── scoring/                  # Real-time ball-by-ball scoring interface
│
└── ui/                           # UI theme, typography, color palette & badges
    ├── CommonPlayerBadge.kt
    └── theme/
```

---

## 6. Junior Developer Checklist for New Features

When adding a new feature or modifying existing code, follow these golden rules:

- [ ] **Thread Safety**: Never perform disk I/O, JSON serialization, or network calls on `Dispatchers.Main`. Wrap them in `withContext(Dispatchers.IO)`.
- [ ] **Background Processing**: Use `.flowOn(Dispatchers.Default)` for any collection processing, filtering, or sorting that could exceed a few milliseconds.
- [ ] **Reactive UI State**: Use `StateFlow` with `SharingStarted.WhileSubscribed(5000)` in ViewModels. Compose screens should observe via `.collectAsStateWithLifecycle()`.
- [ ] **Clean Separation**: ViewModels call UseCases; UseCases call Repositories; Repositories access data sources. Never access Firestore directly inside a Composable function.
- [ ] **Unit Tests**: Whenever modifying `ScoringEngine` or rankings logic, verify that existing tests pass by running:
  ```powershell
  ./gradlew testDebugUnitTest
  ```
- [ ] **Release Verification**: Ensure the release build succeeds without lint or ProGuard errors:
  ```powershell
  ./gradlew assembleRelease
  ```

---
*Happy coding! Keep the scoring accurate and the code clean!* 🏏✨
