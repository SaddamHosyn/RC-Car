package com.rccar.controller.ui.about

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rccar.controller.ui.theme.CardBorder
import com.rccar.controller.ui.theme.CardDark
import com.rccar.controller.ui.theme.DarkNavy
import com.rccar.controller.ui.theme.NeonCyan
import com.rccar.controller.ui.theme.NeonCyanDim
import com.rccar.controller.ui.theme.SecondaryDark
import com.rccar.controller.ui.theme.SurfaceDark
import com.rccar.controller.ui.theme.TertiaryDark
import com.rccar.controller.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        containerColor = DarkNavy,
        topBar = {
            TopAppBar(
                title = { Text("About", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark),
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Spacer(Modifier.height(16.dp))

            // ── App icon + name ──────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = 0.1f))
                        .padding(16.dp),
                )
                Text(
                    text  = "RC Car Controller",
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text  = "Version 2.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeonCyan,
                )
                Text(
                    text  = "A modern Kotlin + Jetpack Compose rewrite of the original 2015 Java/Eclipse FYP",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                )
            }

            HorizontalDivider(color = CardBorder)

            // ── Tech stack ───────────────────────────────────────────────────
            AboutSection(title = "Technology Stack") {
                TechRow(Icons.Default.Code, "Language", "Kotlin 2.0", NeonCyan)
                TechRow(Icons.Default.Code, "UI", "Jetpack Compose + Material 3", SecondaryDark)
                TechRow(Icons.Default.Code, "Architecture", "MVVM + Repository", TertiaryDark)
                TechRow(Icons.Default.Code, "Async", "Kotlin Coroutines + StateFlow", NeonCyanDim)
                TechRow(Icons.Default.Code, "Bluetooth", "Classic SPP (RFCOMM)", NeonCyan)
                TechRow(Icons.Default.Code, "Build", "Gradle 8 · Kotlin DSL", SecondaryDark)
                TechRow(Icons.Default.Code, "Min Android", "API 24 (Android 7.0)", TertiaryDark)
            }

            // ── What improved ─────────────────────────────────────────────────
            AboutSection(title = "vs. The Original FYP (2015)") {
                ImprovementRow("Runtime Bluetooth permissions (API 31+)")
                ImprovementRow("All BT I/O on background coroutines (no ANR risk)")
                ImprovementRow("Device discovery — no more typing device names")
                ImprovementRow("Auto-reconnect with exponential back-off")
                ImprovementRow("Bidirectional comms — reads battery & distance data")
                ImprovementRow("Press-and-hold D-pad — auto-stops on finger lift")
                ImprovementRow("MVVM — ViewModel survives screen rotation")
                ImprovementRow("Single Activity + Navigation Compose")
                ImprovementRow("Speed control and line-follow mode commands")
            }

            // ── Original credit ──────────────────────────────────────────────
            AboutSection(title = "Original Project Credit") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.Default.School, null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                    Text(
                        text  = "Original base by Arvind Sanjeev — DIYhacking.com\nAdapted & rewritten as Undergraduate FYP (2015 → 2026)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AboutSection(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardDark, RoundedCornerShape(14.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = NeonCyan)
        content()
    }
}

@Composable
private fun TechRow(icon: ImageVector, label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
        Text(value, style = MaterialTheme.typography.labelLarge, color = valueColor)
    }
}

@Composable
private fun ImprovementRow(text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("✓", style = MaterialTheme.typography.labelLarge, color = NeonCyan)
        Text(text, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}
