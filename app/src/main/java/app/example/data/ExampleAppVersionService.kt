package app.example.data

import android.content.Context
import app.example.di.ApplicationContext
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn

/**
 * Example service class that demonstrates Metro constructor injection with scoping.
 *
 * Implements [AppVersionService] and is bound in the DI graph via
 * [@ContributesBinding][ContributesBinding], which also makes [@Inject][dev.zacsweers.metro.Inject]
 * implicit (Metro 0.10.0+).
 *
 * This service retrieves the application version at initialization time and caches it.
 *
 * See https://zacsweers.github.io/metro/latest/injection-types/#constructor-injection
 * See https://zacsweers.github.io/metro/latest/scopes/
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class ExampleAppVersionService
    constructor(
        @ApplicationContext context: Context,
    ) : AppVersionService {
        private val versionName: String = context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "Unknown"

        override fun getApplicationVersion(): String = versionName
    }
