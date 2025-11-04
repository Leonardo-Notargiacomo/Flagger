package com.fontys.frontend.common

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fontys.frontend.ui.views.MapsActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Launch MapsActivity as the main screen
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)

        // Finish MainActivity so it's not in the back stack
        finish()
    }
}
