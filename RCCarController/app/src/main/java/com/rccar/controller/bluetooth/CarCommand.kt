package com.rccar.controller.bluetooth

/**
 * All commands the Android app can send to the RC car.
 *
 * Each command maps to a single ASCII byte (matching the original Arduino firmware protocol)
 * so that no changes to the car's firmware are required for Classic BT operation.
 *
 * For BLE, the same byte is written to the UART TX characteristic.
 */
sealed class CarCommand(val byte: Byte) {

    // ── Movement ─────────────────────────────────────────────────────────────
    data object Forward  : CarCommand('a'.code.toByte())
    data object Backward : CarCommand('e'.code.toByte())
    data object TurnLeft : CarCommand('d'.code.toByte())
    data object TurnRight: CarCommand('b'.code.toByte())
    data object Stop     : CarCommand('5'.code.toByte())

    // ── Modes ─────────────────────────────────────────────────────────────────
    /** Switch to autonomous line-following mode. */
    data object LineFollowOn  : CarCommand('l'.code.toByte())
    /** Return to manual RC mode. */
    data object LineFollowOff : CarCommand('m'.code.toByte())

    // ── Speed ─────────────────────────────────────────────────────────────────
    data object SpeedLow  : CarCommand('1'.code.toByte())
    data object SpeedMid  : CarCommand('2'.code.toByte())
    data object SpeedHigh : CarCommand('3'.code.toByte())

    // ── Telemetry request ─────────────────────────────────────────────────────
    /** Ask the car to send a telemetry packet (battery + distance). */
    data object RequestTelemetry : CarCommand('t'.code.toByte())

    companion object {
        /** Maps UI label strings to commands (useful for debug panels). */
        val all: List<CarCommand> = listOf(
            Forward, Backward, TurnLeft, TurnRight, Stop,
            LineFollowOn, LineFollowOff,
            SpeedLow, SpeedMid, SpeedHigh,
            RequestTelemetry,
        )
    }
}
