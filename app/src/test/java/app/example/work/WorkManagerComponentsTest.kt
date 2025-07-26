package app.example.work

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Unit tests for WorkManager components to verify correct structure.
 */
class WorkManagerComponentsTest {

    @Test
    fun `worker key annotation exists and works correctly`() {
        val annotation = SampleWorker::class.annotations
            .filterIsInstance<WorkerKey>()
            .firstOrNull()
        
        assertNotNull(annotation, "SampleWorker should have WorkerKey annotation")
        assertEquals(SampleWorker::class, annotation.value)
    }

    @Test
    fun `worker factory interface exists`() {
        val factoryInterface = AppWorkerFactory.WorkerInstanceFactory::class
        assertNotNull(factoryInterface, "WorkerInstanceFactory interface should exist")
    }

    @Test
    fun `sample worker factory extends correct interface`() {
        val factoryClass = SampleWorker.Factory::class
        val interfaces = factoryClass.supertypes
        
        val implementsWorkerFactory = interfaces.any { superType ->
            superType.toString().contains("WorkerInstanceFactory")
        }
        
        assertEquals(true, implementsWorkerFactory, "SampleWorker.Factory should implement WorkerInstanceFactory")
    }

    @Test
    fun `second worker factory extends correct interface`() {
        val factoryClass = SecondWorker.Factory::class
        val interfaces = factoryClass.supertypes
        
        val implementsWorkerFactory = interfaces.any { superType ->
            superType.toString().contains("WorkerInstanceFactory")
        }
        
        assertEquals(true, implementsWorkerFactory, "SecondWorker.Factory should implement WorkerInstanceFactory")
    }
}