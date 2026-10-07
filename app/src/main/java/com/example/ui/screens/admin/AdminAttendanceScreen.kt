package com.example.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceEntity
import com.example.data.model.StudentEntity
import com.example.data.repository.LibraryRepository
import com.example.ui.components.MethodBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LibraryAmber
import com.example.ui.theme.LibraryNavy
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.SuccessGreen

@Composable
fun AdminAttendanceScreen(
    attendanceList: List<AttendanceEntity>,
    allStudents: List<StudentEntity>,
    selectedDate: String,
    onDateChange: (String) -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    methodFilter: String,
    onMethodFilterChange: (String) -> Unit,
    onMarkManualAttendance: (studentId: String, date: String, time: String, status: String, onResult: (Boolean, String) -> Unit) -> Unit,
    onMarkCheckOut: (Long) -> Unit,
    onDeleteAttendance: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showManualDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<AttendanceEntity?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
        ) {
            // Filters and Date Selector
            Surface(
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedDate,
                            onValueChange = onDateChange,
                            label = { Text("Filter Date (YYYY-MM-DD)", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = LibraryNavy)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("attendance_date_input")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onDateChange(LibraryRepository.getTodayDate()) },
                            colors = ButtonDefaults.buttonColors(containerColor = LibraryNavy),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_set_today")
                        ) {
                            Text("Today", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = { Text("Search by student name or ID...", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Slate500)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("attendance_search_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Method:", fontSize = 11.sp, color = Slate500, fontWeight = FontWeight.Medium)
                        listOf("All", "QR Scan", "Manual").forEach { filter ->
                            FilterChip(
                                selected = methodFilter == filter,
                                onClick = { onMethodFilterChange(filter) },
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

            // Stats row for currently viewed date
            val qrCount = attendanceList.count { it.method == "QR Scan" }
            val manualCount = attendanceList.count { it.method == "Manual" }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Total Logs: ${attendanceList.size} (QR: $qrCount | Manual: $manualCount)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Slate700
                )
            }

            // Attendance list
            if (attendanceList.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.EventAvailable, contentDescription = null, tint = Slate500, modifier = Modifier.size(52.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No attendance records found for $selectedDate",
                            fontWeight = FontWeight.Bold,
                            color = Slate700
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Students can scan the library QR or you can mark manual attendance below.",
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
                    items(attendanceList) { item ->
                        AttendanceRecordCard(
                            record = item,
                            onCheckOut = { onMarkCheckOut(item.id) },
                            onDelete = { itemToDelete = item }
                        )
                    }
                }
            }
        }

        // FAB to Mark Manual Attendance
        FloatingActionButton(
            onClick = { showManualDialog = true },
            containerColor = SuccessGreen,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_mark_manual_attendance")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Mark Manual Attendance")
        }
    }

    // Manual Attendance Dialog
    if (showManualDialog) {
        ManualAttendanceDialog(
            students = allStudents.filter { it.status == "Active" },
            defaultDate = selectedDate,
            onDismiss = { showManualDialog = false },
            onConfirm = { studentId, date, time, status, callback ->
                onMarkManualAttendance(studentId, date, time, status) { success, msg ->
                    callback(success, msg)
                    if (success) {
                        showManualDialog = false
                    }
                }
            }
        )
    }

    // Delete record dialog
    if (itemToDelete != null) {
        val record = itemToDelete!!
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete Attendance Log", fontWeight = FontWeight.Bold) },
            text = {
                Text("Remove attendance log for '${record.studentName}' (${record.date} at ${record.checkInTime})?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAttendance(record.id)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { itemToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AttendanceRecordCard(
    record: AttendanceEntity,
    onCheckOut: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = record.studentName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Slate900
                    )
                    Text(
                        text = "ID: ${record.studentId} • Date: ${record.date}",
                        fontSize = 12.sp,
                        color = Slate500
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MethodBadge(method = record.method)
                    Spacer(modifier = Modifier.width(6.dp))
                    StatusBadge(status = record.status)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Check-in and Check-out details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Check-In Time", fontSize = 10.sp, color = Slate500)
                        Text(record.checkInTime, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = LibraryNavy)
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Column {
                        Text("Check-Out Time", fontSize = 10.sp, color = Slate500)
                        Text(
                            text = record.checkOutTime ?: "Not recorded",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (record.checkOutTime != null) Slate800 else Slate500
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (record.checkOutTime == null) {
                        OutlinedButton(
                            onClick = onCheckOut,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Check Out", fontSize = 11.sp)
                        }
                    }
                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualAttendanceDialog(
    students: List<StudentEntity>,
    defaultDate: String,
    onDismiss: () -> Unit,
    onConfirm: (studentId: String, date: String, time: String, status: String, (Boolean, String) -> Unit) -> Unit
) {
    var selectedStudentId by remember { mutableStateOf(students.firstOrNull()?.id ?: "") }
    var expandedDropdown by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf(defaultDate) }
    var time by remember { mutableStateOf(LibraryRepository.getCurrentTime()) }
    var status by remember { mutableStateOf("Present") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Mark Manual Attendance", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = LibraryNavy)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (errorMsg != null) {
                    Surface(color = Color(0xFFFEE2E2), shape = RoundedCornerShape(6.dp)) {
                        Text(errorMsg ?: "", color = ErrorRed, fontSize = 11.sp, modifier = Modifier.padding(8.dp))
                    }
                }

                // Student Selection Dropdown
                Text("Select Student:", fontSize = 12.sp, color = Slate700, fontWeight = FontWeight.Medium)
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown }
                ) {
                    val currentStudent = students.find { it.id == selectedStudentId }
                    val label = currentStudent?.let { "${it.fullName} (${it.id})" } ?: "Select a student"
                    OutlinedTextField(
                        value = label,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        students.forEach { s ->
                            DropdownMenuItem(
                                text = { Text("${s.fullName} (${s.id}) - ${s.seatNumber}") },
                                onClick = {
                                    selectedStudentId = s.id
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Check-In Time") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Status:", fontSize = 12.sp, color = Slate700)
                    listOf("Present", "Late", "Excused").forEach { st ->
                        FilterChip(
                            selected = status == st,
                            onClick = { status = st },
                            label = { Text(st, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedStudentId.isBlank()) {
                        errorMsg = "Please select a student."
                        return@Button
                    }
                    onConfirm(selectedStudentId, date, time, status) { success, msg ->
                        if (!success) {
                            errorMsg = msg
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                Text("Record Attendance")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
