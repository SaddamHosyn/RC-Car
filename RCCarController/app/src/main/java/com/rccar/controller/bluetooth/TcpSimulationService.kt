package com.rccar.controller.bluetooth

import com.rccar.controller.model.CarTelemetry
import com.rccar.controller.model.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket

/**
 * [TcpSimulationService] implements [ICarService] by connecting to the
 * Python `car_simulator.py` server over a local TCP socket instead of Bluetooth.
 *
 * On the Android Emulator, `10.0.2.2` is the loopback address of the host PC.
 * To bridge the connection, run this ADB command once before launching the app:
 *
 *   `adb reverse tcp:9035 tcp:9035`
 *
 * Then the emulator's `127.0.0.1:9035` is forwarded to the host's `127.0.0.1:9035`
 * where the Python server is listening.
 */
class TcpSimulationService : ICarService {

    companion object {
        // 10.0.2.2 = host loopback as seen from the emulator.
        // After `adb reverse tcp:9035 tcp:9035`, connecting to 127.0.0.1:9035
        // from inside the emulator also works — we use 10.0.2.2 as it is more explicit.
        private const val HOST = "10.0.2.2"
        private const val PORT = 9035
        private const val DEVICE_NAME = "Simulated Car"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _telemetry = MutableStateFlow(CarTelemetry.Empty)
    override val telemetry: StateFlow<CarTelemetry> = _telemetry.asStateFlow()

    private var tcpSocket: Socket? = null
    private var rxJob: Job? = null

    init {
        connect()
    }

    private fun connect() {
        scope.launch {
            _connectionState.value = ConnectionState.Connecting(DEVICE_NAME)
            try {
                val socket = withContext(Dispatchers.IO) {
                    Socket(HOST, PORT).apply {
                        soTimeout = 0           // no read timeout — block until data arrives
                    }
                }
                tcpSocket = socket
                _connectionState.value = ConnectionState.Connected(DEVICE_NAME)
                startReading(socket)
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.Error(
                    "Could not reach simulator at $HOST:$PORT.\n" +
                    "Run: adb reverse tcp:$PORT tcp:$PORT\n" +
                    "then start car_simulator.py"
                )
            }
        }
    }

    /** Reads newline-terminated telemetry lines sent by the Python simulator. */
    private fun startReading(socket: Socket) {
        rxJob = scope.launch(Dispatchers.IO) {
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            try {
                while (true) {
                    val line = reader.readLine() ?: break
                    _telemetry.value = CarTelemetry.parse(line)
                }
            } catch (_: Exception) { }
            _connectionState.value = ConnectionState.Disconnected
        }
    }

    override fun send(command: CarCommand) {
        if (_connectionState.value !is ConnectionState.Connected) return
        scope.launch(Dispatchers.IO) {
            try {
                tcpSocket?.getOutputStream()?.write(byteArrayOf(command.byte))
                tcpSocket?.getOutputStream()?.flush()
            } catch (_: Exception) {
                _connectionState.value = ConnectionState.Disconnected
            }
        }
    }

    override fun disconnect() {
        rxJob?.cancel()
        try { tcpSocket?.close() } catch (_: Exception) {}
        tcpSocket = null
        _connectionState.value = ConnectionState.Disconnected
    }

    override fun destroy() {
        disconnect()
        scope.cancel()
    }
}
