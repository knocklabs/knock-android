package app.knock.client

import android.app.Activity
import android.content.Intent
import com.google.firebase.messaging.RemoteMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PushIntentTests {
    class TestActivity : KnockComponentActivity() {
        override fun onKnockPushNotificationTappedInForeground(message: RemoteMessage) {
            foregroundTaps.add(message.data["knock_message_id"]!!)
        }

        override fun onKnockPushNotificationTappedInBackground(intent: Intent) {
            backgroundTaps.add(intent.getStringExtra("knock_message_id")!!)
        }
    }

    companion object {
        val foregroundTaps = mutableListOf<String>()
        val backgroundTaps = mutableListOf<String>()
    }

    private fun message(id: String) = RemoteMessage.Builder("sender@fcm.googleapis.com")
        .addData("knock_message_id", id).addData("title", "t").build()

    private val app get() = RuntimeEnvironment.getApplication()

    @Before
    fun setUp() {
        foregroundTaps.clear()
        backgroundTaps.clear()
        Knock.setup(app, "pk_test", null, KnockStartupOptions("http://127.0.0.1:9", KnockLoggingOptions.NONE))
        Knock.shared.environment.setUserId("u1")
    }

    @Test
    fun eachNotificationKeepsItsOwnTapIntent() {
        val first = KnockIntent(app, Activity::class.java, message("m1")).pendingIntent
        val second = KnockIntent(app, Activity::class.java, message("m2")).pendingIntent
        assertFalse(shadowOf(first).isCanceled)
        assertFalse(shadowOf(second).isCanceled)
        assertEquals("m1", shadowOf(first).savedIntent.getParcelableExtra<RemoteMessage>("knock_pending_notification_key")!!.data["knock_message_id"])
        assertEquals("m2", shadowOf(second).savedIntent.getParcelableExtra<RemoteMessage>("knock_pending_notification_key")!!.data["knock_message_id"])
    }

    @Test
    fun foregroundTapIsHandledOnceAcrossRecreation() {
        val controller = Robolectric.buildActivity(TestActivity::class.java, KnockIntent(app, TestActivity::class.java, message("m1"))).setup()
        controller.recreate()
        assertEquals(listOf("m1"), foregroundTaps)
    }

    @Test
    fun tapWhileActivityIsOnTopIsDeliveredThroughOnNewIntent() {
        val controller = Robolectric.buildActivity(TestActivity::class.java, Intent(app, TestActivity::class.java)).setup()
        controller.newIntent(KnockIntent(app, TestActivity::class.java, message("m2")))
        assertEquals(listOf("m2"), foregroundTaps)
    }

    @Test
    fun backgroundTapIsHandledOnceAcrossRecreation() {
        val intent = Intent(app, TestActivity::class.java).putExtra("knock_message_id", "m3")
        val controller = Robolectric.buildActivity(TestActivity::class.java, intent).setup()
        controller.recreate()
        assertEquals(listOf("m3"), backgroundTaps)
    }
}
