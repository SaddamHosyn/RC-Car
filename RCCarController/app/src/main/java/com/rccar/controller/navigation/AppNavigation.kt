package com.rccar.controller.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rccar.controller.ui.about.AboutScreen
import com.rccar.controller.ui.control.ControlScreen
import com.rccar.controller.ui.help.HelpScreen
import com.rccar.controller.ui.home.HomeScreen
import com.rccar.controller.ui.scan.ScanScreen
import com.rccar.controller.viewmodel.BluetoothViewModel

/** Navigation route constants. */
object Route {
    const val HOME       = "home"
    const val SCAN       = "scan"
    const val CONTROL    = "control"
    const val SIMULATION = "simulation"   // goes straight to control in sim mode
    const val HELP       = "help"
    const val ABOUT      = "about"
}

/**
 * Root navigation composable.
 *
 * The [BluetoothViewModel] is created once here and passed to child screens so that
 * BT state is shared and survives screen transitions.
 *
 * Simulation path:
 *   Home ──[Simulation Mode]──► SIMULATION route
 *     → enableSimulationMode() on ViewModel
 *     → navigate straight to ControlScreen (no BT scan needed)
 */
@Composable
fun AppNavigation(
    navController:      NavHostController  = rememberNavController(),
    bluetoothViewModel: BluetoothViewModel = viewModel(),
) {
    NavHost(
        navController    = navController,
        startDestination = Route.HOME,
    ) {

        // ── Home ──────────────────────────────────────────────────────────────
        composable(Route.HOME) {
            HomeScreen(
                onNavigateToScan       = { navController.navigate(Route.SCAN) },
                onNavigateToSimulation = { navController.navigate(Route.SIMULATION) },
            )
        }

        // ── Bluetooth scan ────────────────────────────────────────────────────
        composable(Route.SCAN) {
            ScanScreen(
                viewModel            = bluetoothViewModel,
                onNavigateToControl  = {
                    navController.navigate(Route.CONTROL) {
                        popUpTo(Route.SCAN) { inclusive = true }
                    }
                },
                onNavigateBack       = { navController.popBackStack() },
            )
        }

        // ── Real control screen ───────────────────────────────────────────────
        composable(Route.CONTROL) {
            ControlScreen(
                viewModel        = bluetoothViewModel,
                onNavigateToScan = {
                    navController.navigate(Route.SCAN) { popUpTo(Route.HOME) }
                },
                onNavigateBack   = { navController.popBackStack() },
            )
        }

        // ── Simulation entry point ────────────────────────────────────────────
        composable(Route.SIMULATION) {
            // Enable simulation mode in the ViewModel, then go straight to control.
            // LaunchedEffect ensures this runs once when the composable enters.
            androidx.compose.runtime.LaunchedEffect(Unit) {
                bluetoothViewModel.enableSimulationMode()
                navController.navigate(Route.CONTROL) {
                    popUpTo(Route.SIMULATION) { inclusive = true }
                }
            }
        }

        // ── Help & About ──────────────────────────────────────────────────────
        composable(Route.HELP) {
            HelpScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Route.ABOUT) {
            AboutScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
