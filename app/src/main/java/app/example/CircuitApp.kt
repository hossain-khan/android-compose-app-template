package app.example

import android.app.Application
import app.example.di.AppGraph

/**
 * Application class for the app with key initializations.
 */
class CircuitApp : Application() {
    // Using a simple approach that should work with Metro
    lateinit var appGraph: AppGraph

    override fun onCreate() {
        super.onCreate()
        // Initialize the graph here - Metro will provide the implementation
        // For now, we'll leave this as a placeholder
        // appGraph = MetroGeneratedFactory.create(this)
    }

    fun appComponent(): AppGraph = appGraph
}
