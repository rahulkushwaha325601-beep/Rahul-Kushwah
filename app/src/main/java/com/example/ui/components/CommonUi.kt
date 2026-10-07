package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceEntity
import com.example.data.model.StudentEntity
import com.example.qr.QrCodeGenerator
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.ErrorRedLight
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
import com.example.ui.theme.SuccessGreenLight

@Composable
fun LibraryHeaderBar(
    title: String = "R.S LIBRARY",
    subtitle: String = "Study • Focus • Succeed",
    roleLabel: String? = null,
    onSync: (() -> Unit)? = null,
    isSyncing: Boolean = false,
    onLogout: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        color = LibraryNavyDark,
        shadowElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Library Emblem Icon
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(LibraryNavy)
                        .border(1.dp, LibraryGold, RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Library Logo",
                        tint = LibraryGold,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp,
                                fontFamily = FontFamily.Serif
                            ),
                            color = Color.White
                        )
                        if (roleLabel != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = LibraryAmber,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = roleLabel,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = LibraryGold.copy(alpha = 0.9f),
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onSync != null) {
                    IconButton(
                        onClick = onSync,
                        modifier = Modifier.testTag("sync_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync Cloud",
                            tint = if (isSyncing) LibraryGold else Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
                if (onLogout != null) {
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    containerColor: Color = Color.White,
    modifier: Modifier = Modifier,
    subText: String? = null
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate700,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.12f))
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = Slate800
            )
            if (subText != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subText,
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate500
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bg, txt) = when (status) {
        "Present" -> SuccessGreenLight to SuccessGreen
        "Active" -> SuccessGreenLight to SuccessGreen
        "Inactive" -> ErrorRedLight to ErrorRed
        "Absent" -> ErrorRedLight to ErrorRed
        else -> Slate200 to Slate700
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.border(0.5.dp, txt.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
    ) {
        Text(
            text = status,
            color = txt,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun MethodBadge(method: String) {
    val isQr = method.contains("QR", ignoreCase = true)
    Surface(
        color = if (isQr) Color(0xFFEDE9FE) else Slate100,
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.border(0.5.dp, if (isQr) Color(0xFF8B5CF6) else Slate500, RoundedCornerShape(6.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = if (isQr) Icons.Default.QrCode else Icons.Default.Info,
                contentDescription = null,
                tint = if (isQr) Color(0xFF7C3AED) else Slate700,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = method,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = if (isQr) Color(0xFF6D28D9) else Slate700
            )
        }
    }
}

@Composable
fun AttendanceResultDialog(
    isOpen: Boolean,
    title: String,
    message: String,
    isSuccess: Boolean,
    attendance: AttendanceEntity?,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isSuccess) SuccessGreen else ErrorRed,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            }
        },
        text = {
            Column {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate700
                )
                if (attendance != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = Slate100,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Student: ${attendance.studentName} (${attendance.studentId})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate900
                            )
                            Text(
                                text = "Date: ${attendance.date} | Time: ${attendance.checkInTime}",
                                fontSize = 11.sp,
                                color = Slate700
                            )
                            Text(
                                text = "Method: ${attendance.method}",
                                fontSize = 11.sp,
                                color = Slate700
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSuccess) SuccessGreen else LibraryNavy
                ),
                modifier = Modifier.testTag("dialog_confirm_button")
            ) {
                Text("OK", color = Color.White)
            }
        }
    )
}

@Composable
fun StudentQrCodeDialog(
    student: StudentEntity?,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    if (student == null) return

    val payload = QrCodeGenerator.formatStudentQr(student.id, student.fullName)
    val bitmap = QrCodeGenerator.generateQrBitmap(payload, width = 400, height = 400)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Student ID QR Code",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = student.fullName,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = LibraryNavy
                )
                Text(
                    text = "ID: ${student.id} • Seat: ${student.seatNumber}",
                    fontSize = 12.sp,
                    color = Slate700
                )
                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(220.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(2.dp, LibraryNavy, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Student QR Code",
                            modifier = Modifier.size(200.dp)
                        )
                    } else {
                        Text("QR Code generation error")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Official R.S Library Student Pass\nAuthorized for Entrance & Attendance",
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp,
                    color = Slate500
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = LibraryNavy)
            ) {
                Text("Done")
            }
        }
    )
}
