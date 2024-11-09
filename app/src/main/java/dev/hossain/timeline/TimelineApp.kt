package dev.hossain.timeline

import android.app.Application
import dev.hossain.timeline.di.AppComponent

/**
 * Application class for the app with key initializations.
 */
class TimelineApp : Application() {
    private val appComponent: AppComponent by lazy { AppComponent.create(this) }

    fun appComponent(): AppComponent = appComponent
}
