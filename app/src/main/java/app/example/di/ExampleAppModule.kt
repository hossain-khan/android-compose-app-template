package app.example.di

import android.content.Context
import app.example.data.ExampleEmailValidator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

// Example of Metro providers that contribute to the app graph.
// You should delete this file and create your own providers.
@ContributesTo(AppScope::class)
interface ExampleAppModule {
    @Provides
    @SingleIn(AppScope::class)
    fun provideEmailValidator(): ExampleEmailValidator = ExampleEmailValidator()
}
