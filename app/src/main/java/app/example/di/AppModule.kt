package app.example.di

import app.example.EmailRepository
import com.squareup.anvil.annotations.ContributesTo
import dagger.Provides

@ContributesTo(AppScope::class)
@dagger.Module
class AppModule {
    @Provides
    fun provideEmailRepository(): EmailRepository = EmailRepository()
}
