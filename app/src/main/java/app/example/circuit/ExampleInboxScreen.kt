package app.example.circuit

// -------------------------------------------------------------------------------------
//
// THIS IS AN EXAMPLE FILE WITH CIRCUIT SCREENS AND PRESENTERS
// Example content is taken from https://slackhq.github.io/circuit/tutorial/
//
// You should delete this file and create your own screens with presenters.
//
//  -------------------------------------------------------------------------------------

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.example.circuit.overlay.AppInfoOverlay
import app.example.data.Email
import app.example.data.EmailStatus
import app.example.data.ExampleAppVersionService
import app.example.data.ExampleEmailRepository
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.overlay.LocalOverlayHost
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

// See https://slackhq.github.io/circuit/screen/
@Parcelize
data object InboxScreen : Screen {
    data class State(
        val inboxEmails: List<Email>,
        val draftEmails: List<Email>,
        val sentEmails: List<Email>,
        val selectedTabIndex: Int,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    sealed class Event : CircuitUiEvent {
        data class EmailClicked(
            val emailId: String,
        ) : Event()

        data class TabSelected(
            val index: Int,
        ) : Event()

        data object ComposeEmailClicked : Event()

        data object InfoClicked : Event()
    }
}

// See https://slackhq.github.io/circuit/presenter/
@Inject
class InboxPresenter
    constructor(
        @Assisted private val navigator: Navigator,
        private val emailRepository: ExampleEmailRepository,
        private val appVersionService: ExampleAppVersionService,
    ) : Presenter<InboxScreen.State> {
        @Composable
        override fun present(): InboxScreen.State {
            var selectedTabIndex by remember { mutableIntStateOf(0) }

            val inboxEmails by produceState<List<Email>>(initialValue = emptyList()) {
                value = emailRepository.getEmailsByStatus(EmailStatus.INBOX)
            }

            val draftEmails by produceState<List<Email>>(initialValue = emptyList()) {
                value = emailRepository.getEmailsByStatus(EmailStatus.DRAFT)
            }

            val sentEmails by produceState<List<Email>>(initialValue = emptyList()) {
                value = emailRepository.getEmailsByStatus(EmailStatus.SENT)
            }

            // This is just example of how the DI injected service is used in this presenter
            Log.d("InboxPresenter", "Application version: ${appVersionService.getApplicationVersion()}")

            return InboxScreen.State(
                inboxEmails = inboxEmails,
                draftEmails = draftEmails,
                sentEmails = sentEmails,
                selectedTabIndex = selectedTabIndex,
            ) { event ->
                when (event) {
                    // Navigate to the detail screen when an email is clicked
                    is InboxScreen.Event.EmailClicked -> {
                        val email = emailRepository.getEmail(event.emailId)
                        if (email.status == EmailStatus.DRAFT) {
                            navigator.goTo(ComposeEmailScreen(draftId = event.emailId))
                        } else {
                            navigator.goTo(DetailScreen(event.emailId))
                        }
                    }
                    is InboxScreen.Event.TabSelected -> selectedTabIndex = event.index
                    InboxScreen.Event.ComposeEmailClicked -> navigator.goTo(ComposeEmailScreen())
                    // Show app info overlay when info button is clicked
                    InboxScreen.Event.InfoClicked -> {
                        // Overlay will be handled in the UI layer
                    }
                }
            }
        }

        @CircuitInject(InboxScreen::class, AppScope::class)
        @AssistedFactory
        interface Factory {
            fun create(navigator: Navigator): InboxPresenter
        }
    }

@CircuitInject(screen = InboxScreen::class, scope = AppScope::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Inbox(
    state: InboxScreen.State,
    modifier: Modifier = Modifier,
) {
    val overlayHost = LocalOverlayHost.current
    val coroutineScope = rememberCoroutineScope()

    val tabs = listOf("Inbox", "Drafts", "Sent")

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Email") },
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                overlayHost.show(AppInfoOverlay())
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "App Info",
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { state.eventSink(InboxScreen.Event.ComposeEmailClicked) },
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Compose Email",
                )
            }
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            PrimaryTabRow(selectedTabIndex = state.selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = state.selectedTabIndex == index,
                        onClick = { state.eventSink(InboxScreen.Event.TabSelected(index)) },
                        text = { Text(title) },
                    )
                }
            }

            val currentEmails =
                when (state.selectedTabIndex) {
                    0 -> state.inboxEmails
                    1 -> state.draftEmails
                    2 -> state.sentEmails
                    else -> state.inboxEmails
                }

            LazyColumn {
                items(currentEmails) { email ->
                    EmailItem(
                        email = email,
                        onClick = { state.eventSink(InboxScreen.Event.EmailClicked(email.id)) },
                    )
                }
            }
        }
    }
}
