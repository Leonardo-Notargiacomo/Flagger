package com.fontys.frontend.ui.views

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fontys.frontend.ui.viewmodels.FriendsViewModel

class FriendsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface {
                    val viewModel: FriendsViewModel = viewModel()
                    // TODO: Get auth token from SharedPreferences or your auth system
                    // viewModel.setAuthToken(yourAuthToken)
                    FriendsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
