package app.knock.example

// Set these in local.properties (knock.publishableKey, knock.userId, knock.inAppChannelId, knock.pushChannelId,
// knock.hostname) so credentials are never committed.
object Utils {
    val publishableKey = BuildConfig.KNOCK_PUBLISHABLE_KEY
    val userId = BuildConfig.KNOCK_USER_ID
    val inAppChannelId = BuildConfig.KNOCK_IN_APP_CHANNEL_ID
    val pushChannelId = BuildConfig.KNOCK_PUSH_CHANNEL_ID
    val hostname = BuildConfig.KNOCK_HOSTNAME

    val loggingTag = "KNOCK_EXAMPLE_APP"
}
