package com.rccar.controller.model

/**
 * Represents the lifecycle of a Bluetooth connection to the RC car.
 *
 * Used as a [kotlinx.coroutines.flow.StateFlow] value in [com.rccar.controller.viewmodel.BluetoothViewModel].
 */
sealed class ConnectionState {

    /** No device selected, or connection was cleanly closed. */
    data object Disconnected : ConnectionState()

    /** Socket is being opened and RFCOMM connection is being established. */
    data class Connecting(val deviceName: String) : ConnectionState()

    /** RFCOMM socket is open and I/O streams are ready. */
    data class Connected(val deviceName: String) : ConnectionState()

    /**
     * Connection was lost unexpectedly and auto-reconnect is in progress.
     * [attempt] is the current retry count (1-based).
     */
    data class Reconnecting(val deviceName: String, val attempt: Int) : ConnectionState()

    /**
     * An unrecoverable error occurred (e.g. device not found, BT turned off,
     * max reconnect attempts exceeded).
     */
    data class Error(val message: String) : ConnectionState()

    // ── Convenience helpers ──────────────────────────────────────────────────

    val isConnected: Boolean get() = this is Connected
    val isWorking:   Boolean get() = this is Connecting || this is Reconnecting

    val deviceName: String? get() = when (this) {
        is Connecting    -> deviceName
        is Connected     -> deviceName
        is Reconnecting  -> deviceName
        else             -> null
    }
}
