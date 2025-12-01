package com.fontys.frontend.ui.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.fontys.frontend.domain.UserRepository
import com.fontys.frontend.ui.viewmodels.ProfileViewModel
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fontys.frontend.data.models.Flag
import com.fontys.frontend.data.models.FlagShowData
import com.fontys.frontend.ui.components.EditableAccountField
import com.fontys.frontend.ui.components.ProfileHeader
import com.fontys.frontend.ui.theme.ProfileColors
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.rememberCoroutineScope
import com.fontys.frontend.data.UserUpdate
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Profile(viewModel: ProfileViewModel) {
    //Local variables for the profile
    var pfp: String? by remember { mutableStateOf("") }
    var name: String? by remember { mutableStateOf("") }
    var bio: String? by remember { mutableStateOf("") }

    // Dynamic data
    val friends by viewModel.friendsNr.collectAsState()
    val friendsCount = friends ?: 0
    val posts by viewModel.flag.collectAsState()
    val postNr by viewModel.flagNrs.collectAsState()
    val postNames by viewModel.flagName.collectAsState()


    //ViewModel variables
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()


    var changed by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()


    LaunchedEffect(Unit) {
        viewModel.getUser(UserRepository.userId.toString())
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfileColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(8.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(ProfileColors.Container)
                    .border(3.dp, ProfileColors.Border, RoundedCornerShape(24.dp))
            ) {
                ProfileHeader()
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = ProfileColors.Primary)
                        }
                    }

                    error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = error ?: "Unknown error", color = ProfileColors.Primary)
                        }
                    }

                    else -> {
                        user?.let {
                            //Image
                            val uD = user
                            pfp = uD?.userImage
                            name = uD?.userName
                            bio = uD?.bio
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp)
                            ) {
                                TopAppBar(
                                    title = { Text("Account") },
                                    //actions = SettingsMenu
                                    // Future feature
                                )
                                pfp?.let {
                                    val image = remember(pfp) {
                                        viewModel.base64ToImageBitmap(pfp)
                                    }
                                    if (image != null) {
                                        Image(
                                            bitmap = image,
                                            contentDescription = "Profile Picture",
                                            modifier = Modifier
                                                .size(120.dp)
                                                .clip(CircleShape)
                                        )
                                    } else {
                                        CircleInitials(name ?: "G1")
                                        // Have to implement camera here
                                    }
                                }
                                //Username
                                EditableAccountField(
                                    label = "name",
                                    value = name ?: "NO NAME",
                                    onValueChange = {
                                        name = it
                                        changed = true
                                    }
                                )
                                //Friends and Flags list
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier,
                                        verticalArrangement = Arrangement.spacedBy(9.dp)
                                    ) {
                                        Text("Friends")
                                        Text(friendsCount.toString())
                                    }
                                    Column(
                                        modifier = Modifier,
                                        verticalArrangement = Arrangement.spacedBy(9.dp)
                                    ) {
                                        Text("Posts")
                                        Text(postNr.toString())
                                    }
                                }

                                EditableAccountField(
                                    label = "bio",
                                    value = bio ?: "NO BIO",
                                    onValueChange = {
                                        bio = it
                                        changed = true
                                    }
                                )
                                postNames?.let { list ->
                                    LazyColumn {
                                        items(items=list) { flag ->
                                            FlagItem(flag, viewModel)
                                        }
                                    }
                                }
                            }
                            if (changed){
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            val userUpdate = UserUpdate(
                                                id = 1,
                                                userName = name,
                                                bio = bio,
                                                userImage = ""
                                            )
                                            viewModel.updateUser(user!!.id, userUpdate)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(2.dp, ProfileColors.Border, RoundedCornerShape(12.dp)),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ProfileColors.Accent,
                                        contentColor = ProfileColors.Primary
                                    )
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Save")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("SAVE CHANGES")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CircleInitials(name: String, size: Dp = 120.dp) {
    val initials = name
        .split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .joinToString("")

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun SettingsMenu(
    modifier: Modifier = Modifier,
    X: () -> Unit = {},
    Y: () -> Unit = {},
    Z: () -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("X") },
                onClick = {
                    expanded = false
                    X()
                }
            )
            DropdownMenuItem(
                text = { Text("Y") },
                onClick = {
                    expanded = false
                    Y()
                }
            )
            DropdownMenuItem(
                text = { Text("Z") },
                onClick = {
                    expanded = false
                    Z()
                }
            )
        }
    }
}

@Composable
fun FlagItem(flag: FlagShowData, viewModel: ProfileViewModel) {
    val image = viewModel.base64ToImageBitmap(flag.photoCode)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Row (  modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = flag.name)      // adjust to your Flag fields
                Text(text = flag.dateTaken)
            }
           if (image != null) {
               Image(
                   bitmap = image,
                   contentDescription = "Image",
                   modifier = Modifier
                       .size(11.dp)
                       .clip(RectangleShape)
               )
           }
        }
    }
}