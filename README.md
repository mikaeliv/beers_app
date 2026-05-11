# Beers

Beers is a Kotlin Multiplatform application for keeping a personal beer journal. The app lets a user sign in, add beers with photos, ABV, comments and ratings, browse the collection, open beer details, manage profile/settings and keep local data synchronized with a backend.

The project is built around Compose Multiplatform and an offline-first data flow: beers are stored locally with SQLDelight, marked with sync statuses, and synchronized with the server when the network is available.

## Features

- Email/password authentication with access and refresh token storage.
- Beer list, detail screen and add-beer flow.
- Photo support through platform-specific camera/image picker implementations.
- Rating, ABV and comment fields for each beer.
- Offline-first local storage with pending create/delete sync states.
- Pull/push synchronization with a backend API.
- Offline banner based on network connectivity state.
- Profile and settings screens.
- Shared UI components and theme in a dedicated design-system module.

## Tech Stack

- Kotlin Multiplatform
- Compose Multiplatform
- Decompose for navigation and component lifecycle
- SQLDelight for local persistence
- Ktor Client for networking
- kotlinx.serialization for JSON
- Kotlin Coroutines and Flow
- AndroidX Camera on Android
- Gradle version catalogs

## Project Structure

```text
.
├── composeApp/        # Application entry point and root navigation for Android, Desktop, iOS and JS
├── composeDS/         # Shared design system: theme, icons, reusable Compose components
├── core/              # Domain models and shared contracts
├── data/              # SQLDelight database, drivers and beer repository
├── network/           # Ktor clients, auth API, beer API, token storage, connectivity
├── feature-auth/      # Login and registration feature
├── feature-list/      # Beer list feature
├── feature-detail/    # Beer detail feature
├── feature-add/       # Add beer feature
├── feature-camera/    # Camera preview and availability abstractions
├── feature-profile/   # Profile screen and logout flow
├── feature-settings/  # App settings screen
└── iosApp/            # Native iOS SwiftUI host app
```

## Requirements

- JDK 11 or newer
- Android Studio or IntelliJ IDEA with Kotlin Multiplatform support
- Android SDK for Android builds
- Xcode for iOS builds
- A backend API running on port `8085`

Default API base URLs:

- Android emulator: `http://10.0.2.2:8085`
- Desktop, iOS simulator and Web: `http://localhost:8085`

## Run

### Desktop

```shell
./gradlew :composeApp:run
```

### Android

```shell
./gradlew :composeApp:assembleDebug
```

Then install or run the generated debug build from Android Studio.

### Web JS

```shell
./gradlew :composeApp:jsBrowserDevelopmentRun
```

Note: the JS target is present, but the SQLDelight JS database driver is currently not wired in; the JS database factory is a placeholder.

### iOS

Open `iosApp` in Xcode and run the app from there.

Gradle iOS targets in `composeApp` are opt-in so that `./gradlew build` can run on machines without Xcode:

```shell
./gradlew :composeApp:build -PenableIos=true
```

## Test

Run all available tests:

```shell
./gradlew test
```

Run tests for a specific module:

```shell
./gradlew :data:jvmTest
./gradlew :network:jvmTest
./gradlew :composeApp:jvmTest
```

## Build

Build the whole project:

```shell
./gradlew build
```

Build desktop distributions:

```shell
./gradlew :composeApp:packageDistributionForCurrentOS
```

The desktop module is configured to produce DMG, MSI and DEB packages depending on the current host OS.

##