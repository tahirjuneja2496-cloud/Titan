package com.example.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import androidx.compose.ui.graphics.lerp
import java.text.SimpleDateFormat
import java.util.*

// User Screen Enum Navigation
enum class UserTab {
    HOME, PLANS, BOOKINGS, TRAINERS, SUPPORT
}

@Composable
fun GymApp(viewModel: GymViewModel) {
    val context = LocalContext.current
    val showSplash by viewModel.showSplash.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsStateWithLifecycle()
    val adminExists by viewModel.adminExists.collectAsStateWithLifecycle()

    // Interactive state to pre-fill dynamic bookings from plans
    var preselectedPlanForBooking by remember { mutableStateOf("") }
    var currentTab by remember { mutableStateOf(UserTab.HOME) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Crossfade(targetState = showSplash, label = "AppSplashFade") { splash ->
            if (splash) {
                SplashScreen()
            } else {
                when {
                    // 1. Initial Launch: If admin has not been registered yet, enforce setup
                    !adminExists -> {
                        AdminSetupScreen(
                            onAdminRegistered = { email, password ->
                                viewModel.registerAdminAccount(email, password) { success ->
                                    if (success) {
                                        Toast.makeText(context, "Admin Setup Successful!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Fail. Only one admin allowed.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }

                    // 2. Main Login screen if no valid member/admin session is active
                    currentUser == null && !isAdminLoggedIn -> {
                        AuthScreen(
                            viewModel = viewModel,
                            onLoginSuccess = { role ->
                                if (role == "admin") {
                                    Toast.makeText(context, "Welcome Elite Admin!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Success. Let's build your strength!", Toast.LENGTH_LONG).show()
                                }
                            }
                        )
                    }

                    // 3. Admin Control Deck
                    isAdminLoggedIn -> {
                        AdminControlDeck(viewModel = viewModel, onLogout = { viewModel.logout() })
                    }

                    // 4. Client Hub Dashboard
                    else -> {
                        MainClientWorkspace(
                            viewModel = viewModel,
                            currentTab = currentTab,
                            onTabChange = { tab -> currentTab = tab },
                            preselectedPlan = preselectedPlanForBooking,
                            clearPreselectedPlan = { preselectedPlanForBooking = "" },
                            onTriggerPlanJoin = { plan ->
                                preselectedPlanForBooking = plan
                                currentTab = UserTab.BOOKINGS
                            },
                            onLogout = { viewModel.logout() }
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// 1. SPLASH SCREEN (Animate Fitness Logo + Premium Red/Black Theme)
// ----------------------------------------------------------------------------
@Composable
fun SplashScreen() {
    var startAnimate by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = true) {
        startAnimate = true
    }

    val scale by animateFloatAsState(
        targetValue = if (startAnimate) 1.15f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "LogoScale"
    )

    val opacity by animateFloatAsState(
        targetValue = if (startAnimate) 1f else 0f,
        animationSpec = tween(1100, easing = LinearOutSlowInEasing), label = "LogoOpacity"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF070707), Color(0xFF140202))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Stylized Kinetic Logo drawing
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .drawBehind {
                        // Drawing glowing red decorative concentric speed arcs
                        val centerOffset = Offset(size.width / 2, size.height / 2)
                        drawCircle(
                            color = RedPrimary.copy(alpha = 0.15f),
                            radius = size.width * 0.45f * scale,
                            center = centerOffset
                        )
                        drawCircle(
                            color = RedPrimary.copy(alpha = 0.05f),
                            radius = size.width * 0.6f * scale,
                            center = centerOffset
                        )
                    }
                    .padding(18.dp),
                contentAlignment = Alignment.Center
            ) {
                // Elite Double-Winged Dumbbell Vector Graphic via Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    
                    // Main central link rod
                    drawLine(
                        color = RedPrimary,
                        start = Offset(w * 0.25f, h * 0.5f),
                        end = Offset(w * 0.75f, h * 0.5f),
                        strokeWidth = 14f * scale,
                        cap = StrokeCap.Round
                    )
                    
                    // Left weights block
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(w * 0.18f, h * 0.25f),
                        size = androidx.compose.ui.geometry.Size(w * 0.12f, h * 0.5f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                    )
                    drawRoundRect(
                        color = RedAccent,
                        topLeft = Offset(w * 0.08f, h * 0.32f),
                        size = androidx.compose.ui.geometry.Size(w * 0.08f, h * 0.36f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                    )

                    // Right weights block
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(w * 0.70f, h * 0.25f),
                        size = androidx.compose.ui.geometry.Size(w * 0.12f, h * 0.5f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                    )
                    drawRoundRect(
                        color = RedAccent,
                        topLeft = Offset(w * 0.84f, h * 0.32f),
                        size = androidx.compose.ui.geometry.Size(w * 0.08f, h * 0.36f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                    )

                    // Geometric "T" icon for Titan
                    val path = Path().apply {
                        moveTo(w * 0.42f, h * 0.35f)
                        lineTo(w * 0.58f, h * 0.35f)
                        lineTo(w * 0.58f, h * 0.44f)
                        lineTo(w * 0.53f, h * 0.44f)
                        lineTo(w * 0.53f, h * 0.68f)
                        lineTo(w * 0.47f, h * 0.68f)
                        lineTo(w * 0.47f, h * 0.44f)
                        lineTo(w * 0.42f, h * 0.44f)
                        close()
                    }
                    drawPath(
                        path = path,
                        color = RedPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "TITAN FITNESS",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp,
                    color = Color.White
                ),
                modifier = Modifier.testTag("splash_title")
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "FORGE YOUR ULTIMATE POTENTIAL",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontStyle = FontStyle.Normal,
                    letterSpacing = 2.sp,
                    color = RedAccent
                )
            )

            Spacer(modifier = Modifier.height(64.dp))

            CircularProgressIndicator(
                color = RedPrimary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ----------------------------------------------------------------------------
// 2. ADMIN SETUP SCREEN (Create Only One Admin)
// ----------------------------------------------------------------------------
@Composable
fun AdminSetupScreen(onAdminRegistered: (String, String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(RedPrimary.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.SupervisorAccount,
                    contentDescription = "System Admin setup",
                    tint = RedPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "ADMINISTRATOR SETUP",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "This is a one-time configuration. Establish the owner account credentials. Double configuration is prevented at database root.",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (errorMessage.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Admin Email Address") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = RedAccent) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_setup_email"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RedPrimary,
                    cursorColor = RedPrimary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Secure Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = RedAccent) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password mask"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_setup_password"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RedPrimary,
                    cursorColor = RedPrimary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Verify Password") },
                leadingIcon = { Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = RedAccent) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RedPrimary,
                    cursorColor = RedPrimary
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    errorMessage = ""
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Credentials cannot be blank."
                    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        errorMessage = "Please enter a valid administrative email."
                    } else if (password.length < 6) {
                        errorMessage = "For safety, admin password must contain 6+ characters."
                    } else if (password != confirmPassword) {
                        errorMessage = "Verification password does not match original."
                    } else {
                        onAdminRegistered(email, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("admin_setup_submit"),
                colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "DEPLOY SYSTEM ADMIN",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// ----------------------------------------------------------------------------
// 3. SECURE AUTHENTICATION SCREEN (Google Sign in simulated, normal Login)
// ----------------------------------------------------------------------------
@Composable
fun AuthScreen(
    viewModel: GymViewModel,
    onLoginSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    var isLoginTab by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }

    // Password reset simulation modal
    var showForgotPasswordModal by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gym Shield Icon
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, RedPrimary.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.FitnessCenter,
                    contentDescription = null,
                    tint = RedPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "TITAN FITNESS LAB",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Premium segmented Tab control with custom animated offsets
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CharcoalDark),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    Button(
                        onClick = {
                            isLoginTab = true
                            alertMessage = ""
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLoginTab) RedPrimary else Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tab_login"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            "MEMBER LOGIN",
                            fontWeight = FontWeight.Bold,
                            color = if (isLoginTab) Color.White else MutedText,
                            fontSize = 12.sp
                        )
                    }
                    Button(
                        onClick = {
                            isLoginTab = false
                            alertMessage = ""
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isLoginTab) RedPrimary else Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tab_register"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            "REGISTER JOIN",
                            fontWeight = FontWeight.Bold,
                            color = if (!isLoginTab) Color.White else MutedText,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (alertMessage.isNotEmpty()) {
                Surface(
                    color = if (alertMessage.contains("Success") || alertMessage.contains("success"))
                        Color(0xFF1B5E20) else MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp)
                ) {
                    Text(
                        text = alertMessage,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (!isLoginTab) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Member Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = RedAccent) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_name"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RedPrimary,
                        cursorColor = RedPrimary
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Member Email Address") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = RedAccent) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_email"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RedPrimary,
                    cursorColor = RedPrimary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Member Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = RedAccent) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Password toggle mask"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_password"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RedPrimary,
                    cursorColor = RedPrimary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoginTab) {
                AlignTextRight {
                    Text(
                        text = "Forgot password?",
                        color = RedAccent,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .clickable {
                                resetEmail = email
                                showForgotPasswordModal = true
                            }
                            .padding(4.dp)
                            .testTag("btn_forgot_password")
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    alertMessage = ""
                    if (isLoginTab) {
                        viewModel.login(email, password) { result ->
                            if (result == "admin" || result == "user") {
                                onLoginSuccess(result)
                            } else {
                                alertMessage = result
                            }
                        }
                    } else {
                        viewModel.registerUser(email, fullName, password) { result ->
                            if (result == "success") {
                                isLoginTab = true
                                email = ""
                                password = ""
                                fullName = ""
                                alertMessage = "Success! Member account created. Please sign in."
                            } else {
                                alertMessage = result
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_auth_submit")
            ) {
                Text(
                    text = if (isLoginTab) "AUTHENTICATE PROFILE" else "CREATE ACCOUNT PROFILE",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Multi-Auth options divider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.1f))
                Text(
                    "OR SECURE FEDERATION",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedText
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.1f))
            }

            // Google simulated Authentication button
            OutlinedButton(
                onClick = {
                    // Simulate Google federation sign in workflow
                    viewModel.registerUser(
                        email = "google_titan@gmail.com",
                        fullName = "Google Titan",
                        password = "GoogleFakePassword"
                    ) { result ->
                        if (result == "success" || result == "Email already registered.") {
                            // Perfect. Now login
                            viewModel.login("google_titan@gmail.com", "GoogleFakePassword") { loginResult ->
                                onLoginSuccess("user")
                                Toast.makeText(context, "Google Authorization Successful!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            alertMessage = result
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_google_signin"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Custom Google-colored stylized graphic
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .drawBehind {
                                drawCircle(color = Color(0xFF4285F4), radius = size.width / 2)
                            }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Sign in via Google OAuth 2.0",
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }

    // Forgot Password simulation modal
    if (showForgotPasswordModal) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordModal = false },
            title = {
                Text(
                    "MEMBER PASSWORD RECOVERY",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            },
            text = {
                Column {
                    Text(
                        "Enter your email to search the database. Our recovery protocol will force-assign a secure temporary string for client recovery.",
                        fontSize = 14.sp,
                        color = MutedText
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Registered Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RedPrimary)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.forgotPassword(resetEmail) { report ->
                            alertMessage = report
                        }
                        showForgotPasswordModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedPrimary)
                ) {
                    Text("TRIGGER FORCE RECOVERY", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordModal = false }) {
                    Text("ABORT", color = MutedText)
                }
            },
            containerColor = CharcoalDark
        )
    }
}

// Helper to align contents to end
@Composable
fun AlignTextRight(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
        content()
    }
}

// ----------------------------------------------------------------------------
// 4. MAIN CLIENT APP WORKSPACE (Edge-to-edge content & Navigation bar)
// ----------------------------------------------------------------------------
@Composable
fun MainClientWorkspace(
    viewModel: GymViewModel,
    currentTab: UserTab,
    onTabChange: (UserTab) -> Unit,
    preselectedPlan: String,
    clearPreselectedPlan: () -> Unit,
    onTriggerPlanJoin: (String) -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = CharcoalDark,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("app_navigation")
            ) {
                val tabs = listOf(
                    Triple(UserTab.HOME, "Home", Icons.Default.Home),
                    Triple(UserTab.PLANS, "Plans", Icons.Default.CardMembership),
                    Triple(UserTab.BOOKINGS, "Bookings", Icons.Default.Event),
                    Triple(UserTab.TRAINERS, "Coaches", Icons.Default.Group),
                    Triple(UserTab.SUPPORT, "Support", Icons.Default.SupportAgent)
                )

                tabs.forEach { (tab, label, icon) ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { onTabChange(tab) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) Color.White else MutedText
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MutedText,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = RedPrimary
                        )
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentTab, label = "UserFlowFade") { tab ->
                when (tab) {
                    UserTab.HOME -> HomeTab(viewModel = viewModel, onTriggerPlanJoin = onTriggerPlanJoin, onLogout = onLogout)
                    UserTab.PLANS -> PlansTab(viewModel = viewModel, onTriggerPlanJoin = onTriggerPlanJoin)
                    UserTab.BOOKINGS -> BookingsTab(viewModel = viewModel, preselectedPlan = preselectedPlan, clearPreselectedPlan = clearPreselectedPlan)
                    UserTab.TRAINERS -> TrainersTab(viewModel = viewModel)
                    UserTab.SUPPORT -> SupportTab(viewModel = viewModel)
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// MEMBER HOME TAB (Hero, Quotes, Tip sections, Transformation slider, Custom design)
// ----------------------------------------------------------------------------
@Composable
fun HomeTab(
    viewModel: GymViewModel,
    onTriggerPlanJoin: (String) -> Unit,
    onLogout: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val motivationalHeading by viewModel.motivationalHeader.collectAsStateWithLifecycle()
    val rawTips by viewModel.dietTips.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Welcome and Logout header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "WELCOME BACK,",
                        style = MaterialTheme.typography.labelLarge,
                        color = RedAccent
                    )
                    Text(
                        text = currentUser?.fullName?.uppercase(Locale.getDefault()) ?: "TITAN BROTHER",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                        .testTag("btn_logout")
                ) {
                    Icon(Icons.Default.Logout, contentDescription = "Log out account", tint = Color.White)
                }
            }
        }

        // Hero dynamic banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CharcoalDark)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            // Premium background grid lines and red ambient shader
                            drawRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(RedPrimary.copy(alpha = 0.25f), Color.Transparent),
                                    center = Offset(size.width, 0f),
                                    radius = size.width * 1.1f
                                )
                            )
                            val gridCount = 8
                            for (i in 0..gridCount) {
                                val x = size.width * (i.toFloat() / gridCount)
                                drawLine(
                                    color = Color.White.copy(alpha = 0.03f),
                                    start = Offset(x, 0f),
                                    end = Offset(x, size.height),
                                    strokeWidth = 2f
                                )
                            }
                        }
                        .padding(24.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column(verticalArrangement = Arrangement.Center) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = RedPrimary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                "TITAN ATHLETICS LAB",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "FORGING ELITE\nPHYSIQUES",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            lineHeight = 32.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { onTriggerPlanJoin("Premium Plan") },
                                colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                                modifier = Modifier.height(34.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("JOIN NOW", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            }
                            OutlinedButton(
                                onClick = { onTriggerPlanJoin("Basic Plan") },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                                modifier = Modifier.height(34.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("FREE TRIAL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Motivational Quote Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                border = BorderStroke(1.dp, RedPrimary.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatQuote,
                        contentDescription = "Motivational quotes indicator",
                        tint = RedPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = motivationalHeading.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontStyle = FontStyle.Normal,
                            letterSpacing = 0.5.sp,
                            lineHeight = 20.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }

        // Workout and Diet Recommendations Carousel
        item {
            Text(
                "ATHLETIC DIET & TIPS RECOVERY",
                style = MaterialTheme.typography.labelLarge,
                color = RedAccent,
                fontWeight = FontWeight.Bold
            )
        }

        items(rawTips) { tip ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CharcoalDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(RedPrimary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = RedPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = tip,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // Transformations slider section
        item {
            TransformationShowcase()
        }
    }
}

// Full custom interactive vector silhouette physique transformer graphics panel
@Composable
fun TransformationShowcase() {
    var progressSliderValue by remember { mutableStateOf(0.5f) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = CharcoalDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "TITAN LABS TRANSFORMATION",
                style = MaterialTheme.typography.labelLarge,
                color = RedAccent,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Simulate 12 weeks of calorie cycling compound gains. Drag the metric handle:",
                fontSize = 13.sp,
                color = MutedText
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Human Silhouette Canvas Morph mapping
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color.Black, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val centerFactor = 0.5f
                    val scaleFactor = progressSliderValue // 0.0f (before) to 1.0f (after)

                    // Draw Background grid glowing dots for gym tech radar feel
                    for (xGrid in 1..9) {
                        for (yGrid in 1..4) {
                            drawCircle(
                                color = Color.White.copy(alpha = 0.1f),
                                radius = 2f,
                                center = Offset(w * (xGrid / 10f), h * (yGrid / 5f))
                            )
                        }
                    }

                    // Morph shapes - Draw stylized aesthetic chest shoulders torso waist
                    // Morph parameters calculated dynamically on slider progress
                    val chestWidth = 40f + (40f * scaleFactor)
                    val shoulderWidth = 80f + (70f * scaleFactor)
                    val armsWidth = 14f + (14f * scaleFactor)
                    val waistWidth = 38f - (4f * scaleFactor) // leaner wait on progress
                    val quadWidth = 24f + (12f * scaleFactor)

                    val midX = w * centerFactor

                    // Drawing Left outline
                    val physiquePath = Path().apply {
                        // Head
                        moveTo(midX, h * 0.1f)
                        cubicTo(midX - 16f, h * 0.1f, midX - 16f, h * 0.22f, midX, h * 0.22f)
                        cubicTo(midX + 16f, h * 0.22f, midX + 16f, h * 0.1f, midX, h * 0.1f)
                        
                        // Neck to Left Shoulder
                        moveTo(midX, h * 0.22f)
                        lineTo(midX - 14f, h * 0.24f)
                        lineTo(midX - shoulderWidth, h * 0.28f)
                        
                        // Left bicep and outer arm
                        cubicTo(
                            midX - shoulderWidth - armsWidth, h * 0.38f,
                            midX - shoulderWidth - armsWidth, h * 0.54f,
                            midX - shoulderWidth + 4f, h * 0.62f
                        )
                        // Left forearm back up inside pelvic
                        lineTo(midX - chestWidth - 4f, h * 0.44f)
                        lineTo(midX - waistWidth, h * 0.56f)
                        
                        // Left quad leg
                        lineTo(midX - quadWidth, h * 0.74f)
                        lineTo(midX - (quadWidth - 8f), h * 0.90f)
                        
                        // Bottom feet crotch mid point
                        lineTo(midX, h * 0.90f)
                        
                        // Right quad leg symmetry
                        lineTo(midX + (quadWidth - 8f), h * 0.90f)
                        lineTo(midX + quadWidth, h * 0.74f)
                        lineTo(midX + waistWidth, h * 0.56f)
                        
                        // Right side back up
                        lineTo(midX + chestWidth + 4f, h * 0.44f)
                        lineTo(midX + shoulderWidth - 4f, h * 0.62f)
                        cubicTo(
                            midX + shoulderWidth + armsWidth, h * 0.54f,
                            midX + shoulderWidth + armsWidth, h * 0.38f,
                            midX + shoulderWidth, h * 0.28f
                        )
                        lineTo(midX + 14f, h * 0.24f)
                        close()
                    }

                    // Fill physique with gradient based on progress
                    val fillGradient = Brush.verticalGradient(
                        colors = listOf(
                            lerp(Color(0xFF5A5A5A), RedAccent, scaleFactor),
                            lerp(Color(0xFF2E2E2E), GoldAccent, scaleFactor)
                        )
                    )
                    drawPath(path = physiquePath, brush = fillGradient)
                    
                    // Outlined stroke
                    drawPath(
                        path = physiquePath,
                        color = Color.White.copy(alpha = 0.5f),
                        style = Stroke(width = 3f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Percentage gains and text parameters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BEFORE (WEEK 0)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MutedText
                )
                Text(
                    text = if (progressSliderValue > 0.8f) "TITAN LEVEL UNLOCKED!" else "MORPHING...",
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    color = if (progressSliderValue > 0.8f) GoldAccent else RedAccent
                )
                Text(
                    text = "AFTER (WEEK 12)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = GoldAccent
                )
            }

            Slider(
                value = progressSliderValue,
                onValueChange = { progressSliderValue = it },
                colors = SliderDefaults.colors(
                    thumbColor = RedPrimary,
                    activeTrackColor = RedAccent,
                    inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                ),
                modifier = Modifier.testTag("transformation_slider")
            )
            
            // Dynamic text logs
            val bodyFatReduction = (24f - (16f * progressSliderValue)).toInt()
            val muscleMassIncrease = (0f + (9.5f * progressSliderValue)).format(1)
            Text(
                text = "Target Metrics: Bodyfat is ${bodyFatReduction}% • Muscular Hypertrophy gained: +${muscleMassIncrease}kg",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

// Basic float extension
fun Float.format(decimals: Int): String {
    return String.format(Locale.getDefault(), "%.${decimals}f", this)
}

// ----------------------------------------------------------------------------
// EMBEDDED PLAN TAB (Basic, Premium, Personal, Price, Join CTAs)
// ----------------------------------------------------------------------------
@Composable
fun PlansTab(viewModel: GymViewModel, onTriggerPlanJoin: (String) -> Unit) {
    val plans by viewModel.membershipPlans.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "TITAN MEMBERSHIP PLATFORM",
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Choose an architecture designed for your operational training intensity. Direct cancellation supported.",
                fontSize = 14.sp,
                color = MutedText
            )
        }

        if (plans.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Plans loaded. Press reload or create from Admin.", color = MutedText)
                }
            }
        }

        items(plans) { plan ->
            val isGoldAccent = plan.name.contains("Premium") || plan.name.contains("Personal")
            val cardStrokeColor = if (isGoldAccent) GoldAccent.copy(alpha = 0.5f) else RedPrimary.copy(alpha = 0.2f)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CharcoalDark),
                border = BorderStroke(1.5.dp, cardStrokeColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Plan Heading
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = plan.name.uppercase(Locale.getDefault()),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        if (isGoldAccent) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = GoldAccent,
                                modifier = Modifier.padding(start = 12.dp)
                            ) {
                                Text(
                                    "ELITE SIGNATURE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Price display
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$${plan.price}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            color = if (isGoldAccent) GoldAccent else RedPrimary
                        )
                        Text(
                            text = " / ${plan.billingCycle}",
                            fontSize = 14.sp,
                            color = MutedText,
                            modifier = Modifier.padding(start = 8.dp, bottom = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Features list
                    val featuresList = plan.features.split(",").filter { it.isNotBlank() }
                    featuresList.forEach { feat ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Active feature checkmark",
                                tint = if (isGoldAccent) GoldAccent else RedAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = feat.trim(),
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // CTA Book button
                    Button(
                        onClick = { onTriggerPlanJoin(plan.name) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("join_plan_${plan.name.replace(" ", "_")}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isGoldAccent) GoldAccent else RedPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "CONQUER WITH THIS PLAN",
                            fontWeight = FontWeight.Bold,
                            color = if (isGoldAccent) Color.Black else Color.White
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// INTERACTIVE BOOKING SYSTEM TAB (Select Trainer, Slot, Date and View)
// ----------------------------------------------------------------------------
@Composable
fun BookingsTab(
    viewModel: GymViewModel,
    preselectedPlan: String,
    clearPreselectedPlan: () -> Unit
) {
    val context = LocalContext.current
    val trainers by viewModel.trainers.collectAsStateWithLifecycle()
    val plans by viewModel.membershipPlans.collectAsStateWithLifecycle()
    val userBookings by viewModel.userBookings.collectAsStateWithLifecycle()

    var selectedPlanName by remember { mutableStateOf(preselectedPlan) }
    var selectedTrainerName by remember { mutableStateOf("") }
    var selectedTimeSlot by remember { mutableStateOf("") }

    // Slots
    val slots = listOf(
        "Morning Peak (6:00 AM - 8:00 AM)",
        "Morning Steady (8:00 AM - 10:00 AM)",
        "Noon Maintenance (12:00 PM - 2:00 PM)",
        "Evening Lift (5:00 PM - 7:00 PM)",
        "Midnight Titan (8:00 PM - 10:00 PM)"
    )

    // Manual custom Date picking variables for total stability
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    var selectedBookingDate by remember { mutableStateOf(simpleDateFormat.format(Date())) }

    // Listen to changes to preselectedPlan
    LaunchedEffect(preselectedPlan) {
        if (preselectedPlan.isNotEmpty()) {
            selectedPlanName = preselectedPlan
            clearPreselectedPlan()
        }
    }

    // Set fallback initial states
    LaunchedEffect(trainers, plans) {
        if (selectedTrainerName.isEmpty() && trainers.isNotEmpty()) {
            selectedTrainerName = trainers.first().name
        }
        if (selectedPlanName.isEmpty() && plans.isNotEmpty()) {
            selectedPlanName = plans.first().name
        }
        if (selectedTimeSlot.isEmpty()) {
            selectedTimeSlot = slots.first()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Text(
                "TITAN SCHEDULING RADAR",
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Reserve your access window and professional personal coach context.",
                fontSize = 14.sp,
                color = MutedText
            )
        }

        // Dropdown Pickers or Radio Card selector for Plans
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CharcoalDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "1. ASSIGN TARGET PROGRAM PLAN",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.labelLarge,
                        color = RedAccent
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    plans.forEach { p ->
                        val isChecked = selectedPlanName == p.name
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPlanName = p.name }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = isChecked,
                                onClick = { selectedPlanName = p.name },
                                colors = RadioButtonDefaults.colors(selectedColor = RedPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(p.name, color = Color.White)
                        }
                    }
                }
            }
        }

        // Trainer selector
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CharcoalDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "2. CHOOSE FITNESS COACH METHODIST",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.labelLarge,
                        color = RedAccent
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (trainers.isEmpty()) {
                        Text("No trainers roster available in database", color = MutedText)
                    } else {
                        trainers.forEach { trainer ->
                            val isChecked = selectedTrainerName == trainer.name
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedTrainerName = trainer.name }
                                    .padding(vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = isChecked,
                                    onClick = { selectedTrainerName = trainer.name },
                                    colors = RadioButtonDefaults.colors(selectedColor = RedPrimary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(trainer.name, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text(trainer.specialization, color = MutedText, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Slot and Date selection
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CharcoalDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "3. ASSIGN DATE & TRAINING WINDOW",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.labelLarge,
                        color = RedAccent
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Static simple Date selector manually incrementable to bypass complex native date views
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("TARGET TRAINING DATE:", color = MutedText, fontSize = 13.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                try {
                                    val parsed = simpleDateFormat.parse(selectedBookingDate) ?: Date()
                                    val cal = Calendar.getInstance().apply { time = parsed }
                                    cal.add(Calendar.DAY_OF_YEAR, -1)
                                    selectedBookingDate = simpleDateFormat.format(cal.time)
                                } catch (e: Exception) {
                                }
                            }) {
                                Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Prev day")
                            }
                            Text(
                                selectedBookingDate,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(onClick = {
                                try {
                                    val parsed = simpleDateFormat.parse(selectedBookingDate) ?: Date()
                                    val cal = Calendar.getInstance().apply { time = parsed }
                                    cal.add(Calendar.DAY_OF_YEAR, 1)
                                    selectedBookingDate = simpleDateFormat.format(cal.time)
                                } catch (e: Exception) {
                                }
                            }) {
                                Icon(Icons.Default.ArrowForwardIos, contentDescription = "Next day")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("SLOT SCHEDULES:", color = MutedText, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    slots.forEach { slot ->
                        val isChecked = selectedTimeSlot == slot
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTimeSlot = slot }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = isChecked,
                                onClick = { selectedTimeSlot = slot },
                                colors = RadioButtonDefaults.colors(selectedColor = RedPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(slot, color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Booking Action CTA
        item {
            Button(
                onClick = {
                    if (selectedPlanName.isEmpty() || selectedTrainerName.isEmpty() || selectedTimeSlot.isEmpty()) {
                        Toast.makeText(context, "Resolve all fields to training specs first.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    viewModel.createBooking(
                        planName = selectedPlanName,
                        trainerName = selectedTrainerName,
                        timeSlot = selectedTimeSlot,
                        dateString = selectedBookingDate
                    ) { success ->
                        if (success) {
                            Toast.makeText(context, "Session reservation booked successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Session booking failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_confirm_booking"),
                colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("RESERVE SESSION SESSION LIFT", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        // Live list of User's Bookings
        item {
            Text(
                "YOUR REGISTERED RESERVATIONS",
                style = MaterialTheme.typography.labelLarge,
                color = RedAccent,
                fontWeight = FontWeight.Bold
            )
        }

        if (userBookings.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CharcoalDark)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "No training sessions reserved. Clear slate, let's start!",
                            textAlign = TextAlign.Center,
                            color = MutedText
                        )
                    }
                }
            }
        } else {
            items(userBookings) { bk ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CharcoalDark),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    bk.planName.uppercase(Locale.getDefault()),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Text(
                                    "Coach: ${bk.trainerName}",
                                    color = RedAccent,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(onClick = { viewModel.deleteBooking(bk) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Kill booking", tint = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MutedText, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(bk.bookingDate, color = Color.White, fontSize = 12.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = MutedText, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(bk.timeSlot, color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// TRAINERS TAB (Experience, Bio, Contact CTA actions)
// ----------------------------------------------------------------------------
@Composable
fun TrainersTab(viewModel: GymViewModel) {
    val context = LocalContext.current
    val coaches by viewModel.trainers.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "TITAN COACHING ROSTER",
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Our trainers contain veteran credentials focused on hardcore sports nutrition, bodybuilding mechanics and cardiovascular efficiency.",
                fontSize = 14.sp,
                color = MutedText
            )
        }

        if (coaches.isEmpty()) {
            item {
                Text("Roster empty setup in DB", color = MutedText)
            }
        }

        items(coaches) { coach ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CharcoalDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // High-fidelity Graphic replacement for photos with trainer initials
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(RedPrimary.copy(alpha = 0.15f), CircleShape)
                                .border(1.5.dp, RedAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val initials = coach.name.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").uppercase()
                            Text(
                                initials,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(18.dp))

                        Column {
                            Text(
                                coach.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = RedPrimary.copy(alpha = 0.1f),
                                border = BorderStroke(0.5.dp, RedAccent.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    coach.specialization.uppercase(Locale.getDefault()),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = RedAccent,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                "Experience: ${coach.experience}",
                                fontSize = 12.sp,
                                color = MutedText,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(coach.bio, color = Color.White, fontSize = 13.sp, lineHeight = 20.sp)
                    Spacer(modifier = Modifier.height(18.dp))

                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Contact action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                Toast.makeText(context, "Forwarding to WhatsApp Client: msg coach ${coach.name}", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("WHATSAPP", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = {
                                Toast.makeText(context, "Synthesizing Direct Dial Dialers: ${coach.contactPhone}", Toast.LENGTH_SHORT).show()
                            },
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("DIAL DIRECT", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// SUPPORT & NOTIFICATION TAB (Contact, WhatsApp link, Gym Location, Notices)
// ----------------------------------------------------------------------------
@Composable
fun SupportTab(viewModel: GymViewModel) {
    val context = LocalContext.current
    val notificationsList by viewModel.notifications.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Text(
                "TITAN DISPATCH CENTRE",
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Direct pipeline to support agents, global physical location maps, and broadcast announcements.",
                fontSize = 14.sp,
                color = MutedText
            )
        }

        // Live Alerts and Announcements
        item {
            Text(
                "TITAN ALERTS BROADCASTS",
                style = MaterialTheme.typography.labelLarge,
                color = RedAccent,
                fontWeight = FontWeight.Bold
            )
        }

        if (notificationsList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CharcoalDark)
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("No notifications active.", color = MutedText)
                    }
                }
            }
        } else {
            items(notificationsList) { note ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CharcoalDark),
                    border = BorderStroke(1.dp, RedPrimary.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = note.title.uppercase(Locale.getDefault()),
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            val displayTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(note.timestamp))
                            Text(displayTime, fontSize = 11.sp, color = RedAccent)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(note.content, fontSize = 13.sp, color = MutedText, lineHeight = 18.sp)
                    }
                }
            }
        }

        // General Contacts Cards
        item {
            Text(
                "DIAL DIRECT HELP SYSTEM",
                style = MaterialTheme.typography.labelLarge,
                color = RedAccent,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CharcoalDark)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Contact Item
                    Text("CUSTOMER SUPPORT:", color = RedAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("+1 (800) TITAN-FIT", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("support@titanfitness.com", fontSize = 14.sp, color = MutedText)

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("WHATSAPP EMERGENCY SYSTEM:", color = RedAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Button(
                        onClick = {
                            Toast.makeText(context, "Launching secure chat with Titan WhatsApp Agent...", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .height(42.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("LAUNCH CHAT ROOM", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("TITAN PHYSICAL HQ SECTOR:", color = RedAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("99 Powerhouse District, Sector 4, Prime City", fontWeight = FontWeight.Medium, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            Toast.makeText(context, "Rendering GPS coordinates in mapping: Latitude 40.71, Longitude -74.00", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
                        modifier = Modifier.height(40.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("LAUNCH MAP DIRECTIONS", color = Color.White)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// 5. ADMIN CONTROL PANEL DECK SCREEN (Add/Edit/Delete, announcements, logs)
// ----------------------------------------------------------------------------
@Composable
fun AdminControlDeck(viewModel: GymViewModel, onLogout: () -> Unit) {
    var adminSectionTab by remember { mutableStateOf(0) } // 0: plans, 1: trainers, 2: bookings, 3: globals

    val context = LocalContext.current
    val bookings by viewModel.allBookings.collectAsStateWithLifecycle()
    val plans by viewModel.membershipPlans.collectAsStateWithLifecycle()
    val coordinators by viewModel.trainers.collectAsStateWithLifecycle()
    val users by viewModel.allUsers.collectAsStateWithLifecycle()
    val motivationalHeading by viewModel.motivationalHeader.collectAsStateWithLifecycle()
    val tips by viewModel.dietTips.collectAsStateWithLifecycle()

    // Form variable holders
    var planFormId by remember { mutableStateOf(0) }
    var planFormName by remember { mutableStateOf("") }
    var planFormPrice by remember { mutableStateOf("") }
    var planFormFeatures by remember { mutableStateOf("") }

    var trainerFormId by remember { mutableStateOf(0) }
    var trainerFormName by remember { mutableStateOf("") }
    var trainerFormSpec by remember { mutableStateOf("") }
    var trainerFormExp by remember { mutableStateOf("") }
    var trainerFormPhone by remember { mutableStateOf("") }
    var trainerFormBio by remember { mutableStateOf("") }

    var globalAnnounceTitle by remember { mutableStateOf("") }
    var globalAnnounceBody by remember { mutableStateOf("") }

    var appOverrideHeading by remember { mutableStateOf(motivationalHeading) }
    var appOverrideTip1 by remember { mutableStateOf("") }
    var appOverrideTip2 by remember { mutableStateOf("") }
    var appOverrideTip3 by remember { mutableStateOf("") }

    LaunchedEffect(tips, motivationalHeading) {
        if (tips.size >= 3) {
            appOverrideTip1 = tips[0]
            appOverrideTip2 = tips[1]
            appOverrideTip3 = tips[2]
        }
        appOverrideHeading = motivationalHeading
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(CharcoalDark)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "TITAN RADAR ADMIN DECK",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = RedPrimary
                    )

                    Button(
                        onClick = onLogout,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("ADMIN DISCONNECT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                ScrollableTabRow(
                    selectedTabIndex = adminSectionTab,
                    containerColor = CharcoalDark,
                    contentColor = RedPrimary,
                    edgePadding = 0.dp
                ) {
                    Tab(
                        selected = adminSectionTab == 0,
                        onClick = { adminSectionTab = 0 },
                        text = { Text("MEMBERSHIPS", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    )
                    Tab(
                        selected = adminSectionTab == 1,
                        onClick = { adminSectionTab = 1 },
                        text = { Text("COACHES", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    )
                    Tab(
                        selected = adminSectionTab == 2,
                        onClick = { adminSectionTab = 2 },
                        text = { Text("BOOKINGS LOGS (${bookings.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    )
                    Tab(
                        selected = adminSectionTab == 3,
                        onClick = { adminSectionTab = 3 },
                        text = { Text("GLOBAL ANNOUNCING Overrides", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            when (adminSectionTab) {
                // MEMBERSHIPS MANAGEMENT
                0 -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CharcoalDark),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    if (planFormId == 0) "CONFIGURE NEW PLAN PROFILE" else "MODIFY ACTIVE PLAN Specs",
                                    color = RedAccent,
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                
                                OutlinedTextField(
                                    value = planFormName,
                                    onValueChange = { planFormName = it },
                                    label = { Text("Plan Label (e.g. Basic Platinum)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("admin_plan_name"),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RedPrimary)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                OutlinedTextField(
                                    value = planFormPrice,
                                    onValueChange = { planFormPrice = it },
                                    label = { Text("Price Target (US Dollar)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("admin_plan_price"),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RedPrimary)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                OutlinedTextField(
                                    value = planFormFeatures,
                                    onValueChange = { planFormFeatures = it },
                                    label = { Text("Features (Separate with commas)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RedPrimary)
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = {
                                            val prVal = planFormPrice.toDoubleOrNull()
                                            if (planFormName.isBlank() || prVal == null) {
                                                Toast.makeText(context, "Resolve errors in fields first.", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            viewModel.adminSavePlan(
                                                id = planFormId,
                                                name = planFormName,
                                                price = prVal,
                                                features = planFormFeatures
                                            )
                                            planFormId = 0
                                            planFormName = ""
                                            planFormPrice = ""
                                            planFormFeatures = ""
                                            Toast.makeText(context, "Plan database modified successfully", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).testTag("admin_save_plan")
                                    ) {
                                        Text("DEPLOY TO APP DB", color = Color.White)
                                    }

                                    if (planFormId != 0) {
                                        OutlinedButton(
                                            onClick = {
                                                planFormId = 0
                                                planFormName = ""
                                                planFormPrice = ""
                                                planFormFeatures = ""
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("ABORT", color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text("LIVE DATABASED STRUCTURES", fontWeight = FontWeight.Bold, color = RedAccent)
                    }

                    items(plans) { pl ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CharcoalDark),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(pl.name, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                    Text("$${pl.price} / ${pl.billingCycle}", color = RedAccent, fontSize = 13.sp)
                                    Text(pl.features, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp, color = Color.Gray)
                                }
                                Row {
                                    IconButton(onClick = {
                                        planFormId = pl.id
                                        planFormName = pl.name
                                        planFormPrice = pl.price.toString()
                                        planFormFeatures = pl.features
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit specs", tint = Color.LightGray)
                                    }
                                    IconButton(onClick = { viewModel.adminDeletePlan(pl) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Kill plan", tint = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }

                // COACHES DIRECTORIES
                1 -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CharcoalDark),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    if (trainerFormId == 0) "ENLIST ACTIVE TRAINER PROFILE" else "MODIFY TRAINER BIO",
                                    color = RedAccent,
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                
                                OutlinedTextField(
                                    value = trainerFormName,
                                    onValueChange = { trainerFormName = it },
                                    label = { Text("Coach Public Name") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("admin_trainer_name"),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RedPrimary)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                OutlinedTextField(
                                    value = trainerFormSpec,
                                    onValueChange = { trainerFormSpec = it },
                                    label = { Text("Specialization (e.g. Boxing, Fatloss)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RedPrimary)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                OutlinedTextField(
                                    value = trainerFormExp,
                                    onValueChange = { trainerFormExp = it },
                                    label = { Text("Experience Year Tally (e.g. 7 Years)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RedPrimary)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                OutlinedTextField(
                                    value = trainerFormPhone,
                                    onValueChange = { trainerFormPhone = it },
                                    label = { Text("WhatsApp Reachout Contact Number") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RedPrimary)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                OutlinedTextField(
                                    value = trainerFormBio,
                                    onValueChange = { trainerFormBio = it },
                                    label = { Text("Bio and Credentials details") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RedPrimary)
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = {
                                            if (trainerFormName.isBlank() || trainerFormSpec.isBlank()) {
                                                Toast.makeText(context, "Please assign Name & Specs.", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            viewModel.adminSaveTrainer(
                                                id = trainerFormId,
                                                name = trainerFormName,
                                                specialization = trainerFormSpec,
                                                experience = trainerFormExp,
                                                phone = trainerFormPhone,
                                                bio = trainerFormBio
                                            )
                                            trainerFormId = 0
                                            trainerFormName = ""
                                            trainerFormSpec = ""
                                            trainerFormExp = ""
                                            trainerFormPhone = ""
                                            trainerFormBio = ""
                                            Toast.makeText(context, "Trainer roster logs updated", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).testTag("admin_save_trainer")
                                    ) {
                                        Text("DEPLOY ROSTER", color = Color.White)
                                    }

                                    if (trainerFormId != 0) {
                                        OutlinedButton(
                                            onClick = {
                                                trainerFormId = 0
                                                trainerFormName = ""
                                                trainerFormSpec = ""
                                                trainerFormExp = ""
                                                trainerFormPhone = ""
                                                trainerFormBio = ""
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("ABORT", color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text("ROSTERED PERSONNEL", fontWeight = FontWeight.Bold, color = RedAccent)
                    }

                    items(coordinators) { tr ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CharcoalDark),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(tr.name, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(tr.specialization, color = RedAccent, fontSize = 12.sp)
                                    Text("XP: ${tr.experience} • Contact: ${tr.contactPhone}", fontSize = 11.sp, color = Color.Gray)
                                }
                                Row {
                                    IconButton(onClick = {
                                        trainerFormId = tr.id
                                        trainerFormName = tr.name
                                        trainerFormSpec = tr.specialization
                                        trainerFormExp = tr.experience
                                        trainerFormPhone = tr.contactPhone
                                        trainerFormBio = tr.bio
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit trainer", tint = Color.LightGray)
                                    }
                                    IconButton(onClick = { viewModel.adminDeleteTrainer(tr) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Dismiss trainer", tint = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }

                // BOOKINGS LOGS
                2 -> {
                    item {
                        Text("ALL BOOKINGS HISTORY & LOGS", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Active calendar slot allocation logs. Direct client cancels supported.", color = MutedText, fontSize = 12.sp)
                    }

                    if (bookings.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = CharcoalDark)
                            ) {
                                Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text("No reservations received on database.", color = MutedText)
                                }
                            }
                        }
                    } else {
                        items(bookings) { bk ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = CharcoalDark)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column {
                                            Text(
                                                "MEMBER: ${bk.userName.uppercase(Locale.getDefault())}",
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White
                                            )
                                            Text(bk.userEmail, fontSize = 12.sp, color = MutedText)
                                        }

                                        IconButton(onClick = { viewModel.deleteBooking(bk) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Kill client booking", tint = Color.Gray)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text("Program requested: ${bk.planName}", color = RedAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("Coach assigned: ${bk.trainerName}", color = Color.White, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("Date: ${bk.bookingDate}", fontSize = 12.sp, color = MutedText)
                                        Text("Slot: ${bk.timeSlot}", fontSize = 12.sp, color = MutedText)
                                    }
                                }
                            }
                        }
                    }
                    
                    // USER REGISTRATIONS TALLY
                    item {
                        Spacer(modifier = Modifier.height(18.dp))
                        Text("SYSTEM REGISTERED MEMBERS", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    
                    items(users) { usr ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CharcoalDark),
                            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.03f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(usr.fullName, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(usr.email, fontSize = 12.sp, color = MutedText)
                                }
                                val dt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(usr.joinedAt))
                                Text("Joined: $dt", fontSize = 11.sp, color = RedAccent)
                            }
                        }
                    }
                }

                // ANNOUNCEMENTS AND APP OVERRIDES
                3 -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CharcoalDark),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "DISPATCH GLOBAL ALERTS ANNOUNCEMENT",
                                    color = RedAccent,
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Send to support announcements pipeline", fontSize = 11.sp, color = MutedText)
                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedTextField(
                                    value = globalAnnounceTitle,
                                    onValueChange = { globalAnnounceTitle = it },
                                    label = { Text("Announcement Title (e.g. Promo Holiday hours!)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("admin_notif_title"),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RedPrimary)
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = globalAnnounceBody,
                                    onValueChange = { globalAnnounceBody = it },
                                    label = { Text("Announcement body text content details") },
                                    modifier = Modifier.fillMaxWidth().testTag("admin_notif_body"),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RedPrimary)
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        if (globalAnnounceTitle.isBlank() || globalAnnounceBody.isBlank()) {
                                            Toast.makeText(context, "Fill notifications title & body specs first.", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        viewModel.adminSendAnnouncement(globalAnnounceTitle, globalAnnounceBody) {
                                            globalAnnounceTitle = ""
                                            globalAnnounceBody = ""
                                            Toast.makeText(context, "Announcement broadcast successfully!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("admin_send_notif")
                                ) {
                                    Text("DISPATCH ANNOUNCEMENT ALERTS", color = Color.White)
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CharcoalDark),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "APP SYSTEM CONTENT RECOVERY & OVERRIDES",
                                    color = RedAccent,
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedTextField(
                                    value = appOverrideHeading,
                                    onValueChange = { appOverrideHeading = it },
                                    label = { Text("Motivational Home Heading Overrides") },
                                    modifier = Modifier.fillMaxWidth().testTag("admin_motivational_header"),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RedPrimary)
                                )
                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedTextField(
                                    value = appOverrideTip1,
                                    onValueChange = { appOverrideTip1 = it },
                                    label = { Text("Dietary Tip Recovery #1") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RedPrimary)
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = appOverrideTip2,
                                    onValueChange = { appOverrideTip2 = it },
                                    label = { Text("Dietary Tip Recovery #2") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RedPrimary)
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = appOverrideTip3,
                                    onValueChange = { appOverrideTip3 = it },
                                    label = { Text("Dietary Tip Recovery #3") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RedPrimary)
                                )
                                Spacer(modifier = Modifier.height(18.dp))

                                Button(
                                    onClick = {
                                        val tipsList = listOf(appOverrideTip1, appOverrideTip2, appOverrideTip3)
                                        viewModel.updateAppContent(appOverrideHeading, tipsList)
                                        Toast.makeText(context, "App contents updated and logged", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("admin_save_content")
                                ) {
                                    Text("DEPLOY OVERRIDES TO MEMBER HOME", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
