package app.example.circuit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import app.example.data.model.Email
import app.example.data.repository.EmailRepository
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.parcelize.Parcelize

/** Screen that lists all draft emails. */
@Parcelize
data object DraftsScreen : Screen {
    sealed class State : CircuitUiState {
        data object Loading : State()

        data class Success(
            val drafts: List<Email>,
            val eventSink: (Event) -> Unit,
        ) : State()

        data class Error(
            val message: String,
            val eventSink: (Event) -> Unit,
        ) : State()
    }

    sealed class Event : CircuitUiEvent {
        data class OnDraftClicked(
            val draftId: String,
        ) : Event()

        data class OnDeleteDraft(
            val draftId: String,
        ) : Event()

        data object OnNewEmail : Event()

        data object OnRetry : Event()

        data object OnBack : Event()
    }
}

@AssistedInject
class DraftsPresenter
    constructor(
        @Assisted private val navigator: Navigator,
        private val emailRepository: EmailRepository,
    ) : Presenter<DraftsScreen.State> {
        @Composable
        override fun present(): DraftsScreen.State {
            var drafts by rememberRetained { mutableStateOf<List<Email>?>(null) }
            var errorMessage by rememberRetained { mutableStateOf<String?>(null) }
            var retryTrigger by rememberRetained { mutableStateOf(0) }

            LaunchedEffect(retryTrigger) {
                drafts = null
                errorMessage = null
                try {
                    drafts = emailRepository.getDraftEmails()
                } catch (e: Exception) {
                    errorMessage = e.message ?: "Unknown error"
                }
            }

            val eventSink: (DraftsScreen.Event) -> Unit = { event ->
                when (event) {
                    is DraftsScreen.Event.OnDraftClicked -> {
                        navigator.goTo(ComposeEmailScreen(draftId = event.draftId))
                    }

                    is DraftsScreen.Event.OnDeleteDraft -> {
                        drafts = drafts?.filter { it.id != event.draftId }
                        retryTrigger++
                    }

                    DraftsScreen.Event.OnNewEmail -> {
                        navigator.goTo(ComposeEmailScreen())
                    }

                    DraftsScreen.Event.OnRetry -> {
                        retryTrigger++
                    }

                    DraftsScreen.Event.OnBack -> {
                        navigator.pop()
                    }
                }
            }

            return when {
                errorMessage != null -> DraftsScreen.State.Error(errorMessage!!, eventSink)
                drafts != null -> DraftsScreen.State.Success(drafts!!, eventSink)
                else -> DraftsScreen.State.Loading
            }
        }

        @CircuitInject(DraftsScreen::class, AppScope::class)
        @AssistedFactory
        interface Factory {
            fun create(navigator: Navigator): DraftsPresenter
        }
    }

@CircuitInject(screen = DraftsScreen::class, scope = AppScope::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraftsContent(
    state: DraftsScreen.State,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is DraftsScreen.State.Loading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is DraftsScreen.State.Error -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp),
                    )
                    Button(onClick = { state.eventSink(DraftsScreen.Event.OnRetry) }) {
                        Text("Retry")
                    }
                }
            }
        }

        is DraftsScreen.State.Success -> {
            Scaffold(
                modifier = modifier,
                topBar = {
                    TopAppBar(
                        title = { Text("Drafts") },
                        navigationIcon = {
                            IconButton(onClick = { state.eventSink(DraftsScreen.Event.OnBack) }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                )
                            }
                        },
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { state.eventSink(DraftsScreen.Event.OnNewEmail) },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "New Email",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                },
            ) { innerPadding ->
                if (state.drafts.isEmpty()) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No drafts",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.padding(innerPadding)) {
                        items(state.drafts) { draft ->
                            DraftItem(
                                draft = draft,
                                onClick = { state.eventSink(DraftsScreen.Event.OnDraftClicked(draft.id)) },
                                onDelete = { state.eventSink(DraftsScreen.Event.OnDeleteDraft(draft.id)) },
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

/** A draft email list item showing subject, recipients, timestamp and a delete button. */
@Composable
fun DraftItem(
    draft: Email,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {},
) {
    Row(
        modifier =
            modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = draft.subject.ifBlank { "(no subject)" },
                style = MaterialTheme.typography.titleSmall,
            )
            if (draft.recipients.isNotEmpty()) {
                Text(
                    text = "To: ${draft.recipients.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.alpha(0.6f),
                )
            }
            Text(
                text = draft.timestamp,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.alpha(0.5f),
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete draft",
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}
