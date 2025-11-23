package app.example.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo

/**
 * Example of Metro binding container that contributes bindings to the app graph.
 *
 * This demonstrates Metro's aggregation system - bindings defined here are automatically
 * discovered and included in the [AppScope] dependency graph without needing to explicitly
 * declare them in the [AppGraph] interface.
 *
 * Key Metro features used:
 * - [ContributesTo]: Automatically contributes bindings to the specified scope
 *
 * This is an empty example interface. You should delete this file and create your own
 * providers for your application when needed. For simple classes without interfaces,
 * prefer using [@Inject][dev.zacsweers.metro.Inject] constructor injection directly on
 * the class instead of creating a separate binding container.
 *
 * See https://zacsweers.github.io/metro/latest/aggregation/ for more on contribution/aggregation.
 * See https://zacsweers.github.io/metro/latest/injection-types/#constructor-injection for constructor injection.
 */
@ContributesTo(AppScope::class)
interface ExampleAppBindings {
    // Example bindings would go here. Classes with simple constructor injection
    // don't need explicit @Provides functions - they can use @Inject directly.
}
