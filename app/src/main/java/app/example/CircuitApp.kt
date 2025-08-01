package app.example

import android.app.Application
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import app.example.di.AppGraph
import app.example.work.SampleWorker
import dev.zacsweers.metro.createGraphFactory

/**
 * Application class for the app with key initializations.
 */
class CircuitApp :
    Application(),
    Configuration.Provider {
    val appGraph by lazy { createGraphFactory<AppGraph.Factory>().create(this) }

    fun appGraph(): AppGraph = appGraph

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(appGraph.workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        scheduleBackgroundWork()
    }

    /**
     * Schedules a background work request using the [WorkManager].
     * This is just an example to demonstrate how to use WorkManager with Metro DI.
     */
    private fun scheduleBackgroundWork() {
        val workRequest =
            OneTimeWorkRequestBuilder<SampleWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setInputData(workDataOf(SampleWorker.KEY_WORK_NAME to "Circuit App ${System.currentTimeMillis()}"))
                .setConstraints(
                    Constraints
                        .Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build(),
                ).build()

        appGraph.workManager.enqueue(workRequest)
    }
}
