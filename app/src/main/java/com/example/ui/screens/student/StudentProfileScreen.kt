package com.example.ui.screens.student

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.StudentEntity
import com.example.qr.QrCodeGenerator
import com.example.ui.components.StatusBadge
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LibraryAmber
import com.example.ui.theme.LibraryGold
import com.example.ui.theme.LibraryNavy
import com.example.ui.theme.LibraryNavyDark
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.SuccessGreen
import com.example.util.ImageUtils

@Composable
fun StudentProfileScreen(
    student: StudentEntity,
    onDownloadPdf: () -> Unit,
    onLogout: () -> Unit,
    onUpdateProfile: (
        fullName: String,
        parentName: String,
        mobileNumber: String,
        email: String,
        dob: String,
        address: String,
        course: String,
        emergencyContact: String,
        aadhaarFrontBase64: String?,
        aadhaarBackBase64: String?,
        (Boolean, String) -> Unit
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var viewingDocumentBase64 by remember { mutableStateOf<Pair<String, String>?>(null) } // Title to Base64

    val personalQrPayload = remember(student.id, student.fullName) {
        QrCodeGenerator.formatStudentQr(student.id, student.fullName)
    }
    val personalQrBitmap = remember(personalQrPayload) {
        QrCodeGenerator.generateQrBitmap(personalQrPayload, 350, 350)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Profile Header Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = LibraryNavyDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(LibraryNavy)
                        .border(2.dp, LibraryGold, CircleShape)
                ) {
                    Text(
                        text = student.fullName.take(1).uppercase(),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        color = LibraryGold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = student.fullName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Text(
                    text = "ID: ${student.id} • ${student.seatNumber}",
                    fontSize = 12.sp,
                    color = LibraryGold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusBadge(status = student.status)
                    if (student.profileCompleted) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SuccessGreen.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Aadhaar Verified",
                                color = SuccessGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = LibraryAmber.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = "Profile Incomplete",
                                color = LibraryAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { showEditProfileDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = LibraryGold),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_edit_complete_profile")
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = LibraryNavyDark, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (student.profileCompleted) "Edit Profile & Documents" else "Complete Profile & Upload Aadhaar",
                        color = LibraryNavyDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // 2. Aadhaar Card Verification Documents Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = LibraryNavy)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Aadhaar Card Documents",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Slate900
                        )
                    }

                    if (!student.aadhaarFrontBase64.isNullOrBlank() && !student.aadhaarBackBase64.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Both Uploaded", fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = LibraryAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Action Required", fontSize = 11.sp, color = LibraryAmber, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Front and Back Document Previews
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Front Photo Box
                    DocumentPreviewBox(
                        title = "Aadhaar Front Photo",
                        base64 = student.aadhaarFrontBase64,
                        onView = { viewingDocumentBase64 = "Aadhaar Front Photo" to (student.aadhaarFrontBase64 ?: "") },
                        onUploadClick = { showEditProfileDialog = true },
                        modifier = Modifier.weight(1f)
                    )

                    // Back Photo Box
                    DocumentPreviewBox(
                        title = "Aadhaar Back Photo",
                        base64 = student.aadhaarBackBase64,
                        onView = { viewingDocumentBase64 = "Aadhaar Back Photo" to (student.aadhaarBackBase64 ?: "") },
                        onUploadClick = { showEditProfileDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Digital Student QR Pass Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.QrCode, contentDescription = null, tint = LibraryNavy)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "My Digital Student QR Pass",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Slate900
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Personal QR code for reception desk verification & identity confirmation.",
                    fontSize = 11.sp,
                    color = Slate500,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(190.dp)
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .border(1.5.dp, LibraryNavy, RoundedCornerShape(10.dp))
                        .padding(8.dp)
                ) {
                    if (personalQrBitmap != null) {
                        Image(
                            bitmap = personalQrBitmap.asImageBitmap(),
                            contentDescription = "Personal QR",
                            modifier = Modifier.size(174.dp)
                        )
                    } else {
                        Text("QR unavailable")
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Pass Token: ${student.studentQrToken}",
                    fontSize = 10.sp,
                    color = Slate500
                )
            }
        }

        // 4. Personal Registration Details Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Student Profile Information",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Slate900
                )

                ProfileFieldRow("Full Legal Name", student.fullName)
                ProfileFieldRow("User ID (Login)", student.username)
                ProfileFieldRow("Student ID", student.id)
                ProfileFieldRow("Father's / Mother's Name", student.parentName.ifBlank { "Not provided" })
                ProfileFieldRow("Date of Birth", student.dob.ifBlank { "Not completed" })
                ProfileFieldRow("Mobile Number", student.mobileNumber)
                ProfileFieldRow("Email Address", student.email.ifBlank { "Not provided" })
                ProfileFieldRow("Assigned Seat", student.seatNumber)
                ProfileFieldRow("Course / Exam Target", student.course)
                ProfileFieldRow("Emergency Contact", student.emergencyContact.ifBlank { "Not provided" })
                ProfileFieldRow("Joining Date", student.joiningDate)
                ProfileFieldRow("Address", student.address.ifBlank { "Not provided" })
            }
        }

        // 5. PDF Dossier Export Button
        Button(
            onClick = onDownloadPdf,
            colors = ButtonDefaults.buttonColors(containerColor = LibraryNavy),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_student_download_pdf")
        ) {
            Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Download My Attendance PDF Record")
        }

        // 6. Logout Button
        OutlinedButton(
            onClick = onLogout,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_student_logout")
        ) {
            Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log Out of Student Account")
        }
    }

    // Edit / Complete Profile Dialog
    if (showEditProfileDialog) {
        StudentProfileEditDialog(
            student = student,
            onDismiss = { showEditProfileDialog = false },
            onSave = { fullName, parentName, mobile, email, dob, address, course, emergency, frontBase64, backBase64 ->
                onUpdateProfile(fullName, parentName, mobile, email, dob, address, course, emergency, frontBase64, backBase64) { success, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    if (success) {
                        showEditProfileDialog = false
                    }
                }
            }
        )
    }

    // Zoom Document Dialog
    if (viewingDocumentBase64 != null) {
        val (docTitle, docBase64) = viewingDocumentBase64!!
        val docBitmap = remember(docBase64) { ImageUtils.base64ToBitmap(docBase64) }

        Dialog(onDismissRequest = { viewingDocumentBase64 = null }) {
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
                        Text(text = docTitle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        IconButton(onClick = { viewingDocumentBase64 = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    if (docBitmap != null) {
                        Image(
                            bitmap = docBitmap.asImageBitmap(),
                            contentDescription = docTitle,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(420.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text("No document available", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentPreviewBox(
    title: String,
    base64: String?,
    onView: () -> Unit,
    onUploadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(base64) { ImageUtils.base64ToBitmap(base64) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
            .border(1.dp, if (base64 != null) SuccessGreen.copy(alpha = 0.6f) else Slate500.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate700)
        Spacer(modifier = Modifier.height(6.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White)
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onView() },
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = Slate500, modifier = Modifier.size(28.dp))
                    Text("Not uploaded", fontSize = 10.sp, color = Slate500)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (base64 != null) {
            OutlinedButton(
                onClick = onView,
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
            ) {
                Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("View Full", fontSize = 10.sp)
            }
        } else {
            Button(
                onClick = onUploadClick,
                colors = ButtonDefaults.buttonColors(containerColor = LibraryNavy),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
            ) {
                Icon(imageVector = Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Upload", fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun StudentProfileEditDialog(
    student: StudentEntity,
    onDismiss: () -> Unit,
    onSave: (
        fullName: String,
        parentName: String,
        mobile: String,
        email: String,
        dob: String,
        address: String,
        course: String,
        emergency: String,
        frontBase64: String?,
        backBase64: String?
    ) -> Unit
) {
    val context = LocalContext.current
    var fullName by remember { mutableStateOf(student.fullName) }
    var parentName by remember { mutableStateOf(student.parentName) }
    var mobile by remember { mutableStateOf(student.mobileNumber) }
    var email by remember { mutableStateOf(student.email) }
    var dob by remember { mutableStateOf(student.dob) }
    var address by remember { mutableStateOf(student.address) }
    var course by remember { mutableStateOf(student.course) }
    var emergency by remember { mutableStateOf(student.emergencyContact) }

    var aadhaarFront by remember { mutableStateOf(student.aadhaarFrontBase64) }
    var aadhaarBack by remember { mutableStateOf(student.aadhaarBackBase64) }
    var isSaving by remember { mutableStateOf(false) }

    // Front photo picker
    val frontPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val base64 = ImageUtils.uriToBase64(context, uri, maxWidth = 1000, maxHeight = 1000, quality = 80)
            if (base64 != null) {
                aadhaarFront = base64
                Toast.makeText(context, "Aadhaar Front photo loaded", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Back photo picker
    val backPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val base64 = ImageUtils.uriToBase64(context, uri, maxWidth = 1000, maxHeight = 1000, quality = 80)
            if (base64 != null) {
                aadhaarBack = base64
                Toast.makeText(context, "Aadhaar Back photo loaded", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Student Profile & Aadhaar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Slate900
                        )
                        Text(
                            text = "Fill all required details & upload verification cards",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Legal Name *") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = parentName,
                    onValueChange = { parentName = it },
                    label = { Text("Father's / Mother's Name *") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dob,
                        onValueChange = { dob = it },
                        label = { Text("DOB (YYYY-MM-DD) *") },
                        placeholder = { Text("2002-05-18") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { mobile = it },
                        label = { Text("Mobile Number *") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Complete Residential Address *") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = course,
                        onValueChange = { course = it },
                        label = { Text("Target Exam / Course") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = emergency,
                        onValueChange = { emergency = it },
                        label = { Text("Emergency Contact") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Aadhaar Upload Header
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Aadhaar Card Document Photos (JPG, JPEG, PNG)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Slate900
                )

                // Aadhaar Front upload
                AadhaarUploadRow(
                    title = "Aadhaar Front Photo",
                    base64 = aadhaarFront,
                    onPick = {
                        frontPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )

                // Aadhaar Back upload
                AadhaarUploadRow(
                    title = "Aadhaar Back Photo",
                    base64 = aadhaarBack,
                    onPick = {
                        backPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (fullName.isBlank() || parentName.isBlank() || dob.isBlank() || mobile.isBlank()) {
                            Toast.makeText(context, "Please fill all required profile fields", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSaving = true
                        onSave(
                            fullName,
                            parentName,
                            mobile,
                            email,
                            dob,
                            address,
                            course,
                            emergency,
                            aadhaarFront,
                            aadhaarBack
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LibraryNavy),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saving to Firebase...")
                    } else {
                        Text("Save & Complete Profile")
                    }
                }
            }
        }
    }
}

@Composable
fun AadhaarUploadRow(
    title: String,
    base64: String?,
    onPick: () -> Unit
) {
    val bitmap = remember(base64) { ImageUtils.base64ToBitmap(base64) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
            .border(1.dp, if (base64 != null) SuccessGreen else Slate500.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = title,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE2E8F0))
                ) {
                    Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = Slate500, modifier = Modifier.size(22.dp))
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate900)
                Text(
                    text = if (base64 != null) "✓ Document attached" else "Not uploaded yet",
                    fontSize = 11.sp,
                    color = if (base64 != null) SuccessGreen else Slate500
                )
            }
        }

        OutlinedButton(
            onClick = onPick,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Icon(imageVector = Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(if (base64 != null) "Replace" else "Upload", fontSize = 11.sp)
        }
    }
}

@Composable
private fun ProfileFieldRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = Slate500)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
    }
}
