package app.example.di

import app.example.data.ExampleEmailValidator
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

// Example of Metro providers that contribute to the app graph.
// You should delete this file and create your own providers.
@ContributesTo(AppScope::class)
interface ExampleAppModule {
    @Provides
    fun provideEmailValidator(): ExampleEmailValidator = ExampleEmailValidator()
}
