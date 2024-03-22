package com.example.knock_example_app

import android.app.Application
import app.knock.client.Knock
import app.knock.client.KnockLoggingOptions
import app.knock.client.KnockStartupOptions

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