# Knock Android

A client-side Kotlin library to integrate Knock into Android applications.

# Documentation

See the [documentation](https://docs.knock.app/in-app-ui/android/reference) for usage examples.

# Requirements & Support

<table>
    <thead>
        <tr>
            <th width="880px" align="left">Requirements</th>
            <th width="120px" align="center"></th>
        </tr>
    </thead>
    <tbody>
        <tr width="600px">
            <td align="left">Knock Account</td>
            <td align="center">
                <a href="https://dashboard.knock.app/signup">
                    <code>Sign Up</code>
                </a>
            </td>
        </tr>
        <tr width="600px">
            <td align="left">Minimum Android SDK Version</td>
            <td align="center">
                <code>29</code>
            </td>
        </tr>
    </tbody>
</table>

&emsp;

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
val publishableKey = "<KNOCK_PUBLIC_KEY>"
val userId = "<CURRENT_USER_ID>"
val inAppChannelId = "<IN_APP_CHANNEL_ID"

val knockClient = Knock(publishableKey = publishableKey, userId = userId)
val feedManager = FeedManager(client = knockClient, feedId = inAppChannelId)

```
# Usage

### Connect to a feed and get new-messages events

```kotlin

val feedOptions = FeedClientOptions(tenant = "team-a", hasTenant = true)
feedManager.connectToFeed(feedOptions)
feedManager.on("new-message") {
    println("new message")
}

```

### Get feed content

```kotlin

feedManager.getUserFeedContent(feedOptions) { result ->
    result.fold(
        onSuccess = { feed ->
            println("getUserFeedContent succeeded: $feed")
        },
        onFailure = { e ->
            println("getUserFeedContent failed: $e")
        }
    )
}

```

### Users

```kotlin

// Get a user
knockClient.getUser { result ->
    result.fold(
        onSuccess = { user ->
            println("getUser succeeded: $user")
        },
        onFailure = { e ->
            println("getUser failed: $e")
        }
    )
}

// Update a user (and add extra fields)
user.phoneNumber = "123-456"
user.properties["extra-1"] = 234

knockClient.updateUser(user) { result ->
    result.fold(
        onSuccess = {user ->
            println("updateUser succeeded, user: $user")
        },
        onFailure = { e ->
            println("updateUser failed: $e")
        }
    )
}

```
