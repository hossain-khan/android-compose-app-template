package dev.hossain.compose.example.di

import android.content.Context
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import com.slack.circuit.foundation.Circuit
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metrox.android.MetroAppComponentProviders

/**
 * Metro dependency graph for the application, which includes all necessary bindings and providers.
 *
 * This is the root dependency graph (component) for the application. Declaring `scope = AppScope::class`
 * implicitly scopes this graph to [AppScope] (equivalent to `@SingleIn(AppScope::class)`), making
 * all scoped bindings singletons within the application lifecycle.
 *
 * Implementing [MetroAppComponentProviders] exposes the Android component multibinding maps used by
 * `metrox-android`'s `MetroAppComponentFactory` for constructor injection of Activities, Services,
 * BroadcastReceivers, and ContentProviders.
 *
 * See https://zacsweers.github.io/metro/latest/dependency-graphs/ for more information on dependency graphs.
 * See https://zacsweers.github.io/metro/latest/scopes/ for more information on scopes.
 */
@DependencyGraph(scope = AppScope::class)
interface AppGraph : MetroAppComponentProviders {
    /**
     * Circuit instance for handling UI presentation and state management.
     *
     * See https://slackhq.github.io/circuit/
     */
    val circuit: Circuit

    val workManager: WorkManager
    val workerFactory: WorkerFactory

    @Provides
    fun providesWorkManager(
        @ApplicationContext context: Context,
    ): WorkManager = WorkManager.getInstance(context)

    /**
     * Factory for creating the [AppGraph] with runtime inputs.
     *
     * The [ApplicationContext] parameter is marked with [@Provides][Provides] which makes it
     * available as a binding in the graph. This is analogous to Dagger's @BindsInstance.
     *
     * See https://zacsweers.github.io/metro/latest/dependency-graphs/#provides
     */
    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @ApplicationContext @Provides context: Context,
        ): AppGraph
    }
}
