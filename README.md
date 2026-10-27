# 🏏 IGCC XI — Cricket Live Scoring & Analytics App

[![Version](https://img.shields.io/badge/version-1.3.0-blue.svg)](https://github.com/Himanshuy269/IGCC-XI/releases)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-purple.svg)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Platform-Android%207.0%2B%20(API%2024%2B)-green.svg)](https://developer.android.com)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-brightgreen.svg)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Database-Firebase%20Firestore-orange.svg)](https://firebase.google.com)

A modern, fast, and production-ready Android cricket scoring app tailored for cricket clubs, tournaments, and gully cricket matches. Built from scratch with **Kotlin**, **Jetpack Compose**, **Material 3**, **Clean Architecture**, and **Firebase Firestore** for real-time cloud sync.

---

## ✨ Features

- **⚡ Live Ball-by-Ball Scoring**: Record runs (0–6), wickets, and extras (wides, no-balls with MCC Free-Hit rule, byes, leg-byes) with undo support.
- **🏏 Gully Cricket Friendly**:
  - **Custom Team Sizes**: Play with any number of players per side (e.g., 5, 7, 11).
  - **Last Man Standing**: In an $X$-player team, all $X$ wickets must fall; lone batters can continue batting.
- **🧭 Bottom Navigation**: 
  - **Home**: Club stats overview, featured live match, and quick action cards.
  - **Scorecards**: Full match archive with live filter chips (All / Live / Finished), date picker filter, and search.
  - **Leaderboard & Rankings**: Comprehensive player rankings for runs, wickets, and club achievements.
- **👥 Active Users Counter**: Live pill counter displaying active viewers currently connected via Firestore presence.
- **📄 PDF Scorecard Export**: Export and share professional match summary PDFs directly from the app.
- **📲 Direct In-App Updates**: Seamless update checks against GitHub Releases with background download and installation.
- **📱 Clean Edge-to-Edge Design**: Full edge-to-edge Material 3 layout flush against system notification and navigation bars.

---

## 🛠️ Architecture & Tech Stack

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 design system
- **Architecture**: Clean Architecture (Domain, Data, UI / Presentation) + MVVM pattern
- **Concurrency & Reactivity**: Kotlin Coroutines & `StateFlow`
- **Cloud Backend**: Google Cloud Firestore (real-time listeners and offline caching)
- **Navigation**: Jetpack Navigation Compose with animated transitions
- **Testing**: Comprehensive unit test suite (35 unit tests covering scoring engine, rankings, and use cases)
- **CI/CD**: GitHub Actions automated release pipeline for signed APK artifacts

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug / Meerkat or later
- JDK 17 or 21 (bundled JBR recommended)
- Android SDK 35 (minSdk 24)

### Clone & Build
```bash
# Clone the repository
git clone https://github.com/Himanshuy269/IGCC-XI.git
cd IGCC-XI

# Run unit tests
./gradlew testDebugUnitTest

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```

---

## 📦 Downloads

Grab the latest signed APK directly from the [GitHub Releases](https://github.com/Himanshuy269/IGCC-XI/releases) page.

---

## 📄 License
This project is maintained for the IGCC cricket club.
