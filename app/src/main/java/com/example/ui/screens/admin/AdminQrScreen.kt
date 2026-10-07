package com.example.ui.screens.admin

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.model.LibrarySettingsEntity
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
import java.io.File
import java.io.FileOutputStream

@Composable
fun AdminQrScreen(
    settings: LibrarySettingsEntity,
    qrBitmap: Bitmap?,
    onRegenerateToken: ((String) -> Unit) -> Unit,
    onToggleActive: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showRegenerateConfirm by remember { mutableStateOf(false) }
    var notificationMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Security Banner
        Surface(
            color = LibraryNavyDark,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = LibraryGold,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Official Reception QR Security",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Only Owner/Admin can generate or modify this attendance QR code. Students cannot create or tamper with this code.",
                        color = LibraryGold.copy(alpha = 0.9f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Official QR Reception Poster Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header inside poster
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = LibraryNavy,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = settings.libraryName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Serif
                        ),
                        color = LibraryNavyDark
                    )
                }
                Text(
                    text = "OFFICIAL ATTENDANCE CHECK-IN QR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = LibraryAmber
                )
                Text(
                    text = "Display at Reception / Entrance Gate",
                    fontSize = 11.sp,
                    color = Slate500
                )

                Spacer(modifier = Modifier.height(14.dp))

                // The QR Code Image Box
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(240.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(
                            2.dp,
                            if (settings.isQrActive) LibraryNavy else ErrorRed,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    if (settings.isQrActive && qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "Official Attendance QR Code",
                            modifier = Modifier.size(216.dp)
                        )
                    } else if (!settings.isQrActive) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = ErrorRed,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "QR Code is Disabled",
                                fontWeight = FontWeight.Bold,
                                color = ErrorRed
                            )
                            Text(
                                text = "Enable below to allow students to scan.",
                                fontSize = 11.sp,
                                color = Slate500,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Text("Generating QR Code...")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Instructions strip
                Surface(
                    color = Slate100,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Text(
                            text = "How Students Mark Attendance:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Slate800
                        )
                        Text(
                            text = "1. Open R.S Library App > 2. Tap 'Scan QR' > 3. System verifies student & records check-in.",
                            fontSize = 10.sp,
                            color = Slate700,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Active Token: ${settings.officialQrToken}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate700
                )
                Text(
                    text = "Last Generated: ${settings.qrGeneratedDate}",
                    fontSize = 10.sp,
                    color = Slate500
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Controls Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "QR Code Controls",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Slate900
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Active / Inactive Switch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Attendance QR Status",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Slate800
                        )
                        Text(
                            text = if (settings.isQrActive) "Active • Students can check in" else "Disabled • Scans will be rejected",
                            fontSize = 11.sp,
                            color = if (settings.isQrActive) SuccessGreen else ErrorRed
                        )
                    }
                    Switch(
                        checked = settings.isQrActive,
                        onCheckedChange = { onToggleActive(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SuccessGreen
                        ),
                        modifier = Modifier.testTag("switch_qr_active")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Regenerate QR Button
                OutlinedButton(
                    onClick = { showRegenerateConfirm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_regenerate_qr")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Regenerate QR Token (Invalidates Old QR)")
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Share / Download QR
                Button(
                    onClick = {
                        if (qrBitmap != null) {
                            shareQrBitmap(context, qrBitmap, settings.libraryName)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LibraryNavy),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_share_qr")
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share / Print Reception QR Poster")
                }
            }
        }
    }

    // Confirmation dialog for regenerating token
    if (showRegenerateConfirm) {
        AlertDialog(
            onDismissRequest = { showRegenerateConfirm = false },
            title = { Text("Regenerate Official QR Code?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This will create a new security token. Any previously printed QR code will no longer work, preventing unauthorized or saved scans."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRegenerateToken { newToken ->
                            showRegenerateConfirm = false
                            notificationMessage = "New QR code generated: $newToken"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LibraryAmber)
                ) {
                    Text("Generate New QR")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRegenerateConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Success Notification Dialog
    if (notificationMessage != null) {
        AlertDialog(
            onDismissRequest = { notificationMessage = null },
            title = { Text("Success", fontWeight = FontWeight.Bold) },
            text = { Text(notificationMessage ?: "") },
            confirmButton = {
                Button(onClick = { notificationMessage = null }) {
                    Text("OK")
                }
            }
        )
    }
}

private fun shareQrBitmap(context: Context, bitmap: Bitmap, libraryName: String) {
    try {
        val file = File(context.cacheDir, "RS_Library_Reception_QR.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()

        val uri = FileProvider.getUriForFile(context, "com.example.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "$libraryName - Official Attendance QR Code")
            putExtra(Intent.EXTRA_TEXT, "Official attendance QR code for $libraryName reception desk.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Reception QR Code"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
