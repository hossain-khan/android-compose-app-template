package app.example

import android.app.Application
import app.example.di.AppGraph
import dev.zacsweers.metro.createGraphFactory

/**
 * Application class for the app with key initializations.
 */
class CircuitApp : Application() {
    val appGraph by lazy { createGraphFactory<AppGraph.Factory>().create(this) }

    fun appComponent(): AppGraph = appGraph
}
