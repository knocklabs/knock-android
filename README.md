# Official Knock Android SDK

A client-side Kotlin library to integrate Knock into Android applications.

[![GitHub Release](https://img.shields.io/github/v/release/knocklabs/knock-android?style=flat)](https://github.com/knocklabs/knock-android/releases/latest)
[![Jitpack compatible](https://img.shields.io/badge/Jitpack-compatible)](https://jitpack.io/#knocklabs/knock-android)
![min Android SDK version](https://img.shields.io/badge/min%20sdk%20version-26-orange)
[![GitHub license](https://img.shields.io/badge/license-MIT-lightgrey.svg?style=flat)](https://github.com/knocklabs/knock-android/blob/main/LICENSE)

---

Knock is a flexible, reliable notifications infrastructure that's built to scale with you. Use our Android SDK to engage users with in-app feeds, setup push notifications, and manage notification preferences.

---

## Documentation

See the [documentation](https://docs.knock.app/sdks/android/overview) for full documentation.

## Migrations

See the [Migration Guide](https://github.com/knocklabs/knock-android/blob/main/MIGRATIONS.md) if upgrading from a previous version,
and the [Changelog](https://github.com/knocklabs/knock-android/blob/main/CHANGELOG.md) for what changed in each release.




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

`<VERSION>` is the release tag, e.g. `v1.2.6`. The SDK ships its own R8/ProGuard rules, so no extra configuration
is needed for minified builds.

### 3. Init the SDK and FeedManager

```kotlin
// Step 1: Early initialization in Application.onCreate(), so Knock is also set up when FCM starts
// your app's process in the background to deliver a push.
Knock.setup(context = applicationContext, publishableKey = "your-pk", pushChannelId = "fcm-push-channel-id")

// Step 2: Sign in the user. Ideal timing: as soon as you have the userId.
Knock.shared.signIn(userId = "userId", userToken = "userToken")
```

## Example app

`knock-example-app` reads its Knock credentials from `local.properties` (not committed):

```properties
knock.publishableKey=pk_test_...
knock.userId=your-user-id
knock.inAppChannelId=your-in-app-feed-channel-id
knock.pushChannelId=your-fcm-channel-id
```

Push notifications are optional: add your Firebase `google-services.json` to `knock-example-app/` to enable them.

