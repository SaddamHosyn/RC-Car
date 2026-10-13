package com.rccar.controller.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rccar.controller.bluetooth.CarCommand
import com.rccar.controller.ui.theme.CardBorder
import com.rccar.controller.ui.theme.CardDark
import com.rccar.controller.ui.theme.NeonCyan

/**
 * A single D-pad button with press-and-hold semantics.
 *
 * When pressed:
 *   - [onCommand] is called with [command]
 * When released:
 *   - [onCommand] is called with [CarCommand.Stop]
 *
 * This means the car stops the moment the user lifts their finger — much safer
 * than the original app's tap-to-send approach.
 */
@Composable
fun DPadButton(
    icon:      ImageVector,
    command:   CarCommand,
    enabled:   Boolean,
    onCommand: (CarCommand) -> Unit,
    modifier:  Modifier = Modifier,
    size:      Dp = 72.dp,
) {
    var pressed by remember { mutableStateOf(false) }

    val bgColor = when {
        !enabled -> CardDark.copy(alpha = 0.3f)
        pressed  -> NeonCyan.copy(alpha = 0.25f)
        else     -> CardDark
    }
    val borderColor = when {
        !enabled -> CardBorder.copy(alpha = 0.3f)
        pressed  -> NeonCyan
        else     -> CardBorder
    }
    val iconColor = when {
        !enabled -> CardBorder
        pressed  -> NeonCyan
        else     -> Color.White.copy(alpha = 0.8f)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onPress = {
                        pressed = true
                        onCommand(command)
                        tryAwaitRelease()
                        pressed = false
                        onCommand(CarCommand.Stop)
                    }
                )
            }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = command::class.simpleName,
            tint = iconColor,
            modifier = Modifier.size(32.dp),
        )
    }
}

/**
 * The full D-pad layout:
 *
 * ```
 *        [▲ Forward]
 * [◄ Left] [⬛ Stop] [► Right]
 *        [▼ Back  ]
 * ```
 */
@Composable
fun DPad(
    enabled:   Boolean,
    onCommand: (CarCommand) -> Unit,
    modifier:  Modifier = Modifier,
) {
    // Use a manual grid layout for pixel-perfect control.
    val btnSize = 72.dp
    val gap     = 10.dp

    // Total width = 3 buttons + 2 gaps
    val totalWidth  = btnSize * 3 + gap * 2
    val totalHeight = btnSize * 3 + gap * 2

    Box(
        modifier = modifier.size(totalWidth, totalHeight),
    ) {
        // Forward — top center
        DPadButton(
            icon = Icons.Default.ArrowUpward,
            command = CarCommand.Forward,
            enabled = enabled,
            onCommand = onCommand,
            modifier = Modifier.align(Alignment.TopCenter),
            size = btnSize,
        )
        // Left — middle left
        DPadButton(
            icon = Icons.Default.ArrowBack,
            command = CarCommand.TurnLeft,
            enabled = enabled,
            onCommand = onCommand,
            modifier = Modifier.align(Alignment.CenterStart),
            size = btnSize,
        )
        // Stop — center
        StopButton(
            enabled = enabled,
            onCommand = onCommand,
            modifier = Modifier.align(Alignment.Center),
            size = btnSize,
        )
        // Right — middle right
        DPadButton(
            icon = Icons.Default.ArrowForward,
            command = CarCommand.TurnRight,
            enabled = enabled,
            onCommand = onCommand,
            modifier = Modifier.align(Alignment.CenterEnd),
            size = btnSize,
        )
        // Backward — bottom center
        DPadButton(
            icon = Icons.Default.ArrowDownward,
            command = CarCommand.Backward,
            enabled = enabled,
            onCommand = onCommand,
            modifier = Modifier.align(Alignment.BottomCenter),
            size = btnSize,
        )
    }
}

/** The center stop button — always red to stand out. */
@Composable
private fun StopButton(
    enabled:   Boolean,
    onCommand: (CarCommand) -> Unit,
    modifier:  Modifier = Modifier,
    size:      Dp = 72.dp,
) {
    var pressed by remember { mutableStateOf(false) }
    val color = if (enabled) com.rccar.controller.ui.theme.Danger else CardBorder

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(if (pressed) color.copy(alpha = 0.3f) else color.copy(alpha = 0.15f))
            .border(1.5.dp, color, CircleShape)
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onPress = {
                        pressed = true
                        onCommand(CarCommand.Stop)
                        tryAwaitRelease()
                        pressed = false
                    }
                )
            }
    ) {
        Icon(
            imageVector = Icons.Default.Stop,
            contentDescription = "Stop",
            tint = if (enabled) color else CardBorder,
            modifier = Modifier.size(32.dp),
        )
    }
}
