package app.example.data.model

import androidx.compose.runtime.Immutable

/**
 * Domain model representing an email.
 *
 * This is the model used by presenters and UI layers. It is mapped from the API DTO
 * by [app.example.data.repository.EmailRepositoryImpl].
 *
 * Marked as [@Immutable][Immutable] so the Compose compiler can treat it as stable,
 * enabling smart-recomposition and improving rendering performance when this model
 * is used inside [com.slack.circuit.runtime.CircuitUiState] state classes.
 *
 * See https://developer.android.com/jetpack/compose/performance/stability
 */
@Immutable
data class Email(
    val id: String,
    val subject: String,
    val body: String,
    val sender: String,
    val senderEmail: String,
    val recipients: List<String>,
    val timestamp: String,
    val status: String,
)
