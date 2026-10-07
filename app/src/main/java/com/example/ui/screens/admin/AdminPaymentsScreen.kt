package com.example.ui.screens.admin

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.LibrarySettingsEntity
import com.example.data.model.PaymentEntity
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
fun AdminPaymentsScreen(
    payments: List<PaymentEntity>,
    settings: LibrarySettingsEntity,
    selectedFilter: String,
    searchQuery: String,
    onFilterChange: (String) -> Unit,
    onSearchChange: (String) -> Unit,
    onUpdateStatus: (paymentId: String, status: String, adminNote: String) -> Unit,
    onUpdatePaytmSettings: (paytmQrBase64: String?, monthlyFee: Double?, upiId: String?, instructions: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showSettingsDialog by remember { mutableStateOf(false) }
    var viewingScreenshotBase64 by remember { mutableStateOf<String?>(null) }
    var paymentToReject by remember { mutableStateOf<PaymentEntity?>(null) }
    var rejectReason by remember { mutableStateOf("") }

    // Summary calculations
    val totalVerifiedAmount = payments.filter { it.status == "Verified" }.sumOf { it.amount }
    val pendingCount = payments.count { it.status == "Pending" }
    val verifiedCount = payments.count { it.status == "Verified" }
    val rejectedCount = payments.count { it.status == "Rejected" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Header Card with Quick Action to Manage Paytm QR
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = LibraryNavyDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "Payment Management",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "Verified: ₹${totalVerifiedAmount.toInt()} ($verifiedCount approved)",
                                    fontSize = 12.sp,
                                    color = LibraryGold
                                )
                            }

                            Button(
                                onClick = { showSettingsDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = LibraryGold),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("btn_manage_paytm_qr")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCode,
                                    contentDescription = null,
                                    tint = LibraryNavyDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Paytm QR Settings", fontSize = 12.sp, color = LibraryNavyDark, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 2. Metrics Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricChip(
                        title = "Pending",
                        value = "$pendingCount",
                        color = LibraryAmber,
                        modifier = Modifier.weight(1f)
                    )
                    MetricChip(
                        title = "Verified",
                        value = "$verifiedCount",
                        color = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                    MetricChip(
                        title = "Rejected",
                        value = "$rejectedCount",
                        color = ErrorRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3. Search and Filters
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = { Text("Search by Student Name, ID, or Ref No...") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Slate500)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_payment_input")
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val filters = listOf("All", "Pending", "Verified", "Rejected")
                        items(filters) { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { onFilterChange(filter) },
                                label = { Text(filter) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = LibraryNavy,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // 4. Payment Submissions List
            if (payments.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = Slate500,
                                modifier = Modifier.size(42.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No payments found",
                                fontWeight = FontWeight.Bold,
                                color = Slate700
                            )
                            Text(
                                text = "Submitted payment proofs from students will appear here for review.",
                                fontSize = 12.sp,
                                color = Slate500,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(payments) { payment ->
                    AdminPaymentCard(
                        payment = payment,
                        onViewProof = { viewingScreenshotBase64 = payment.screenshotBase64 },
                        onVerify = {
                            onUpdateStatus(payment.id, "Verified", "Verified and approved by Administrator.")
                            Toast.makeText(context, "Payment marked as Verified", Toast.LENGTH_SHORT).show()
                        },
                        onReject = {
                            paymentToReject = payment
                            rejectReason = ""
                        }
                    )
                }
            }
        }
    }

    // Settings Dialog: Configure Owner Paytm QR and Fee Amount
    if (showSettingsDialog) {
        AdminPaytmSettingsDialog(
            settings = settings,
            onDismiss = { showSettingsDialog = false },
            onSave = { qrBase64, fee, upiId, instructions ->
                onUpdatePaytmSettings(qrBase64, fee, upiId, instructions)
                showSettingsDialog = false
                Toast.makeText(context, "Paytm QR & Fee settings updated", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Rejection Reason Dialog
    if (paymentToReject != null) {
        val p = paymentToReject!!
        AlertDialog(
            onDismissRequest = { paymentToReject = null },
            title = { Text("Reject Payment Proof", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter rejection reason for ${p.studentName} (₹${p.amount.toInt()}):", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        placeholder = { Text("e.g. Unreadable screenshot / Transaction not credited") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val reason = rejectReason.trim().ifBlank { "Rejected by Admin" }
                        onUpdateStatus(p.id, "Rejected", reason)
                        paymentToReject = null
                        Toast.makeText(context, "Payment marked as Rejected", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Confirm Rejection", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { paymentToReject = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Full screen screenshot preview dialog
    if (viewingScreenshotBase64 != null) {
        val proofBitmap = remember(viewingScreenshotBase64) { ImageUtils.base64ToBitmap(viewingScreenshotBase64) }
        Dialog(onDismissRequest = { viewingScreenshotBase64 = null }) {
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
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = { viewingScreenshotBase64 = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                    if (proofBitmap != null) {
                        Image(
                            bitmap = proofBitmap.asImageBitmap(),
                            contentDescription = "Full Payment Proof",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(440.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text("Unable to render proof image", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminPaymentCard(
    payment: PaymentEntity,
    onViewProof: () -> Unit,
    onVerify: () -> Unit,
    onReject: () -> Unit
) {
    val statusColor = when (payment.status) {
        "Verified" -> SuccessGreen
        "Rejected" -> ErrorRed
        else -> LibraryAmber
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Student info & Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = payment.studentName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Slate900
                    )
                    Text(
                        text = "ID: ${payment.studentId} • User: ${payment.studentUsername}",
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = payment.status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Payment Details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Text(text = "Amount: ₹${payment.amount.toInt()}", fontWeight = FontWeight.Bold, color = Slate900)
                    Text(text = "Date: ${payment.date} at ${payment.time}", fontSize = 11.sp, color = Slate500)
                    if (payment.transactionRef.isNotBlank()) {
                        Text(text = "UTR/Ref: ${payment.transactionRef}", fontSize = 11.sp, color = Slate700)
                    }
                }

                if (payment.screenshotBase64.isNotBlank()) {
                    OutlinedButton(
                        onClick = onViewProof,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View Proof", fontSize = 11.sp)
                    }
                }
            }

            if (payment.adminNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Review note: ${payment.adminNote}",
                    fontSize = 11.sp,
                    color = Slate700,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (payment.status != "Verified") {
                    Button(
                        onClick = onVerify,
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Verify Payment", fontSize = 12.sp)
                    }
                }

                if (payment.status != "Rejected") {
                    OutlinedButton(
                        onClick = onReject,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reject", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun MetricChip(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
        ) {
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(text = title, fontSize = 11.sp, color = Slate500)
        }
    }
}

@Composable
fun AdminPaytmSettingsDialog(
    settings: LibrarySettingsEntity,
    onDismiss: () -> Unit,
    onSave: (paytmQrBase64: String?, monthlyFee: Double?, upiId: String?, instructions: String?) -> Unit
) {
    val context = LocalContext.current
    var qrBase64 by remember { mutableStateOf(settings.paytmQrCodeBase64) }
    var feeText by remember { mutableStateOf(settings.monthlyFee.toInt().toString()) }
    var upiIdText by remember { mutableStateOf(settings.upiId) }
    var instructionsText by remember { mutableStateOf(settings.paymentInstructions) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val base64 = ImageUtils.uriToBase64(context, uri, maxWidth = 800, maxHeight = 800, quality = 80)
            if (base64 != null) {
                qrBase64 = base64
                Toast.makeText(context, "Paytm QR code image loaded", Toast.LENGTH_SHORT).show()
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
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Paytm QR & Fee Config",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Slate900
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Paytm QR Image Preview / Upload
                Text(text = "Owner's Paytm QR Code Image:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate700)

                val previewBitmap = remember(qrBase64) { ImageUtils.base64ToBitmap(qrBase64) }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                        .border(1.dp, Slate500.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                ) {
                    if (previewBitmap != null) {
                        Image(
                            bitmap = previewBitmap.asImageBitmap(),
                            contentDescription = "Current Paytm QR",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.QrCode, contentDescription = null, tint = Slate500, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("No custom QR image uploaded", fontSize = 11.sp, color = Slate500)
                        }
                    }
                }

                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (qrBase64 != null) "Change Paytm QR Code Photo" else "Upload Paytm QR Code Photo")
                }

                OutlinedTextField(
                    value = feeText,
                    onValueChange = { feeText = it },
                    label = { Text("Monthly Fee Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = upiIdText,
                    onValueChange = { upiIdText = it },
                    label = { Text("Owner UPI ID") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = instructionsText,
                    onValueChange = { instructionsText = it },
                    label = { Text("Payment Instructions for Students") },
                    shape = RoundedCornerShape(8.dp),
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        val fee = feeText.toDoubleOrNull() ?: settings.monthlyFee
                        onSave(qrBase64, fee, upiIdText.trim(), instructionsText.trim())
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LibraryNavy),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text("Save & Publish to Students")
                }
            }
        }
    }
}
