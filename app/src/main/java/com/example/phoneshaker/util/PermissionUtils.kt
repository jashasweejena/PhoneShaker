package com.example.phoneshaker.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

object PermissionUtils {

    // Launcher for requesting multiple permissions.
    private var multiPermissionLauncher: ActivityResultLauncher<Array<String>>? = null

    /**
     * Registers a launcher for multiple permissions.
     * Call this in your Fragment’s onCreate.
     *
     * @param fragment The Fragment used for launcher registration.
     * @param callback A callback that receives a map of permission results.
     */
    fun registerMultiPermissionLauncher(
        fragment: Fragment,
        callback: (Map<String, Boolean>) -> Unit,
        onPermissionDenied: ((Map<String, Boolean>) -> Unit)
    ) {
        multiPermissionLauncher =
            fragment.registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { result ->
                if (result.all { it.value }) {
                    // All permissions granted
                    callback(result)
                } else {
                    // Some permissions denied
                    //need to check with design if permission is not granted.
                    Log.e("PermissionUtils", "Some permissions denied")
                    onPermissionDenied?.invoke(result)
                }
            }
    }

    fun registerMultiPermissionLauncher(
        activity: ComponentActivity,
        callback: (Map<String, Boolean>) -> Unit,
        onPermissionDenied: ((Map<String, Boolean>) -> Unit)
    ) {
        multiPermissionLauncher =
            activity.registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { result ->
                if (result.all { it.value }) {
                    // All permissions granted
                    callback(result)
                } else {
                    // Some permissions denied
                    //need to check with design if permission is not granted.
                    Log.e("PermissionUtils", "Some permissions denied")
                    onPermissionDenied?.invoke(result)
                }
            }
    }

    /**
     * Checks if all given permissions (as a list of AppPermission enums) are granted.
     * Internally, it converts the list of enums into their corresponding permission strings.
     */
    fun arePermissionsGranted(context: Context, permissions: List<AppPermission>): Boolean {
        val permissionStrings = permissions.map { it.permission }
        return permissionStrings.all { permission ->
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Checks and requests the provided permissions (as a list of AppPermission enums) if not already granted.
     * If all permissions are granted, the onAllPermissionsGranted callback is invoked immediately.
     * Otherwise, the registered launcher is used to request them.
     *
     * @param fragment The Fragment from which permissions are requested.
     * @param permissions The list of AppPermission enums to check/request.
     * @param onAllPermissionsGranted Invoked when all permissions are granted.
     * @param onPermissionDenied (Optional) Invoked if any permission is denied.
     */
    fun checkAndRequestPermissions(
        fragment: Fragment,
        permissions: List<AppPermission>,
        onAllPermissionsGranted: () -> Unit,
        onPermissionDenied: (() -> Unit)? = null
    ) {
        val permissionStrings = permissions.map { it.permission }.toTypedArray()
        if (permissionStrings.all {
                ContextCompat.checkSelfPermission(
                    fragment.requireContext(),
                    it
                ) == PackageManager.PERMISSION_GRANTED
            }) {
            onAllPermissionsGranted()
        } else {
            multiPermissionLauncher?.launch(permissionStrings)
        }
    }

    fun checkAndRequestPermissions(
        activity: ComponentActivity,
        permissions: List<AppPermission>,
        onAllPermissionsGranted: () -> Unit,
        onPermissionDenied: (() -> Unit)? = null
    ) {
        val permissionStrings = permissions.map { it.permission }.toTypedArray()
        if (permissionStrings.all {
                ContextCompat.checkSelfPermission(
                    activity,
                    it
                ) == PackageManager.PERMISSION_GRANTED
            }) {
            onAllPermissionsGranted()
        } else {
            multiPermissionLauncher?.launch(permissionStrings)
        }
    }

    /**
     * Returns the required permissions for camera usage as a list of AppPermission enums.
     * On devices with API ≤ P, WRITE_EXTERNAL_STORAGE is also required.
     */
    fun getCameraPermissionEnums(): List<AppPermission> {
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            listOf(AppPermission.CAMERA, AppPermission.WRITE_EXTERNAL_STORAGE)
        } else {
            listOf(AppPermission.CAMERA)
        }
    }

    /**
     * Returns the required permissions for post notification as a list of AppPermission enums.
     */
    fun getPostNotificationPermissionEnums(): List<AppPermission> {
        return listOf(AppPermission.POST_NOTIFICATION)
    }
}

enum class AppPermission(val permission: String) {
    CAMERA(Manifest.permission.CAMERA),
    WRITE_EXTERNAL_STORAGE(Manifest.permission.WRITE_EXTERNAL_STORAGE),
    POST_NOTIFICATION(Manifest.permission.POST_NOTIFICATIONS),
}


