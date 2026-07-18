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
                assertEquals(ScreenTab.INBOX, successState.selectedTab)
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
    fun `present - TabSelected event updates selected tab`() =
        runTest {
            val presenter =
                InboxPresenter(
                    navigator = fakeNavigator,
                    emailRepository = FakeEmailRepository(
                        inboxEmails = listOf(testEmail()),
                        draftEmails = listOf(testEmail(id = "draft-1"))
                    ),
                    appVersionService = fakeVersionService,
                )

            presenter.test {
                assertEquals(InboxScreen.State.Loading, awaitItem())
                val successState = awaitItem() as InboxScreen.State.Success
                assertEquals(ScreenTab.INBOX, successState.selectedTab)

                // Select DRAFTS tab
                successState.eventSink(InboxScreen.Event.TabSelected(ScreenTab.DRAFTS))
                
                // Emits Success tab change with old inbox emails first
                val firstEmit = awaitItem() as InboxScreen.State.Success
                assertEquals(ScreenTab.DRAFTS, firstEmit.selectedTab)
                
                // Emits Success with drafts (loading is skipped synchronously in tests)
                val draftsState = awaitItem() as InboxScreen.State.Success
                assertEquals(ScreenTab.DRAFTS, draftsState.selectedTab)
                assertEquals("draft-1", draftsState.emails.first().id)
            }
        }

    @Test
    fun `present - DeleteDraft event optimistically filters list`() =
        runTest {
            val drafts = listOf(testEmail(id = "draft-1"), testEmail(id = "draft-2"))
            val presenter =
                InboxPresenter(
                    navigator = fakeNavigator,
                    emailRepository = FakeEmailRepository(draftEmails = drafts),
                    appVersionService = fakeVersionService,
                )

            presenter.test {
                assertEquals(InboxScreen.State.Loading, awaitItem())
                val initialSuccess = awaitItem() as InboxScreen.State.Success
                
                // Select DRAFTS tab first
                initialSuccess.eventSink(InboxScreen.Event.TabSelected(ScreenTab.DRAFTS))
                
                // Consume tab change emissions
                awaitItem() // Success tab change (old inbox emails)
                val draftsSuccess = awaitItem() as InboxScreen.State.Success // Success drafts
                assertEquals(2, draftsSuccess.emails.size)

                // Trigger delete
                draftsSuccess.eventSink(InboxScreen.Event.DeleteDraft("draft-1"))
                
                // Expect optimistic UI update (removal of draft-1)
                val updatedSuccess = awaitItem() as InboxScreen.State.Success
                assertEquals(1, updatedSuccess.emails.size)
                assertEquals("draft-2", updatedSuccess.emails.first().id)
                cancelAndIgnoreRemainingEvents()
            }
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
