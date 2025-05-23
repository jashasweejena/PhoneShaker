package com.example.phoneshaker.activity.ui

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.phoneshaker.di.ApplicationCompositionRoot
import com.example.phoneshaker.service.FlashLightService
import com.example.phoneshaker.util.DataStoreConstants
import kotlinx.coroutines.launch

@Composable
fun AppEntry(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferencesManager = remember { ApplicationCompositionRoot.getPreferenceManager() }
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