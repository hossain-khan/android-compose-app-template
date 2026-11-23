package dev.hossain.compose.example.circuit

import com.google.common.truth.Truth.assertThat
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import dev.hossain.compose.example.data.model.Email
import dev.hossain.compose.example.data.repository.EmailRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ComposeEmailPresenterTest {
    @Test
    fun `present - new email starts in Success and updates fields on input events`() =
        runTest {
            val screen = ComposeEmailScreen()
            val navigator = FakeNavigator(screen)
            val presenter =
                ComposeEmailPresenter(
                    navigator = navigator,
                    screen = screen,
                    emailRepository = FakeEmailRepository(),
                )

            presenter.test {
                val initial = awaitItem() as ComposeEmailScreen.State.Success
                assertThat(initial.to).isEmpty()
                assertThat(initial.subject).isEmpty()
                assertThat(initial.body).isEmpty()
                assertThat(initial.isSaving).isFalse()

                initial.eventSink(ComposeEmailScreen.Event.OnToChanged("alice@example.com"))
                val withTo = awaitItem() as ComposeEmailScreen.State.Success
                assertThat(withTo.to).isEqualTo("alice@example.com")

                withTo.eventSink(ComposeEmailScreen.Event.OnSubjectChanged("Hello"))
                val withSubject = awaitItem() as ComposeEmailScreen.State.Success
                assertThat(withSubject.subject).isEqualTo("Hello")

                withSubject.eventSink(ComposeEmailScreen.Event.OnBodyChanged("Body text"))
                val withBody = awaitItem() as ComposeEmailScreen.State.Success
                assertThat(withBody.body).isEqualTo("Body text")
            }
        }

    @Test
    fun `present - OnBack pops the navigator`() =
        runTest {
            val screen = ComposeEmailScreen()
            val navigator = FakeNavigator(screen)
            val presenter =
                ComposeEmailPresenter(
                    navigator = navigator,
                    screen = screen,
                    emailRepository = FakeEmailRepository(),
                )

            presenter.test {
                val state = awaitItem() as ComposeEmailScreen.State.Success
                state.eventSink(ComposeEmailScreen.Event.OnBack)
            }

            assertThat(navigator.awaitPop()).isNotNull()
        }

    @Test
    fun `present - editing existing draft emits Loading then Success with draft values`() =
        runTest {
            val screen = ComposeEmailScreen(draftId = "draft-1")
            val navigator = FakeNavigator(screen)
            val draft =
                testEmail(
                    id = "draft-1",
                    subject = "Draft Subject",
                    body = "Draft Body",
                    recipients = listOf("a@example.com", "b@example.com"),
                    status = "draft",
                )
            val presenter =
                ComposeEmailPresenter(
                    navigator = navigator,
                    screen = screen,
                    emailRepository = FakeEmailRepository(draftEmails = listOf(draft)),
                )

            presenter.test {
                assertThat(awaitItem()).isEqualTo(ComposeEmailScreen.State.Loading)

                val loaded = awaitItem() as ComposeEmailScreen.State.Success
                assertThat(loaded.to).isEqualTo("a@example.com, b@example.com")
                assertThat(loaded.subject).isEqualTo("Draft Subject")
                assertThat(loaded.body).isEqualTo("Draft Body")
            }
        }

    @Test
    fun `present - editing missing draft emits Error`() =
        runTest {
            val screen = ComposeEmailScreen(draftId = "missing")
            val navigator = FakeNavigator(screen)
            val presenter =
                ComposeEmailPresenter(
                    navigator = navigator,
                    screen = screen,
                    emailRepository = FakeEmailRepository(draftEmails = emptyList()),
                )

            presenter.test {
                assertThat(awaitItem()).isEqualTo(ComposeEmailScreen.State.Loading)

                val error = awaitItem() as ComposeEmailScreen.State.Error
                assertThat(error.message).isEqualTo("Draft not found")
            }
        }

    @Test
    fun `present - loading draft failure emits Error`() =
        runTest {
            val screen = ComposeEmailScreen(draftId = "draft-1")
            val navigator = FakeNavigator(screen)
            val presenter =
                ComposeEmailPresenter(
                    navigator = navigator,
                    screen = screen,
                    emailRepository = FakeEmailRepository(getDraftsException = Exception("Load failed")),
                )

            presenter.test {
                assertThat(awaitItem()).isEqualTo(ComposeEmailScreen.State.Loading)

                val error = awaitItem() as ComposeEmailScreen.State.Error
                assertThat(error.message).isEqualTo("Load failed")
            }
        }

    @Test
    fun `present - OnSaveDraft saves draft and pops navigator`() =
        runTest {
            val screen = ComposeEmailScreen()
            val navigator = FakeNavigator(screen)
            val presenter =
                ComposeEmailPresenter(
                    navigator = navigator,
                    screen = screen,
                    emailRepository = FakeEmailRepository(),
                )

            presenter.test {
                val initial = awaitItem() as ComposeEmailScreen.State.Success
                initial.eventSink(ComposeEmailScreen.Event.OnSaveDraft)
                val saving = awaitItem() as ComposeEmailScreen.State.Success
                assertThat(saving.isSaving).isTrue()
                cancelAndIgnoreRemainingEvents()
            }

            assertThat(navigator.awaitPop()).isNotNull()
        }

    @Test
    fun `present - OnSend sends email and pops navigator`() =
        runTest {
            val screen = ComposeEmailScreen()
            val navigator = FakeNavigator(screen)
            val presenter =
                ComposeEmailPresenter(
                    navigator = navigator,
                    screen = screen,
                    emailRepository = FakeEmailRepository(),
                )

            presenter.test {
                val initial = awaitItem() as ComposeEmailScreen.State.Success
                initial.eventSink(ComposeEmailScreen.Event.OnSend)
                val saving = awaitItem() as ComposeEmailScreen.State.Success
                assertThat(saving.isSaving).isTrue()
                cancelAndIgnoreRemainingEvents()
            }

            assertThat(navigator.awaitPop()).isNotNull()
        }

    @Test
    fun `present - OnSend failure shows errorMessage`() =
        runTest {
            val screen = ComposeEmailScreen()
            val navigator = FakeNavigator(screen)
            val presenter =
                ComposeEmailPresenter(
                    navigator = navigator,
                    screen = screen,
                    emailRepository = FailingSendEmailRepository(),
                )

            presenter.test {
                val initial = awaitItem() as ComposeEmailScreen.State.Success
                initial.eventSink(ComposeEmailScreen.Event.OnSend)
                awaitItem() // isSaving = true
                val failed = awaitItem() as ComposeEmailScreen.State.Success
                assertThat(failed.isSaving).isFalse()
                assertThat(failed.errorMessage).isEqualTo("Send failed")
            }
        }
}

private class FailingSendEmailRepository : EmailRepository {
    override val updates: Flow<Unit> = emptyFlow()

    override suspend fun getInboxEmails(): List<Email> = emptyList()

    override suspend fun getEmail(emailId: String): Email? = null

    override suspend fun getDraftEmails(): List<Email> = emptyList()

    override suspend fun getSentEmails(): List<Email> = emptyList()

    override suspend fun sendEmail(
        to: String,
        subject: String,
        body: String,
        draftId: String?,
    ): Email = throw IllegalStateException("Send failed")

    override suspend fun saveDraft(
        to: String,
        subject: String,
        body: String,
    ): Email = throw IllegalStateException("Save failed")

    override suspend fun deleteDraft(draftId: String): Boolean = false

    override suspend fun updateEmailReadStatus(
        emailId: String,
        isRead: Boolean?,
    ): Email = testEmail()

    override suspend fun resetDemoData(): Boolean = false
}
