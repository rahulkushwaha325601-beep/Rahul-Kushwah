package com.example.ui.screens.admin

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentEntity
import com.example.data.repository.LibraryRepository
import com.example.pdf.PdfReportGenerator
import com.example.ui.theme.LibraryAmber
import com.example.ui.theme.LibraryNavy
import com.example.ui.theme.LibraryNavyDark
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.SuccessGreen
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportsScreen(
    students: List<StudentEntity>,
    onExportAllStudentsPdf: ((File) -> Unit) -> Unit,
    onExportAttendancePdf: (String, (File) -> Unit) -> Unit,
    onExportIndividualPdf: (StudentEntity, (File) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var lastGeneratedFile by remember { mutableStateOf<File?>(null) }
    var selectedDate by remember { mutableStateOf(LibraryRepository.getTodayDate()) }

    var selectedStudentId by remember { mutableStateOf(students.firstOrNull()?.id ?: "") }
    var expandedStudentDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Banner
        Surface(
            color = LibraryNavyDark,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "PDF Reports & Document Generator",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Generate printable official A4 reports formatted with R.S Library branding, tables, timestamps, and signatures.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }

        // Generated Notification Banner
        if (lastGeneratedFile != null) {
            val file = lastGeneratedFile!!
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD1FAE5)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("PDF Generated Successfully", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Slate900)
                            Text(file.name, fontSize = 11.sp, color = Slate700)
                        }
                    }
                    Button(
                        onClick = {
                            val intent = PdfReportGenerator.createSharePdfIntent(context, file)
                            context.startActivity(Intent.createChooser(intent, "Share Report"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("btn_share_pdf")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share / Save", fontSize = 11.sp)
                    }
                }
            }
        }

        // Report Option 1: All Students Directory
        ReportCard(
            title = "Complete Student Directory PDF",
            description = "Full list of all registered students with contact details, assigned seat numbers, courses, and account status.",
            icon = Icons.Default.Group,
            onGenerate = {
                onExportAllStudentsPdf { file ->
                    lastGeneratedFile = file
                }
            },
            testTag = "btn_generate_students_pdf"
        )

        // Report Option 2: Date-wise Attendance Report
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = LibraryNavy, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Date-Wise Attendance Report PDF", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Daily attendance sheet showing student check-in time, check-out time, and verification method (QR vs Manual).",
                    fontSize = 12.sp,
                    color = Slate500
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = selectedDate,
                        onValueChange = { selectedDate = it },
                        label = { Text("Select Date (YYYY-MM-DD)", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { selectedDate = LibraryRepository.getTodayDate() },
                        colors = ButtonDefaults.buttonColors(containerColor = LibraryNavy)
                    ) {
                        Text("Today", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        onExportAttendancePdf(selectedDate) { file ->
                            lastGeneratedFile = file
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LibraryNavy),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_generate_attendance_pdf")
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download Attendance Report for $selectedDate")
                }
            }
        }

        // Report Option 3: Individual Student Dossier
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = LibraryAmber, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Individual Student Dossier & Attendance PDF", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Comprehensive student profile document including personal information, assigned seat, and complete attendance history.",
                    fontSize = 12.sp,
                    color = Slate500
                )
                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedStudentDropdown,
                    onExpandedChange = { expandedStudentDropdown = !expandedStudentDropdown }
                ) {
                    val currentStudent = students.find { it.id == selectedStudentId }
                    val label = currentStudent?.let { "${it.fullName} (${it.id})" } ?: "Select a student"
                    OutlinedTextField(
                        value = label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Choose Student") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStudentDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedStudentDropdown,
                        onDismissRequest = { expandedStudentDropdown = false }
                    ) {
                        students.forEach { s ->
                            DropdownMenuItem(
                                text = { Text("${s.fullName} (${s.id})") },
                                onClick = {
                                    selectedStudentId = s.id
                                    expandedStudentDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val student = students.find { it.id == selectedStudentId } ?: students.firstOrNull()
                        if (student != null) {
                            onExportIndividualPdf(student) { file ->
                                lastGeneratedFile = file
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LibraryAmber),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_generate_individual_pdf")
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download Individual Student Report")
                }
            }
        }
    }
}

@Composable
fun ReportCard(
    title: String,
    description: String,
    icon: ImageVector,
    onGenerate: () -> Unit,
    testTag: String
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = LibraryNavy, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(description, fontSize = 12.sp, color = Slate500)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onGenerate,
                colors = ButtonDefaults.buttonColors(containerColor = LibraryNavy),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(testTag)
            ) {
                Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate & Download PDF")
            }
        }
    }
}
