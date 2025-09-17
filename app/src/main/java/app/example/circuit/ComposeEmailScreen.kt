package app.example.circuit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.example.data.Email
import app.example.data.EmailStatus
import app.example.data.ExampleEmailRepository
import com.slack.circuit.codegen.annotations.CircuitInject
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

@Parcelize
data class ComposeEmailScreen(
    val draftId: String? = null,
) : Screen {
    data class State(
        val recipients: String,
        val subject: String,
        val body: String,
        val isDraft: Boolean,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    sealed class Event : CircuitUiEvent {
        data object BackClicked : Event()

        data class UpdateRecipients(
            val recipients: String,
        ) : Event()

        data class UpdateSubject(
            val subject: String,
        ) : Event()

        data class UpdateBody(
            val body: String,
        ) : Event()

        data object SaveDraft : Event()

        data object SendEmail : Event()
    }
}

@Inject
class ComposeEmailPresenter
    constructor(
        @Assisted private val navigator: Navigator,
        @Assisted private val screen: ComposeEmailScreen,
        private val emailRepository: ExampleEmailRepository,
    ) : Presenter<ComposeEmailScreen.State> {
        @Composable
        override fun present(): ComposeEmailScreen.State {
            var recipients by remember { mutableStateOf("") }
            var subject by remember { mutableStateOf("") }
            var body by remember { mutableStateOf("") }
            var isDraft by remember { mutableStateOf(false) }

            // Load draft if editing existing draft
            if (screen.draftId != null && !isDraft) {
                val draft = emailRepository.getEmail(screen.draftId)
                recipients = draft.recipients.joinToString(", ")
                subject = draft.subject
                body = draft.body
                isDraft = true
            }

            return ComposeEmailScreen.State(
                recipients = recipients,
                subject = subject,
                body = body,
                isDraft = isDraft,
            ) { event ->
                when (event) {
                    ComposeEmailScreen.Event.BackClicked -> navigator.pop()
                    is ComposeEmailScreen.Event.UpdateRecipients -> recipients = event.recipients
                    is ComposeEmailScreen.Event.UpdateSubject -> subject = event.subject
                    is ComposeEmailScreen.Event.UpdateBody -> body = event.body
                    ComposeEmailScreen.Event.SaveDraft -> {
                        val email =
                            Email(
                                id = screen.draftId ?: "",
                                subject = subject,
                                body = body,
                                sender = "Me", // In a real app, this would come from user settings
                                timestamp = "",
                                recipients = recipients.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                                status = EmailStatus.DRAFT,
                            )
                        emailRepository.saveEmailAsDraft(email)
                        navigator.pop()
                    }
                    ComposeEmailScreen.Event.SendEmail -> {
                        if (screen.draftId != null) {
                            emailRepository.sendEmail(screen.draftId)
                        } else {
                            // Create draft first, then send
                            val email =
                                Email(
                                    id = "",
                                    subject = subject,
                                    body = body,
                                    sender = "Me",
                                    timestamp = "",
                                    recipients = recipients.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                                    status = EmailStatus.DRAFT,
                                )
                            val savedDraft = emailRepository.saveEmailAsDraft(email)
                            emailRepository.sendEmail(savedDraft.id)
                        }
                        navigator.pop()
                    }
                }
            }
        }

        @CircuitInject(ComposeEmailScreen::class, AppScope::class)
        @AssistedFactory
        interface Factory {
            fun create(
                navigator: Navigator,
                screen: ComposeEmailScreen,
            ): ComposeEmailPresenter
        }
    }

@CircuitInject(ComposeEmailScreen::class, AppScope::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeEmail(
    state: ComposeEmailScreen.State,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Compose Email") },
                navigationIcon = {
                    IconButton(onClick = { state.eventSink(ComposeEmailScreen.Event.BackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = state.recipients,
                onValueChange = { state.eventSink(ComposeEmailScreen.Event.UpdateRecipients(it)) },
                label = { Text("To") },
                placeholder = { Text("email@example.com") },
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = state.subject,
                onValueChange = { state.eventSink(ComposeEmailScreen.Event.UpdateSubject(it)) },
                label = { Text("Subject") },
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = state.body,
                onValueChange = { state.eventSink(ComposeEmailScreen.Event.UpdateBody(it)) },
                label = { Text("Message") },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                minLines = 8,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = { state.eventSink(ComposeEmailScreen.Event.SaveDraft) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Save Draft")
                }

                Button(
                    onClick = { state.eventSink(ComposeEmailScreen.Event.SendEmail) },
                    modifier = Modifier.weight(1f),
                    enabled = state.recipients.isNotEmpty() && state.subject.isNotEmpty(),
                ) {
                    Text("Send")
                }
            }
        }
    }
}
