package com.fontys.frontend

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fontys.frontend.utils.NotificationPermissionHelper

class MainActivity : AppCompatActivity() {

    private lateinit var notificationPermissionHelper: NotificationPermissionHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize notification permission helper
        notificationPermissionHelper = NotificationPermissionHelper(this)

        // Request notification permission if not granted
        if (!notificationPermissionHelper.isNotificationPermissionGranted()) {
            notificationPermissionHelper.requestNotificationPermission()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        notificationPermissionHelper.handlePermissionResult(
            requestCode,
            grantResults,
            onGranted = {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            },
            onDenied = {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        )
    }
}