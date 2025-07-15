package app.example.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo

/**
 * Example of Metro providers that contribute to the app graph.
 * 
 * In Metro 0.5.0, you can organize dependencies using:
 * 1. Traditional @ContributesTo modules (like this one)
 * 2. New @BindingContainer feature (see DataBindingContainer.kt)
 * 3. @ContributesBinding directly on implementation classes
 * 
 * This module is kept minimal as an example. In real applications,
 * you might have separate modules for different layers (network, database, etc.)
 * or use binding containers for better organization.
 * 
 * You should delete this file and create your own providers.
 */
@ContributesTo(AppScope::class)
interface ExampleAppModule {
    // Module is kept empty as dependencies are now provided via:
    // - ExampleEmailValidator: DataBindingContainer
    // - ExampleEmailRepository: @ContributesBinding on ExampleEmailRepositoryImpl
    // - ExampleAppVersionService: @Inject constructor
}
