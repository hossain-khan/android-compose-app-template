package app.example

import android.app.Application
import android.util.Log
import app.example.di.AppGraph
import dev.zacsweers.metro.createGraphFactory

/**
 * Application class for the app with key initializations.
 */
class CircuitApp : Application() {
    val appGraph by lazy { createGraphFactory<AppGraph.Factory>().create(this) }

    override fun onCreate() {
        super.onCreate()

        Log.d("CircuitApp", "Application onCreate called")
    }

    fun appComponent(): AppGraph = appGraph
}
