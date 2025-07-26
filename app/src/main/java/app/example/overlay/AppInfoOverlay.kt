package app.example.overlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.overlay.Overlay
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuitx.overlays.BottomSheetOverlay
import dev.zacsweers.metro.AppScope
import kotlinx.parcelize.Parcelize

/**
 * Overlay that shows app information in a bottom sheet.
 */
@Parcelize
data object AppInfoOverlay : Overlay<AppInfoOverlay.Result> {
    data class Result(
        val dismissed: Boolean = true,
    )
}

@CircuitInject(AppInfoOverlay::class, AppScope::class)
@Composable
fun AppInfoBottomSheet(
    overlay: AppInfoOverlay,
    navigator: OverlayNavigator<AppInfoOverlay.Result>,
    modifier: Modifier = Modifier,
) {
    BottomSheetOverlay<AppInfoOverlay.Result>(
        model = overlay,
        onDismiss = { navigator.finish(AppInfoOverlay.Result(dismissed = true)) },
    ) {
        AppInfoContent(
            onDismiss = { navigator.finish(AppInfoOverlay.Result(dismissed = true)) },
            modifier = modifier,
        )
    }
}

@Composable
private fun AppInfoContent(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "App Info",
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "App Information",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            InfoRow(label = "App Name", value = "Circuit App Template")
            InfoRow(label = "Version", value = "1.0")
            InfoRow(label = "Package", value = "app.example")
            InfoRow(label = "Framework", value = "Circuit + Compose")

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Close")
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
