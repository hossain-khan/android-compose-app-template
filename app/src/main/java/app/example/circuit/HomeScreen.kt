package app.example.circuit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import kotlinx.parcelize.Parcelize

/**
 * Home screen - the app's starting point.
 *
 * Replace this with your own initial screen and presenter.
 * See the Example* screens in this package for Circuit patterns and examples.
 */
@Parcelize
data object HomeScreen : Screen {
    data object State : CircuitUiState
}

@CircuitInject(HomeScreen::class, AppScope::class)
@Inject
class HomePresenter
    constructor() : Presenter<HomeScreen.State> {
        @Composable
        override fun present(): HomeScreen.State = HomeScreen.State
    }

@CircuitInject(HomeScreen::class, AppScope::class)
@Composable
fun HomeContent(
    state: HomeScreen.State,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Welcome! Replace this screen with your own.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}
