package com.rccar.controller.ui.help

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.rccar.controller.ui.theme.CardBorder
import com.rccar.controller.ui.theme.CardDark
import com.rccar.controller.ui.theme.DarkNavy
import com.rccar.controller.ui.theme.NeonCyan
import com.rccar.controller.ui.theme.SurfaceDark
import com.rccar.controller.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        containerColor = DarkNavy,
        topBar = {
            TopAppBar(
                title = { Text("How to Use", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark),
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            HelpSection(title = "Getting Started") {
                HelpStep("1", "Make sure Bluetooth is enabled on your phone.")
                HelpStep("2", "Power on your RC car. The Bluetooth module LED should blink.")
                HelpStep("3", "Pair the car with your phone in Android Settings if using for the first time.")
                HelpStep("4", "Open the app, tap Find My Car, and select your device from the list.")
            }

            HelpSection(title = "Controls") {
                HelpRow(Icons.Default.ArrowUpward,   "Forward — press and hold to move forward")
                HelpRow(Icons.Default.ArrowDownward,  "Backward — press and hold to reverse")
                HelpRow(Icons.Default.SwapHoriz,      "Left / Right — steer while moving")
                HelpRow(Icons.Default.Stop,           "Stop — releases immediately when you lift your finger")
            }

            HelpSection(title = "Speed Control") {
                HelpRow(Icons.Default.Speed, "Select Low, Mid, or High to change how fast the motors run.")
                HelpStep("→", "Low is recommended when learning, or in tight spaces.")
                HelpStep("→", "High gives maximum speed on open ground.")
            }

            HelpSection(title = "Line Follow Mode") {
                HelpRow(Icons.Default.Loop, "Toggle to let the car follow a black line on a white surface autonomously.")
                HelpStep("!", "Your car firmware must support this feature (IR line sensors required).")
            }

            HelpSection(title = "Telemetry") {
                HelpRow(Icons.Default.Bluetooth, "Battery % and distance sensor data are shown at the top of the control screen.")
                HelpStep("→", "Battery turns red below 20%.")
                HelpStep("→", "Your Arduino firmware must send: {\"bat\":78,\"dist\":34} or 78,34 over serial.")
            }

            HelpSection(title = "Auto-Reconnect") {
                HelpStep("→", "If the connection drops, the app automatically retries up to 5 times with exponential backoff.")
                HelpStep("→", "The status bar shows 'Reconnecting… (attempt N)' while retrying.")
                HelpStep("→", "Tap Disconnect to cancel and return to the scan screen.")
            }

            HelpSection(title = "Bluetooth Modules Supported") {
                HelpStep("✓", "HC-05 / HC-06 (Classic BT, SPP protocol)")
                HelpStep("✓", "FireFly (Classic BT)")
                HelpStep("✓", "HM-10 (BLE — requires BLE variant firmware)")
                HelpStep("✓", "ESP32 with BT Serial library")
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun HelpSection(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardDark, RoundedCornerShape(14.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text  = title,
            style = MaterialTheme.typography.titleMedium,
            color = NeonCyan,
        )
        content()
    }
}

@Composable
private fun HelpRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

@Composable
private fun HelpStep(prefix: String, text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(text = prefix, style = MaterialTheme.typography.labelLarge, color = NeonCyan)
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}
