package app.example.di

import android.app.Activity
import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory
import javax.inject.Provider

@DependencyGraph(AppScope::class)
@SingleIn(AppScope::class)
interface AppGraph {
    val activityProviders: Map<Class<out Activity>, @JvmSuppressWildcards Provider<Activity>>

    @DependencyGraph.Factory
    interface Factory {
        fun create(
            @ApplicationContext @Provides context: Context,
        ): AppGraph
    }

    companion object {
        fun create(context: Context): AppGraph =
            createGraphFactory<AppGraph.Factory>()
                .create(context)
    }
}
