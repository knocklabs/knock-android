# Knock Kotlin client-side SDK

A client-side Kotlin library to integrate Knock into Android applications.

## Documentation

See the [documentation](https://docs.knock.app/in-app-ui/android/reference) for usage examples.

## Installation

Add the dependency to your `build.grandle` file as follows:

```java
dependencies {
    implementation 'app.knock.client:knock:VERSION'
}
```

Or to your `maven.xml` file:

```xml
<dependencies>
    <!-- more dependencies here -->
    <dependency>
        <groupId>app.knock.client</groupId>
        <artifactId>knock</artifactId>
        <version>VERSION</version>
    </dependency>
    <!-- more dependencies here -->
</dependencies>
```

## Examples

### Init the SDK and FeedManager

```kotlin
val publishableKey = "..."
val userId = "..."
val inAppChannelId = "..."

val knockClient = Knock(publishableKey = publishableKey, userId = userId)
val feedManager = FeedManager(client = knockClient, feedId = inAppChannelId)

```

## Connect to a feed and get new-messages events

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
