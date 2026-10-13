package com.rccar.controller.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rccar.controller.bluetooth.BluetoothService
import com.rccar.controller.bluetooth.CarCommand
import com.rccar.controller.bluetooth.ICarService
import com.rccar.controller.bluetooth.TcpSimulationService
import com.rccar.controller.model.CarDevice
import com.rccar.controller.model.CarTelemetry
import com.rccar.controller.model.ConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * [BluetoothViewModel] is the single shared ViewModel for all car-control operations.
 *
 * It supports two modes:
 *   - **Real mode**: Uses [BluetoothService] (RFCOMM over Classic Bluetooth)
 *   - **Simulation mode**: Uses [TcpSimulationService] (TCP socket to Python simulator on PC)
 *
 * The active service is exposed via [activeService], which is swapped by [enableSimulationMode].
 */
@SuppressLint("MissingPermission")
class BluetoothViewModel(application: Application) : AndroidViewModel(application) {

    // ── Services ──────────────────────────────────────────────────────────────
    val bluetoothService = BluetoothService(application.applicationContext)

    /** The currently active service (real BT or TCP simulation). */
    private var activeService: ICarService = bluetoothService

    // ── Simulation mode flag ──────────────────────────────────────────────────
    private val _isSimulationMode = MutableStateFlow(false)
    val isSimulationMode: StateFlow<Boolean> = _isSimulationMode.asStateFlow()

    // ── State flows (re-computed when service changes) ────────────────────────
    // These delegate to the active service and are kept in sync via collect.

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _telemetry = MutableStateFlow(CarTelemetry.Empty)
    val telemetry: StateFlow<CarTelemetry> = _telemetry.asStateFlow()

    // BT-only flows (null in simulation mode)
    val discoveredDevices = bluetoothService.discoveredDevices
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val isDiscovering = bluetoothService.isDiscovering
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val errors: SharedFlow<String> = bluetoothService.errors

    // ── Local UI state ────────────────────────────────────────────────────────
    private val _pairedDevices = MutableStateFlow<List<CarDevice>>(emptyList())
    val pairedDevices: StateFlow<List<CarDevice>> = _pairedDevices.asStateFlow()

    private val _speedLevel = MutableStateFlow(2)
    val speedLevel: StateFlow<Int> = _speedLevel.asStateFlow()

    private val _lineFollowMode = MutableStateFlow(false)
    val lineFollowMode: StateFlow<Boolean> = _lineFollowMode.asStateFlow()

    val isBluetoothReady: Boolean
        get() = bluetoothService.isBluetoothSupported && bluetoothService.isBluetoothEnabled

    // ── Collector job ─────────────────────────────────────────────────────────
    private var collectorJob: kotlinx.coroutines.Job? = null

    init {
        loadPairedDevices()
        startCollecting(bluetoothService)
    }

    /** Starts collecting state from the given [service] into our own StateFlows. */
    private fun startCollecting(service: ICarService) {
        collectorJob?.cancel()
        collectorJob = viewModelScope.launch {
            launch { service.connectionState.collect { _connectionState.value = it } }
            launch { service.telemetry.collect { _telemetry.value = it } }
        }
    }

    // ── Simulation mode ───────────────────────────────────────────────────────

    /**
     * Switches to [TcpSimulationService].
     * Tears down any existing BT connection, swaps the active service,
     * and immediately starts collecting the new service's state.
     */
    fun enableSimulationMode() {
        if (_isSimulationMode.value) return
        bluetoothService.disconnect()
        val sim = TcpSimulationService()
        activeService = sim
        _isSimulationMode.value = true
        startCollecting(sim)
    }

    /**
     * Switches back to [BluetoothService] (real hardware mode).
     */
    fun disableSimulationMode() {
        if (!_isSimulationMode.value) return
        activeService.disconnect()
        activeService = bluetoothService
        _isSimulationMode.value = false
        startCollecting(bluetoothService)
    }

    // ── Scan actions (BT only) ────────────────────────────────────────────────

    fun loadPairedDevices() {
        _pairedDevices.value = bluetoothService.getPairedDevices()
    }

    fun startDiscovery() = bluetoothService.startDiscovery()
    fun stopDiscovery()  = bluetoothService.stopDiscovery()

    // ── Connection actions ────────────────────────────────────────────────────

    fun connect(device: CarDevice) {
        val adapter = bluetoothService.adapter ?: return
        val btDevice: BluetoothDevice = adapter.getRemoteDevice(device.address)
        bluetoothService.connect(btDevice)
    }

    fun disconnect() = activeService.disconnect()

    // ── Control actions ───────────────────────────────────────────────────────

    fun sendCommand(command: CarCommand) = activeService.send(command)

    fun setSpeed(level: Int) {
        _speedLevel.value = level.coerceIn(1, 3)
        val cmd = when (level) { 1 -> CarCommand.SpeedLow; 3 -> CarCommand.SpeedHigh; else -> CarCommand.SpeedMid }
        activeService.send(cmd)
    }

    fun toggleLineFollow() {
        val newState = !_lineFollowMode.value
        _lineFollowMode.value = newState
        activeService.send(if (newState) CarCommand.LineFollowOn else CarCommand.LineFollowOff)
    }

    fun requestTelemetry() = activeService.send(CarCommand.RequestTelemetry)

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCleared() {
        super.onCleared()
        bluetoothService.destroy()
        if (activeService !== bluetoothService) activeService.destroy()
    }
}
