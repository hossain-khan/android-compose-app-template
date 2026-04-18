package app.example.data.repository

import app.example.data.model.Email
import app.example.data.network.EmailApiService
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
    }
