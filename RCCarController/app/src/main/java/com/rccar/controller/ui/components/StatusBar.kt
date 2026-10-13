package com.rccar.controller.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rccar.controller.model.ConnectionState
import com.rccar.controller.ui.theme.Connected
import com.rccar.controller.ui.theme.Connecting
import com.rccar.controller.ui.theme.Danger
import com.rccar.controller.ui.theme.Disconnected
import com.rccar.controller.ui.theme.TextSecondary

/**
 * A slim banner that shows the current Bluetooth connection status.
 * Animates its background and icon color when state changes.
 */
@Composable
fun ConnectionStatusBar(
    state: ConnectionState,
    modifier: Modifier = Modifier,
) {
    val (statusColor, statusText, icon) = when (state) {
        is ConnectionState.Connected    -> Triple(Connected,     "Connected · ${state.deviceName}",           Icons.Default.BluetoothConnected)
        is ConnectionState.Connecting   -> Triple(Connecting,    "Connecting to ${state.deviceName}…",         Icons.Default.BluetoothSearching)
        is ConnectionState.Reconnecting -> Triple(Connecting,    "Reconnecting… (attempt ${state.attempt})",   Icons.Default.BluetoothSearching)
        is ConnectionState.Error        -> Triple(Danger,        state.message,                                Icons.Default.Warning)
        ConnectionState.Disconnected    -> Triple(Disconnected,  "Not connected",                              Icons.Default.BluetoothDisabled)
    }

    val animatedColor by animateColorAsState(
        targetValue = statusColor,
        animationSpec = tween(400),
        label = "statusColor",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        // Pulsing indicator dot
        Spacer(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(animatedColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = animatedColor,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelLarge,
            color = animatedColor,
        )
    }
}
