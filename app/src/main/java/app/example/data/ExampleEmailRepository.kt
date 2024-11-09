package app.example.data

import app.example.Email

/**
 * This is example repository. It is used to demonstrate how to use Dagger in Anvil.
 */
class ExampleEmailRepository {
    fun getEmails(): List<Email> =
        listOf(
            Email(
                id = "1",
                subject = "Meeting re-sched!",
                body = "Hey, I'm going to be out of the office tomorrow. Can we reschedule?",
                sender = "Ali Connors",
                timestamp = "3:00 PM",
                recipients = listOf("a@example.com"),
            ),
        )

    fun getEmail(emailId: String): Email = getEmails().find { it.id == emailId } ?: throw IllegalArgumentException("Email not found")
}
