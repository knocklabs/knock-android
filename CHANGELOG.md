# Changelog

## 1.3.0

Toolchain and dependency update. No API or behavior changes beyond 1.2.6, but **apps must meet new minimum build
requirements**. See [MIGRATIONS.md](MIGRATIONS.md#upgrading-to-version-130).

### Changed

- Built with Gradle 9.5.1, Android Gradle Plugin 8.13.2, Kotlin 2.2.21 (Compose compiler plugin), compileSdk 36 and
  Java/JVM target 11. The project now builds on JDK 17 through JDK 25.
- Dependencies: Compose BOM 2026.06.01 (Compose 1.11), AndroidX core 1.16.0, activity 1.10.1, lifecycle 2.10.0,
  appcompat 1.7.1, JavaPhoenixClient 1.3.1, Jackson 2.19.4, Arrow 1.2.4, Coil 2.7.0.
- `com.google.firebase:firebase-messaging-ktx` (discontinued by Firebase) is replaced by
  `com.google.firebase:firebase-messaging` 25.1.3.
- Removed the unused Material Components (`com.google.android.material:material`) dependency.
- The published artifact includes a sources jar.

### Example app

- Firebase is optional: the google-services plugin is applied only when `google-services.json` exists, and the
  placeholder file was removed.
- Credentials are read from `local.properties`.
- Targets SDK 36.

## 1.2.6

Stabilization release: bug fixes only, no new features. See [MIGRATIONS.md](MIGRATIONS.md#upgrading-to-version-126)
for the few source-level changes.

### Fixed

- **Minified apps:** the SDK now ships consumer R8/ProGuard rules. Without them R8 renamed the API models and every
  API call failed to decode in minified release builds.
- **In-app feed UI:** tapping a row in `InAppFeedView` now marks the item as interacted/read and emits
  `didTapFeedItemRowPublisher`.
- **Realtime:** `on()` handlers now survive reconnects, so realtime updates keep arriving after the app returns from
  the background. Navigating between activities no longer disconnects the feed, and lifecycle handling now works on
  all supported API levels (it required API 29+).
- **Realtime after pagination:** new messages are fetched correctly after loading more pages (previously already
  loaded items were duplicated and the new message was missed).
- **Networking:** a request whose response body failed to read (e.g. a dropped connection or read timeout) no longer
  hangs forever. Responses are always closed, cancelled coroutines cancel their HTTP call, and IDs are URL-encoded in
  request paths.
- **Sign in / out:** `signIn` and `signOut` no longer throw when Firebase isn't configured, and `signOut` always
  clears the signed-in user. The feed re-joins as the new user after `signIn` with a different user or token, and
  disconnects on `signOut`.
- **Decoding:** messages with `bounced`/`not_sent` status or without a workflow, unknown statuses, unknown content
  block types, and preference conditions without an argument no longer fail to decode.
- **Preferences:** `http` channel type, per-channel `channels` settings and `commercial_subscribed` are preserved
  when saving a preference set (they were reset).
- **Push:** rotated FCM tokens are removed from the user's channel data; each notification keeps its own tap intent
  (a new notification used to disable taps on earlier ones); taps are handled in `onNewIntent` and are not replayed
  after a configuration change; `KnockMessagingService`/`KnockActivity` no longer crash when `Knock.setup()` wasn't
  called in the process.
- `InAppFeedViewModel`: the refresh spinner is cleared when a refresh fails, failed realtime refreshes no longer crash
  the app, and the default filters no longer retain an Activity context.
- `signOut(completionHandler)` can be called from Kotlin.
- Socket join parameters use API values (e.g. `unread`), and device locales are sent as BCP 47 tags.
- The `User-Agent` header reports the actual SDK version.

### Packaging

- `androidx.test` libraries are no longer runtime dependencies of the SDK.
- Removed unused dependencies: `gson`, `arrow-integrations-jackson-module`, `lifecycle-runtime-compose`.
