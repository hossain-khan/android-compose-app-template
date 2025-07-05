package app.example

import android.app.Application
import app.example.di.AppGraph

/**
 * Application class for the app with key initializations.
 */
class CircuitApp : Application() {
    private val appGraph: AppGraph by lazy { AppGraph.create(this) }

    fun appComponent(): AppGraph = appGraph
}
