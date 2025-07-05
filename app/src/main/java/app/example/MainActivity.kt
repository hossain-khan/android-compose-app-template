package app.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.runtime.remember
import app.example.circuit.InboxScreen
import app.example.ui.theme.CircuitAppTheme
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuit.sharedelements.SharedElementTransitionLayout
import com.slack.circuitx.gesturenavigation.GestureNavigationDecorationFactory
import javax.inject.Inject

class MainActivity : ComponentActivity() {

    @Inject
    lateinit var circuit: Circuit

    @OptIn(ExperimentalSharedTransitionApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        (application as CircuitApp).graph.membersInjector.inject(this)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            CircuitAppTheme {
                // See https://slackhq.github.io/circuit/navigation/
                val backStack = rememberSaveableBackStack(root = InboxScreen)
                val navigator = rememberCircuitNavigator(backStack)

                // See https://slackhq.github.io/circuit/circuit-content/
                CircuitCompositionLocals(circuit) {
                    // See https://slackhq.github.io/circuit/shared-elements/
                    SharedElementTransitionLayout {
                        // See https://slackhq.github.io/circuit/overlays/
                        ContentWithOverlays {
                            NavigableCircuitContent(
                                navigator = navigator,
                                backStack = backStack,
                                decoratorFactory =
                                remember(navigator) {
                                    GestureNavigationDecorationFactory(onBackInvoked = navigator::pop)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
