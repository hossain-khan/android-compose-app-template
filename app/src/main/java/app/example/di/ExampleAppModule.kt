package app.example.di

import app.example.data.ExampleEmailValidator
import com.squareup.metro.ContributesTo
import com.squareup.metro.Provides

// Example of Metro providers that contribute to the app graph.
// You should delete this file and create your own providers.
@ContributesTo(AppScope::class)
interface ExampleAppModule {
    @Provides
    fun provideEmailValidator(): ExampleEmailValidator = ExampleEmailValidator()
}
