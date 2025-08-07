package com.saschl.sonygps

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.saschl.sonygps.service.CompanionDeviceManagerSample
import com.saschl.sonygps.service.FileTree
import timber.log.Timber

class MainActivity : ComponentActivity() {


    // needed to communicate with the service.

    // we need notification permission to be able to display a notification for the foreground service
    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            // if permission was denied, the service can still run only the notification won't be visible
        }

    // we need location permission to be able to start the service


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Only plant Timber once in the app lifecycle
        if (Timber.treeCount == 0) {
            Timber.plant(Timber.DebugTree(), FileTree())
        }

        Timber.i("onCreate called")
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(16, 16, 16, 16)

        val logButton = Button(this).apply {
            text = "View Logs"
            setOnClickListener {
                startActivity(Intent(this@MainActivity, com.saschl.sonygps.ui.LogViewerActivity::class.java))
            }
        }
        layout.addView(logButton)
        val composeView = androidx.compose.ui.platform.ComposeView(this).apply {
            setContent { CompanionDeviceManagerSample() }
        }
        layout.addView(composeView)
        setContentView(layout)

        // Handle system bars to prevent button from appearing in status bar
        ViewCompat.setOnApplyWindowInsetsListener(layout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(16, systemBars.top + 16, 16, systemBars.bottom + 16)
            insets
        }

        checkAndRequestNotificationPermission()
    }


    /**
     * Check for notification permission before starting the service so that the notification is visible
     */
    private fun checkAndRequestNotificationPermission() {
            when (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            )) {
                android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                    // permission already granted
                }

                else -> {
                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
        }
    }


    companion object {
        private const val TAG = "MainActivity"
    }
}