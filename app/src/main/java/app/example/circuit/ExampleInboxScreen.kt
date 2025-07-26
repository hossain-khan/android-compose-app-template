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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import app.example.data.Email
import app.example.data.ExampleAppVersionService
import app.example.data.ExampleEmailRepository
import app.example.overlay.AppInfoOverlay
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.Inject
import kotlinx.parcelize.Parcelize

// See https://slackhq.github.io/circuit/screen/
@Parcelize
data object InboxScreen : Screen {
    data class State(
        val emails: List<Email>,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    sealed class Event : CircuitUiEvent {
        data class EmailClicked(
            val emailId: String,
        ) : Event()

        data object InfoClicked : Event()
    }
}

// See https://slackhq.github.io/circuit/presenter/
@Inject
class InboxPresenter
    constructor(
        @Assisted private val navigator: Navigator,
        @Assisted private val overlayNavigator: OverlayNavigator,
        private val emailRepository: ExampleEmailRepository,
        private val appVersionService: ExampleAppVersionService,
    ) : Presenter<InboxScreen.State> {
        @Composable
        override fun present(): InboxScreen.State {
            val emails by produceState<List<Email>>(initialValue = emptyList()) {
                value = emailRepository.getEmails()
            }

            // This is just example of how the DI injected service is used in this presenter
            Log.d("InboxPresenter", "Application version: ${appVersionService.getApplicationVersion()}")

            return InboxScreen.State(emails) { event ->
                when (event) {
                    // Navigate to the detail screen when an email is clicked
                    is InboxScreen.Event.EmailClicked -> navigator.goTo(DetailScreen(event.emailId))
                    // Show app info overlay when info button is clicked
                    InboxScreen.Event.InfoClicked -> {
                        overlayNavigator.show(AppInfoOverlay)
                    }
                }
            }
        }

        @CircuitInject(InboxScreen::class, AppScope::class)
        @AssistedFactory
        interface Factory {
            fun create(
                navigator: Navigator,
                overlayNavigator: OverlayNavigator,
            ): InboxPresenter
        }
    }

@CircuitInject(screen = InboxScreen::class, scope = AppScope::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Inbox(
    state: InboxScreen.State,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Inbox") },
                actions = {
                    IconButton(
                        onClick = { state.eventSink(InboxScreen.Event.InfoClicked) },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "App Info",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(state.emails) { email ->
                EmailItem(
                    email = email,
                    onClick = { state.eventSink(InboxScreen.Event.EmailClicked(email.id)) },
                )
            }
        }
    }
}
