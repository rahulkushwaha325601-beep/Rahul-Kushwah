package com.example.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LibraryAmber
import com.example.ui.theme.LibraryGold
import com.example.ui.theme.LibraryNavy
import com.example.ui.theme.LibraryNavyDark
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800

@Composable
fun LoginScreen(
    onAdminLogin: (username: String, password: String, (Boolean, String) -> Unit) -> Unit,
    onStudentLogin: (username: String, password: String, (Boolean, String) -> Unit) -> Unit,
    onSync: (() -> Unit)? = null,
    isSyncing: Boolean = false,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: Admin, 1: Student
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Admin login inputs (Starts completely empty)
    var adminUsername by remember { mutableStateOf("") }
    var adminPassword by remember { mutableStateOf("") }
    var isAdminPasswordVisible by remember { mutableStateOf(false) }

    // Student login inputs (Starts completely empty)
    var studentUsername by remember { mutableStateOf("") }
    var studentPassword by remember { mutableStateOf("") }
    var isStudentPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
    ) {
        // Official R.S Library Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(LibraryNavyDark)
                .padding(vertical = 36.dp, horizontal = 20.dp)
        ) {
            if (onSync != null) {
                IconButton(
                    onClick = onSync,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .testTag("login_sync_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sync Cloud",
                        tint = if (isSyncing) LibraryGold else Color.White.copy(alpha = 0.85f)
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(LibraryNavy)
                        .border(1.5.dp, LibraryGold, RoundedCornerShape(16.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Logo",
                        tint = LibraryGold,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "R.S LIBRARY",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    ),
                    color = Color.White
                )

                Text(
                    text = "Library Management & Attendance Portal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LibraryGold.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Study • Focus • Succeed",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Role Tabs: Admin vs Student
        Surface(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 2.dp,
            color = Color.White
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = LibraryNavy
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = {
                        selectedTabIndex = 0
                        errorMessage = null
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Admin / Owner", fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.testTag("tab_admin_login")
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = {
                        selectedTabIndex = 1
                        errorMessage = null
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.School, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Student", fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.testTag("tab_student_login")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Secure Login Form
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                if (selectedTabIndex == 0) {
                    // Admin Login Form
                    Text(
                        text = "Administrator Sign In",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = LibraryNavy
                    )
                    Text(
                        text = "Sign in to manage library students, attendance, QR codes, and reports.",
                        fontSize = 12.sp,
                        color = Slate500
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    OutlinedTextField(
                        value = adminUsername,
                        onValueChange = { adminUsername = it },
                        label = { Text("Admin Username") },
                        placeholder = { Text("Enter your username") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = LibraryNavy)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_username_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = adminPassword,
                        onValueChange = { adminPassword = it },
                        label = { Text("Admin Password") },
                        placeholder = { Text("Enter your password") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = LibraryNavy)
                        },
                        trailingIcon = {
                            IconButton(onClick = { isAdminPasswordVisible = !isAdminPasswordVisible }) {
                                Icon(
                                    imageVector = if (isAdminPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (isAdminPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (adminUsername.isNotBlank() && adminPassword.isNotBlank()) {
                                    isLoading = true
                                    errorMessage = null
                                    onAdminLogin(adminUsername, adminPassword) { success, msg ->
                                        isLoading = false
                                        if (!success) errorMessage = msg
                                    }
                                }
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_password_input")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (adminUsername.isBlank() || adminPassword.isBlank()) {
                                errorMessage = "Please enter both username and password."
                                return@Button
                            }
                            isLoading = true
                            errorMessage = null
                            onAdminLogin(adminUsername, adminPassword) { success, msg ->
                                isLoading = false
                                if (!success) errorMessage = msg
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LibraryNavy),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("admin_login_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Sign In to Admin Dashboard", fontWeight = FontWeight.Bold)
                        }
                    }

                } else {
                    // Student Login Form
                    Text(
                        text = "Student Sign In",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = LibraryNavy
                    )
                    Text(
                        text = "Sign in on your mobile phone using the unique User ID and Password created for you by the Admin.",
                        fontSize = 12.sp,
                        color = Slate500
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    OutlinedTextField(
                        value = studentUsername,
                        onValueChange = { studentUsername = it },
                        label = { Text("Student User ID or Username") },
                        placeholder = { Text("e.g. student001 or RSL-001") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = LibraryNavy)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("student_username_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = studentPassword,
                        onValueChange = { studentPassword = it },
                        label = { Text("Student Password") },
                        placeholder = { Text("Enter your password") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = LibraryNavy)
                        },
                        trailingIcon = {
                            IconButton(onClick = { isStudentPasswordVisible = !isStudentPasswordVisible }) {
                                Icon(
                                    imageVector = if (isStudentPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (isStudentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (studentUsername.isNotBlank() && studentPassword.isNotBlank()) {
                                    isLoading = true
                                    errorMessage = null
                                    onStudentLogin(studentUsername, studentPassword) { success, msg ->
                                        isLoading = false
                                        if (!success) errorMessage = msg
                                    }
                                }
                            }
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("student_password_input")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (studentUsername.isBlank() || studentPassword.isBlank()) {
                                errorMessage = "Please enter both username and password."
                                return@Button
                            }
                            isLoading = true
                            errorMessage = null
                            onStudentLogin(studentUsername, studentPassword) { success, msg ->
                                isLoading = false
                                if (!success) errorMessage = msg
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LibraryAmber),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("student_login_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Sign In to Student Account", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                // Error Feedback
                AnimatedVisibility(visible = errorMessage != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = ErrorRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // System Footer
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "R.S Library Management & Security System",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Slate700
            )
            Text(
                text = "Official attendance portal • Authorized access only",
                fontSize = 10.sp,
                color = Slate500
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
