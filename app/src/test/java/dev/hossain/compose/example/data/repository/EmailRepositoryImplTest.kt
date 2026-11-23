package dev.hossain.compose.example.data.repository

import com.google.common.truth.Truth.assertThat
import dev.hossain.compose.example.data.network.EmailApiService
import dev.hossain.compose.example.data.network.dto.CreateDraftRequest
import dev.hossain.compose.example.data.network.dto.DeleteEmailResponse
import dev.hossain.compose.example.data.network.dto.EmailDto
import dev.hossain.compose.example.data.network.dto.EmailListResponse
import dev.hossain.compose.example.data.network.dto.SendEmailRequest
import dev.hossain.compose.example.data.network.dto.SingleEmailResponse
import dev.hossain.compose.example.data.network.dto.SystemResetResponse
import dev.hossain.compose.example.data.network.dto.UpdateReadStatusRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EmailRepositoryImplTest {
    @Test
    fun `getInboxEmails - returns mapped emails and populates cache`() =
        runTest {
            val apiService =
                FakeEmailApiService(
                    inboxDtos = listOf(testDto(id = "inbox-1", subject = "Inbox Hello")),
                )
            val repository = EmailRepositoryImpl(apiService)

            val emails = repository.getInboxEmails()

            assertThat(emails).hasSize(1)
            assertThat(emails.first().id).isEqualTo("inbox-1")
            assertThat(emails.first().subject).isEqualTo("Inbox Hello")
            assertThat(apiService.getInboxCallCount).isEqualTo(1)
        }

    @Test
    fun `getEmail - uses cached inbox emails when available`() =
        runTest {
            val apiService =
                FakeEmailApiService(
                    inboxDtos = listOf(testDto(id = "inbox-1", subject = "Cached")),
                )
            val repository = EmailRepositoryImpl(apiService)

            repository.getInboxEmails()
            val email = repository.getEmail("inbox-1")

            assertThat(email).isNotNull()
            assertThat(email?.subject).isEqualTo("Cached")
            assertThat(apiService.getInboxCallCount).isEqualTo(1)
        }

    @Test
    fun `getEmail - falls back to API when cache is empty and returns null if missing`() =
        runTest {
            val apiService =
                FakeEmailApiService(
                    inboxDtos = listOf(testDto(id = "inbox-1")),
                )
            val repository = EmailRepositoryImpl(apiService)

            val found = repository.getEmail("inbox-1")
            val missing = repository.getEmail("non-existent")

            assertThat(found?.id).isEqualTo("inbox-1")
            assertThat(missing).isNull()
            assertThat(apiService.getInboxCallCount).isEqualTo(2)
        }

    @Test
    fun `getDraftEmails - returns mapped draft emails`() =
        runTest {
            val apiService =
                FakeEmailApiService(
                    draftDtos = listOf(testDto(id = "draft-1", status = "draft")),
                )
            val repository = EmailRepositoryImpl(apiService)

            val drafts = repository.getDraftEmails()

            assertThat(drafts).hasSize(1)
            assertThat(drafts.first().id).isEqualTo("draft-1")
            assertThat(drafts.first().status).isEqualTo("draft")
        }

    @Test
    fun `getSentEmails - returns mapped sent emails`() =
        runTest {
            val apiService =
                FakeEmailApiService(
                    sentDtos = listOf(testDto(id = "sent-1", status = "sent")),
                )
            val repository = EmailRepositoryImpl(apiService)

            val sent = repository.getSentEmails()

            assertThat(sent).hasSize(1)
            assertThat(sent.first().id).isEqualTo("sent-1")
            assertThat(sent.first().status).isEqualTo("sent")
        }

    @Test
    fun `sendEmail - parses recipients, invalidates caches, and emits update`() =
        runTest {
            val apiService =
                FakeEmailApiService(
                    inboxDtos = listOf(testDto(id = "inbox-1")),
                )
            val repository = EmailRepositoryImpl(apiService)
            repository.getInboxEmails()

            var updateEmitted = false
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    repository.updates.first()
                    updateEmitted = true
                }

            val sent =
                repository.sendEmail(
                    to = "alice@example.com, , bob@example.com ",
                    subject = "Greetings",
                    body = "Hi team",
                    draftId = "draft-9",
                )

            assertThat(sent.subject).isEqualTo("Greetings")
            assertThat(apiService.lastSendRequest?.recipients)
                .containsExactly("alice@example.com", "bob@example.com")
                .inOrder()
            assertThat(apiService.lastSendRequest?.draftId).isEqualTo("draft-9")
            assertThat(updateEmitted).isTrue()

            // Cache should be invalidated so getEmail hits the API again
            repository.getEmail("inbox-1")
            assertThat(apiService.getInboxCallCount).isEqualTo(2)
            collectJob.cancel()
        }

    @Test
    fun `saveDraft - parses recipients, invalidates draft cache, and emits update`() =
        runTest {
            val apiService = FakeEmailApiService()
            val repository = EmailRepositoryImpl(apiService)

            var updateEmitted = false
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    repository.updates.first()
                    updateEmitted = true
                }

            val draft =
                repository.saveDraft(
                    to = "alice@example.com, bob@example.com",
                    subject = "WIP",
                    body = "Draft body",
                )

            assertThat(draft.subject).isEqualTo("WIP")
            assertThat(apiService.lastCreateDraftRequest?.recipients)
                .containsExactly("alice@example.com", "bob@example.com")
                .inOrder()
            assertThat(updateEmitted).isTrue()
            collectJob.cancel()
        }

    @Test
    fun `deleteDraft - returns true and emits update when API succeeds`() =
        runTest {
            val apiService =
                FakeEmailApiService(
                    draftDtos = listOf(testDto(id = "draft-1"), testDto(id = "draft-2")),
                    deleteSuccess = true,
                )
            val repository = EmailRepositoryImpl(apiService)
            repository.getDraftEmails()

            var updateEmitted = false
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    repository.updates.first()
                    updateEmitted = true
                }

            val result = repository.deleteDraft("draft-1")

            assertThat(result).isTrue()
            assertThat(updateEmitted).isTrue()
            collectJob.cancel()
        }

    @Test
    fun `deleteDraft - returns false and does not emit update when API fails`() =
        runTest {
            val apiService = FakeEmailApiService(deleteSuccess = false)
            val repository = EmailRepositoryImpl(apiService)

            var updateEmitted = false
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    repository.updates.first()
                    updateEmitted = true
                }

            val result = repository.deleteDraft("draft-1")

            assertThat(result).isFalse()
            assertThat(updateEmitted).isFalse()
            collectJob.cancel()
        }

    @Test
    fun `updateEmailReadStatus - updates cached lists and emits update`() =
        runTest {
            val apiService =
                FakeEmailApiService(
                    inboxDtos = listOf(testDto(id = "email-1", isRead = false)),
                    draftDtos = listOf(testDto(id = "draft-1", isRead = false)),
                    sentDtos = listOf(testDto(id = "sent-1", isRead = false)),
                )
            val repository = EmailRepositoryImpl(apiService)
            repository.getInboxEmails()
            repository.getDraftEmails()
            repository.getSentEmails()

            var updateEmitted = false
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    repository.updates.first()
                    updateEmitted = true
                }

            val updated = repository.updateEmailReadStatus("email-1", isRead = true)

            assertThat(updated.isRead).isTrue()
            assertThat(repository.getEmail("email-1")?.isRead).isTrue()
            assertThat(updateEmitted).isTrue()
            collectJob.cancel()
        }

    @Test
    fun `resetDemoData - clears caches and emits update on success`() =
        runTest {
            val apiService =
                FakeEmailApiService(
                    inboxDtos = listOf(testDto(id = "inbox-1")),
                    resetSuccess = true,
                )
            val repository = EmailRepositoryImpl(apiService)
            repository.getInboxEmails()

            var updateEmitted = false
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    repository.updates.first()
                    updateEmitted = true
                }

            val result = repository.resetDemoData()

            assertThat(result).isTrue()
            assertThat(updateEmitted).isTrue()
            repository.getEmail("inbox-1")
            assertThat(apiService.getInboxCallCount).isEqualTo(2)
            collectJob.cancel()
        }

    @Test
    fun `resetDemoData - returns false and keeps cache when API returns false`() =
        runTest {
            val apiService =
                FakeEmailApiService(
                    inboxDtos = listOf(testDto(id = "inbox-1")),
                    resetSuccess = false,
                )
            val repository = EmailRepositoryImpl(apiService)
            repository.getInboxEmails()

            val result = repository.resetDemoData()

            assertThat(result).isFalse()
            repository.getEmail("inbox-1")
            assertThat(apiService.getInboxCallCount).isEqualTo(1)
        }
}

private class FakeEmailApiService(
    private val inboxDtos: List<EmailDto> = emptyList(),
    private val draftDtos: List<EmailDto> = emptyList(),
    private val sentDtos: List<EmailDto> = emptyList(),
    private val deleteSuccess: Boolean = true,
    private val resetSuccess: Boolean = true,
) : EmailApiService {
    var getInboxCallCount = 0
        private set
    var lastSendRequest: SendEmailRequest? = null
        private set
    var lastCreateDraftRequest: CreateDraftRequest? = null
        private set

    override suspend fun getInboxEmails(
        limit: Int,
        offset: Int,
    ): EmailListResponse {
        getInboxCallCount++
        return EmailListResponse(success = true, data = inboxDtos, count = inboxDtos.size)
    }

    override suspend fun getDraftEmails(): EmailListResponse = EmailListResponse(success = true, data = draftDtos, count = draftDtos.size)

    override suspend fun getSentEmails(): EmailListResponse = EmailListResponse(success = true, data = sentDtos, count = sentDtos.size)

    override suspend fun sendEmail(request: SendEmailRequest): SingleEmailResponse {
        lastSendRequest = request
        return SingleEmailResponse(
            success = true,
            data =
                testDto(
                    id = "sent-new",
                    subject = request.subject,
                    body = request.body,
                    recipients = request.recipients,
                    status = "sent",
                ),
        )
    }

    override suspend fun createDraft(request: CreateDraftRequest): SingleEmailResponse {
        lastCreateDraftRequest = request
        return SingleEmailResponse(
            success = true,
            data =
                testDto(
                    id = "draft-new",
                    subject = request.subject,
                    body = request.body,
                    recipients = request.recipients,
                    status = "draft",
                ),
        )
    }

    override suspend fun deleteEmail(emailId: String): DeleteEmailResponse = DeleteEmailResponse(success = deleteSuccess)

    override suspend fun updateEmailReadStatus(
        emailId: String,
        request: UpdateReadStatusRequest?,
    ): SingleEmailResponse =
        SingleEmailResponse(
            success = true,
            data = testDto(id = emailId, isRead = request?.isRead ?: true),
        )

    override suspend fun resetSystemData(): SystemResetResponse = SystemResetResponse(success = resetSuccess)
}

private fun testDto(
    id: String = "id-1",
    subject: String = "Subject",
    body: String = "Body",
    sender: String = "Sender",
    senderEmail: String = "sender@example.com",
    recipients: List<String> = listOf("recipient@example.com"),
    timestamp: String = "2026-07-18T21:33:17.000Z",
    status: String = "inbox",
    isRead: Boolean? = false,
): EmailDto =
    EmailDto(
        id = id,
        subject = subject,
        body = body,
        sender = sender,
        senderEmail = senderEmail,
        recipients = recipients,
        timestamp = timestamp,
        status = status,
        isRead = isRead,
    )
