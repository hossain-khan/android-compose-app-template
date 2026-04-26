package app.example.data

/**
 * Service interface for retrieving application version information.
 *
 * Abstracting this behind an interface allows presenters to depend on an abstraction
 * rather than a concrete Android-platform class, making them easier to test in
 * plain JVM unit tests without the Android runtime.
 *
 * This follows the Dependency Inversion Principle of clean architecture:
 * high-level modules (presenters) should not depend on low-level modules (platform services);
 * both should depend on abstractions.
 *
 * See https://developer.android.com/topic/architecture/recommendations
 */
fun interface AppVersionService {
    /** Returns the human-readable version name of the application (e.g. "1.3.0"). */
    fun getApplicationVersion(): String
}
