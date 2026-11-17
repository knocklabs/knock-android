# Knock Android SDK example app

This example app demonstrates setting up an in-app feed using Knock's Android SDK.

## Setup

1. Open the root of this repository using Android Studio.

2. Open [`Utils.kt`](src/main/java/app/knock/example/Utils.kt) and update the following values:

   - `publishableKey` - set this to a valid [public API key](https://docs.knock.app/developer-tools/api-keys)
   - `userId` - set this to a valid user ID
   - `inAppChannelId` - set this to the channel ID of a valid in-app channel
   - `pushChannelId` (optional) - set this to the channel ID of a valid Firebase Cloud Messaging channel

3. [Build and run](https://developer.android.com/studio/run) the `knock-example-app` app.

