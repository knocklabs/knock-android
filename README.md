# Knock's Kotlin SDK

## Init the SDK and FeedManager

``` kotlin
val publishableKey = "..."
val userId = "..."
val inAppChannelId = "..."

val knockClient = Knock(publishableKey = publishableKey, userId = userId)
val feedManager = FeedManager(client = knockClient, feedId = inAppChannelId)

```

## Connect to a feed and get new-messages events

``` kotlin

val feedOptions = FeedClientOptions(tenant = "team-a", hasTenant = true)
feedManager.connectToFeed(feedOptions)
feedManager.on("new-message") {
    println("new message")
}

```

## Get feed content

``` kotlin

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

## Users

``` kotlin

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
