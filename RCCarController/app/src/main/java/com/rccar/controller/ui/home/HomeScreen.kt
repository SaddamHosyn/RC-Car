package com.rccar.controller.ui.home

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rccar.controller.ui.theme.CardBorder
import com.rccar.controller.ui.theme.CardDark
import com.rccar.controller.ui.theme.DarkNavy
import com.rccar.controller.ui.theme.NeonCyan
import com.rccar.controller.ui.theme.NeonCyanDim
import com.rccar.controller.ui.theme.SecondaryDark
import com.rccar.controller.ui.theme.SurfaceDark
import com.rccar.controller.ui.theme.TextSecondary

/**
 * Home/landing screen.
 *
 * Shows the app identity and two call-to-action buttons:
 *   1. "Find My Car"       → real Bluetooth scan
 *   2. "Simulation Mode"  → skip Bluetooth, connect to Python TCP simulator
 */
@Composable
fun HomeScreen(
    onNavigateToScan:       () -> Unit,
    onNavigateToSimulation: () -> Unit,
) {
    // Pulsing animation for the car icon glow
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowScale by infiniteTransition.animateFloat(
        initialValue   = 1.0f,
        targetValue    = 1.12f,
        animationSpec  = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label          = "glowScale",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(SurfaceDark, DarkNavy))),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {

            // ── App icon with pulsing glow ring ───────────────────────────────
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(glowScale)
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = 0.08f))
                )
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(NeonCyanDim.copy(alpha = 0.3f), CardDark)
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = "RC Car",
                        tint = NeonCyan,
                        modifier = Modifier.size(56.dp),
                    )
                }
            }

            // ── Title ─────────────────────────────────────────────────────────
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text  = "RC Car Controller",
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = "Bluetooth-powered remote control\nfor your Arduino RC car",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Feature pills ─────────────────────────────────────────────────
            val features = listOf(
                "Classic BT & BLE",
                "Live Telemetry",
                "Auto-Reconnect",
                "Press-and-Hold Controls",
            )
            features.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { label -> FeaturePill(label, Modifier.weight(1f)) }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Primary CTA: Real Bluetooth ───────────────────────────────────
            Button(
                onClick = onNavigateToScan,
                shape   = RoundedCornerShape(16.dp),
                colors  = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan,
                    contentColor   = DarkNavy,
                ),
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                Icon(Icons.Default.Bluetooth, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Find My Car", style = MaterialTheme.typography.titleMedium, color = DarkNavy)
            }

            // ── Secondary CTA: Simulation Mode ────────────────────────────────
            OutlinedButton(
                onClick  = onNavigateToSimulation,
                shape    = RoundedCornerShape(16.dp),
                border   = androidx.compose.foundation.BorderStroke(1.5.dp, SecondaryDark),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Icon(Icons.Default.Computer, null, tint = SecondaryDark, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text  = "Simulation Mode",
                        style = MaterialTheme.typography.titleMedium,
                        color = SecondaryDark,
                    )
                }
            }

            // Simulation hint
            Text(
                text  = "No hardware? Connect to the Python car_simulator.py on your PC",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun FeaturePill(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(CardDark)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text  = "✓  $label",
            style = MaterialTheme.typography.labelSmall,
            color = NeonCyan,
            textAlign = TextAlign.Center,
        )
    }
}
