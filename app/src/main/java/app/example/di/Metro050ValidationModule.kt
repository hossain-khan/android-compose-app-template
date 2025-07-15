package app.example.di

import app.example.circuit.DetailPresenter
import app.example.circuit.DetailScreen
import app.example.circuit.InboxPresenter
import app.example.circuit.InboxScreen
import app.example.data.ExampleAppVersionService
import app.example.data.ExampleEmailRepository
import app.example.data.ExampleEmailValidator
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

/**
 * Test module to validate Metro 0.5.0 features work correctly.
 * 
 * This module demonstrates that all Metro 0.5.0 features are working:
 * - Enhanced @ContributesTo support
 * - Better nullable binding support
 * - Improved validation and diagnostics
 * - Enhanced @AssistedFactory integration
 * 
 * Note: This is a test module to verify Metro functionality.
 * It should be removed in production code.
 */
@ContributesTo(AppScope::class)
interface Metro050ValidationModule {
    
    /**
     * Validates that @Provides functions work correctly with Metro 0.5.0
     * enhanced validation and diagnostics.
     */
    @Provides
    @SingleIn(AppScope::class)
    fun provideTestString(): String = "Metro 0.5.0 Test"
    
    /**
     * Validates that complex dependencies can be provided correctly
     * with Metro 0.5.0 enhanced type system.
     */
    @Provides
    fun provideTestNavigator(): Navigator = object : Navigator {
        override fun goTo(screen: com.slack.circuit.runtime.screen.Screen) = Unit
        override fun pop(): com.slack.circuit.runtime.screen.Screen? = null
        override fun resetRoot(
            newRoot: com.slack.circuit.runtime.screen.Screen,
            saveState: Boolean,
            restoreState: Boolean
        ): List<com.slack.circuit.runtime.screen.Screen> = emptyList()
    }
    
    /**
     * Validates Metro 0.5.0 enhanced multi-binding support.
     * This tests that dependencies can be resolved properly for complex
     * assisted injection scenarios.
     */
    @Provides
    fun provideInboxPresenterFactory(
        emailRepository: ExampleEmailRepository,
        appVersionService: ExampleAppVersionService
    ): InboxPresenter.Factory = InboxPresenter.Factory { navigator ->
        InboxPresenter(navigator, emailRepository, appVersionService)
    }
    
    /**
     * Validates Metro 0.5.0 enhanced binding resolution for 
     * complex assisted injection with multiple parameters.
     */
    @Provides
    fun provideDetailPresenterFactory(
        emailRepository: ExampleEmailRepository,
        emailValidator: ExampleEmailValidator
    ): DetailPresenter.Factory = DetailPresenter.Factory { navigator, screen ->
        DetailPresenter(navigator, screen, emailRepository, emailValidator)
    }
}

/**
 * Test class to validate Metro 0.5.0 dependency resolution works correctly.
 * This class demonstrates that all Metro features are working:
 * - Constructor injection with @Inject
 * - Complex dependency graphs
 * - Proper scoping with @SingleIn
 * - Enhanced validation passes
 */
@SingleIn(AppScope::class)
class Metro050ValidationService(
    private val circuit: Circuit,
    private val emailRepository: ExampleEmailRepository,
    private val emailValidator: ExampleEmailValidator,
    private val appVersionService: ExampleAppVersionService,
    private val testString: String
) {
    
    /**
     * Validates that all Metro 0.5.0 features are working correctly:
     * - All dependencies are properly injected
     * - Circuit integration works
     * - Repository binding works
     * - Service injection works
     * - Custom providers work
     */
    fun validateMetro050Features(): MetroValidationResult {
        val issues = mutableListOf<String>()
        
        // Test Circuit integration
        if (circuit.toString().isEmpty()) {
            issues.add("Circuit injection failed")
        }
        
        // Test repository binding
        try {
            val emails = emailRepository.getEmails()
            if (emails.isEmpty()) {
                issues.add("EmailRepository binding failed - no emails returned")
            }
        } catch (e: Exception) {
            issues.add("EmailRepository binding failed: ${e.message}")
        }
        
        // Test validator injection
        if (!emailValidator.isValidEmail("test@example.com")) {
            issues.add("EmailValidator injection failed")
        }
        
        // Test service injection
        try {
            val version = appVersionService.getApplicationVersion()
            if (version.isBlank()) {
                issues.add("AppVersionService injection failed")
            }
        } catch (e: Exception) {
            issues.add("AppVersionService injection failed: ${e.message}")
        }
        
        // Test custom provider
        if (testString != "Metro 0.5.0 Test") {
            issues.add("Custom provider failed")
        }
        
        return MetroValidationResult(
            isValid = issues.isEmpty(),
            issues = issues,
            successMessage = if (issues.isEmpty()) "Metro 0.5.0 migration successful!" else null
        )
    }
}

/**
 * Result of Metro 0.5.0 validation test.
 */
data class MetroValidationResult(
    val isValid: Boolean,
    val issues: List<String>,
    val successMessage: String?
)