package dev.hossain.compose.example.data.network.dto

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EmailDtoMapperTest {
    @Test
    fun `toDomain - maps all fields and formats ISO timestamp`() {
        val dto =
            EmailDto(
                id = "email-1",
                subject = "Hello",
                body = "World",
                sender = "Alice",
                senderEmail = "alice@example.com",
                recipients = listOf("bob@example.com"),
                timestamp = "2026-07-18T21:33:17.000Z",
                status = "inbox",
                isRead = true,
            )

        val domain = dto.toDomain()

        assertThat(domain.id).isEqualTo("email-1")
        assertThat(domain.subject).isEqualTo("Hello")
        assertThat(domain.body).isEqualTo("World")
        assertThat(domain.sender).isEqualTo("Alice")
        assertThat(domain.senderEmail).isEqualTo("alice@example.com")
        assertThat(domain.recipients).containsExactly("bob@example.com")
        assertThat(domain.timestamp).isNotEmpty()
        assertThat(domain.status).isEqualTo("inbox")
        assertThat(domain.isRead).isTrue()
    }

    @Test
    fun `toDomain - handles null isRead, blank timestamp, and invalid timestamp`() {
        val blankTimestampDto =
            EmailDto(
                id = "1",
                subject = "S",
                body = "B",
                sender = "A",
                senderEmail = "a@b.com",
                recipients = emptyList(),
                timestamp = "   ",
                status = "draft",
                isRead = null,
            )
        val invalidTimestampDto = blankTimestampDto.copy(timestamp = "not-a-date")

        val blankDomain = blankTimestampDto.toDomain()
        val invalidDomain = invalidTimestampDto.toDomain()

        assertThat(blankDomain.isRead).isFalse()
        assertThat(blankDomain.timestamp).isEqualTo("   ")
        assertThat(invalidDomain.timestamp).isEqualTo("not-a-date")
    }

    @Test
    fun `dto default values are initialized as expected`() {
        val readReq = UpdateReadStatusRequest()
        val resetResp = SystemResetResponse(success = true, data = ResetData())
        val deleteResp = DeleteEmailResponse(success = true)
        val singleResp =
            SingleEmailResponse(
                success = true,
                data =
                    EmailDto(
                        id = "1",
                        subject = "S",
                        body = "B",
                        sender = "A",
                        senderEmail = "a@b.com",
                        recipients = emptyList(),
                        timestamp = "",
                        status = "sent",
                    ),
            )

        assertThat(readReq.isRead).isNull()
        assertThat(resetResp.data?.emailsSeeded).isNull()
        assertThat(deleteResp.message).isNull()
        assertThat(singleResp.message).isNull()
    }
}
