package com.fontys.frontend.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.fontys.frontend.utils.PermissionHandler
import com.fontys.frontend.utils.PermissionHandler.PermissionDialogState

/**
 * Generic permission dialog with GIF support for initial permission requests.
 */
@Composable
fun PermissionDialog(
    title: String,
    message: String,
    buttonText: String,
    gifUrl: String,
    imageLoader: ImageLoader,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = { /* Non-dismissible */ }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                // GIF Image
                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(gifUrl)
                        .build(),
                    imageLoader = imageLoader
                )

                Image(
                    painter = painter,
                    contentDescription = "Permission GIF",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Fit
                )

                // Message
                Text(
                    text = message,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Start,
                    lineHeight = 20.sp
                )

                // Button
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = buttonText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Dialog shown when notification permission is denied but can be requested again.
 */
@Composable
fun NotificationDeniedDialog(
    imageLoader: ImageLoader,
    onTryAgain: () -> Unit,
    onExitApp: () -> Unit
) {
    Dialog(onDismissRequest = { /* Non-dismissible */ }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title with random humorous message
                val titles = listOf(
                    "😭 come on...",
                    "💔 really?",
                    "🥺 we hope that was an accident",
                    "😢 nah fr?",
                    "🤨 you serious rn?",
                    "😤 don't do us like that",
                    "🫠 this is awkward..."
                )
                val randomTitle = titles.random()

                Text(
                    text = randomTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                // GIF Image - sad/disappointed GIF
                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("https://media.tenor.com/-2rbVbnfW24AAAAi/crying-cat-sad-kitty.gif")
                        .build(),
                    imageLoader = imageLoader
                )

                Image(
                    painter = painter,
                    contentDescription = "Sad GIF",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Fit
                )

                // Message
                val messages = listOf(
                    "we really hope that was a mistake 🥺\n\nwithout notifications we can't:\n\n💬 tell u when friends accept ur requests\n🎮 send u challenges\n🔥 remind u to keep ur streak alive\n\nit's gonna be sad without u...",
                    "bestie... we need those notifications 😢\n\nlook, without them:\n\n❌ no friend request alerts\n❌ no daily reminders\n❌ no achievement unlocks\n\nit's giving 'i hate fun' energy ngl",
                    "okay so like... what just happened? 🤔\n\nwe can't function without notifications:\n\n📱 no updates when ur friends vibe with u\n✨ no motivation to explore\n🏆 no achievement notifications\n\npls reconsider 🙏"
                )
                val randomMessage = messages.random()

                Text(
                    text = randomMessage,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Start,
                    lineHeight = 20.sp
                )

                // Two buttons: Try Again (primary) and Exit App (destructive)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Exit App button (destructive/secondary)
                    OutlinedButton(
                        onClick = onExitApp,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(
                            text = "exit app 😔",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Try Again button (primary)
                    Button(
                        onClick = onTryAgain,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "try again 🙏",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog shown when notification permission is permanently denied and user needs to go to Settings.
 */
@Composable
fun NotificationSettingsDialog(
    imageLoader: ImageLoader,
    onOpenSettings: () -> Unit,
    onExitApp: () -> Unit
) {
    Dialog(onDismissRequest = { /* Non-dismissible */ }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                val titles = listOf(
                    "😩 okay we get it...",
                    "🫤 so it's like that huh",
                    "💀 you really said no twice",
                    "😮‍💨 alright bet...",
                    "🤷 if you say so..."
                )
                val randomTitle = titles.random()

                Text(
                    text = randomTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                // GIF Image - more intense sad/crying GIF
                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("https://media1.tenor.com/m/NFsmfmT6DzAAAAAd/funny-funnyfix.gif")
                        .build(),
                    imageLoader = imageLoader
                )

                Image(
                    painter = painter,
                    contentDescription = "Confused GIF",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Fit
                )

                // Message - explain they need to go to Settings
                Text(
                    text = "you denied it twice so now android won't let us ask again 💔\n\nbut fr we NEED notifications to work properly:\n\n⚙️ you gotta go to Settings manually\n🔔 turn on notifications there\n📱 then come back here\n\nit's the only way this works bestie 🙏",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Start,
                    lineHeight = 20.sp
                )

                // Two buttons: Go to Settings (primary) and Exit App (destructive)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Exit App button (destructive/secondary)
                    OutlinedButton(
                        onClick = onExitApp,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(
                            text = "nah i'm out 👋",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Open Settings button (primary)
                    Button(
                        onClick = onOpenSettings,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "open settings ⚙️",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog shown when location permission is denied but can be requested again.
 */
@Composable
fun LocationDeniedDialog(
    imageLoader: ImageLoader,
    onTryAgain: () -> Unit,
    onExitApp: () -> Unit
) {
    Dialog(onDismissRequest = { /* Non-dismissible */ }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title with random humorous message
                val titles = listOf(
                    "📍 wait what?",
                    "🗺️ nah you can't be serious",
                    "😭 but we need this fr",
                    "🤨 location denied? really?",
                    "😢 that hurt ngl",
                    "💔 we thought we had something",
                    "🫠 this is not it chief"
                )
                val randomTitle = titles.random()

                Text(
                    text = randomTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                // GIF Image - disappointed/confused GIF
                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("https://media.tenor.com/u4mpua8vD3IAAAAi/%C3%BCzg%C3%BCnkedikuzeyefe.gif")
                        .build(),
                    imageLoader = imageLoader
                )

                Image(
                    painter = painter,
                    contentDescription = "Disappointed GIF",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Fit
                )

                // Message
                val messages = listOf(
                    "okay so like... we kinda NEED this 🥺\n\nwithout ur location:\n\n❌ can't find cool spots near u\n❌ can't track ur adventure streak\n❌ can't help u explore new places\n\nit's literally the whole point bestie 😭",
                    "fr tho we can't work without this 💔\n\nlook what ur missing:\n\n🗺️ no personalized spot recommendations\n🏆 no location-based achievements\n📍 no way to track where u've been\n\nwe need this to vibe together 🙏",
                    "this ain't it chief... 😢\n\nwithout location access:\n\n🚫 we're basically useless\n🚫 can't help u explore\n🚫 can't track ur progress\n\nwe really hope that was an accident 🤞"
                )
                val randomMessage = messages.random()

                Text(
                    text = randomMessage,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Start,
                    lineHeight = 20.sp
                )

                // Two buttons: Try Again (primary) and Exit App (destructive)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Exit App button (destructive/secondary)
                    OutlinedButton(
                        onClick = onExitApp,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(
                            text = "exit app 😔",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Try Again button (primary)
                    Button(
                        onClick = onTryAgain,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "try again 🙏",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog shown when location permission is permanently denied and user needs to go to Settings.
 */
@Composable
fun LocationSettingsDialog(
    imageLoader: ImageLoader,
    onOpenSettings: () -> Unit,
    onExitApp: () -> Unit
) {
    Dialog(onDismissRequest = { /* Non-dismissible */ }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                val titles = listOf(
                    "😩 you really don't want us huh",
                    "🫤 blocked twice? damn",
                    "💀 android said we can't ask anymore",
                    "😮‍💨 this is getting awkward...",
                    "🤷 location is kinda our thing tho"
                )
                val randomTitle = titles.random()

                Text(
                    text = randomTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                // GIF Image - very sad/desperate GIF
                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("https://media1.tenor.com/m/PsLxxXWJu9AAAAAC/sad-cat-sad.gif")
                        .build(),
                    imageLoader = imageLoader
                )

                Image(
                    painter = painter,
                    contentDescription = "Very Sad GIF",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Fit
                )

                // Message - explain they need to go to Settings
                Text(
                    text = "okay so... you denied it twice 💔\n\nandroid blocked us from asking again but we REALLY need location to work:\n\n⚙️ go to Settings manually\n📍 enable location permission\n📱 come back here\n\nwithout location we're literally useless bestie 😭",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Start,
                    lineHeight = 20.sp
                )

                // Two buttons: Go to Settings (primary) and Exit App (destructive)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Exit App button (destructive/secondary)
                    OutlinedButton(
                        onClick = onExitApp,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(
                            text = "nah i'm out 👋",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Open Settings button (primary)
                    Button(
                        onClick = onOpenSettings,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "open settings ⚙️",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Main composable that renders the appropriate permission dialog based on the permission handler state.
 * This centralizes all permission dialog logic in one place.
 */
@Composable
fun PermissionDialogs(
    permissionHandler: PermissionHandler,
    imageLoader: ImageLoader
) {
    when (permissionHandler.dialogState) {
        is PermissionDialogState.AskingLocation -> {
            PermissionDialog(
                title = "🗺️ yo, where u at?",
                message = "we need to know your location don't ask why 🤷\n\nbut if you do want to know here are the reasons:\n\n📍 we can help you find interesting spots around u\n\n🏠 we can check on you if you are just rotting at home\n\n🔥 and track ur adventure streak\n\nno cap, it's gonna be fire 🚀",
                buttonText = "say less 💯",
                gifUrl = "https://media.tenor.com/bGK0XXfceUoAAAAi/baby-pear.gif",
                imageLoader = imageLoader,
                onConfirm = {
                    permissionHandler.requestLocationPermission()
                }
            )
        }

        is PermissionDialogState.LocationDenied -> {
            LocationDeniedDialog(
                imageLoader = imageLoader,
                onTryAgain = {
                    permissionHandler.requestLocationPermission()
                },
                onExitApp = {
                    permissionHandler.exitApp()
                }
            )
        }

        is PermissionDialogState.LocationPermanentlyDenied -> {
            LocationSettingsDialog(
                imageLoader = imageLoader,
                onOpenSettings = {
                    permissionHandler.openAppSettings()
                },
                onExitApp = {
                    permissionHandler.exitApp()
                }
            )
        }

        is PermissionDialogState.AskingNotification -> {
            PermissionDialog(
                title = "🔔 don't ghost us!",
                message = "turn on the notifications so we can:\n\n✨ slide into ur dms with daily inspo\n👥 tell u when u get friend requests\n🌱 remind you to touch some grass\n\ntrust, u don't wanna miss this 😤",
                buttonText = "bet 🤝",
                gifUrl = "https://media1.tenor.com/m/DxZ2AOxOIHEAAAAd/wacky-man.gif",
                imageLoader = imageLoader,
                onConfirm = {
                    permissionHandler.requestNotificationPermission()
                }
            )
        }

        is PermissionDialogState.NotificationDenied -> {
            NotificationDeniedDialog(
                imageLoader = imageLoader,
                onTryAgain = {
                    permissionHandler.requestNotificationPermission()
                },
                onExitApp = {
                    permissionHandler.exitApp()
                }
            )
        }

        is PermissionDialogState.NotificationPermanentlyDenied -> {
            NotificationSettingsDialog(
                imageLoader = imageLoader,
                onOpenSettings = {
                    permissionHandler.openAppSettings()
                },
                onExitApp = {
                    permissionHandler.exitApp()
                }
            )
        }

        is PermissionDialogState.None -> {
            // No dialog to show
        }
    }
}
