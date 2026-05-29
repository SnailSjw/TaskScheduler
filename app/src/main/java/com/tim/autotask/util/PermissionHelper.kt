package com.tim.autotask.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object PermissionHelper {

    fun getRequiredPermissions(): Array<String> {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        return permissions.toTypedArray()
    }

    fun getBackgroundLocationPermission(): String =
        Manifest.permission.ACCESS_BACKGROUND_LOCATION

    fun hasPermission(context: Context, permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    fun hasAllPermissions(context: Context, permissions: Array<String>): Boolean =
        permissions.all { hasPermission(context, it) }

    fun hasLocationPermissions(context: Context): Boolean = hasAllPermissions(
        context,
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    fun hasBackgroundLocation(context: Context): Boolean =
        hasPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
}
