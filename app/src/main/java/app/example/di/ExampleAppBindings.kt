package app.example.di

import app.example.data.ExampleEmailValidator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

/**
 * Example of Metro binding container that contributes bindings to the app graph.
 *
 * This demonstrates Metro's aggregation system - bindings defined here are automatically
 * discovered and included in the [AppScope] dependency graph without needing to explicitly
 * declare them in the [AppGraph] interface.
 *
 * Key Metro features used:
 * - [ContributesTo]: Automatically contributes bindings to the specified scope
 * - [Provides]: Functions that provide dependencies to the graph
 * - [SingleIn]: Scopes the binding to ensure only one instance exists per scope
 *
 * You should delete this file and create your own providers for your application.
 *
 * See https://zacsweers.github.io/metro/latest/aggregation/ for more on contribution/aggregation.
 * See https://zacsweers.github.io/metro/latest/bindings/#provides for more on @Provides.
 * See https://zacsweers.github.io/metro/latest/scopes/ for more on scoping.
 */
@ContributesTo(AppScope::class)
interface ExampleAppBindings {
    /**
     * Provides an [ExampleEmailValidator] instance.
     *
     * This is scoped to [AppScope] which means only one instance will be created
     * and shared across the entire application lifetime.
     */
    @Provides
    @SingleIn(AppScope::class)
    fun provideEmailValidator(): ExampleEmailValidator = ExampleEmailValidator()
}
