# VYRO — Next-Generation Creator & Video Platform

**VYRO** is an enterprise-grade, decentralized video entertainment and creator economy platform built with **Kotlin** and **Jetpack Compose** (Material 3).

---

## 🌟 Key Features

- **Adaptive Responsive Layout (`MainScaffold`)**:
  - **Mobile (< 600dp)**: Custom bottom navigation bar with a centered diamond creator upload action.
  - **Desktop / Tablet (>= 600dp)**: Vertical side `NavigationRail` with full-height fluid layouts.
- **Centralized Navigation Architecture (`VyroNavController` / `NavigationManager`)**:
  - State persistence across configuration changes, screen rotations, and process recreations via `SavedStateHandle` and `SaveableStateHolder`.
- **Feed & Discovery Engine**:
  - Multi-category video browsing (Trending, Tech & AI, Gaming, Music, Design, Culture, Finance).
  - Real-time search, tag filtering, and creator discovery.
- **Full-Viewport Shorts Experience**:
  - Vertical gesture swiping, audio player state management, like/comment sheets, and creator tipping.
- **Creator Studio & Analytics**:
  - Video upload with AI title/description generator, engagement statistics, and channel monetization dashboard.
- **Micro-Economy & Ledger**:
  - Creator tipping, subscription passes (VYRO+ VIP), and balance withdrawal workflows.
- **Independent Infrastructure**:
  - Pluggable video transcoding pipeline (FFmpeg ladder simulations), S3-compatible storage abstraction, edge CDN switching, and native auth adapters.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 2.2+
- **UI Framework**: Jetpack Compose with Material Design 3 (M3)
- **Local Persistence**: Room Database (`@Database`, `@Entity`, `@Dao`, KSP)
- **Architecture**: MVVM + Clean Architecture with Coroutines & StateFlow
- **Image Loading**: Coil Compose
- **Secrets Management**: Secrets Gradle Plugin (`.env` / `.env.example`)
- **Networking**: Retrofit 2 + OkHttp + Moshi

---

## 🚀 Getting Started

### Prerequisites

1. **Android Studio**: Ladybug (2024.2.1) or newer
2. **JDK**: Java 17 or higher
3. **Android SDK**: Compile SDK 36, Min SDK 24

### Environment Variables & Secrets

VYRO uses the **Secrets Gradle Plugin** to securely inject credentials without hardcoding secrets in source code:

1. Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```
2. Set your optional API keys inside `.env`:
   ```properties
   GEMINI_API_KEY=your_actual_gemini_api_key_here
   ```
   *(Note: In Google AI Studio, secrets are automatically injected from the Secrets Panel into `BuildConfig` at build time).*

### Building & Running Locally

```bash
# Clone the repository
git clone <your-github-repo-url>
cd vyro

# Build Debug APK
./gradlew assembleDebug

# Run Unit Tests
./gradlew testDebugUnitTest
```

The compiled APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 📦 Publishing & Deployment

### 1. Connecting to GitHub
- In Google AI Studio, click the **Settings / Export** menu.
- Choose **"Push to GitHub"** or **"Export to GitHub"** to link your repository.
- GitHub Actions CI workflow (`.github/workflows/build.yml`) will automatically verify every push and pull request.

### 2. Generating Release APK / Android App Bundle (AAB)
To create a production build for the Google Play Store:
```bash
./gradlew assembleRelease
# Or for Play Store submission:
./gradlew bundleRelease
```

Ensure your release keystore variables (`KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD`) are configured in your CI/CD environment or release signing configuration.

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.
