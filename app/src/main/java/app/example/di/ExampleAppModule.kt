package app.example.di

import app.example.data.ExampleEmailValidator
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.AppScope // Changed from local AppScope
import dev.zacsweers.metro.Provides

// Example of a Dagger module that provides dependencies for the app.
// You should delete this file and create your own modules.
@ContributesTo(AppScope::class)
// @Module annotation removed
interface ExampleAppModule { // Changed from class to interface
    @Provides
    fun provideEmailRepository(): ExampleEmailValidator = ExampleEmailValidator()
}
