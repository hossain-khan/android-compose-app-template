package app.example.di

import android.app.Activity
import android.content.Context
import com.slack.circuit.foundation.Circuit
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlin.reflect.KClass

@DependencyGraph(scope = AppScope::class)
@SingleIn(AppScope::class)
interface AppGraph {
    val activityProviders: Map<KClass<out Activity>, Provider<Activity>>
    val circuit: Circuit

    @DependencyGraph.Factory
    interface Factory {
        fun create(
            @ApplicationContext @Provides context: Context,
        ): AppGraph
    }
}
