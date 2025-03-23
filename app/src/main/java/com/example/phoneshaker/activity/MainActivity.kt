package com.example.phoneshaker.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.phoneshaker.service.FlashLightService
import com.example.phoneshaker.ui.theme.PhoneShakerTheme
import com.example.phoneshaker.util.PermissionUtils

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PhoneShakerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android", modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
        registerPermissionLauncher()
        checkNotificationPermission()

        val serviceIntent = Intent(this, FlashLightService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
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