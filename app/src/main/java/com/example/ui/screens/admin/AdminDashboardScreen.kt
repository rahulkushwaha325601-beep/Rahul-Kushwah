package com.example.ui.screens.admin

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceEntity
import com.example.data.model.StudentEntity
import com.example.data.repository.LibraryRepository
import com.example.ui.components.MethodBadge
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LibraryAmber
import com.example.ui.theme.LibraryGold
import com.example.ui.theme.LibraryNavy
import com.example.ui.theme.LibraryNavyDark
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.SuccessGreen

@Composable
fun AdminDashboardScreen(
    students: List<StudentEntity>,
    attendance: List<AttendanceEntity>,
    onNavigateToAddStudent: () -> Unit,
    onNavigateToQr: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToReports: () -> Unit,
    modifier: Modifier = Modifier
) {
    val todayDate = LibraryRepository.getTodayDate()
    val todayAttendance = attendance.filter { it.date == todayDate }
    val totalStudents = students.size
    val activeStudents = students.count { it.status == "Active" }
    val presentToday = todayAttendance.size
    val absentToday = (activeStudents - presentToday).coerceAtLeast(0)
    val attendancePercentage = if (activeStudents > 0) {
        ((presentToday.toFloat() / activeStudents.toFloat()) * 100).toInt()
    } else 0

    val recentAttendance = attendance.take(5)
    val recentStudents = students.take(4)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome & Date Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LibraryNavyDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Admin Control Center",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Today: $todayDate • R.S Library",
                            fontSize = 12.sp,
                            color = LibraryGold
                        )
                    }
                    Surface(
                        color = LibraryAmber,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "LIVE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Quick Action Buttons Row
        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Slate800
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Button(
                        onClick = onNavigateToAddStudent,
                        colors = ButtonDefaults.buttonColors(containerColor = LibraryNavy),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("action_add_student")
                    ) {
                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Student", fontSize = 12.sp)
                    }
                }
                item {
                    Button(
                        onClick = onNavigateToQr,
                        colors = ButtonDefaults.buttonColors(containerColor = LibraryAmber),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("action_generate_qr")
                    ) {
                        Icon(imageVector = Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Library QR Code", fontSize = 12.sp)
                    }
                }
                item {
                    Button(
                        onClick = onNavigateToAttendance,
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("action_mark_attendance")
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Attendance Sheet", fontSize = 12.sp)
                    }
                }
                item {
                    OutlinedButton(
                        onClick = onNavigateToReports,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("action_download_pdf")
                    ) {
                        Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download PDF", fontSize = 12.sp)
                    }
                }
            }
        }

        // 2x2 Grid of Stat Cards
        item {
            Text(
                text = "Attendance & Student Metrics",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Slate800
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Students",
                    value = totalStudents.toString(),
                    icon = Icons.Default.Group,
                    iconTint = LibraryNavy,
                    subText = "$activeStudents Active Members",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Today's Present",
                    value = presentToday.toString(),
                    icon = Icons.Default.HowToReg,
                    iconTint = SuccessGreen,
                    subText = "${todayAttendance.count { it.method == "QR Scan" }} via QR Scan",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Today's Absent",
                    value = absentToday.toString(),
                    icon = Icons.Default.EventBusy,
                    iconTint = ErrorRed,
                    subText = "Of $activeStudents Active Students",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Attendance Rate",
                    value = "$attendancePercentage%",
                    icon = Icons.Default.PieChart,
                    iconTint = LibraryAmber,
                    subText = "Total Logs: ${attendance.size}",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Progress Bar Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Today's Attendance Rate",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Slate800
                        )
                        Text(
                            text = "$presentToday / $activeStudents Students ($attendancePercentage%)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = LibraryNavy
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { (attendancePercentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = if (attendancePercentage >= 75) SuccessGreen else LibraryAmber,
                        trackColor = Slate200
                    )
                }
            }
        }

        // Recent Attendance List
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Recent Attendance Check-Ins",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Slate800
                )
                OutlinedButton(
                    onClick = onNavigateToAttendance,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text("View All", fontSize = 11.sp)
                }
            }
        }

        if (recentAttendance.isEmpty()) {
            item {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No attendance logs recorded yet.",
                        color = Slate500,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(18.dp)
                    )
                }
            }
        } else {
            items(recentAttendance) { item ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(LibraryNavy.copy(alpha = 0.1f))
                            ) {
                                Text(
                                    text = item.studentName.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = LibraryNavy
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = item.studentName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Slate900
                                )
                                Text(
                                    text = "${item.studentId} • ${item.checkInTime} (${item.date})",
                                    fontSize = 11.sp,
                                    color = Slate500
                                )
                            }
                        }
                        MethodBadge(method = item.method)
                    }
                }
            }
        }

        // Recent Registrations
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Recent Registered Students",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Slate800
                )
                OutlinedButton(
                    onClick = onNavigateToAddStudent,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text("+ Add New", fontSize = 11.sp)
                }
            }
        }

        items(recentStudents) { student ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = student.fullName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Slate900
                        )
                        Text(
                            text = "${student.id} • ${student.seatNumber} • ${student.course}",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                    StatusBadge(status = student.status)
                }
            }
        }
    }
}
