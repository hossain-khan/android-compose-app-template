package dev.hossain.compose.example.data.network.dto

import dev.hossain.compose.example.data.model.Email
import kotlinx.serialization.Serializable

/**
 * DTO representing an email as returned by the API.
 *
 * Maps to the `Email` schema defined in the OpenAPI spec at
 * https://email-demo.gohk.xyz/openapi.json
 */
@Serializable
data class EmailDto(
    val id: String,
    val subject: String,
    val body: String,
    val sender: String,
    val senderEmail: String,
    val recipients: List<String>,
    val timestamp: String,
    val status: String,
    val isRead: Boolean? = false,
)

/** Request payload for toggling or setting email read status. */
@Serializable
data class UpdateReadStatusRequest(
    val isRead: Boolean? = null,
)

/** API response wrapper for a list of emails (used for inbox and drafts). */
@Serializable
data class EmailListResponse(
    val success: Boolean,
    val data: List<EmailDto>,
    val count: Int? = null,
)

/** API response wrapper for a single email (used for send/draft creation). */
@Serializable
data class SingleEmailResponse(
    val success: Boolean,
    val data: EmailDto,
    val message: String? = null,
)

/** API response wrapper for delete operations. */
@Serializable
data class DeleteEmailResponse(
    val success: Boolean,
    val message: String? = null,
)

/** Maps an [EmailDto] to the domain [Email] model. */
fun EmailDto.toDomain(): Email =
    Email(
        id = id,
        subject = subject,
        body = body,
        sender = sender,
        senderEmail = senderEmail,
        recipients = recipients,
        timestamp = formatTimestamp(timestamp),
        status = status,
        isRead = isRead ?: false,
    )

/** Formats ISO 8601 timestamps (e.g. 2026-07-18T21:33:17.000Z) into readable dates (e.g. Jul 18). */
private fun formatTimestamp(rawTimestamp: String): String {
    if (rawTimestamp.isBlank()) return rawTimestamp
    return try {
        val instant = java.time.Instant.parse(rawTimestamp)
        val zoneId = java.time.ZoneId.systemDefault()
        val localDateTime = instant.atZone(zoneId)
        val formatter =
            java.time.format.DateTimeFormatter
                .ofPattern("MMM d", java.util.Locale.getDefault())
        localDateTime.format(formatter)
    } catch (e: Exception) {
        rawTimestamp
    }
}
