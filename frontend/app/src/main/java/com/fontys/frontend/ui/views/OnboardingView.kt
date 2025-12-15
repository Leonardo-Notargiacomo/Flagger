package com.fontys.frontend.ui.views

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.Crossfade
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingView(
    navController: NavHostController
) {
    val pagerState = rememberPagerState(pageCount = { 4 })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Skip button (top right)
        TextButton(
            onClick = {
                // Navigate to login and mark onboarding as seen
                navController.navigate("login") {
                    popUpTo("onboarding") { inclusive = true }
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Text(
                text = "Skip",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Main content
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Pager content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> OnboardingScreen1()
                    1 -> OnboardingScreen2()
                    2 -> OnboardingScreen3(isVisible = pagerState.currentPage == 2)
                    3 -> OnboardingScreen4(navController)
                }
            }

            // Page indicators with smooth transitions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 80.dp, top = 16.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { index ->
                    val isSelected = pagerState.currentPage == index
                    val size by animateDpAsState(
                        targetValue = if (isSelected) 10.dp else 8.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "dot_size_$index"
                    )

                    val color by animateColorAsState(
                        targetValue = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
                        animationSpec = tween(300),
                        label = "dot_color_$index"
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .size(size)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }
        }
    }
}

// Screen 1: The Problem - Gaming vs Reality (Material Icons with animations)
@Composable
fun OnboardingScreen1() {
    // Animation for game controller pulse
    val infiniteTransition = rememberInfiniteTransition(label = "controller_pulse")
    val controllerScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "controller_scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    // Animation for tree fade
    val treeAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tree_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Split-screen with icons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp)),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // LEFT SIDE - GAMING (vibrant)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color(0xFF1A0F0D)),
                contentAlignment = Alignment.Center
            ) {
                // Glow layers with animation
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha),
                            CircleShape
                        )
                )
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha + 0.1f),
                            CircleShape
                        )
                )

                // Game controller icon with pulse
                Icon(
                    imageVector = Icons.Outlined.SportsEsports,
                    contentDescription = "Gaming",
                    modifier = Modifier
                        .size(80.dp)
                        .scale(controllerScale),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // RIGHT SIDE - REALITY (muted)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color(0xFFD4C5B0).copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                // Tree/nature icon with subtle fade
                Icon(
                    imageVector = Icons.Outlined.Park,
                    contentDescription = "Reality",
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = treeAlpha)
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Why is exploring in\ngames more exciting\nthan real life?",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "It shouldn't be that way",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

// Screen 2: The Solution (flag with orbiting elements + animations)
@Composable
fun OnboardingScreen2() {
    // Animation for flag gentle wave/rotation
    val infiniteTransition = rememberInfiniteTransition(label = "flag_wave")
    val flagRotation by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flag_rotation"
    )

    // Pulsing glow
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )

    // Orbiting icons rotation
    val orbitRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Flag with orbiting icons
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background circle with pulsing glow effect
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(glowScale)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        CircleShape
                    )
            )

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(glowScale)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        CircleShape
                    )
            )

            // Orbiting icons container with rotation
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .rotate(orbitRotation)
            ) {
                // Top
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = 20.dp)
                        .rotate(-orbitRotation) // Counter-rotate to keep upright
                ) {
                    SmallIconCircle(Icons.Outlined.EmojiEvents)
                }

                // Bottom
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (-20).dp)
                        .rotate(-orbitRotation)
                ) {
                    SmallIconCircle(Icons.Outlined.Whatshot)
                }

                // Left
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = 20.dp)
                        .rotate(-orbitRotation)
                ) {
                    SmallIconCircle(Icons.Outlined.LocationOn)
                }

                // Right
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(x = (-20).dp)
                        .rotate(-orbitRotation)
                ) {
                    SmallIconCircle(Icons.Outlined.Groups)
                }
            }

            // Center flag icon with gentle wave
            Icon(
                imageVector = Icons.Outlined.Flag,
                contentDescription = "Flag",
                modifier = Modifier
                    .size(80.dp)
                    .rotate(flagRotation),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Flagger turns real-world\nexploring into a game",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            lineHeight = 34.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Visit places • Collect flags\nUnlock badges • Compete with friends\nBuild your streak",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

// Screen 3: How It Works (three-step flow with sequential drop-in)
@Composable
fun OnboardingScreen3(isVisible: Boolean = true) {
    // Sequential drop-in animations
    var icon1Visible by remember { mutableStateOf(false) }
    var arrow1Visible by remember { mutableStateOf(false) }
    var icon2Visible by remember { mutableStateOf(false) }
    var arrow2Visible by remember { mutableStateOf(false) }
    var icon3Visible by remember { mutableStateOf(false) }

    // Reset and trigger animation when screen becomes visible
    LaunchedEffect(isVisible) {
        if (isVisible) {
            // Reset first
            icon1Visible = false
            arrow1Visible = false
            icon2Visible = false
            arrow2Visible = false
            icon3Visible = false

            // Then animate in sequence: icon -> arrow -> icon -> arrow -> icon
            delay(200)
            icon1Visible = true
            delay(300)
            arrow1Visible = true
            delay(200)
            icon2Visible = true
            delay(300)
            arrow2Visible = true
            delay(200)
            icon3Visible = true
        }
    }

    val icon1Scale by animateFloatAsState(
        targetValue = if (icon1Visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "icon1_scale"
    )

    val arrow1Alpha by animateFloatAsState(
        targetValue = if (arrow1Visible) 1f else 0f,
        animationSpec = tween(300),
        label = "arrow1_alpha"
    )

    val icon2Scale by animateFloatAsState(
        targetValue = if (icon2Visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "icon2_scale"
    )

    val arrow2Alpha by animateFloatAsState(
        targetValue = if (arrow2Visible) 1f else 0f,
        animationSpec = tween(300),
        label = "arrow2_alpha"
    )

    val icon3Scale by animateFloatAsState(
        targetValue = if (icon3Visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "icon3_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Three-step icons with arrows - animated in sequence
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.scale(icon1Scale)) {
                IconCircle(icon = Icons.Outlined.LocationOn)
            }
            Box(modifier = Modifier.graphicsLayer { alpha = arrow1Alpha }) {
                ArrowRight()
            }
            Box(modifier = Modifier.scale(icon2Scale)) {
                IconCircle(icon = Icons.Outlined.CameraAlt)
            }
            Box(modifier = Modifier.graphicsLayer { alpha = arrow2Alpha }) {
                ArrowRight()
            }
            Box(modifier = Modifier.scale(icon3Scale)) {
                IconCircle(icon = Icons.Outlined.EmojiEvents)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Three steps
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            StepItem(number = "1", text = "Find a place near you")
            Spacer(modifier = Modifier.height(16.dp))
            StepItem(number = "2", text = "Visit it and snap a photo")
            Spacer(modifier = Modifier.height(16.dp))
            StepItem(number = "3", text = "Claim your flag & earn badges")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "It's that simple. No virtual creatures.\nJust real places waiting to be discovered.",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
fun IconCircle(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .background(
                MaterialTheme.colorScheme.primaryContainer,
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun SmallIconCircle(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                MaterialTheme.colorScheme.primaryContainer,
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun ArrowRight() {
    Text(
        text = "→",
        fontSize = 24.sp,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun StepItem(number: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    MaterialTheme.colorScheme.primary,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Medium
        )
    }
}

// Screen 4: CTA (with animations)
@Composable
fun OnboardingScreen4(navController: NavHostController) {
    // Flag subtle rotation
    val infiniteTransition = rememberInfiniteTransition(label = "flag_rotation")
    val flagRotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flag_rotation"
    )

    // Button scale pulse
    val buttonScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "button_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Large flag icon with rotation
        Icon(
            imageVector = Icons.Outlined.Flag,
            contentDescription = "Flag",
            modifier = Modifier
                .size(120.dp)
                .rotate(flagRotation),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Your first adventure\nstarts now",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Start Exploring button with pulse animation
        Button(
            onClick = {
                navController.navigate("registration") {
                    popUpTo("onboarding") { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .scale(buttonScale),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)
        ) {
            Text(
                text = "Start Exploring",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // I have an account button
        TextButton(
            onClick = {
                navController.navigate("login") {
                    popUpTo("onboarding") { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "I have an account",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
