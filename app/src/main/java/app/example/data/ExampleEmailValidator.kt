package app.example.data

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Example of a class that participates in Metro dependency injection using constructor injection.
 *
 * This demonstrates Metro's constructor injection - the [@Inject][Inject] annotation on the
 * constructor tells Metro to create this class as a dependency.
 *
 * The [@SingleIn][SingleIn] annotation ensures only one instance is created per [AppScope].
 *
 * See https://zacsweers.github.io/metro/latest/injection-types/#constructor-injection
 * See https://zacsweers.github.io/metro/latest/scopes/
 */
@SingleIn(AppScope::class)
@Inject
class ExampleEmailValidator {
    fun isValidEmail(email: String): Boolean = email.contains("@")
}
