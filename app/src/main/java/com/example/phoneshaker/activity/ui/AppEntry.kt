package com.example.phoneshaker.activity.ui

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.phoneshaker.service.FlashLightService
import com.example.phoneshaker.util.DataStoreConstants
import com.example.phoneshaker.util.PreferenceManager
import kotlinx.coroutines.launch
import org.koin.compose.koinInject


@Composable
fun AppEntry(modifier: Modifier = Modifier, preferencesManager: PreferenceManager = koinInject()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val enabledState by preferencesManager.getPrimitiveFlow(
        DataStoreConstants.IS_SERVICE_ENABLED,
        defaultValue = false
    ).collectAsState(initial = false)
    Switch(
        checked = enabledState,
        modifier = modifier,
        onCheckedChange = {
            scope.launch {
                runCatching {
                    preferencesManager.savePrimitive(DataStoreConstants.IS_SERVICE_ENABLED, it)
                }
            }
            if (it) {
                FlashLightService.start(context)
            } else {
                FlashLightService.stop(context)
            }
        }
    )
}