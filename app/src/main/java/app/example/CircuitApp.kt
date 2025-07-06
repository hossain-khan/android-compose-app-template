package app.example

import android.app.Application
import app.example.di.AppGraph

/**
 * Application class for the app with key initializations.
 */
class CircuitApp : Application() {
    val appGraph: AppGraph by lazy { 
        // Metro will generate a factory implementation
        // This will be populated by the generated code
        TODO("Metro implementation will replace this")
    }

    fun appComponent(): AppGraph = appGraph
}
