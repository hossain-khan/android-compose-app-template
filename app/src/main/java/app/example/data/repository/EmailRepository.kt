package app.example.data.repository

import app.example.data.model.Email
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for email operations.
 *
 * All operations are suspend functions for safe use in coroutines.
 * Implementations are responsible for fetching data from the network and
 * mapping API responses to domain [Email] models.
 */
interface EmailRepository {
    /** Flow emitting updates whenever the cache or repository data is mutated. */
    val updates: Flow<Unit>

    /**
     * Returns all emails in the inbox.
     *
     * @throws Exception if the network request fails.
     */
    suspend fun getInboxEmails(): List<Email>

    /**
     * Returns the email with the given [emailId], or `null` if not found.
     *
     * @param emailId The UUID of the email to retrieve.
     * @throws Exception if the network request fails.
     */
    suspend fun getEmail(emailId: String): Email?

    /**
     * Returns all draft emails.
     *
     * @throws Exception if the network request fails.
     */
    suspend fun getDraftEmails(): List<Email>

    /**
     * Returns all sent emails.
     *
     * Note: Implementations may derive this by filtering the inbox.
     *
     * @throws Exception if the network request fails.
     */
    suspend fun getSentEmails(): List<Email>

    /**
     * Sends an email.
     *
     * Note: This operation may invalidate local email caches.
     *
     * @param to Comma-separated recipient addresses.
     * @param subject Email subject line.
     * @param body Email body text.
     * @param draftId Optional ID of a draft to send.
     * @return The sent [Email] domain model.
     * @throws Exception if the network request fails.
     */
    suspend fun sendEmail(
        to: String,
        subject: String,
        body: String,
        draftId: String? = null,
    ): Email

    /**
     * Saves an email as a draft.
     *
     * Note: This operation may invalidate local draft caches.
     *
     * @param to Comma-separated recipient addresses.
     * @param subject Email subject line.
     * @param body Email body text.
     * @return The saved draft [Email] domain model.
     * @throws Exception if the network request fails.
     */
    suspend fun saveDraft(
        to: String,
        subject: String,
        body: String,
    ): Email

    /**
     * Deletes the draft with the given [draftId].
     *
     * @param draftId The UUID of the draft to delete.
     * @return `true` if the deletion succeeded.
     * @throws Exception if the network request fails.
     */
    suspend fun deleteDraft(draftId: String): Boolean
}
