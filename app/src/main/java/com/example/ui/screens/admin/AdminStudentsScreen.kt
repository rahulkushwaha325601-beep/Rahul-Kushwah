package com.example.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import com.example.util.ImageUtils
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentEntity
import com.example.data.repository.LibraryRepository
import com.example.ui.components.StatusBadge
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LibraryAmber
import com.example.ui.theme.LibraryGold
import com.example.ui.theme.LibraryNavy
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@Composable
fun AdminStudentsScreen(
    students: List<StudentEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    statusFilter: String,
    onStatusFilterChange: (String) -> Unit,
    onAddOrUpdateStudent: (StudentEntity, newPasswordIfAny: String?, isEdit: Boolean, (Boolean, String) -> Unit) -> Unit,
    onDeleteStudent: (StudentEntity, () -> Unit) -> Unit,
    onResetStudentPassword: (studentId: String, newPassword: String, (Boolean, String) -> Unit) -> Unit,
    onShowStudentQr: (StudentEntity) -> Unit,
    onDownloadStudentPdf: (StudentEntity) -> Unit,
    defaultNextStudentId: String,
    modifier: Modifier = Modifier
) {
    var showStudentFormDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<StudentEntity?>(null) }
    var studentToDelete by remember { mutableStateOf<StudentEntity?>(null) }
    var studentToResetPassword by remember { mutableStateOf<StudentEntity?>(null) }
    var studentViewingDocs by remember { mutableStateOf<StudentEntity?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
        ) {
            // Search Bar & Filter Strip
            Surface(
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Search by name, ID, username, phone, seat...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = LibraryNavy)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_student_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Status:", fontSize = 12.sp, color = Slate500, fontWeight = FontWeight.Medium)
                        listOf("All", "Active", "Inactive").forEach { filter ->
                            FilterChip(
                                selected = statusFilter == filter,
                                onClick = { onStatusFilterChange(filter) },
                                label = { Text(filter, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = LibraryNavy,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Summary Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Registered Students (${students.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Slate700
                )
                Text(
                    text = "Tap student for actions",
                    fontSize = 11.sp,
                    color = Slate500
                )
            }

            // Student List or Empty State
            if (students.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Slate500, modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No students match '$searchQuery'" else "No students registered yet",
                            fontWeight = FontWeight.Bold,
                            color = Slate700
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Add your first student account using the '+' button below.",
                            fontSize = 12.sp,
                            color = Slate500
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(students) { student ->
                        StudentItemCard(
                            student = student,
                            onViewProfileDocs = { studentViewingDocs = student },
                            onEdit = {
                                editingStudent = student
                                showStudentFormDialog = true
                            },
                            onDelete = { studentToDelete = student },
                            onResetPassword = { studentToResetPassword = student },
                            onShowQr = { onShowStudentQr(student) },
                            onDownloadPdf = { onDownloadStudentPdf(student) }
                        )
                    }
                }
            }
        }

        // Floating Action Button: Add Student
        FloatingActionButton(
            onClick = {
                editingStudent = null
                showStudentFormDialog = true
            },
            containerColor = LibraryNavy,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_student")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Student")
        }
    }

    // Add / Edit Student Dialog (Includes Username & Password fields)
    if (showStudentFormDialog) {
        StudentFormDialog(
            student = editingStudent,
            defaultId = defaultNextStudentId,
            onDismiss = { showStudentFormDialog = false },
            onSave = { student, plainPassword, isEdit, callback ->
                onAddOrUpdateStudent(student, plainPassword, isEdit) { success, msg ->
                    callback(success, msg)
                    if (success) {
                        showStudentFormDialog = false
                    }
                }
            }
        )
    }

    // Reset Password Dialog (Admin Only)
    if (studentToResetPassword != null) {
        val student = studentToResetPassword!!
        ResetPasswordDialog(
            student = student,
            onDismiss = { studentToResetPassword = null },
            onReset = { newPass, callback ->
                onResetStudentPassword(student.id, newPass) { success, msg ->
                    callback(success, msg)
                    if (success) {
                        studentToResetPassword = null
                    }
                }
            }
        )
    }

    // View Profile & Documents Dialog
    if (studentViewingDocs != null) {
        StudentProfileAndDocsDialog(
            student = studentViewingDocs!!,
            onDismiss = { studentViewingDocs = null },
            onEdit = {
                editingStudent = studentViewingDocs
                studentViewingDocs = null
                showStudentFormDialog = true
            }
        )
    }

    // Delete Confirmation Dialog
    if (studentToDelete != null) {
        val student = studentToDelete!!
        AlertDialog(
            onDismissRequest = { studentToDelete = null },
            title = {
                Text("Delete Student Account", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure you want to permanently delete '${student.fullName}' (Username: ${student.username}, ID: ${student.id})?\nTheir account and attendance records will be removed."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteStudent(student) {
                            studentToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Delete Student", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { studentToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StudentItemCard(
    student: StudentEntity,
    onViewProfileDocs: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onResetPassword: () -> Unit,
    onShowQr: () -> Unit,
    onDownloadPdf: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewProfileDocs() }
            .testTag("student_card_${student.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(LibraryNavy.copy(alpha = 0.12f))
                    ) {
                        Text(
                            text = student.fullName.take(1).uppercase(),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = LibraryNavy
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = student.fullName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Slate900
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Username: ${student.username}",
                                fontSize = 12.sp,
                                color = LibraryNavy,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• Seat: ${student.seatNumber}",
                                fontSize = 12.sp,
                                color = LibraryAmber,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (!student.aadhaarFrontBase64.isNullOrBlank() && !student.aadhaarBackBase64.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = com.example.ui.theme.SuccessGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Aadhaar ✓",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.SuccessGreen,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = LibraryAmber.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = "Aadhaar ⌛",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = LibraryAmber,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    StatusBadge(status = student.status)
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("View Profile & Documents") },
                                onClick = { showMenu = false; onViewProfileDocs() },
                                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = LibraryNavy) }
                            )
                            DropdownMenuItem(
                                text = { Text("View Personal QR Code") },
                                onClick = { showMenu = false; onShowQr() },
                                leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null, tint = LibraryNavy) }
                            )
                            DropdownMenuItem(
                                text = { Text("Share Login Pass / Credentials") },
                                onClick = {
                                    showMenu = false
                                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(android.content.Intent.EXTRA_SUBJECT, "R.S Library Access Pass - ${student.fullName}")
                                        putExtra(
                                            android.content.Intent.EXTRA_TEXT,
                                            """
                                            *R.S LIBRARY - STUDENT ACCESS PASS*
                                            Welcome ${student.fullName}!
                                            
                                            *Your Login Credentials for R.S Library Mobile App:*
                                            • User ID: ${student.username}
                                            • Student ID: ${student.id}
                                            • Assigned Seat: ${student.seatNumber}
                                            • Target Course: ${student.course}
                                            
                                            *How to Access on Your Mobile Phone:*
                                            1. Open the R.S Library Application on your phone.
                                            2. Select 'Student Login'.
                                            3. Enter your User ID: ${student.username} and your assigned password.
                                            4. Scan the Reception QR Code at the library entrance to mark your attendance!
                                            """.trimIndent()
                                        )
                                    }
                                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Student Login Pass"))
                                },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = LibraryNavy) }
                            )
                            DropdownMenuItem(
                                text = { Text("Change / Reset Password") },
                                onClick = { showMenu = false; onResetPassword() },
                                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = LibraryAmber) }
                            )
                            DropdownMenuItem(
                                text = { Text("Download Student Dossier PDF") },
                                onClick = { showMenu = false; onDownloadPdf() },
                                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = Slate700) }
                            )
                            DropdownMenuItem(
                                text = { Text("Edit Student Details") },
                                onClick = { showMenu = false; onEdit() },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Account", color = ErrorRed) },
                                onClick = { showMenu = false; onDelete() },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(13.dp), tint = Slate500)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(student.mobileNumber, fontSize = 11.sp, color = Slate700)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.School, contentDescription = null, modifier = Modifier.size(13.dp), tint = Slate500)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(student.course, fontSize = 11.sp, color = Slate700)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("ID: ${student.id}", fontSize = 11.sp, color = Slate700, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Joined: ${student.joiningDate}", fontSize = 11.sp, color = Slate500)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onShowQr,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Icon(imageVector = Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Personal QR", fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = onResetPassword,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Password", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun StudentFormDialog(
    student: StudentEntity?,
    defaultId: String,
    onDismiss: () -> Unit,
    onSave: (StudentEntity, newPassword: String?, isEdit: Boolean, (Boolean, String) -> Unit) -> Unit
) {
    val isEdit = student != null

    var id by remember { mutableStateOf(student?.id ?: defaultId) }
    var username by remember { mutableStateOf(student?.username ?: "") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var fullName by remember { mutableStateOf(student?.fullName ?: "") }
    var parentName by remember { mutableStateOf(student?.parentName ?: "") }
    var mobile by remember { mutableStateOf(student?.mobileNumber ?: "") }
    var email by remember { mutableStateOf(student?.email ?: "") }
    var address by remember { mutableStateOf(student?.address ?: "") }
    var seatNumber by remember { mutableStateOf(student?.seatNumber ?: "") }
    var course by remember { mutableStateOf(student?.course ?: "") }
    var joiningDate by remember { mutableStateOf(student?.joiningDate ?: LibraryRepository.getTodayDate()) }
    var status by remember { mutableStateOf(student?.status ?: "Active") }

    var formError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isEdit) "Edit Student Details" else "Add New Student Account",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = LibraryNavy
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (formError != null) {
                    Surface(
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = formError ?: "",
                            color = ErrorRed,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                // Authentication Credentials Section
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Login Credentials (Private to Student)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = LibraryNavy
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Student Username * (e.g. student001)") },
                            enabled = !isEdit,
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_student_username")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text(if (isEdit) "New Password (Leave empty to keep existing)" else "Student Password *") },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_student_password")
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = id,
                        onValueChange = { id = it },
                        label = { Text("Student ID *") },
                        enabled = !isEdit,
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_student_id")
                    )
                    OutlinedTextField(
                        value = seatNumber,
                        onValueChange = { seatNumber = it },
                        label = { Text("Seat / Table # *") },
                        placeholder = { Text("e.g. Seat 12") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_student_seat")
                    )
                }

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_student_name")
                )

                OutlinedTextField(
                    value = parentName,
                    onValueChange = { parentName = it },
                    label = { Text("Father's / Mother's Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("Mobile Number *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_student_mobile")
                )

                OutlinedTextField(
                    value = course,
                    onValueChange = { course = it },
                    label = { Text("Course / Exam Target *") },
                    placeholder = { Text("e.g. UPSC, NEET, Self-Study") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = joiningDate,
                        onValueChange = { joiningDate = it },
                        label = { Text("Joining Date") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    // Account Status Chips
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Account Status", fontSize = 11.sp, color = Slate500)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilterChip(
                                selected = status == "Active",
                                onClick = { status = "Active" },
                                label = { Text("Active", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = status == "Inactive",
                                onClick = { status = "Inactive" },
                                label = { Text("Disabled", fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (id.isBlank() || username.isBlank() || fullName.isBlank() || mobile.isBlank() || seatNumber.isBlank() || course.isBlank()) {
                        formError = "Please fill in all required fields (Username, ID, Name, Mobile, Seat, Course)."
                        return@Button
                    }
                    if (!isEdit && password.isBlank()) {
                        formError = "Please enter a password for the student account."
                        return@Button
                    }
                    val updatedStudent = StudentEntity(
                        id = id.trim(),
                        username = username.trim(),
                        passwordHash = student?.passwordHash ?: "",
                        fullName = fullName.trim(),
                        parentName = parentName.trim(),
                        mobileNumber = mobile.trim(),
                        email = email.trim(),
                        address = address.trim(),
                        joiningDate = joiningDate.trim(),
                        seatNumber = seatNumber.trim(),
                        course = course.trim(),
                        status = status
                    )
                    onSave(updatedStudent, password.ifBlank { null }, isEdit) { success, msg ->
                        if (!success) formError = msg
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LibraryNavy),
                modifier = Modifier.testTag("save_student_button")
            ) {
                Text(if (isEdit) "Update Student" else "Create Student Account")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ResetPasswordDialog(
    student: StudentEntity,
    onDismiss: () -> Unit,
    onReset: (newPassword: String, (Boolean, String) -> Unit) -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Reset Student Password", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = LibraryNavy)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Change login password for ${student.fullName} (Username: ${student.username}).",
                    fontSize = 12.sp,
                    color = Slate700
                )

                if (errorMessage != null) {
                    Surface(
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = ErrorRed,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newPassword.isBlank()) {
                        errorMessage = "Password cannot be empty."
                        return@Button
                    }
                    if (newPassword != confirmPassword) {
                        errorMessage = "Passwords do not match."
                        return@Button
                    }
                    onReset(newPassword) { success, msg ->
                        if (!success) errorMessage = msg
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LibraryNavy)
            ) {
                Text("Set Password")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun StudentProfileAndDocsDialog(
    student: StudentEntity,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    var viewingZoomDoc by remember { mutableStateOf<Pair<String, String>?>(null) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = student.fullName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Slate900
                        )
                        Text(
                            text = "Student ID: ${student.id} • Username: ${student.username}",
                            fontSize = 12.sp,
                            color = Slate500
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Status & Aadhaar Verification State
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusBadge(status = student.status)
                    if (!student.aadhaarFrontBase64.isNullOrBlank() && !student.aadhaarBackBase64.isNullOrBlank()) {
                        Surface(shape = RoundedCornerShape(10.dp), color = com.example.ui.theme.SuccessGreen.copy(alpha = 0.15f)) {
                            Text(
                                text = "Aadhaar Verified",
                                color = com.example.ui.theme.SuccessGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Surface(shape = RoundedCornerShape(10.dp), color = LibraryAmber.copy(alpha = 0.2f)) {
                            Text(
                                text = "Aadhaar Documents Pending",
                                color = LibraryAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Profile Fields Grid
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AdminProfileField("Parent Name", student.parentName.ifBlank { "Not provided" })
                        AdminProfileField("Date of Birth", student.dob.ifBlank { "Not provided" })
                        AdminProfileField("Mobile Number", student.mobileNumber)
                        AdminProfileField("Email Address", student.email.ifBlank { "Not provided" })
                        AdminProfileField("Assigned Seat", student.seatNumber)
                        AdminProfileField("Course / Target", student.course)
                        AdminProfileField("Emergency Contact", student.emergencyContact.ifBlank { "Not provided" })
                        AdminProfileField("Joining Date", student.joiningDate)
                        AdminProfileField("Residential Address", student.address.ifBlank { "Not provided" })
                    }
                }

                // Aadhaar Documents
                Text(
                    text = "Submitted Aadhaar Card Documents",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Slate900
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminDocPreview(
                        title = "Front Photo",
                        base64 = student.aadhaarFrontBase64,
                        onZoom = { viewingZoomDoc = "Aadhaar Front Photo" to (student.aadhaarFrontBase64 ?: "") },
                        modifier = Modifier.weight(1f)
                    )
                    AdminDocPreview(
                        title = "Back Photo",
                        base64 = student.aadhaarBackBase64,
                        onZoom = { viewingZoomDoc = "Aadhaar Back Photo" to (student.aadhaarBackBase64 ?: "") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onEdit,
                        colors = ButtonDefaults.buttonColors(containerColor = LibraryNavy),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit Details")
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }

    if (viewingZoomDoc != null) {
        val (docTitle, docBase64) = viewingZoomDoc!!
        val docBitmap = remember(docBase64) { ImageUtils.base64ToBitmap(docBase64) }
        androidx.compose.ui.window.Dialog(onDismissRequest = { viewingZoomDoc = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.95f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = docTitle, color = Color.White, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { viewingZoomDoc = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                    if (docBitmap != null) {
                        androidx.compose.foundation.Image(
                            bitmap = docBitmap.asImageBitmap(),
                            contentDescription = docTitle,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(420.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDocPreview(title: String, base64: String?, onZoom: () -> Unit, modifier: Modifier = Modifier) {
    val bitmap = remember(base64) { ImageUtils.base64ToBitmap(base64) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
            .border(1.dp, if (base64 != null) com.example.ui.theme.SuccessGreen.copy(alpha = 0.5f) else Slate500.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate700)
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White)
        ) {
            if (bitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize().clickable { onZoom() },
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Text("Not uploaded", fontSize = 10.sp, color = Slate500)
            }
        }
        if (base64 != null) {
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedButton(
                onClick = onZoom,
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                modifier = Modifier.fillMaxWidth().height(28.dp)
            ) {
                Text("View Full", fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun AdminProfileField(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = Slate500)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
    }
}
