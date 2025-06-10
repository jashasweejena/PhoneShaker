package com.example.phoneshaker.activity.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.phoneshaker.activity.ui.composition.LocalBatteryHelper
import com.example.phoneshaker.service.FlashLightService
import com.example.phoneshaker.util.BatteryOptimizationState
import com.example.phoneshaker.util.DataStoreConstants
import com.example.phoneshaker.util.PreferenceManager
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun SettingsPage(modifier: Modifier = Modifier) {
    val batteryOptimizationHelper = LocalBatteryHelper.current
    val batteryOptimizationStatus by batteryOptimizationHelper.getBatteryOptimizationStatus()
        .collectAsStateWithLifecycle()
    Column(modifier = modifier.padding(horizontal = 12.dp)) {
        var isSliderEnabled by remember { mutableStateOf(false) }
        val shouldShowDialog = batteryOptimizationStatus != BatteryOptimizationState.GRANTED
        AnimatedVisibility(shouldShowDialog) {
            BatteryOptimizationDialog(onAllowClick = {
                batteryOptimizationHelper.requestIgnoreBatteryOptimizations()
            }, onDismiss = {

            })
        }
        EnableSwitch {
            isSliderEnabled = it
        }
        SliderItem(isEnabled = isSliderEnabled)
    }
}

@Composable
fun BatteryOptimizationDialog(
    modifier: Modifier = Modifier,
    onAllowClick: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header with close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Icon and title row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = "Battery Alert",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Disable Battery Optimization",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Close button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Text(
                text = "To ensure this app works properly in the background and doesn't miss important notifications, please disable battery optimization.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Additional info
            Text(
                text = "This will allow the app to run in the background when needed.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cancel button
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(
                        text = "Not Now",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Allow button
                Button(
                    onClick = onAllowClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Open Settings",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}
@Composable
fun SliderItem(modifier: Modifier = Modifier, isEnabled: Boolean) {
    val sliderRange = 30f..80f
    var sliderPosition by remember { mutableFloatStateOf(sliderRange.start) }
    val animatedAlpha: Float by animateFloatAsState(
        if (isEnabled) 1f else 0.5f, label = "alpha", animationSpec = tween(300)
    )

    Column(
        modifier = modifier.graphicsLayer { alpha = animatedAlpha }) {
        Text("Acceleration Threshold")
        Slider(
            enabled = isEnabled, value = sliderPosition, onValueChange = {
                sliderPosition = it
                FlashLightService.ACCELERATION_THRESHOLD = it.toLong()
            }, valueRange = sliderRange
        )
        Text("Threshold: ${sliderPosition.toInt()}")
    }
}

@Composable
fun EnableSwitch(
    modifier: Modifier = Modifier,
    preferencesManager: PreferenceManager = koinInject(),
    onCheckedChange: (Boolean) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val enabledState by preferencesManager.getPrimitiveFlow(
        DataStoreConstants.IS_SERVICE_ENABLED, defaultValue = false
    ).collectAsState(initial = false)

    LaunchedEffect(enabledState) {
        onCheckedChange(enabledState)
        if (enabledState) {
            FlashLightService.start(context)
        } else {
            FlashLightService.stop(context)
        }
    }
    Column {
        Text("Enable Service")
        Switch(
            checked = enabledState, modifier = modifier, onCheckedChange = {
                scope.launch {
                    runCatching {
                        preferencesManager.savePrimitive(DataStoreConstants.IS_SERVICE_ENABLED, it)
                    }
                }
            })
    }
}