package com.rccar.controller.model

/**
 * Represents a snapshot of data received FROM the RC car.
 *
 * The car firmware should send a JSON line (or CSV) over the Bluetooth serial link, e.g.:
 *   `{"bat":78,"dist":34}`
 *
 * This data class is populated by [com.rccar.controller.bluetooth.BluetoothService]
 * after parsing each incoming line.
 */
data class CarTelemetry(
    /** Battery percentage 0–100, or null if not yet received. */
    val batteryPercent: Int? = null,

    /** Distance reading from an ultrasonic sensor in centimetres, or null if not available. */
    val distanceCm: Int? = null,

    /** Raw last line received (useful for debugging). */
    val rawLine: String = "",
) {
    companion object {
        /** Parses a raw string line from the car into a [CarTelemetry] object.
         *  Expected formats (tolerant parser — any field can be absent):
         *    JSON : `{"bat":78,"dist":34}`
         *    CSV  : `78,34`
         */
        fun parse(line: String): CarTelemetry {
            if (line.isBlank()) return CarTelemetry(rawLine = line)

            return try {
                when {
                    // JSON format
                    line.trimStart().startsWith('{') -> {
                        val bat  = Regex(""""bat"\s*:\s*(\d+)""").find(line)?.groupValues?.get(1)?.toIntOrNull()
                        val dist = Regex(""""dist"\s*:\s*(\d+)""").find(line)?.groupValues?.get(1)?.toIntOrNull()
                        CarTelemetry(batteryPercent = bat, distanceCm = dist, rawLine = line)
                    }
                    // CSV format: "bat,dist"
                    line.contains(',') -> {
                        val parts = line.split(',')
                        CarTelemetry(
                            batteryPercent = parts.getOrNull(0)?.trim()?.toIntOrNull(),
                            distanceCm     = parts.getOrNull(1)?.trim()?.toIntOrNull(),
                            rawLine        = line,
                        )
                    }
                    else -> CarTelemetry(rawLine = line)
                }
            } catch (e: Exception) {
                CarTelemetry(rawLine = line)
            }
        }

        /** Sentinel value emitted when no telemetry has been received yet. */
        val Empty = CarTelemetry()
    }
}
