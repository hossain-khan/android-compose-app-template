package app.example.data.repository

import app.example.data.model.Email
import app.example.data.network.EmailApiService
import app.example.data.network.dto.CreateDraftRequest
import app.example.data.network.dto.SendEmailRequest
import app.example.data.network.dto.toDomain
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn

/**
 * Production implementation of [EmailRepository] that fetches emails from the
 * live email demo API via [EmailApiService].
 *
 * Results are cached in memory after the first successful fetch so that
 * navigating to a detail screen does not require an additional network call.
 *
 * Note: [@Inject][dev.zacsweers.metro.Inject] is implicit when using
 * [@ContributesBinding][ContributesBinding] as of Metro 0.10.0.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class EmailRepositoryImpl
    constructor(
        private val apiService: EmailApiService,
    ) : EmailRepository {
        /** In-memory cache populated on the first successful inbox fetch. */
        private var cachedEmails: List<Email>? = null

        /** In-memory cache populated on the first successful drafts fetch. */
        private var cachedDraftEmails: List<Email>? = null

        override suspend fun getInboxEmails(): List<Email> =
            apiService
                .getInboxEmails()
                .data
                .map { it.toDomain() }
                .also { cachedEmails = it }

        override suspend fun getEmail(emailId: String): Email? =
            // The API does not provide a GET /emails/{id} endpoint, so we fall back to fetching
            // the full inbox list when the requested email is not already in the local cache.
            cachedEmails?.find { it.id == emailId }
                ?: getInboxEmails().find { it.id == emailId }

        override suspend fun getDraftEmails(): List<Email> =
            apiService
                .getDraftEmails()
                .data
                .map { it.toDomain() }
                .also { cachedDraftEmails = it }

        override suspend fun getSentEmails(): List<Email> = getInboxEmails().filter { it.status == "sent" }

        override suspend fun sendEmail(
            to: String,
            subject: String,
            body: String,
        ): Email {
            val recipients = to.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val request =
                SendEmailRequest(
                    subject = subject,
                    body = body,
                    sender = "Me",
                    senderEmail = "me@example.com",
                    recipients = recipients,
                )
            val result = apiService.sendEmail(request).data.toDomain()
            // Invalidate inbox cache so sent email appears on next inbox load
            cachedEmails = null
            return result
        }

        override suspend fun saveDraft(
            to: String,
            subject: String,
            body: String,
        ): Email {
            val recipients = to.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val request =
                CreateDraftRequest(
                    subject = subject,
                    body = body,
                    sender = "Me",
                    senderEmail = "me@example.com",
                    recipients = recipients,
                )
            val result = apiService.createDraft(request).data.toDomain()
            // Invalidate draft cache so it is refreshed on next load
            cachedDraftEmails = null
            return result
        }

        override suspend fun deleteDraft(draftId: String): Boolean {
            val response = apiService.deleteEmail(draftId)
            if (response.success) {
                cachedDraftEmails = cachedDraftEmails?.filter { it.id != draftId }
            }
            return response.success
        }
    }
