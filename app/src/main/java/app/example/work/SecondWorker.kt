package app.example.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.example.work.AppWorkerFactory.WorkerInstanceFactory
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

/**
 * A second sample worker that demonstrates WorkManager integration with Metro DI.
 */
@Inject
class SecondWorker(
    context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        val workName = inputData.getString("workName") ?: "unknown"
        Log.d("SecondWorker", "Second doWork running: $workName")
        
        // Simulate some work
        delay(1.seconds)
        
        return Result.success()
    }

    @WorkerKey(SecondWorker::class)
    @ContributesIntoMap(
        AppScope::class,
        binding = binding<WorkerInstanceFactory<*>>(),
    )
    @AssistedFactory
    abstract class Factory : WorkerInstanceFactory<SecondWorker>
}