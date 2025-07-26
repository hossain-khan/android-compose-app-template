package app.example

import android.app.Application
import androidx.work.Configuration
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import app.example.di.AppGraph
import app.example.work.SampleWorker
import app.example.work.SecondWorker
import dev.zacsweers.metro.createGraphFactory

/**
 * Application class for the app with key initializations.
 */
class CircuitApp : Application(), Configuration.Provider {
    val appGraph by lazy { createGraphFactory<AppGraph.Factory>().create(this) }

    fun appGraph(): AppGraph = appGraph

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(appGraph.workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        scheduleBackgroundWork()
    }

    private fun scheduleBackgroundWork() {
        val workRequest =
            OneTimeWorkRequestBuilder<SampleWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setInputData(Data.Builder().putString("workName", "onCreate").build())
                .build()

        appGraph.workManager.enqueue(workRequest)

        val secondWorkRequest =
            OneTimeWorkRequestBuilder<SecondWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setInputData(Data.Builder().putString("workName", "onCreate").build())
                .build()

        appGraph.workManager.enqueue(secondWorkRequest)
    }
}
