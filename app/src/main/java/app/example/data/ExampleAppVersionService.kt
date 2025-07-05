package app.example.data

import android.content.Context
import app.example.di.ApplicationScope
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

// Example service class that does not need DI module or binding
@ContributesBinding(ApplicationScope::class)
class ExampleAppVersionService
    @Inject
    constructor(
        context: Context,
    ) {
        private val versionName: String = context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "Unknown"

        fun getApplicationVersion(): String = versionName
    }
