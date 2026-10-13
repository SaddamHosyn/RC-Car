package com.rccar.controller.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.rccar.controller.model.CarDevice
import com.rccar.controller.model.CarTelemetry
import com.rccar.controller.model.ConnectionState
import com.rccar.controller.model.toCarDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.UUID

/**
 * [BluetoothService] manages the entire lifecycle of a Classic Bluetooth SPP connection
 * to the RC car.
 *
 * Design decisions:
 * - All socket I/O is performed on [Dispatchers.IO] so the main thread is never blocked.
 * - [connectionState] is a [StateFlow] — the ViewModel and UI collect it reactively.
 * - [telemetry] is a [StateFlow] of the latest parsed data packet from the car.
 * - [discoveredDevices] is a [StateFlow] populated by a [BroadcastReceiver] during discovery.
 * - Auto-reconnect uses exponential back-off capped at [MAX_RECONNECT_DELAY_MS].
 * - The service is scoped to the Application process; it survives screen rotation.
 */
@SuppressLint("MissingPermission")   // Permissions are checked in the ViewModel layer.
class BluetoothService(private val context: Context) : ICarService {

    // ── Coroutine scope ──────────────────────────────────────────────────────
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ── Bluetooth adapter ────────────────────────────────────────────────────
    val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    val isBluetoothSupported: Boolean get() = adapter != null
    val isBluetoothEnabled: Boolean   get() = adapter?.isEnabled == true

    // ── State flows exposed to ViewModel ─────────────────────────────────────
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _telemetry = MutableStateFlow(CarTelemetry.Empty)
    val telemetry: StateFlow<CarTelemetry> = _telemetry.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<CarDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<CarDevice>> = _discoveredDevices.asStateFlow()

    private val _isDiscovering = MutableStateFlow(false)
    val isDiscovering: StateFlow<Boolean> = _isDiscovering.asStateFlow()

    /** One-shot error events (e.g. "Pairing failed") that the UI shows as a Snackbar. */
    private val _errors = MutableSharedFlow<String>()
    val errors: SharedFlow<String> = _errors.asSharedFlow()

    // ── Internal connection state ─────────────────────────────────────────────
    private var socket: BluetoothSocket? = null
    private var connectedDevice: BluetoothDevice? = null
    private var rxJob: Job? = null
    private var reconnectJob: Job? = null
    private var reconnectEnabled = true

    // ── Constants ─────────────────────────────────────────────────────────────
    companion object {
        /** Standard Serial Port Profile UUID — recognised by HC-05, HC-06, FireFly, etc. */
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

        private const val MAX_RECONNECT_ATTEMPTS  = 5
        private const val INITIAL_RECONNECT_DELAY = 1_000L  // 1 s
        private const val MAX_RECONNECT_DELAY_MS  = 30_000L // 30 s
        private const val READ_BUFFER_SIZE        = 1_024
    }

    // ── Discovery BroadcastReceiver ───────────────────────────────────────────

    private val discoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) ?: return
                    val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE).toInt()
                    val newDevice = device.toCarDevice(rssi = rssi)
                    _discoveredDevices.value = (_discoveredDevices.value + newDevice)
                        .distinctBy { it.address }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _isDiscovering.value = false
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    _isDiscovering.value = true
                }
            }
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        context.registerReceiver(discoveryReceiver, filter)
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /** Returns the set of already-paired devices. Safe to call from any thread. */
    fun getPairedDevices(): List<CarDevice> =
        adapter?.bondedDevices?.map { it.toCarDevice() } ?: emptyList()

    /** Starts Classic BT discovery. Clears the previous discovered list first. */
    fun startDiscovery() {
        if (adapter?.isEnabled != true) {
            serviceScope.launch { _errors.emit("Please enable Bluetooth first.") }
            return
        }
        _discoveredDevices.value = emptyList()
        adapter.startDiscovery()
    }

    /** Cancels an in-progress discovery scan. */
    fun stopDiscovery() {
        adapter?.cancelDiscovery()
    }

    /**
     * Opens an RFCOMM connection to [device] on a background coroutine.
     * Sets [connectionState] to [ConnectionState.Connecting] immediately,
     * then to [ConnectionState.Connected] on success or [ConnectionState.Error] on failure.
     */
    fun connect(device: BluetoothDevice) {
        reconnectEnabled = true
        connectedDevice = device
        reconnectJob?.cancel()
        serviceScope.launch { openSocket(device) }
    }

    /** Sends a [CarCommand] byte over the open socket. No-op if not connected. */
    fun send(command: CarCommand) {
        if (_connectionState.value !is ConnectionState.Connected) return
        serviceScope.launch {
            try {
                socket?.outputStream?.write(byteArrayOf(command.byte))
                socket?.outputStream?.flush()
            } catch (e: IOException) {
                handleDisconnect("Send failed: ${e.message}")
            }
        }
    }

    /**
     * Cleanly closes the connection and disables auto-reconnect.
     * Sets [connectionState] to [ConnectionState.Disconnected].
     */
    fun disconnect() {
        reconnectEnabled = false
        reconnectJob?.cancel()
        closeSocket()
        _connectionState.value = ConnectionState.Disconnected
        connectedDevice = null
    }

    /** Call in Application.onTerminate or when the process exits. */
    fun destroy() {
        stopDiscovery()
        try { context.unregisterReceiver(discoveryReceiver) } catch (_: Exception) {}
        disconnect()
        serviceScope.cancel()
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    /**
     * Opens the RFCOMM socket, connects it, and starts the RX reader loop.
     * Must be called from a coroutine on [Dispatchers.IO].
     */
    private suspend fun openSocket(device: BluetoothDevice) = withContext(Dispatchers.IO) {
        val deviceName = device.name ?: device.address
        _connectionState.value = ConnectionState.Connecting(deviceName)

        // Cancel discovery before connecting — dramatically improves connection reliability.
        adapter?.cancelDiscovery()

        closeSocket()  // ensure any previous socket is gone

        try {
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            socket!!.connect()                       // blocks until connected or throws

            _connectionState.value = ConnectionState.Connected(deviceName)
            startReading()
        } catch (e: IOException) {
            closeSocket()
            handleDisconnect("Connection failed: ${e.message}")
        }
    }

    /**
     * Reads lines from the socket's [InputStream] until an [IOException] is thrown
     * (indicating a disconnect or error). Each line is parsed as [CarTelemetry].
     */
    private fun startReading() {
        rxJob?.cancel()
        rxJob = serviceScope.launch(Dispatchers.IO) {
            val reader = BufferedReader(
                InputStreamReader(socket?.inputStream ?: return@launch)
            )
            try {
                while (true) {
                    val line = reader.readLine() ?: break   // null = stream closed
                    val parsed = CarTelemetry.parse(line)
                    _telemetry.value = parsed
                }
            } catch (e: IOException) {
                // Socket was closed — trigger reconnect logic.
            } finally {
                val deviceName = connectedDevice?.name ?: "Device"
                handleDisconnect("Connection to $deviceName was lost.")
            }
        }
    }

    /**
     * Called whenever the connection drops unexpectedly.
     * Initiates exponential back-off reconnect if [reconnectEnabled] is true.
     */
    private fun handleDisconnect(reason: String) {
        closeSocket()
        if (!reconnectEnabled || connectedDevice == null) {
            _connectionState.value = ConnectionState.Disconnected
            return
        }
        reconnectJob = serviceScope.launch {
            var delayMs = INITIAL_RECONNECT_DELAY
            for (attempt in 1..MAX_RECONNECT_ATTEMPTS) {
                val deviceName = connectedDevice?.name ?: "Device"
                _connectionState.value = ConnectionState.Reconnecting(deviceName, attempt)
                delay(delayMs)
                if (!reconnectEnabled) break

                try {
                    openSocket(connectedDevice!!)
                    return@launch   // success
                } catch (e: IOException) {
                    delayMs = minOf(delayMs * 2, MAX_RECONNECT_DELAY_MS)
                }
            }
            // All attempts exhausted.
            _connectionState.value = ConnectionState.Error("Could not reconnect. $reason")
            _errors.emit("Auto-reconnect failed after $MAX_RECONNECT_ATTEMPTS attempts.")
            reconnectEnabled = false
        }
    }

    private fun closeSocket() {
        rxJob?.cancel()
        try { socket?.close() } catch (_: IOException) {}
        socket = null
    }
}
