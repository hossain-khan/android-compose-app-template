package dev.hossain.timeline.di

import com.squareup.anvil.annotations.ContributesTo
import dagger.Provides
import dev.hossain.timeline.EmailRepository

@ContributesTo(AppScope::class)
@dagger.Module
class AppModule {
    @Provides
    fun provideEmailRepository(): EmailRepository = EmailRepository()
}
