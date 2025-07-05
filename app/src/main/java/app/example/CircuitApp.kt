package app.example

import android.app.Application
import app.example.di.AppGraph

/**
 * Application class for the app with key initializations.
 */
class CircuitApp : Application() {
    val graph: AppGraph by lazy { AppGraph::class.create(this) }
}
