package app.example.circuit

import app.example.data.model.Email
import app.example.data.repository.EmailRepository

/**
 * A test-only fake implementation of [EmailRepository].
 *
 * Configure the desired responses via constructor parameters before running presenter tests.
 * Throws are modeled by passing an exception to the corresponding [*Exception] parameter.
 *
 * See https://slackhq.github.io/circuit/testing/ for Circuit presenter testing guidance.
 */
class FakeEmailRepository(
    private val inboxEmails: List<Email> = emptyList(),
    private val draftEmails: List<Email> = emptyList(),
    private val sentEmails: List<Email> = emptyList(),
    private val getInboxException: Exception? = null,
    private val getDraftsException: Exception? = null,
    private val getSentException: Exception? = null,
) : EmailRepository {
    override suspend fun getInboxEmails(): List<Email> {
        getInboxException?.let { throw it }
        return inboxEmails
    }

    override suspend fun getEmail(emailId: String): Email? {
        getInboxException?.let { throw it }
        return inboxEmails.find { it.id == emailId }
    }

    override suspend fun getDraftEmails(): List<Email> {
        getDraftsException?.let { throw it }
        return draftEmails
    }

    override suspend fun getSentEmails(): List<Email> {
        getSentException?.let { throw it }
        return sentEmails
    }

    override suspend fun sendEmail(
        to: String,
        subject: String,
        body: String,
    ): Email = testEmail(id = "sent-1", subject = subject, body = body, status = "sent")

    override suspend fun saveDraft(
        to: String,
        subject: String,
        body: String,
    ): Email = testEmail(id = "draft-1", subject = subject, body = body, status = "draft")

    override suspend fun deleteDraft(draftId: String): Boolean = true
}

/** Convenience factory for creating [Email] instances in tests. */
fun testEmail(
    id: String = "test-id",
    subject: String = "Test Subject",
    body: String = "Test body",
    sender: String = "sender@example.com",
    senderEmail: String = "sender@example.com",
    recipients: List<String> = listOf("recipient@example.com"),
    timestamp: String = "12:00 PM",
    status: String = "inbox",
): Email =
    Email(
        id = id,
        subject = subject,
        body = body,
        sender = sender,
        senderEmail = senderEmail,
        recipients = recipients,
        timestamp = timestamp,
        status = status,
    )
