package dev.hossain.compose.example.circuit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import dev.hossain.compose.example.R
import dev.hossain.compose.example.circuit.overlay.AppInfoOverlay
import dev.hossain.compose.example.data.AppVersionService
import dev.hossain.compose.example.data.model.Email
import dev.hossain.compose.example.data.network.NetworkMonitor
import dev.hossain.compose.example.data.preferences.ThemeMode
import dev.hossain.compose.example.data.preferences.UserPreferencesRepository
import dev.hossain.compose.example.data.repository.EmailRepository
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.foundation.CircuitContent
import com.slack.circuit.overlay.OverlayEffect
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.ParcelableScreen
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.serialization.CircuitSerializable
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/** Standalone enum defining tabs in the unified email navigation container. */
enum class ScreenTab {
    INBOX,
    DRAFTS,
    SENT,
}

@Parcelize
@Serializable
@CircuitSerializable(AppScope::class)
data object InboxScreen : ParcelableScreen {
    @Stable
    sealed interface State : CircuitUiState {
        data object Loading : State

        data class Success(
            val emails: List<Email>,
            val selectedTab: ScreenTab,
            val showAppInfo: Boolean = false,
            val isRefreshing: Boolean = false,
            val searchQuery: String = "",
            val showUnreadOnly: Boolean = false,
            val themeMode: ThemeMode = ThemeMode.SYSTEM,
            val isOnline: Boolean = true,
            val eventSink: (Event) -> Unit,
        ) : State

        data class Error(
            val message: String,
            val eventSink: (Event) -> Unit,
        ) : State
    }

    @Immutable
    sealed interface Event : CircuitUiEvent {
        data class EmailClicked(
            val emailId: String,
        ) : Event

        data class DraftClicked(
            val draftId: String,
        ) : Event

        data class DeleteDraft(
            val draftId: String,
        ) : Event

        data class TabSelected(
            val tab: ScreenTab,
        ) : Event

        data object InfoClicked : Event

        data object InfoDismissed : Event

        data object Retry : Event

        data object OnNewEmail : Event

        data object OnRefresh : Event

        data class OnSearchQueryChanged(
            val query: String,
        ) : Event

        data class OnToggleUnreadFilter(
            val unreadOnly: Boolean,
        ) : Event

        data class OnSetThemeMode(
            val themeMode: ThemeMode,
        ) : Event

        data class OnToggleReadStatus(
            val emailId: String,
            val isRead: Boolean? = null,
        ) : Event

        data object OnResetDemoData : Event
    }
}

@AssistedInject
class InboxPresenter
    constructor(
        @Assisted private val navigator: Navigator,
        private val emailRepository: EmailRepository,
        private val appVersionService: AppVersionService,
        private val networkMonitor: NetworkMonitor,
        private val userPreferencesRepository: UserPreferencesRepository,
    ) : Presenter<InboxScreen.State> {
        @Composable
        override fun present(): InboxScreen.State {
            var emails by rememberRetained { mutableStateOf<List<Email>?>(null) }
            var selectedTab by rememberRetained { mutableStateOf(ScreenTab.INBOX) }
            var errorMessage by rememberRetained { mutableStateOf<String?>(null) }
            var showAppInfo by rememberRetained { mutableStateOf(false) }
            var isRefreshing by rememberRetained { mutableStateOf(false) }
            var searchQuery by rememberRetained { mutableStateOf("") }
            var retryTrigger by rememberRetained { mutableStateOf(0) }
            var pendingDeleteId by rememberRetained { mutableStateOf<String?>(null) }

            val isOnline by networkMonitor.isOnline.collectAsState(initial = true)
            val showUnreadOnly by userPreferencesRepository.showUnreadOnly.collectAsState(initial = false)
            val themeMode by userPreferencesRepository.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val scope = rememberCoroutineScope()

            var lastSelectedTab by rememberRetained { mutableStateOf(ScreenTab.INBOX) }
            var lastRetryTrigger by rememberRetained { mutableStateOf(0) }
            if (selectedTab != lastSelectedTab || retryTrigger != lastRetryTrigger) {
                if (!isRefreshing) {
                    emails = null
                }
                lastSelectedTab = selectedTab
                lastRetryTrigger = retryTrigger
            }

            LaunchedEffect(selectedTab, retryTrigger) {
                if (!isRefreshing) {
                    emails = null
                }
                errorMessage = null
                try {
                    emails =
                        when (selectedTab) {
                            ScreenTab.INBOX -> emailRepository.getInboxEmails()
                            ScreenTab.DRAFTS -> emailRepository.getDraftEmails()
                            ScreenTab.SENT -> emailRepository.getSentEmails()
                        }
                } catch (e: Exception) {
                    errorMessage = e.message ?: "Unknown error"
                } finally {
                    isRefreshing = false
                }
            }

            LaunchedEffect(emailRepository) {
                emailRepository.updates.collect {
                    retryTrigger++
                }
            }

            LaunchedEffect(pendingDeleteId) {
                val id = pendingDeleteId ?: return@LaunchedEffect
                try {
                    emailRepository.deleteDraft(id)
                } catch (e: Exception) {
                    retryTrigger++
                } finally {
                    pendingDeleteId = null
                }
            }

            val eventSink: (InboxScreen.Event) -> Unit = { event ->
                when (event) {
                    is InboxScreen.Event.EmailClicked -> {
                        scope.launch {
                            try {
                                emailRepository.updateEmailReadStatus(event.emailId, isRead = true)
                            } catch (_: Exception) {
                            }
                        }
                        navigator.goTo(DetailScreen(event.emailId))
                    }

                    is InboxScreen.Event.DraftClicked -> {
                        navigator.goTo(ComposeEmailScreen(draftId = event.draftId))
                    }

                    is InboxScreen.Event.DeleteDraft -> {
                        emails = emails?.filter { it.id != event.draftId }
                        pendingDeleteId = event.draftId
                    }

                    is InboxScreen.Event.TabSelected -> {
                        selectedTab = event.tab
                    }

                    InboxScreen.Event.InfoClicked -> {
                        showAppInfo = true
                    }

                    InboxScreen.Event.InfoDismissed -> {
                        showAppInfo = false
                    }

                    InboxScreen.Event.Retry -> {
                        retryTrigger++
                    }

                    InboxScreen.Event.OnNewEmail -> {
                        navigator.goTo(ComposeEmailScreen())
                    }

                    InboxScreen.Event.OnRefresh -> {
                        isRefreshing = true
                        retryTrigger++
                    }

                    is InboxScreen.Event.OnSearchQueryChanged -> {
                        searchQuery = event.query
                    }

                    is InboxScreen.Event.OnToggleUnreadFilter -> {
                        scope.launch {
                            userPreferencesRepository.setShowUnreadOnly(event.unreadOnly)
                        }
                    }

                    is InboxScreen.Event.OnSetThemeMode -> {
                        scope.launch {
                            userPreferencesRepository.setThemeMode(event.themeMode)
                        }
                    }

                    is InboxScreen.Event.OnToggleReadStatus -> {
                        scope.launch {
                            try {
                                emailRepository.updateEmailReadStatus(event.emailId, event.isRead)
                            } catch (_: Exception) {
                            }
                        }
                    }

                    InboxScreen.Event.OnResetDemoData -> {
                        scope.launch {
                            try {
                                emailRepository.resetDemoData()
                            } catch (_: Exception) {
                            }
                        }
                    }
                }
            }

            val filteredEmails =
                emails?.filter { email ->
                    val matchesSearch =
                        searchQuery.isBlank() ||
                            email.subject.contains(searchQuery, ignoreCase = true) ||
                            email.sender.contains(searchQuery, ignoreCase = true) ||
                            email.body.contains(searchQuery, ignoreCase = true)
                    val matchesUnread =
                        !showUnreadOnly || email.status.equals("unread", ignoreCase = true)
                    matchesSearch && matchesUnread
                }

            return when {
                errorMessage != null -> {
                    InboxScreen.State.Error(errorMessage!!, eventSink)
                }

                filteredEmails != null -> {
                    InboxScreen.State.Success(
                        emails = filteredEmails,
                        selectedTab = selectedTab,
                        showAppInfo = showAppInfo,
                        isRefreshing = isRefreshing,
                        searchQuery = searchQuery,
                        showUnreadOnly = showUnreadOnly,
                        themeMode = themeMode,
                        isOnline = isOnline,
                        eventSink = eventSink,
                    )
                }

                else -> {
                    InboxScreen.State.Loading
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
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Inbox(
    state: InboxScreen.State,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is InboxScreen.State.Loading -> {
            Box(
                modifier =
                    modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center,
            ) {
                ExpressiveLoadingIndicator()
            }
        }

        is InboxScreen.State.Error -> {
            Box(
                modifier =
                    modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp),
                    )
                    Button(onClick = { state.eventSink(InboxScreen.Event.Retry) }) {
                        Text("Retry")
                    }
                }
            }
        }

        is InboxScreen.State.Success -> {
            if (state.showAppInfo) {
                OverlayEffect {
                    show(
                        AppInfoOverlay(
                            currentThemeMode = state.themeMode,
                            onThemeModeSelected = { themeMode ->
                                state.eventSink(InboxScreen.Event.OnSetThemeMode(themeMode))
                            },
                            onResetDemoData = {
                                state.eventSink(InboxScreen.Event.OnResetDemoData)
                            },
                        ),
                    )
                    state.eventSink(InboxScreen.Event.InfoDismissed)
                }
            }

            val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

            NavigationSuiteScaffold(
                modifier = modifier,
                navigationSuiteItems = {
                    item(
                        selected = state.selectedTab == ScreenTab.INBOX,
                        onClick = { state.eventSink(InboxScreen.Event.TabSelected(ScreenTab.INBOX)) },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.email_24dp),
                                contentDescription = "Inbox",
                            )
                        },
                        label = { Text("Inbox") },
                    )
                    item(
                        selected = state.selectedTab == ScreenTab.DRAFTS,
                        onClick = { state.eventSink(InboxScreen.Event.TabSelected(ScreenTab.DRAFTS)) },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.edit_24dp),
                                contentDescription = "Drafts",
                            )
                        },
                        label = { Text("Drafts") },
                    )
                    item(
                        selected = state.selectedTab == ScreenTab.SENT,
                        onClick = { state.eventSink(InboxScreen.Event.TabSelected(ScreenTab.SENT)) },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.send_24dp),
                                contentDescription = "Sent",
                            )
                        },
                        label = { Text("Sent") },
                    )
                },
            ) {
                val windowAdaptiveInfo = currentWindowAdaptiveInfoV2()
                val isWideScreen = windowAdaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(600)

                if (isWideScreen && state.selectedTab == ScreenTab.INBOX) {
                    val navigator = rememberListDetailPaneScaffoldNavigator<String>()

                    ListDetailPaneScaffold(
                        directive = navigator.scaffoldDirective,
                        value = navigator.scaffoldValue,
                        listPane = {
                            InboxListContent(
                                state = state,
                                isWideScreen = true,
                                onEmailClicked = { emailId ->
                                    coroutineScope.launch {
                                        navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, emailId)
                                    }
                                },
                            )
                        },
                        detailPane = {
                            val selectedEmailId = navigator.currentDestination?.contentKey
                            if (selectedEmailId != null) {
                                CircuitContent(DetailScreen(selectedEmailId))
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "Select an email to read",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        },
                    )
                } else {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        when (state.selectedTab) {
                                            ScreenTab.INBOX -> "Inbox"
                                            ScreenTab.DRAFTS -> "Drafts"
                                            ScreenTab.SENT -> "Sent"
                                        },
                                    )
                                },
                                actions = {
                                    if (state.selectedTab == ScreenTab.INBOX) {
                                        IconButton(
                                            onClick = { state.eventSink(InboxScreen.Event.InfoClicked) },
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.baseline_info_24),
                                                contentDescription = "App Info",
                                            )
                                        }
                                    }
                                },
                            )
                        },
                        floatingActionButton = {
                            if (state.selectedTab != ScreenTab.SENT) {
                                FloatingActionButton(
                                    onClick = { state.eventSink(InboxScreen.Event.OnNewEmail) },
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.add_24dp),
                                        contentDescription = "New Email",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                }
                            }
                        },
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            when (state.selectedTab) {
                                ScreenTab.INBOX -> {
                                    InboxListContent(
                                        state = state,
                                        isWideScreen = false,
                                        onEmailClicked = { emailId ->
                                            state.eventSink(InboxScreen.Event.EmailClicked(emailId))
                                        },
                                    )
                                }

                                ScreenTab.DRAFTS -> {
                                    DraftsListContent(
                                        state = state,
                                        onDraftClicked = { draftId ->
                                            state.eventSink(InboxScreen.Event.DraftClicked(draftId))
                                        },
                                        onDelete = { draftId ->
                                            state.eventSink(InboxScreen.Event.DeleteDraft(draftId))
                                        },
                                    )
                                }

                                ScreenTab.SENT -> {
                                    SentListContent(state = state)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpressiveLoadingIndicator(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 1500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "progress",
    )

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LinearWavyProgressIndicator(
            progress = { animatedProgress },
            modifier =
                Modifier
                    .fillMaxWidth(0.6f)
                    .height(10.dp),
        )
    }
}

@Composable
fun OfflineBanner(
    isOnline: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(visible = !isOnline) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_info_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "You are currently offline",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
    }
}

@Composable
fun SearchAndFilterHeader(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    showUnreadOnly: Boolean,
    onToggleUnreadFilter: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = { Text("Search emails...") },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.search_24dp),
                    contentDescription = "Search",
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.close_24dp),
                            contentDescription = "Clear",
                        )
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = showUnreadOnly,
                onClick = { onToggleUnreadFilter(!showUnreadOnly) },
                label = { Text("Unread only") },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxListContent(
    state: InboxScreen.State.Success,
    isWideScreen: Boolean,
    onEmailClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        OfflineBanner(isOnline = state.isOnline)
        SearchAndFilterHeader(
            searchQuery = state.searchQuery,
            onSearchQueryChanged = { state.eventSink(InboxScreen.Event.OnSearchQueryChanged(it)) },
            showUnreadOnly = state.showUnreadOnly,
            onToggleUnreadFilter = { state.eventSink(InboxScreen.Event.OnToggleUnreadFilter(it)) },
        )

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { state.eventSink(InboxScreen.Event.OnRefresh) },
            modifier = Modifier.fillMaxSize(),
        ) {
            if (state.emails.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No matching emails", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.emails) { email ->
                        ExpressiveEmailItem(
                            email = email,
                            onClick = { onEmailClicked(email.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DraftsListContent(
    state: InboxScreen.State.Success,
    onDraftClicked: (String) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.emails.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No drafts", style = MaterialTheme.typography.bodyMedium)
        }
    } else {
        LazyColumn(modifier = modifier.fillMaxSize()) {
            items(state.emails) { draft ->
                ExpressiveDraftItem(
                    draft = draft,
                    onClick = { onDraftClicked(draft.id) },
                    onDelete = { onDelete(draft.id) },
                )
            }
        }
    }
}

@Composable
fun SentListContent(
    state: InboxScreen.State.Success,
    modifier: Modifier = Modifier,
) {
    if (state.emails.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No sent emails", style = MaterialTheme.typography.bodyMedium)
        }
    } else {
        LazyColumn(modifier = modifier.fillMaxSize()) {
            items(state.emails) { email ->
                ExpressiveEmailItem(
                    email = email,
                    onClick = {},
                )
            }
        }
    }
}

@Composable
fun ExpressiveEmailItem(
    email: Email,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.material3.Card(
        onClick = onClick,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = MaterialTheme.shapes.large,
        colors =
            androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(
                            if (!email.isRead) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.primaryContainer
                            },
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = email.sender.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color =
                        if (!email.isRead) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        },
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (!email.isRead) {
                            Box(
                                modifier =
                                    Modifier
                                        .size(8.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                            )
                        }
                        Text(
                            text = email.sender,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight =
                                if (!email.isRead) {
                                    androidx.compose.ui.text.font.FontWeight.Bold
                                } else {
                                    androidx.compose.ui.text.font.FontWeight.Medium
                                },
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = email.timestamp,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = email.subject,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight =
                        if (!email.isRead) {
                            androidx.compose.ui.text.font.FontWeight.Bold
                        } else {
                            androidx.compose.ui.text.font.FontWeight.Normal
                        },
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = email.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun ExpressiveDraftItem(
    draft: Email,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.material3.Card(
        onClick = onClick,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = MaterialTheme.shapes.large,
        colors =
            androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = draft.subject.ifBlank { "(no subject)" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                )
                if (draft.recipients.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "To: ${draft.recipients.joinToString(", ")}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = draft.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    painter = painterResource(id = R.drawable.delete_24dp),
                    contentDescription = "Delete draft",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
