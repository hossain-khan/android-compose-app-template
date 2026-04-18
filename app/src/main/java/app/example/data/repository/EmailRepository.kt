package app.example.data.repository

import app.example.data.model.Email

/**
 * Repository interface for email operations.
 *
 * All operations are suspend functions for safe use in coroutines.
 * Implementations are responsible for fetching data from the network and
 * mapping API responses to domain [Email] models.
 */
interface EmailRepository {
    /**
     * Returns all emails in the inbox.
     *
     * @throws Exception if the network request fails.
     */
    suspend fun getInboxEmails(): List<Email>

    /**
     * Returns the email with the given [emailId], or `null` if not found.
     *
     * @throws Exception if the network request fails.
     */
    suspend fun getEmail(emailId: String): Email?
}
