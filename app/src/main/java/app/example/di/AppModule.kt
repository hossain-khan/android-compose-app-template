package app.example.di

import com.squareup.anvil.annotations.ContributesTo
import dagger.Provides
import app.example.EmailRepository

@ContributesTo(AppScope::class)
@dagger.Module
class AppModule {
    @Provides
    fun provideEmailRepository(): EmailRepository = EmailRepository()
}
