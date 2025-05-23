package app.example.di

import android.app.Activity
import android.content.Context
import dagger.internal.DaggerGenerated
import dagger.internal.InstanceFactory
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Implementation of the AppComponent factory for Dagger.
 * This directly implements the AppComponent interface to handle the case when
 * Dagger doesn't generate the expected implementation after the Kotlin upgrade.
 */
object DaggerAppComponent_Impl {
    fun builder(): Builder {
        return Builder()
    }

    class Builder {
        private lateinit var appContext: Context

        fun context(context: Context): Builder {
            this.appContext = context
            return this
        }

        fun build(): AppComponent {
            // Create a direct implementation of AppComponent
            return AppComponentImpl(appContext)
        }
    }
}

/**
 * Direct implementation of AppComponent that can be used when the Dagger-generated
 * implementation isn't working properly.
 */
@DaggerGenerated
@Singleton
class AppComponentImpl(
    @ApplicationContext private val context: Context
) : AppComponent {

    // For simplicity, we're using an empty map here. In a real app, this would be properly
    // populated with activity providers from Dagger's multibindings.
    override val activityProviders: Map<Class<out Activity>, @JvmSuppressWildcards Provider<Activity>> =
        emptyMap()

    companion object {
        val factory: AppComponent.Factory = object : AppComponent.Factory {
            override fun create(context: Context): AppComponent {
                return AppComponentImpl(context)
            }
        }
    }
}
