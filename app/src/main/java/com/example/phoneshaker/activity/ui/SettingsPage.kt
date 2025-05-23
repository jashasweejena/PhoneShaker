package com.example.phoneshaker.activity.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.phoneshaker.service.FlashLightService
import com.example.phoneshaker.util.DataStoreConstants
import com.example.phoneshaker.util.PreferenceManager
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun SettingsPage(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(horizontal = 12.dp)) {
        var isSliderEnabled by remember { mutableStateOf(false) }

        EnableSwitch {
            isSliderEnabled = it
        }
        SliderItem(isEnabled = isSliderEnabled)
    }
}

@Composable
fun SliderItem(modifier: Modifier = Modifier, isEnabled: Boolean) {
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    val animatedAlpha: Float by animateFloatAsState(
        if (isEnabled) 1f else 0.5f,
        label = "alpha",
        animationSpec = tween(300)
    )

    Column(
        modifier = modifier
            .graphicsLayer { alpha = animatedAlpha }
    ) {
        Text("Acceleration Threshold")
        Slider(
            enabled = isEnabled,
            value = sliderPosition,
            onValueChange = {
                sliderPosition = it
                FlashLightService.ACCELERATION_THRESHOLD = it.toLong()
            },
            steps = 3,
            valueRange = 50f..80f
        )
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
        DataStoreConstants.IS_SERVICE_ENABLED,
        defaultValue = false
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
            checked = enabledState,
            modifier = modifier,
            onCheckedChange = {
                scope.launch {
                    runCatching {
                        preferencesManager.savePrimitive(DataStoreConstants.IS_SERVICE_ENABLED, it)
                    }
                }
            }
        )
    }
}