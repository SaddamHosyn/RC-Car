package com.rccar.controller.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.rccar.controller.model.CarTelemetry
import com.rccar.controller.ui.theme.CardBorder
import com.rccar.controller.ui.theme.CardDark
import com.rccar.controller.ui.theme.Connected
import com.rccar.controller.ui.theme.Danger
import com.rccar.controller.ui.theme.NeonCyan
import com.rccar.controller.ui.theme.TextSecondary

/**
 * A pair of cards showing live battery and distance sensor telemetry from the car.
 * Shows "—" when no data has been received yet.
 */
@Composable
fun TelemetryRow(
    telemetry: CarTelemetry,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Battery card
        val batteryColor = when {
            telemetry.batteryPercent == null -> TextSecondary
            telemetry.batteryPercent > 50    -> Connected
            telemetry.batteryPercent > 20    -> Color(0xFFFFAB00)
            else                             -> Danger
        }
        TelemetryCard(
            icon      = Icons.Default.BatteryFull,
            label     = "Battery",
            value     = telemetry.batteryPercent?.let { "$it%" } ?: "—",
            iconTint  = batteryColor,
            modifier  = Modifier.weight(1f),
        )

        // Distance card
        TelemetryCard(
            icon      = Icons.Default.Radar,
            label     = "Distance",
            value     = telemetry.distanceCm?.let { "${it} cm" } ?: "—",
            iconTint  = NeonCyan,
            modifier  = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TelemetryCard(
    icon:     ImageVector,
    label:    String,
    value:    String,
    iconTint: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(28.dp),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text  = value,
            style = MaterialTheme.typography.headlineMedium,
            color = iconTint,
        )
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
        )
    }
}
