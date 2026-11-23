package dev.hossain.compose.example.circuit

import com.google.common.truth.Truth.assertThat
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import dev.hossain.compose.example.data.AppVersionService
import dev.hossain.compose.example.data.network.NetworkMonitor
import dev.hossain.compose.example.data.preferences.ThemeMode
import dev.hossain.compose.example.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Unit tests for [InboxPresenter] using Circuit's [Presenter.test] helper and [FakeNavigator].
 */
class InboxPresenterTest {
    private val fakeNavigator = FakeNavigator(InboxScreen)
    private val fakeVersionService = AppVersionService { "1.0.0-test" }
    private val fakeNetworkMonitor = FakeNetworkMonitor()
    private val fakeUserPreferencesRepository = FakeUserPreferencesRepository()

    @Test
    fun `present - emits Loading then Success when repository returns emails`() =
        runTest {
            val emails = listOf(testEmail(id = "1"), testEmail(id = "2"))
            val presenter =
                InboxPresenter(
                    navigator = fakeNavigator,
                    emailRepository = FakeEmailRepository(inboxEmails = emails),
                    appVersionService = fakeVersionService,
                    networkMonitor = fakeNetworkMonitor,
                    userPreferencesRepository = fakeUserPreferencesRepository,
                )

            presenter.test {
                assertThat(awaitItem()).isEqualTo(InboxScreen.State.Loading)

                val successState = awaitItem() as InboxScreen.State.Success
                assertThat(successState.emails).isEqualTo(emails)
                assertThat(successState.selectedTab).isEqualTo(ScreenTab.INBOX)
                assertThat(successState.showAppInfo).isFalse()
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
                    networkMonitor = fakeNetworkMonitor,
                    userPreferencesRepository = fakeUserPreferencesRepository,
                )

            presenter.test {
                assertThat(awaitItem()).isEqualTo(InboxScreen.State.Loading)

                val errorState = awaitItem() as InboxScreen.State.Error
                assertThat(errorState.message).isEqualTo("Network error")
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
                    networkMonitor = fakeNetworkMonitor,
                    userPreferencesRepository = fakeUserPreferencesRepository,
                )

            presenter.test {
                assertThat(awaitItem()).isEqualTo(InboxScreen.State.Loading)
                val successState = awaitItem() as InboxScreen.State.Success

                successState.eventSink(InboxScreen.Event.EmailClicked("email-123"))
            }

            assertThat(fakeNavigator.awaitNextScreen()).isEqualTo(DetailScreen("email-123"))
        }

    @Test
    fun `present - TabSelected event updates selected tab`() =
        runTest {
            val presenter =
                InboxPresenter(
                    navigator = fakeNavigator,
                    emailRepository =
                        FakeEmailRepository(
                            inboxEmails = listOf(testEmail()),
                            draftEmails = listOf(testEmail(id = "draft-1")),
                        ),
                    appVersionService = fakeVersionService,
                    networkMonitor = fakeNetworkMonitor,
                    userPreferencesRepository = fakeUserPreferencesRepository,
                )

            presenter.test {
                assertThat(awaitItem()).isEqualTo(InboxScreen.State.Loading)
                val successState = awaitItem() as InboxScreen.State.Success
                assertThat(successState.selectedTab).isEqualTo(ScreenTab.INBOX)

                // Select DRAFTS tab
                successState.eventSink(InboxScreen.Event.TabSelected(ScreenTab.DRAFTS))

                // Emits Loading immediately as emails list is cleared synchronously
                assertThat(awaitItem()).isEqualTo(InboxScreen.State.Loading)

                // Emits Success with drafts
                val draftsState = awaitItem() as InboxScreen.State.Success
                assertThat(draftsState.selectedTab).isEqualTo(ScreenTab.DRAFTS)
                assertThat(draftsState.emails.first().id).isEqualTo("draft-1")
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
                    networkMonitor = fakeNetworkMonitor,
                    userPreferencesRepository = fakeUserPreferencesRepository,
                )

            presenter.test {
                assertThat(awaitItem()).isEqualTo(InboxScreen.State.Loading)
                val initialSuccess = awaitItem() as InboxScreen.State.Success

                // Select DRAFTS tab first
                initialSuccess.eventSink(InboxScreen.Event.TabSelected(ScreenTab.DRAFTS))

                // Consume tab change emissions
                awaitItem() // Success tab change (old inbox emails)
                val draftsSuccess = awaitItem() as InboxScreen.State.Success // Success drafts
                assertThat(draftsSuccess.emails).hasSize(2)

                // Trigger delete
                draftsSuccess.eventSink(InboxScreen.Event.DeleteDraft("draft-1"))

                // Expect optimistic UI update (removal of draft-1)
                val updatedSuccess = awaitItem() as InboxScreen.State.Success
                assertThat(updatedSuccess.emails).hasSize(1)
                assertThat(updatedSuccess.emails.first().id).isEqualTo("draft-2")
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
                    networkMonitor = fakeNetworkMonitor,
                    userPreferencesRepository = fakeUserPreferencesRepository,
                )

            presenter.test {
                assertThat(awaitItem()).isEqualTo(InboxScreen.State.Loading)
                val successState = awaitItem() as InboxScreen.State.Success

                successState.eventSink(InboxScreen.Event.OnNewEmail)
            }

            assertThat(fakeNavigator.awaitNextScreen()).isEqualTo(ComposeEmailScreen())
        }

    @Test
    fun `present - InfoClicked event shows app info overlay`() =
        runTest {
            val presenter =
                InboxPresenter(
                    navigator = fakeNavigator,
                    emailRepository = FakeEmailRepository(inboxEmails = listOf(testEmail())),
                    appVersionService = fakeVersionService,
                    networkMonitor = fakeNetworkMonitor,
                    userPreferencesRepository = fakeUserPreferencesRepository,
                )

            presenter.test {
                assertThat(awaitItem()).isEqualTo(InboxScreen.State.Loading)
                val successState = awaitItem() as InboxScreen.State.Success
                assertThat(successState.showAppInfo).isFalse()

                successState.eventSink(InboxScreen.Event.InfoClicked)

                val withInfoState = awaitItem() as InboxScreen.State.Success
                assertThat(withInfoState.showAppInfo).isTrue()
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
                    networkMonitor = fakeNetworkMonitor,
                    userPreferencesRepository = fakeUserPreferencesRepository,
                )

            presenter.test {
                assertThat(awaitItem()).isEqualTo(InboxScreen.State.Loading)
                val successState = awaitItem() as InboxScreen.State.Success

                successState.eventSink(InboxScreen.Event.InfoClicked)
                val withInfoState = awaitItem() as InboxScreen.State.Success
                assertThat(withInfoState.showAppInfo).isTrue()

                withInfoState.eventSink(InboxScreen.Event.InfoDismissed)
                val dismissedState = awaitItem() as InboxScreen.State.Success
                assertThat(dismissedState.showAppInfo).isFalse()
            }
        }

    @Test
    fun `present - OnToggleReadStatus event triggers repository update`() =
        runTest {
            val presenter =
                InboxPresenter(
                    navigator = fakeNavigator,
                    emailRepository = FakeEmailRepository(inboxEmails = listOf(testEmail(id = "1", isRead = false))),
                    appVersionService = fakeVersionService,
                    networkMonitor = fakeNetworkMonitor,
                    userPreferencesRepository = fakeUserPreferencesRepository,
                )

            presenter.test {
                assertThat(awaitItem()).isEqualTo(InboxScreen.State.Loading)
                val successState = awaitItem() as InboxScreen.State.Success

                successState.eventSink(InboxScreen.Event.OnToggleReadStatus("1", isRead = true))
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `present - OnResetDemoData event triggers repository system reset`() =
        runTest {
            val presenter =
                InboxPresenter(
                    navigator = fakeNavigator,
                    emailRepository = FakeEmailRepository(inboxEmails = listOf(testEmail())),
                    appVersionService = fakeVersionService,
                    networkMonitor = fakeNetworkMonitor,
                    userPreferencesRepository = fakeUserPreferencesRepository,
                )

            presenter.test {
                assertThat(awaitItem()).isEqualTo(InboxScreen.State.Loading)
                val successState = awaitItem() as InboxScreen.State.Success

                successState.eventSink(InboxScreen.Event.OnResetDemoData)
                cancelAndIgnoreRemainingEvents()
            }
        }
}

class FakeNetworkMonitor(
    initialOnline: Boolean = true,
) : NetworkMonitor {
    private val _isOnline = MutableStateFlow(initialOnline)
    override val isOnline: Flow<Boolean> = _isOnline.asStateFlow()
}

class FakeUserPreferencesRepository(
    initialThemeMode: ThemeMode = ThemeMode.SYSTEM,
    initialShowUnreadOnly: Boolean = false,
) : UserPreferencesRepository {
    private val _themeMode = MutableStateFlow(initialThemeMode)
    private val _showUnreadOnly = MutableStateFlow(initialShowUnreadOnly)

    override val themeMode: Flow<ThemeMode> = _themeMode.asStateFlow()
    override val showUnreadOnly: Flow<Boolean> = _showUnreadOnly.asStateFlow()

    override suspend fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    override suspend fun setShowUnreadOnly(unreadOnly: Boolean) {
        _showUnreadOnly.value = unreadOnly
    }
}
