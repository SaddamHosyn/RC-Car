package com.rccar.controller.model

import android.bluetooth.BluetoothDevice

/**
 * Domain model representing a discovered Bluetooth device.
 * Wraps [BluetoothDevice] to avoid leaking Android framework types into the ViewModel/UI layers.
 */
data class CarDevice(
    val address: String,
    val name: String,
    val isPaired: Boolean,
    val rssi: Int = -1,                // Signal strength (dBm) — -1 if unknown
    val isBle: Boolean = false,        // true = BLE (HM-10 / ESP32), false = Classic SPP
) {
    /** Human-readable signal strength label */
    val signalLabel: String get() = when {
        rssi >= -60 -> "Excellent"
        rssi >= -70 -> "Good"
        rssi >= -80 -> "Fair"
        else        -> "Weak"
    }
}

/** Convert an Android [BluetoothDevice] to a [CarDevice] domain object. */
fun BluetoothDevice.toCarDevice(rssi: Int = -1, isBle: Boolean = false): CarDevice = CarDevice(
    address  = address ?: "Unknown",
    name     = name ?: "Unknown Device",
    isPaired = bondState == BluetoothDevice.BOND_BONDED,
    rssi     = rssi,
    isBle    = isBle,
)
