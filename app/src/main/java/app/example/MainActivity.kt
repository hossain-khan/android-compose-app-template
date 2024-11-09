package app.example

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import app.example.di.ActivityKey
import app.example.di.AppScope
import app.example.ui.theme.ComposeAppTheme
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuitx.gesturenavigation.GestureNavigationDecoration
import com.squareup.anvil.annotations.ContributesMultibinding
import javax.inject.Inject

@ContributesMultibinding(AppScope::class, boundType = Activity::class)
@ActivityKey(MainActivity::class)
class MainActivity
    @Inject
    constructor(
        private val circuit: Circuit,
    ) : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()

            setContent {
                ComposeAppTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                        val backStack = rememberSaveableBackStack(root = InboxScreen)
                        val navigator =
                            rememberCircuitNavigator(backStack) {
                                // Do something when the root screen is popped, usually exiting the app
                                Log.i("ComposeApp", "Root screen is popped.")
                            }
                        CircuitCompositionLocals(circuit) {
                            NavigableCircuitContent(
                                navigator = navigator,
                                backStack = backStack,
                                Modifier.padding(padding),
                                decoration =
                                    GestureNavigationDecoration {
                                        navigator.pop()
                                    },
                            )
                        }
                    }
                }
            }
        }
    }
