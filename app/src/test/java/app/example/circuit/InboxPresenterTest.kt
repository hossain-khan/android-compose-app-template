package app.example.circuit

import app.example.data.AppVersionService
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [InboxPresenter] using Circuit's [Presenter.test] helper and [FakeNavigator].
 *
 * Tests verify the UDF state machine: initial loading, success with data, error handling,
 * and navigation events. Circuit's test utilities allow direct interaction with the presenter
 * without mocking Compose internals.
 *
 * See https://slackhq.github.io/circuit/testing/#presenter-unit-tests
 */
class InboxPresenterTest {
    private val fakeNavigator = FakeNavigator(InboxScreen)
    private val fakeVersionService = AppVersionService { "1.0.0-test" }

    @Test
    fun `present - emits Loading then Success when repository returns emails`() =
        runTest {
            val emails = listOf(testEmail(id = "1"), testEmail(id = "2"))
            val presenter =
                InboxPresenter(
                    navigator = fakeNavigator,
                    emailRepository = FakeEmailRepository(inboxEmails = emails),
                    appVersionService = fakeVersionService,
                )

            presenter.test {
                assertEquals(InboxScreen.State.Loading, awaitItem())

                val successState = awaitItem() as InboxScreen.State.Success
                assertEquals(emails, successState.emails)
                assertFalse(successState.showAppInfo)
            }
        }

    @Test
    fun `present - emits Loading then Error when repository throws`() =
        runTest {
            val presenter =
                InboxPresenter(
                    navigator = fakeNavigator,
                    emailRepository =
                        FakeEmailRepository(
                            getInboxException = Exception("Network error"),
                        ),
                    appVersionService = fakeVersionService,
                )

            presenter.test {
                assertEquals(InboxScreen.State.Loading, awaitItem())

                val errorState = awaitItem() as InboxScreen.State.Error
                assertEquals("Network error", errorState.message)
            }
        }

    @Test
    fun `present - EmailClicked event navigates to DetailScreen`() =
        runTest {
            val email = testEmail(id = "email-123")
            val presenter =
                InboxPresenter(
                    navigator = fakeNavigator,
                    emailRepository = FakeEmailRepository(inboxEmails = listOf(email)),
                    appVersionService = fakeVersionService,
                )

            presenter.test {
                assertEquals(InboxScreen.State.Loading, awaitItem())
                val successState = awaitItem() as InboxScreen.State.Success

                successState.eventSink(InboxScreen.Event.EmailClicked("email-123"))
            }

            assertEquals(DetailScreen("email-123"), fakeNavigator.awaitNextScreen())
        }

    @Test
    fun `present - OnViewDrafts event navigates to DraftsScreen`() =
        runTest {
            val presenter =
                InboxPresenter(
                    navigator = fakeNavigator,
                    emailRepository = FakeEmailRepository(inboxEmails = listOf(testEmail())),
                    appVersionService = fakeVersionService,
                )

            presenter.test {
                assertEquals(InboxScreen.State.Loading, awaitItem())
                val successState = awaitItem() as InboxScreen.State.Success

                successState.eventSink(InboxScreen.Event.OnViewDrafts)
            }

            assertEquals(DraftsScreen, fakeNavigator.awaitNextScreen())
        }

    @Test
    fun `present - OnViewSent event navigates to SentScreen`() =
        runTest {
            val presenter =
                InboxPresenter(
                    navigator = fakeNavigator,
                    emailRepository = FakeEmailRepository(inboxEmails = listOf(testEmail())),
                    appVersionService = fakeVersionService,
                )

            presenter.test {
                assertEquals(InboxScreen.State.Loading, awaitItem())
                val successState = awaitItem() as InboxScreen.State.Success

                successState.eventSink(InboxScreen.Event.OnViewSent)
            }

            assertEquals(SentScreen, fakeNavigator.awaitNextScreen())
        }

    @Test
    fun `present - OnNewEmail event navigates to ComposeEmailScreen`() =
        runTest {
            val presenter =
                InboxPresenter(
                    navigator = fakeNavigator,
                    emailRepository = FakeEmailRepository(inboxEmails = listOf(testEmail())),
                    appVersionService = fakeVersionService,
                )

            presenter.test {
                assertEquals(InboxScreen.State.Loading, awaitItem())
                val successState = awaitItem() as InboxScreen.State.Success

                successState.eventSink(InboxScreen.Event.OnNewEmail)
            }

            assertEquals(ComposeEmailScreen(), fakeNavigator.awaitNextScreen())
        }

    @Test
    fun `present - InfoClicked event shows app info overlay`() =
        runTest {
            val presenter =
                InboxPresenter(
                    navigator = fakeNavigator,
                    emailRepository = FakeEmailRepository(inboxEmails = listOf(testEmail())),
                    appVersionService = fakeVersionService,
                )

            presenter.test {
                assertEquals(InboxScreen.State.Loading, awaitItem())
                val successState = awaitItem() as InboxScreen.State.Success
                assertFalse(successState.showAppInfo)

                successState.eventSink(InboxScreen.Event.InfoClicked)

                val withInfoState = awaitItem() as InboxScreen.State.Success
                assertTrue(withInfoState.showAppInfo)
            }
        }

    @Test
    fun `present - InfoDismissed event hides app info overlay`() =
        runTest {
            val presenter =
                InboxPresenter(
                    navigator = fakeNavigator,
                    emailRepository = FakeEmailRepository(inboxEmails = listOf(testEmail())),
                    appVersionService = fakeVersionService,
                )

            presenter.test {
                assertEquals(InboxScreen.State.Loading, awaitItem())
                val successState = awaitItem() as InboxScreen.State.Success

                successState.eventSink(InboxScreen.Event.InfoClicked)
                val withInfoState = awaitItem() as InboxScreen.State.Success
                assertTrue(withInfoState.showAppInfo)

                withInfoState.eventSink(InboxScreen.Event.InfoDismissed)
                val dismissedState = awaitItem() as InboxScreen.State.Success
                assertFalse(dismissedState.showAppInfo)
            }
        }
}
