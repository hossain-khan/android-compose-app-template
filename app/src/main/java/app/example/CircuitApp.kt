package app.example

import android.app.Application
import app.example.di.AppGraph // Changed from AppComponent
import dev.zacsweers.metro.createGraphFactory // Import for graph creation

/**
 * Application class for the app with key initializations.
 */
class CircuitApp : Application() {
    private val appGraph: AppGraph by lazy { createGraphFactory<AppGraph.Factory>().create(this) } // Changed to AppGraph and used createGraphFactory

    fun appComponent(): AppGraph = appGraph // Method name kept for compatibility, returns AppGraph
}
