package com.rccar.controller.bluetooth

import com.rccar.controller.model.CarTelemetry
import com.rccar.controller.model.ConnectionState
import kotlinx.coroutines.flow.StateFlow

/**
 * Common interface for both the real [BluetoothService] and the [TcpSimulationService].
 *
 * The [com.rccar.controller.viewmodel.BluetoothViewModel] depends on this interface,
 * which means simulation mode is a zero-cost swap — no UI code changes at all.
 */
interface ICarService {
    val connectionState: StateFlow<ConnectionState>
    val telemetry: StateFlow<CarTelemetry>

    fun send(command: CarCommand)
    fun disconnect()
    fun destroy()
}
