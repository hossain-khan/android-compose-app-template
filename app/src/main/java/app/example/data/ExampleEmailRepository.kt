package app.example.data

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

// -------------------------------------------------------------------------------------
//
// THIS IS AN EXAMPLE FILE WITH REPOSITORY THAT IS INJECTED IN CIRCUIT SCREEN
//
// You should create your own repository and delete this file.
//
//  -------------------------------------------------------------------------------------

/**
 * Email status enum.
 */
enum class EmailStatus {
    INBOX,
    DRAFT,
    SENT,
}

/**
 * Data class representing an email.
 */
data class Email(
    val id: String,
    val subject: String,
    val body: String,
    val sender: String,
    val timestamp: String,
    val recipients: List<String>,
    val status: EmailStatus = EmailStatus.INBOX,
)

interface ExampleEmailRepository {
    fun getEmails(): List<Email>

    fun getEmail(emailId: String): Email

    fun getEmailsByStatus(status: EmailStatus): List<Email>

    fun saveEmailAsDraft(email: Email): Email

    fun sendEmail(emailId: String): Email

    fun deleteEmail(emailId: String)
}

/**
 * This is example repository. It is used to demonstrate how to use Metro DI.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class ExampleEmailRepositoryImpl
    constructor() : ExampleEmailRepository {
        // In-memory storage for dynamic emails (drafts, sent)
        private val dynamicEmails = mutableListOf<Email>()
        private var nextEmailId = 100 // Starting from 100 to avoid conflicts with hardcoded emails

        override fun getEmails(): List<Email> = getInboxEmails() + dynamicEmails

        override fun getEmailsByStatus(status: EmailStatus): List<Email> =
            when (status) {
                EmailStatus.INBOX -> getInboxEmails()
                EmailStatus.DRAFT -> dynamicEmails.filter { it.status == EmailStatus.DRAFT }
                EmailStatus.SENT -> dynamicEmails.filter { it.status == EmailStatus.SENT }
            }

        override fun saveEmailAsDraft(email: Email): Email {
            val draftEmail =
                if (email.id.isEmpty()) {
                    email.copy(id = (nextEmailId++).toString(), status = EmailStatus.DRAFT)
                } else {
                    email.copy(status = EmailStatus.DRAFT)
                }

            // Remove existing draft with same ID if exists
            dynamicEmails.removeAll { it.id == draftEmail.id }
            dynamicEmails.add(draftEmail)

            return draftEmail
        }

        override fun sendEmail(emailId: String): Email {
            val email = getEmail(emailId)
            if (email.status != EmailStatus.DRAFT) {
                throw IllegalStateException("Only draft emails can be sent")
            }

            val sentEmail = email.copy(status = EmailStatus.SENT, timestamp = getCurrentTimestamp())
            dynamicEmails.removeAll { it.id == emailId }
            dynamicEmails.add(sentEmail)

            return sentEmail
        }

        override fun deleteEmail(emailId: String) {
            dynamicEmails.removeAll { it.id == emailId }
        }

        private fun getCurrentTimestamp(): String {
            val now = java.time.LocalTime.now()
            val formatter =
                java.time.format.DateTimeFormatter
                    .ofPattern("h:mm a")
            return now.format(formatter)
        }

        private fun getInboxEmails(): List<Email> =
            listOf(
                Email(
                    id = "1",
                    subject = "Meeting re-sched!",
                    body = "Hey, I'm going to be out of the office tomorrow. Can we reschedule?",
                    sender = "Ali Connors",
                    timestamp = "3:00 PM",
                    recipients = listOf("a@example.com"),
                    status = EmailStatus.INBOX,
                ),
                Email(
                    id = "2",
                    subject = "Team retro",
                    body = "Don't forget about the team retrospective meeting tomorrow. Lunch will be provided. Please RSVP.",
                    sender = "John Doe",
                    timestamp = "4:00 PM",
                    recipients = listOf("b@example.com"),
                    status = EmailStatus.INBOX,
                ),
                Email(
                    id = "3",
                    subject = "Upcoming Holiday Event",
                    body =
                        "Join us for the upcoming holiday event next Friday. " +
                            "There will be games, food, and fun activities for everyone. Please RSVP.",
                    sender = "Jane Smith",
                    timestamp = "5:00 PM",
                    recipients = listOf("everyone@example.com"),
                    status = EmailStatus.INBOX,
                ),
                Email(
                    id = "4",
                    subject = "Project Update",
                    body =
                        "Hi team, a quick update on the project status. We are on track to meet the deadline. " +
                            "Please keep up the great work!",
                    sender = "Project Lead",
                    timestamp = "9:00 AM",
                    recipients = listOf("devteam@example.com"),
                    status = EmailStatus.INBOX,
                ),
                Email(
                    id = "5",
                    subject = "Your Order has Shipped!",
                    body =
                        "Good news! Your recent order has been shipped and is on its way. " +
                            "You can track its progress using the link below.",
                    sender = "OnlineStore",
                    timestamp = "10:30 AM",
                    recipients = listOf("customer@example.com"),
                    status = EmailStatus.INBOX,
                ),
                Email(
                    id = "6",
                    subject = "Weekend Plans?",
                    body = "Hey, any plans for the weekend? Let me know if you're free to catch up!",
                    sender = "Alex Green",
                    timestamp = "1:15 PM",
                    recipients = listOf("friend@example.com"),
                    status = EmailStatus.INBOX,
                ),
                Email(
                    id = "7",
                    subject = "New Blog Post: Mastering Kotlin Coroutines",
                    body =
                        "Check out our latest blog post on mastering Kotlin Coroutines. " +
                            "Learn how to write efficient and concurrent code.",
                    sender = "Tech Blog",
                    timestamp = "2:45 PM",
                    recipients = listOf("subscriber@example.com"),
                    status = EmailStatus.INBOX,
                ),
            )

        override fun getEmail(emailId: String): Email =
            getEmails().find { it.id == emailId }
                ?: throw IllegalArgumentException("Email not found")
    }
