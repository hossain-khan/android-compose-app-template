package app.example.circuit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.example.R
import app.example.data.model.Email
import app.example.data.repository.EmailRepository
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.ParcelableScreen
import com.slack.circuit.runtime.screen.Screen
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.parcelize.Parcelize
import org.json.JSONObject
import retrofit2.HttpException

/** Screen for composing a new email or editing an existing draft. */
@Parcelize
data class ComposeEmailScreen(
    val draftId: String? = null,
) : ParcelableScreen {
    // See https://slackhq.github.io/circuit/states-and-events/
    @Stable
    sealed interface State : CircuitUiState {
        data object Loading : State

        data class Success(
            val to: String,
            val subject: String,
            val body: String,
            val isSaving: Boolean,
            val errorMessage: String? = null,
            val eventSink: (Event) -> Unit,
        ) : State

        data class Error(
            val message: String,
            val eventSink: (Event) -> Unit,
        ) : State
    }

    @Immutable
    sealed interface Event : CircuitUiEvent {
        data class OnToChanged(
            val to: String,
        ) : Event

        data class OnSubjectChanged(
            val subject: String,
        ) : Event

        data class OnBodyChanged(
            val body: String,
        ) : Event

        data object OnSaveDraft : Event

        data object OnSend : Event

        data object OnBack : Event
    }
}

/** Represents a pending async action triggered from the compose screen. */
private enum class PendingAction { SAVE_DRAFT, SEND }

@AssistedInject
class ComposeEmailPresenter
    constructor(
        @Assisted private val navigator: Navigator,
        @Assisted private val screen: ComposeEmailScreen,
        private val emailRepository: EmailRepository,
    ) : Presenter<ComposeEmailScreen.State> {
        @Composable
        override fun present(): ComposeEmailScreen.State {
            var to by rememberRetained { mutableStateOf("") }
            var subject by rememberRetained { mutableStateOf("") }
            var body by rememberRetained { mutableStateOf("") }
            var isSaving by rememberRetained { mutableStateOf(false) }
            var isLoading by rememberRetained { mutableStateOf(screen.draftId != null) }
            var loadError by rememberRetained { mutableStateOf<String?>(null) }
            var actionError by rememberRetained { mutableStateOf<String?>(null) }
            var loadedDraft by rememberRetained { mutableStateOf<Email?>(null) }
            var pendingAction by rememberRetained { mutableStateOf<PendingAction?>(null) }

            // Load the draft if editing an existing one
            LaunchedEffect(screen.draftId) {
                if (screen.draftId != null && loadedDraft == null) {
                    isLoading = true
                    loadError = null
                    try {
                        val drafts = emailRepository.getDraftEmails()
                        val draft = drafts.find { it.id == screen.draftId }
                        if (draft != null) {
                            to = draft.recipients.joinToString(", ")
                            subject = draft.subject
                            body = draft.body
                            loadedDraft = draft
                        } else {
                            loadError = "Draft not found"
                        }
                    } catch (e: Exception) {
                        loadError = e.message ?: "Failed to load draft"
                    } finally {
                        isLoading = false
                    }
                }
            }

            // Execute pending save/send actions
            LaunchedEffect(pendingAction) {
                val action = pendingAction ?: return@LaunchedEffect
                try {
                    when (action) {
                        PendingAction.SAVE_DRAFT -> emailRepository.saveDraft(to, subject, body)
                        PendingAction.SEND -> emailRepository.sendEmail(to, subject, body)
                    }
                    navigator.pop()
                } catch (e: HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    val parsedError =
                        try {
                            if (!errorBody.isNullOrBlank()) {
                                val json = JSONObject(errorBody)
                                if (json.has("error")) json.getString("error") else null
                            } else {
                                null
                            }
                        } catch (jsonEx: Exception) {
                            null
                        }
                    actionError = parsedError ?: e.message() ?: "Operation failed"
                } catch (e: Exception) {
                    actionError = e.message ?: "Operation failed"
                } finally {
                    isSaving = false
                    pendingAction = null
                }
            }

            val eventSink: (ComposeEmailScreen.Event) -> Unit = { event ->
                when (event) {
                    is ComposeEmailScreen.Event.OnToChanged -> {
                        to = event.to
                        actionError = null
                    }

                    is ComposeEmailScreen.Event.OnSubjectChanged -> {
                        subject = event.subject
                        actionError = null
                    }

                    is ComposeEmailScreen.Event.OnBodyChanged -> {
                        body = event.body
                        actionError = null
                    }

                    ComposeEmailScreen.Event.OnBack -> {
                        navigator.pop()
                    }

                    ComposeEmailScreen.Event.OnSaveDraft -> {
                        isSaving = true
                        pendingAction = PendingAction.SAVE_DRAFT
                    }

                    ComposeEmailScreen.Event.OnSend -> {
                        isSaving = true
                        pendingAction = PendingAction.SEND
                    }
                }
            }

            return when {
                isLoading -> {
                    ComposeEmailScreen.State.Loading
                }

                loadError != null -> {
                    ComposeEmailScreen.State.Error(
                        message = loadError!!,
                        eventSink = eventSink,
                    )
                }

                else -> {
                    ComposeEmailScreen.State.Success(
                        to = to,
                        subject = subject,
                        body = body,
                        isSaving = isSaving,
                        errorMessage = actionError,
                        eventSink = eventSink,
                    )
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
fun ComposeEmailContent(
    state: ComposeEmailScreen.State,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is ComposeEmailScreen.State.Loading -> {
            Box(
                modifier =
                    modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        is ComposeEmailScreen.State.Error -> {
            Box(
                modifier =
                    modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp),
                    )
                    Button(onClick = { state.eventSink(ComposeEmailScreen.Event.OnBack) }) {
                        Text("Go Back")
                    }
                }
            }
        }

        is ComposeEmailScreen.State.Success -> {
            Scaffold(
                modifier = modifier,
                topBar = {
                    TopAppBar(
                        title = { Text("Compose Email") },
                        navigationIcon = {
                            IconButton(onClick = { state.eventSink(ComposeEmailScreen.Event.OnBack) }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.arrow_back_24dp),
                                    contentDescription = "Back",
                                )
                            }
                        },
                    )
                },
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                    ) {
                        OutlinedTextField(
                            value = state.to,
                            onValueChange = { state.eventSink(ComposeEmailScreen.Event.OnToChanged(it)) },
                            label = { Text("To") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isSaving,
                            singleLine = true,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.subject,
                            onValueChange = { state.eventSink(ComposeEmailScreen.Event.OnSubjectChanged(it)) },
                            label = { Text("Subject") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isSaving,
                            singleLine = true,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.body,
                            onValueChange = { state.eventSink(ComposeEmailScreen.Event.OnBodyChanged(it)) },
                            label = { Text("Body") },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                            enabled = !state.isSaving,
                            maxLines = 10,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        if (state.errorMessage != null) {
                            Text(
                                text = state.errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 16.dp),
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { state.eventSink(ComposeEmailScreen.Event.OnSaveDraft) },
                                modifier = Modifier.weight(1f),
                                enabled = !state.isSaving,
                            ) {
                                Text("Save Draft")
                            }
                            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                            Button(
                                onClick = { state.eventSink(ComposeEmailScreen.Event.OnSend) },
                                modifier = Modifier.weight(1f),
                                enabled = !state.isSaving,
                            ) {
                                Text("Send")
                            }
                        }
                    }

                    if (state.isSaving) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}
