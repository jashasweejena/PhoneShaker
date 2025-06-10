package com.example.phoneshaker.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.phoneshaker.activity.ui.AppEntry
import com.example.phoneshaker.activity.ui.composition.LocalBatteryHelper
import com.example.phoneshaker.service.FlashLightService
import com.example.phoneshaker.ui.theme.PhoneShakerTheme
import com.example.phoneshaker.util.BatteryOptimizationHelper
import com.example.phoneshaker.util.PermissionUtils
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class MainActivity : ComponentActivity() {
    private val batteryOptimizationHelper: BatteryOptimizationHelper by inject { parametersOf(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        batteryOptimizationHelper.initialize()
        enableEdgeToEdge()
        setContent {
            PhoneShakerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CompositionLocalProvider(LocalBatteryHelper provides batteryOptimizationHelper) {
                        AppEntry(modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
        registerPermissionLauncher()
        checkNotificationPermission()
    }

    private fun registerPermissionLauncher() {
        PermissionUtils.registerMultiPermissionLauncher(activity = this,
            callback = {},
            onPermissionDenied = {})
    }

    private fun checkNotificationPermission() {
        PermissionUtils.checkAndRequestPermissions(activity = this,
            permissions = PermissionUtils.getPostNotificationPermissionEnums(),
            onAllPermissionsGranted = {},
            onPermissionDenied = {})
    }

    override fun onDestroy() {
        val serviceIntent = Intent(this, FlashLightService::class.java)
        stopService(serviceIntent)
        super.onDestroy()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!", modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PhoneShakerTheme {
        Greeting("Android")
    }
}