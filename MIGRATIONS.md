# Migration Guide

## Upgrading to Version 1.2.6

1.2.6 is a bug-fix release. A few fixes change types or behavior you may depend on:

- `KnockMessage.workflow` and `Condition.argument` are now nullable, matching the Knock API (broadcast messages have
  no workflow; `exists`/`empty` conditions have no argument). Handle `null` where you read them.
- `KnockMessageStatus` gained `NOT_SENT`, `BOUNCED` and `UNKNOWN`, and `KnockMessageEngagementStatus` gained
  `UNKNOWN` (used for values added to the API after this release). Exhaustive `when` expressions over these enums
  need the new branches or an `else`.
- `PreferenceSet` now includes `channels` and `commercialSubscribed`, `ChannelTypePreferences` includes `http`, and
  `WorkflowPreference` includes `channels`. Keep them when you copy a preference set before calling
  `setUserPreferences` (a `copy(...)` does this automatically).
- The `Knock.signOut(completionHandler)` callback now returns `Unit` (it returned `Void` and couldn't be called from
  Kotlin).
- `FeedManager` follows the signed-in user: `signIn` with a different user or token re-joins the feed as that user
  and `signOut` disconnects it. A `FeedManager` can now be created before `signIn`.
- `Knock.setup()` should be called from `Application.onCreate()`. `KnockMessagingService` and `KnockActivity` no
  longer crash when it wasn't called, but they can't record push engagement without it.

## Upgrading to Version 1.0.0

Version 1.0.0 of our Android SDK introduces significant improvements and modernizations, including the adoption of Coroutine patterns for more concise and readable asynchronous code. While maintaining backward compatibility with completion handlers for all our APIs, we've also introduced several enhancements to optimize and streamline the SDK's usability.

### Key Enhancements:

- **Refined Initialization Process**: We've redesigned the initialization process for the Knock instance, dividing it into two distinct phases. This change offers greater flexibility in integrating our SDK into your projects.

#### Previous Initialization Approach:
```kotlin
val knockClient = Knock(publishableKey = publishableKey, userId = userId)
```

#### New in Version 1.0.0:
```kotlin
// Step 1: Early initialization. Ideal place: Application().onCreate() or MainActivity.
Knock.setup(context = "applicationContext", publishableKey = "your-pk", pushChannelId = "apns-channel-id")

// Step 2: Sign in the user. Ideal timing: as soon as you have the userId.
Knock.shared.signIn(userId = "userId", userToken = "userToken")
```

- **KnockActivity and KnockMessagingService for Simplified Notification Management**: The introduction of `KnockActivity` and `KnockMessagingService` allows for effortless integration of push notification handling and token management, reducing boilerplate code and simplifying implementation.

- **Enhanced User Session Management**: New functionalities to sign users out and unregister device tokens have been added, providing more control over user sessions and device management.

- **Centralized Access with Shared Instance**: The SDK now utilizes a shared instance for the Knock client, facilitating easier access and interaction within your app's codebase.