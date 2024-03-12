# Official Knock Android SDK

A client-side Kotlin library to integrate Knock into Android applications.

[![GitHub Release](https://img.shields.io/github/v/release/knocklabs/knock-android?style=flat)](https://github.com/knocklabs/knock-swift/releases/latest)
[![Jitpack compatible](https://img.shields.io/badge/Jitpack-compatible)](https://jitpack.io/#knocklabs/knock-android)
![min Android SDK version](https://img.shields.io/badge/min%20Swift%20version-5.3-orange)
[![GitHub license](https://img.shields.io/badge/license-MIT-lightgrey.svg?style=flat)](https://github.com/knocklabs/knock-android/blob/main/LICENSE)

---

Knock is a flexible, reliable notifications infrastructure that's built to scale with you. Use our iOS SDK to engage users with in-app feeds, setup push notifications, and manage notification preferences.

---

## Documentation

See the [documentation](https://docs.knock.app/sdks/android/overview) for full documentation.

## Migrations

See the [Migration Guide](https://github.com/knocklabs/knock-android/blob/main/MIGRATIONS.md) if upgrading from a previous version.




# Installation

### 1. Add Jitpack repository support in your `settings.gradle` file

```gradle
pluginManagement {
    repositories {
        ..
        maven { url 'https://jitpack.io' }
    }
}

dependencyResolutionManagement {
    repositories {
        ..
        maven { url 'https://jitpack.io' }
    }
}
```
### 2. Add the implementation to your app `build.gradle` file

```gradle
dependencies {
    implementation 'com.github.knocklabs:knock-android:<VERSION>'
}
```

### 3. Init the SDK and FeedManager

```kotlin
// Step 1: Early initialization. Ideal place: Application().onCreate() or MainActivity.
Knock.setup(context = "applicationContext", publishableKey = "your-pk", pushChannelId = "apns-channel-id")

// Step 2: Sign in the user. Ideal timing: as soon as you have the userId.
Knock.shared.signIn(userId = "userId", userToken = "userToken")
```