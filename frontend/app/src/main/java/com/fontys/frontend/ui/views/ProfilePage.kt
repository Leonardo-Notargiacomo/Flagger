package com.fontys.frontend.ui.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.fontys.frontend.data.UserReturn
import com.fontys.frontend.domain.UserRepository
import com.fontys.frontend.ui.viewmodels.ProfileViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.Icon
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue


@Composable
fun Profile(viewModel: ProfileViewModel){
    var pfp by remember { mutableStateOf("") }
    var name: String? by remember { mutableStateOf("") }
    var bio: String? by remember { mutableStateOf("") }

    var followers: Int? by remember { mutableStateOf(0) }
    var following: Int? by remember { mutableStateOf(0) }

    var postsNr: Int? by remember { mutableStateOf(0) }

    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()


    var changes= false

    var image = remember(pfp){
        viewModel.base64ToImageBitmap(pfp)
    }
    LaunchedEffect(Unit) {
        viewModel.getUser(UserRepository.userId.toString())
        var user: UserReturn? = viewModel.user.value
        user?.let {nonNullUser ->

            name = nonNullUser.userName
            bio = nonNullUser.bio
            pfp = (nonNullUser.userImage ?: "no Image") as String
        }
    }
    image?.let { bitmap ->
        Image(
            bitmap = bitmap,
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape),
            contentDescription = "Profile picture",
            contentScale = ContentScale.Crop
        )
    }
   /* image == null {
       // To be done
    }*/

    Text(text = name ?: "Loading...")
    Text(text = bio ?: "Loading...")

}