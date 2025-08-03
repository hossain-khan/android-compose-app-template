package app.example.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

/**
 * Unit test for EmailRepository functionality.
 */
class ExampleEmailRepositoryTest {
    private lateinit var repository: ExampleEmailRepository

    @Before
    fun setup() {
        repository = ExampleEmailRepositoryImpl()
    }

    @Test
    fun getEmailsByStatus_inbox_returnsInboxEmails() {
        val inboxEmails = repository.getEmailsByStatus(EmailStatus.INBOX)
        assertEquals(7, inboxEmails.size)
        assertEquals(EmailStatus.INBOX, inboxEmails.first().status)
    }

    @Test
    fun saveEmailAsDraft_newEmail_createsWithId() {
        val email =
            Email(
                id = "",
                subject = "Test Subject",
                body = "Test Body",
                sender = "Test Sender",
                timestamp = "",
                recipients = listOf("test@example.com"),
                status = EmailStatus.DRAFT,
            )

        val savedEmail = repository.saveEmailAsDraft(email)
        assertEquals("100", savedEmail.id) // First dynamic email gets ID 100
        assertEquals(EmailStatus.DRAFT, savedEmail.status)
        assertEquals("Test Subject", savedEmail.subject)
    }

    @Test
    fun getEmailsByStatus_afterSavingDraft_returnsDraft() {
        val email =
            Email(
                id = "",
                subject = "Draft Subject",
                body = "Draft Body",
                sender = "Me",
                timestamp = "",
                recipients = listOf("test@example.com"),
                status = EmailStatus.DRAFT,
            )

        repository.saveEmailAsDraft(email)
        val drafts = repository.getEmailsByStatus(EmailStatus.DRAFT)

        assertEquals(1, drafts.size)
        assertEquals("Draft Subject", drafts.first().subject)
    }

    @Test
    fun sendEmail_existingDraft_movesDraftToSent() {
        val email =
            Email(
                id = "",
                subject = "Draft to Send",
                body = "Draft Body",
                sender = "Me",
                timestamp = "",
                recipients = listOf("test@example.com"),
                status = EmailStatus.DRAFT,
            )

        val savedDraft = repository.saveEmailAsDraft(email)
        val sentEmail = repository.sendEmail(savedDraft.id)

        assertEquals(EmailStatus.SENT, sentEmail.status)
        assertEquals("Draft to Send", sentEmail.subject)

        // Verify draft list is empty and sent list has the email
        assertEquals(0, repository.getEmailsByStatus(EmailStatus.DRAFT).size)
        assertEquals(1, repository.getEmailsByStatus(EmailStatus.SENT).size)
    }

    @Test
    fun sendEmail_nonDraftEmail_throwsException() {
        assertThrows(IllegalStateException::class.java) {
            repository.sendEmail("1") // ID 1 is an inbox email
        }
    }

    @Test
    fun deleteEmail_existingDraft_removesFromRepository() {
        val email =
            Email(
                id = "",
                subject = "Draft to Delete",
                body = "Draft Body",
                sender = "Me",
                timestamp = "",
                recipients = listOf("test@example.com"),
                status = EmailStatus.DRAFT,
            )

        val savedDraft = repository.saveEmailAsDraft(email)
        assertEquals(1, repository.getEmailsByStatus(EmailStatus.DRAFT).size)

        repository.deleteEmail(savedDraft.id)
        assertEquals(0, repository.getEmailsByStatus(EmailStatus.DRAFT).size)
    }
}
