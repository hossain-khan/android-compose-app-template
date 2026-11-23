package dev.hossain.compose.example.circuit

import com.google.common.truth.Truth.assertThat
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import dev.hossain.compose.example.data.ExampleEmailValidator
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Unit tests for [DetailPresenter] using Circuit's [Presenter.test] helper and [FakeNavigator].
 *
 * Tests verify the full state machine: initial loading, success with email data, error handling
 * when email is not found, and back/retry navigation events.
 *
 * See https://slackhq.github.io/circuit/testing/#presenter-unit-tests
 */
class DetailPresenterTest {
    private val screen = DetailScreen(emailId = "email-1")
    private val fakeNavigator = FakeNavigator(screen)
    private val emailValidator = ExampleEmailValidator()

    @Test
    fun `present - emits Loading then Success when email is found`() =
        runTest {
            val email = testEmail(id = "email-1", subject = "Hello World", isRead = true)
            val presenter =
                DetailPresenter(
                    navigator = fakeNavigator,
                    screen = screen,
                    emailRepository = FakeEmailRepository(inboxEmails = listOf(email)),
                    exampleEmailValidator = emailValidator,
                )

            presenter.test {
                assertThat(awaitItem()).isEqualTo(DetailScreen.State.Loading)

                val successState = awaitItem() as DetailScreen.State.Success
                assertThat(successState.email).isEqualTo(email)
            }
        }

    @Test
    fun `present - emits Loading then Error when email is not found`() =
        runTest {
            val presenter =
                DetailPresenter(
                    navigator = fakeNavigator,
                    screen = screen,
                    emailRepository = FakeEmailRepository(inboxEmails = emptyList()),
                    exampleEmailValidator = emailValidator,
                )

            presenter.test {
                assertThat(awaitItem()).isEqualTo(DetailScreen.State.Loading)

                val errorState = awaitItem() as DetailScreen.State.Error
                assertThat(errorState.message).isEqualTo("Email not found")
            }
        }

    @Test
    fun `present - emits Loading then Error when repository throws`() =
        runTest {
            val presenter =
                DetailPresenter(
                    navigator = fakeNavigator,
                    screen = screen,
                    emailRepository =
                        FakeEmailRepository(
                            getInboxException = Exception("Connection timeout"),
                        ),
                    exampleEmailValidator = emailValidator,
                )

            presenter.test {
                assertThat(awaitItem()).isEqualTo(DetailScreen.State.Loading)

                val errorState = awaitItem() as DetailScreen.State.Error
                assertThat(errorState.message).isEqualTo("Connection timeout")
            }
        }

    @Test
    fun `present - BackClicked event pops the back stack`() =
        runTest {
            val email = testEmail(id = "email-1", isRead = true)
            val presenter =
                DetailPresenter(
                    navigator = fakeNavigator,
                    screen = screen,
                    emailRepository = FakeEmailRepository(inboxEmails = listOf(email)),
                    exampleEmailValidator = emailValidator,
                )

            presenter.test {
                assertThat(awaitItem()).isEqualTo(DetailScreen.State.Loading)
                val successState = awaitItem() as DetailScreen.State.Success

                successState.eventSink(DetailScreen.Event.BackClicked)
            }

            assertThat(fakeNavigator.awaitPop()).isNotNull()
        }

    @Test
    fun `present - BackClicked from Error pops the back stack`() =
        runTest {
            val presenter =
                DetailPresenter(
                    navigator = fakeNavigator,
                    screen = screen,
                    emailRepository =
                        FakeEmailRepository(
                            getInboxException = Exception("Error"),
                        ),
                    exampleEmailValidator = emailValidator,
                )

            presenter.test {
                assertThat(awaitItem()).isEqualTo(DetailScreen.State.Loading)
                val errorState = awaitItem() as DetailScreen.State.Error

                errorState.eventSink(DetailScreen.Event.BackClicked)
            }

            assertThat(fakeNavigator.awaitPop()).isNotNull()
        }
}
