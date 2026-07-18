package app.example.data.repository

import app.example.data.model.Email
import app.example.data.network.EmailApiService
import app.example.data.network.dto.CreateDraftRequest
import app.example.data.network.dto.SendEmailRequest
import app.example.data.network.dto.toDomain
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

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
        private val _updates = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        override val updates: Flow<Unit> = _updates.asSharedFlow()

        /** In-memory cache populated on the first successful inbox fetch. */
        private var cachedEmails: List<Email>? = null

        /** In-memory cache populated on the first successful drafts fetch. */
        private var cachedDraftEmails: List<Email>? = null

        /** In-memory cache populated on the first successful sent fetch. */
        private var cachedSentEmails: List<Email>? = null

        override suspend fun getInboxEmails(): List<Email> =
            apiService
                .getInboxEmails()
                .data
                .map { it.toDomain() }
                .also { cachedEmails = it }

        /**
         * Implementation that uses the in-memory [cachedEmails] if available,
         * otherwise falls back to fetching the full inbox list since the API
         * does not provide a single email retrieval endpoint.
         */
        override suspend fun getEmail(emailId: String): Email? =
            cachedEmails?.find { it.id == emailId }
                ?: getInboxEmails().find { it.id == emailId }

        override suspend fun getDraftEmails(): List<Email> =
            apiService
                .getDraftEmails()
                .data
                .map { it.toDomain() }
                .also { cachedDraftEmails = it }

        /**
         * Implementation that fetches sent emails from the live sent API.
         */
        override suspend fun getSentEmails(): List<Email> =
            apiService
                .getSentEmails()
                .data
                .map { it.toDomain() }
                .also { cachedSentEmails = it }

        /**
         * Sends an email and invalidates the [cachedEmails] to ensure the
         * newly sent email appears in the inbox on the next fetch.
         */
        override suspend fun sendEmail(
            to: String,
            subject: String,
            body: String,
            draftId: String?,
        ): Email {
            val recipients = to.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val request =
                SendEmailRequest(
                    subject = subject,
                    body = body,
                    sender = "Me",
                    senderEmail = "me@example.com",
                    recipients = recipients,
                    draftId = draftId,
                )
            val result = apiService.sendEmail(request).data.toDomain()
            // Invalidate inbox, sent, and drafts caches so sent/draft emails update on next loads
            cachedEmails = null
            cachedSentEmails = null
            cachedDraftEmails = null
            _updates.tryEmit(Unit)
            return result
        }

        /**
         * Saves a draft email and invalidates the [cachedDraftEmails] to ensure
         * the new draft is visible on the next drafts list fetch.
         */
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
            _updates.tryEmit(Unit)
            return result
        }

        /**
         * Deletes a draft and removes it from the [cachedDraftEmails] if the
         * operation was successful.
         */
        override suspend fun deleteDraft(draftId: String): Boolean {
            val response = apiService.deleteEmail(draftId)
            if (response.success) {
                cachedDraftEmails = cachedDraftEmails?.filter { it.id != draftId }
                _updates.tryEmit(Unit)
            }
            return response.success
        }
    }
