package app.example.di

import android.app.Activity
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Multibinds
import kotlin.reflect.KClass

@ContributesTo(AppScope::class)
interface BaseUiModule {
    @Multibinds
    fun provideActivityProviders(): Map<KClass<out Activity>, Activity>
}
