package com.rccar.controller.ui.control

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rccar.controller.bluetooth.CarCommand
import com.rccar.controller.model.ConnectionState
import com.rccar.controller.ui.components.ConnectionStatusBar
import com.rccar.controller.ui.components.DPad
import com.rccar.controller.ui.components.TelemetryRow
import com.rccar.controller.ui.theme.CardBorder
import com.rccar.controller.ui.theme.CardDark
import com.rccar.controller.ui.theme.Danger
import com.rccar.controller.ui.theme.DarkNavy
import com.rccar.controller.ui.theme.NeonCyan
import com.rccar.controller.ui.theme.SurfaceDark
import com.rccar.controller.ui.theme.TextSecondary
import com.rccar.controller.viewmodel.BluetoothViewModel

/**
 * Main control screen.
 *
 * Displays:
 *  - Connection status bar
 *  - Live telemetry (battery + distance)
 *  - D-pad with press-and-hold semantics
 *  - Speed selector (Low / Mid / High)
 *  - Line-follow mode toggle
 *  - Disconnect button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlScreen(
    viewModel:        BluetoothViewModel,
    onNavigateToScan: () -> Unit,
    onNavigateBack:   () -> Unit,
) {
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val telemetry       by viewModel.telemetry.collectAsStateWithLifecycle()
    val speedLevel      by viewModel.speedLevel.collectAsStateWithLifecycle()
    val lineFollowMode  by viewModel.lineFollowMode.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Show error events as snackbars
    LaunchedEffect(Unit) {
        viewModel.errors.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // If connection is lost and reconnect fails, go back to scan
    LaunchedEffect(connectionState) {
        if (connectionState is ConnectionState.Error) {
            snackbarHostState.showSnackbar((connectionState as ConnectionState.Error).message)
        }
    }

    val isConnected = connectionState is ConnectionState.Connected
    val isWorking   = connectionState.isWorking

    Scaffold(
        containerColor = DarkNavy,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData    = data,
                    containerColor  = CardDark,
                    contentColor    = Color.White,
                    actionColor     = NeonCyan,
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = connectionState.deviceName ?: "RC Car",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark,
                    titleContentColor = Color.White,
                ),
            )
        },
        bottomBar = {
            ConnectionStatusBar(state = connectionState)
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            // ── Telemetry row ────────────────────────────────────────────────
            TelemetryRow(telemetry = telemetry)

            // ── D-pad ────────────────────────────────────────────────────────
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                DPad(
                    enabled   = isConnected,
                    onCommand = viewModel::sendCommand,
                )
            }

            // ── Speed selector ───────────────────────────────────────────────
            SpeedSelector(
                currentLevel = speedLevel,
                enabled      = isConnected,
                onSelect     = viewModel::setSpeed,
            )

            // ── Line-follow toggle ───────────────────────────────────────────
            LineFollowToggle(
                enabled        = isConnected,
                isActive       = lineFollowMode,
                onToggle       = { viewModel.toggleLineFollow() },
            )

            Spacer(Modifier.weight(1f))

            // ── Disconnect button ─────────────────────────────────────────────
            Button(
                onClick = {
                    viewModel.disconnect()
                    onNavigateToScan()
                },
                shape   = RoundedCornerShape(14.dp),
                colors  = ButtonDefaults.buttonColors(
                    containerColor = Danger.copy(alpha = 0.15f),
                    contentColor   = Danger,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.BluetoothDisabled,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.size(8.dp))
                Text("Disconnect", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SpeedSelector(
    currentLevel: Int,
    enabled:      Boolean,
    onSelect:     (Int) -> Unit,
) {
    val levels = listOf(1 to "Low", 2 to "Mid", 3 to "High")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.size(6.dp))
            Text("Speed", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            levels.forEach { (level, label) ->
                FilterChip(
                    selected = currentLevel == level,
                    onClick  = { if (enabled) onSelect(level) },
                    label    = { Text(label) },
                    enabled  = enabled,
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                        selectedLabelColor     = NeonCyan,
                        selectedLeadingIconColor = NeonCyan,
                        containerColor         = CardDark,
                        labelColor             = TextSecondary,
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled  = enabled,
                        selected = currentLevel == level,
                        selectedBorderColor = NeonCyan,
                        borderColor = CardBorder,
                    ),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun LineFollowToggle(
    enabled:  Boolean,
    isActive: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardDark, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Loop,
                contentDescription = null,
                tint = if (isActive) NeonCyan else TextSecondary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.size(10.dp))
            Column {
                Text(
                    text  = "Line Follow Mode",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text  = if (isActive) "Autonomous — car drives itself" else "Manual control active",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }
        }
        Switch(
            checked  = isActive,
            onCheckedChange = { if (enabled) onToggle() },
            enabled  = enabled,
            colors   = SwitchDefaults.colors(
                checkedThumbColor   = DarkNavy,
                checkedTrackColor   = NeonCyan,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = CardBorder,
            ),
        )
    }
}
