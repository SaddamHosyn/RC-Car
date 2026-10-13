package com.rccar.controller

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.rccar.controller.navigation.AppNavigation
import com.rccar.controller.ui.theme.RCCarTheme

/**
 * The single Activity that hosts the entire Compose UI tree.
 *
 * Responsibilities:
 *  - Enable edge-to-edge display
 *  - Request Bluetooth enable if it is off when the app starts
 *  - Mount the theme and the navigation graph
 *
 * Everything else is handled by composables and the ViewModel.
 */
class MainActivity : ComponentActivity() {

    /** Launcher for the "please enable Bluetooth" system dialog. */
    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* Result handled reactively via BluetoothService state flows */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // If Bluetooth is available but off, prompt the user to enable it.
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter != null && !adapter.isEnabled) {
            enableBluetoothLauncher.launch(
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            )
        }

        setContent {
            RCCarTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = MaterialTheme.colorScheme.background,
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
