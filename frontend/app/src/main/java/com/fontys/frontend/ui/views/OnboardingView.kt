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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

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
                    2 -> OnboardingScreen3()
                    3 -> OnboardingScreen4(navController)
                }
            }

            // Page indicators (more visible)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 80.dp, top = 16.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .size(if (pagerState.currentPage == index) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f)
                            )
                    )
                }
            }
        }
    }
}

// Screen 1: The Problem - Gaming vs Reality (Material Icons)
@Composable
fun OnboardingScreen1() {
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
                // Glow layers
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            CircleShape
                        )
                )
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                            CircleShape
                        )
                )

                // Game controller icon
                Icon(
                    imageVector = Icons.Outlined.SportsEsports,
                    contentDescription = "Gaming",
                    modifier = Modifier.size(80.dp),
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
                // Tree/nature icon
                Icon(
                    imageVector = Icons.Outlined.Park,
                    contentDescription = "Reality",
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f)
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

// Screen 2: The Solution (flag with orbiting elements)
@Composable
fun OnboardingScreen2() {
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
            // Background circle with subtle glow effect
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        CircleShape
                    )
            )

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        CircleShape
                    )
            )

            // Center flag icon
            Icon(
                imageVector = Icons.Outlined.Flag,
                contentDescription = "Flag",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            // Orbiting icons - positioned around the flag
            // Top
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 20.dp)
            ) {
                SmallIconCircle(Icons.Outlined.EmojiEvents)
            }

            // Bottom
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-20).dp)
            ) {
                SmallIconCircle(Icons.Outlined.Whatshot)
            }

            // Left
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = 20.dp)
            ) {
                SmallIconCircle(Icons.Outlined.LocationOn)
            }

            // Right
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = (-20).dp)
            ) {
                SmallIconCircle(Icons.Outlined.Groups)
            }
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

// Screen 3: How It Works (three-step flow)
@Composable
fun OnboardingScreen3() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Three-step icons with arrows
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconCircle(icon = Icons.Outlined.LocationOn)
            ArrowRight()
            IconCircle(icon = Icons.Outlined.CameraAlt)
            ArrowRight()
            IconCircle(icon = Icons.Outlined.EmojiEvents)
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
            StepItem(number = "3", text = "Claim your flag & earn XP")
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

// Screen 4: CTA
@Composable
fun OnboardingScreen4(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Large flag icon
        Icon(
            imageVector = Icons.Outlined.Flag,
            contentDescription = "Flag",
            modifier = Modifier.size(120.dp),
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

        // Start Exploring button
        Button(
            onClick = {
                navController.navigate("registration") {
                    popUpTo("onboarding") { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
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
