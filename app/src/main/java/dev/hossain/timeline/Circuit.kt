package dev.hossain.timeline

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dev.hossain.timeline.di.AppScope
import kotlinx.parcelize.Parcelize

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
    }
}

@CircuitInject(screen = InboxScreen::class, scope = AppScope::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Inbox(
    state: InboxScreen.State,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier, topBar = { TopAppBar(title = { Text("Inbox") }) }) { innerPadding ->
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

// Write one or use EmailItem from ui.kt

/** A simple email item to show in a list. */
@Composable
fun EmailItem(
    email: Email,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Row(
        modifier.clickable(onClick = onClick).padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Image(
            Icons.Default.Person,
            modifier =
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Magenta)
                    .padding(4.dp),
            colorFilter = ColorFilter.tint(Color.White),
            contentDescription = null,
        )
        Column {
            Row {
                Text(
                    text = email.sender,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    text = email.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.alpha(0.5f),
                )
            }

            Text(text = email.subject, style = MaterialTheme.typography.labelLarge)
            Text(
                text = email.body,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.alpha(0.5f),
            )
        }
    }
}

data class Email(
    val id: String,
    val subject: String,
    val body: String,
    val sender: String,
    val timestamp: String,
    val recipients: List<String>,
)

class InboxPresenter
    @AssistedInject
    constructor(
        @Assisted private val navigator: Navigator,
        private val emailRepository: EmailRepository,
    ) : Presenter<InboxScreen.State> {
        @Composable
        override fun present(): InboxScreen.State {
            val emails by produceState<List<Email>>(initialValue = emptyList()) {
                value = emailRepository.getEmails()
            }
            // Or a flow!
            // val emails by emailRepository.getEmailsFlow().collectAsState(initial = emptyList())
            return InboxScreen.State(emails) { event ->
                when (event) {
                    // Navigate to the detail screen when an email is clicked
                    is InboxScreen.Event.EmailClicked -> navigator.goTo(DetailScreen(event.emailId))
                }
            }
        }

        @CircuitInject(InboxScreen::class, AppScope::class)
        @AssistedFactory
        fun interface Factory {
            fun create(navigator: Navigator): InboxPresenter
        }
    }

class EmailRepository {
    fun getEmails(): List<Email> =
        listOf(
            Email(
                id = "1",
                subject = "Meeting re-sched!",
                body = "Hey, I'm going to be out of the office tomorrow. Can we reschedule?",
                sender = "Ali Connors",
                timestamp = "3:00 PM",
                recipients = listOf("a@example.com"),
            ),
        )

    fun getEmail(emailId: String): Email = getEmails().find { it.id == emailId } ?: throw IllegalArgumentException("Email not found")
}

@Parcelize
data class DetailScreen(
    val emailId: String,
) : Screen {
    data class State(
        val email: Email,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    sealed class Event : CircuitUiEvent {
        data object BackClicked : Event()
    }
}

class DetailPresenter
    @AssistedInject
    constructor(
        @Assisted private val navigator: Navigator,
        @Assisted private val screen: DetailScreen,
        private val emailRepository: EmailRepository,
    ) : Presenter<DetailScreen.State> {
        @Composable
        override fun present(): DetailScreen.State {
            val email = emailRepository.getEmail(screen.emailId)
            return DetailScreen.State(email) { event ->
                when (event) {
                    DetailScreen.Event.BackClicked -> navigator.pop()
                }
            }
        }

        @CircuitInject(DetailScreen::class, AppScope::class)
        @AssistedFactory
        fun interface Factory {
            fun create(
                navigator: Navigator,
                screen: DetailScreen,
            ): DetailPresenter
        }
    }

@CircuitInject(DetailScreen::class, AppScope::class)
@Composable
fun EmailDetailContent(
    state: DetailScreen.State,
    modifier: Modifier = Modifier,
) {
    val email = state.email
    Column(modifier.padding(16.dp)) {
        // Add a button with text back here
        Button(onClick = { state.eventSink(DetailScreen.Event.BackClicked) }) {
            Text("Back")
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Image(
                Icons.Default.Person,
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Magenta)
                        .padding(4.dp),
                colorFilter = ColorFilter.tint(Color.White),
                contentDescription = null,
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row {
                    Text(
                        text = email.sender,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = email.timestamp,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.alpha(0.5f),
                    )
                }
                Text(text = email.subject, style = MaterialTheme.typography.labelMedium)
                Row {
                    Text("To: ", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = email.recipients.joinToString(","),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.alpha(0.5f),
                    )
                }
            }
        }
        @Suppress("DEPRECATION") // Deprecated in Android but not yet available in CM
        Divider(modifier = Modifier.padding(vertical = 16.dp))
        Text(text = email.body, style = MaterialTheme.typography.bodyMedium)
    }
}
