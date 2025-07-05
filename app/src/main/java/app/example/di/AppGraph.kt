package app.example.di

import android.app.Activity
import android.content.Context
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.AppScope // Changed from local AppScope
import dev.zacsweers.metro.SingleIn // Changed import
import dev.zacsweers.metro.Provides // Changed import for factory parameter
import dev.zacsweers.metro.Provider // Changed import for Provider type
import dev.zacsweers.metro.createGraphFactory // Changed for graph creation
import kotlin.reflect.KClass // For KClass type

@DependencyGraph(scope = AppScope::class)
// modules attribute removed, @ContributesTo handles this
@SingleIn(AppScope::class) // Scope updated to Metro's AppScope
interface AppGraph { // Renamed from AppComponent
    val activityProviders: Map<KClass<out Activity>, @JvmSuppressWildcards Provider<Activity>> // Key type changed to KClass, Provider import updated

    @DependencyGraph.Factory // Changed from @MergeComponent.Factory
    interface Factory {
        fun create(
            @ApplicationContext @Provides context: Context, // Changed from @BindsInstance to @Provides
        ): AppGraph // Return type changed
    }

    companion object {
        // This specific static create method might not be directly needed if instantiation is always done via Application class
        // or if createGraphFactory is used directly. Keeping it for now, adapted.
        fun create(context: Context): AppGraph = createGraphFactory<Factory>().create(context)
    }
}
