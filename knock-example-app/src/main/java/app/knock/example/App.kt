package app.knock.example

import android.app.Application
import app.knock.client.Knock
import app.knock.client.KnockLoggingOptions
import app.knock.client.KnockStartupOptions
import app.knock.client.modules.FeedManager

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Knock.setup(
            applicationContext,
            publishableKey = Utils.publishableKey,
            pushChannelId = Utils.pushChannelId,
            KnockStartupOptions(Utils.hostname, KnockLoggingOptions.VERBOSE)
        )
    }
}