package com.tim.autotask

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.tim.autotask.ui.navigation.AppNavigation
import com.tim.autotask.ui.theme.AutoTaskTheme
import com.tim.autotask.util.PermissionHelper

class MainActivity : ComponentActivity() {

    private var navigateToTaskId by mutableStateOf<Long?>(null)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestRequiredPermissions()

        navigateToTaskId = intent?.getLongExtra("navigate_to_task", -1L)?.takeIf { it != -1L }

        setContent {
            AutoTaskTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(initialTaskId = navigateToTaskId)
                }
            }
        }
    }

    private fun requestRequiredPermissions() {
        val permissions = PermissionHelper.getRequiredPermissions()
        if (!PermissionHelper.hasAllPermissions(this, permissions)) {
            permissionLauncher.launch(permissions)
        }
    }
}
