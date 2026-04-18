package app.example.data.network.dto

import kotlinx.serialization.Serializable

/**
 * Request body for creating or updating a draft via `POST /api/emails/drafts`.
 *
 * See https://email-demo.gohk.xyz/openapi.json for the full schema.
 */
@Serializable
data class CreateDraftRequest(
    val subject: String,
    val body: String,
    val sender: String,
    val senderEmail: String,
    val recipients: List<String>,
)
