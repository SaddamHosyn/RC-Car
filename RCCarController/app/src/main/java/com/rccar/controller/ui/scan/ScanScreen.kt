package com.rccar.controller.ui.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.rccar.controller.model.CarDevice
import com.rccar.controller.model.ConnectionState
import com.rccar.controller.ui.components.ConnectionStatusBar
import com.rccar.controller.ui.theme.CardBorder
import com.rccar.controller.ui.theme.CardDark
import com.rccar.controller.ui.theme.Connected
import com.rccar.controller.ui.theme.DarkNavy
import com.rccar.controller.ui.theme.NeonCyan
import com.rccar.controller.ui.theme.SurfaceDark
import com.rccar.controller.ui.theme.TextSecondary
import com.rccar.controller.viewmodel.BluetoothViewModel

/**
 * Device scan screen.
 *
 * - Handles runtime Bluetooth permissions via Accompanist
 * - Shows paired devices first (no scan needed for known devices)
 * - Runs a live discovery scan and appends results as they arrive
 * - Tapping any device initiates a connection and navigates to ControlScreen
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    viewModel:             BluetoothViewModel,
    onNavigateToControl:   () -> Unit,
    onNavigateBack:        () -> Unit,
) {
    // ── Collect state ─────────────────────────────────────────────────────────
    val connectionState  by viewModel.connectionState.collectAsStateWithLifecycle()
    val pairedDevices    by viewModel.pairedDevices.collectAsStateWithLifecycle()
    val discoveredDevices by viewModel.discoveredDevices.collectAsStateWithLifecycle()
    val isDiscovering    by viewModel.isDiscovering.collectAsStateWithLifecycle()

    // Navigate to control screen once connected.
    LaunchedEffect(connectionState) {
        if (connectionState is ConnectionState.Connected) {
            onNavigateToControl()
        }
    }

    // ── Runtime permissions ───────────────────────────────────────────────────
    val bluetoothPermissions = rememberMultiplePermissionsState(
        permissions = buildList {
            if (android.os.Build.VERSION.SDK_INT >= 31) {
                add(android.Manifest.permission.BLUETOOTH_SCAN)
                add(android.Manifest.permission.BLUETOOTH_CONNECT)
            } else {
                add(android.Manifest.permission.BLUETOOTH)
                add(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    )

    LaunchedEffect(Unit) {
        if (!bluetoothPermissions.allPermissionsGranted) {
            bluetoothPermissions.launchMultiplePermissionRequest()
        } else {
            viewModel.loadPairedDevices()
        }
    }

    Scaffold(
        containerColor = DarkNavy,
        topBar = {
            TopAppBar(
                title = { Text("Select Device", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark,
                    titleContentColor = Color.White,
                ),
                actions = {
                    IconButton(
                        onClick = {
                            if (isDiscovering) viewModel.stopDiscovery()
                            else viewModel.startDiscovery()
                        }
                    ) {
                        if (isDiscovering) {
                            CircularProgressIndicator(
                                color = NeonCyan,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Scan",
                                tint = NeonCyan,
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            ConnectionStatusBar(state = connectionState)
        }
    ) { paddingValues ->

        if (!bluetoothPermissions.allPermissionsGranted) {
            PermissionRationale(
                onRequest = { bluetoothPermissions.launchMultiplePermissionRequest() },
                modifier = Modifier.padding(paddingValues),
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { Spacer(Modifier.height(12.dp)) }

            // ── Paired devices ────────────────────────────────────────────────
            if (pairedDevices.isNotEmpty()) {
                item {
                    SectionHeader("Paired Devices")
                }
                items(pairedDevices, key = { it.address }) { device ->
                    DeviceRow(
                        device      = device,
                        isConnecting = connectionState is ConnectionState.Connecting &&
                                       connectionState.deviceName == device.name,
                        onClick     = { viewModel.connect(device) },
                    )
                }
                item { Spacer(Modifier.height(8.dp)) }
            }

            // ── Discovered devices ────────────────────────────────────────────
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    SectionHeader("Nearby Devices")
                    if (isDiscovering) {
                        Text(
                            text  = "Scanning…",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan,
                        )
                    }
                }
            }

            if (discoveredDevices.isEmpty() && !isDiscovering) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Bluetooth,
                                contentDescription = null,
                                tint = CardBorder,
                                modifier = Modifier.size(48.dp),
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text  = "Tap the refresh icon to scan",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                            )
                        }
                    }
                }
            } else {
                items(discoveredDevices, key = { it.address }) { device ->
                    DeviceRow(
                        device       = device,
                        isConnecting = connectionState is ConnectionState.Connecting &&
                                       connectionState.deviceName == device.name,
                        onClick      = { viewModel.connect(device) },
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text     = title.uppercase(),
        style    = MaterialTheme.typography.labelSmall,
        color    = TextSecondary,
        modifier = Modifier.padding(vertical = 4.dp),
    )
}

@Composable
private fun DeviceRow(
    device:      CarDevice,
    isConnecting: Boolean,
    onClick:     () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardDark)
            .border(1.dp, if (device.isPaired) NeonCyan.copy(alpha = 0.3f) else CardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Device icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (device.isPaired) NeonCyan.copy(alpha = 0.12f) else CardBorder.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (device.isPaired) Icons.Default.BluetoothConnected
                                  else Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = if (device.isPaired) NeonCyan else TextSecondary,
                    modifier = Modifier.size(22.dp),
                )
            }

            Spacer(Modifier.width(12.dp))

            // Name + address
            Column {
                Text(
                    text     = device.name,
                    style    = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text  = device.address,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }
        }

        // Right side: signal or loading indicator
        if (isConnecting) {
            CircularProgressIndicator(
                color = NeonCyan,
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
            )
        } else if (device.rssi > Int.MIN_VALUE / 2 && device.rssi != -1) {
            Column(horizontalAlignment = Alignment.End) {
                Icon(
                    imageVector = Icons.Default.SignalCellular4Bar,
                    contentDescription = "Signal",
                    tint = Connected,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text  = "${device.rssi} dBm",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun PermissionRationale(
    onRequest: () -> Unit,
    modifier:  Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Bluetooth,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(64.dp),
            )
            Text(
                text  = "Bluetooth Permission Required",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text  = "This app needs Bluetooth access to discover and connect to your RC car.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
            )
            Button(
                onClick = onRequest,
                colors  = ButtonDefaults.buttonColors(containerColor = NeonCyan),
            ) {
                Text("Grant Permission", color = DarkNavy)
            }
        }
    }
}
