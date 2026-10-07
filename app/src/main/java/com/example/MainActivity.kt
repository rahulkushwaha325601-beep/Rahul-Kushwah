package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pdf.PdfReportGenerator
import com.example.ui.components.AttendanceResultDialog
import com.example.ui.components.LibraryHeaderBar
import com.example.ui.components.StudentQrCodeDialog
import com.example.ui.screens.admin.AdminAttendanceScreen
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.admin.AdminPaymentsScreen
import com.example.ui.screens.admin.AdminQrScreen
import com.example.ui.screens.admin.AdminReportsScreen
import com.example.ui.screens.admin.AdminSettingsScreen
import com.example.ui.screens.admin.AdminStudentsScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.student.StudentAttendanceScreen
import com.example.ui.screens.student.StudentDashboardScreen
import com.example.ui.screens.student.StudentPaymentScreen
import com.example.ui.screens.student.StudentProfileScreen
import com.example.ui.screens.student.StudentScanQrScreen
import com.example.ui.theme.LibraryAmber
import com.example.ui.theme.LibraryGold
import com.example.ui.theme.LibraryNavy
import com.example.ui.theme.LibraryNavyDark
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.viewmodel.AdminTab
import com.example.ui.viewmodel.LibraryViewModel
import com.example.ui.viewmodel.StudentTab
import com.example.ui.viewmodel.UserRole

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent()
            }
        }
    }
}

@Composable
fun MainAppContent(
    viewModel: LibraryViewModel = viewModel()
) {
    val context = LocalContext.current
    val userRole by viewModel.currentUserRole.collectAsStateWithLifecycle()
    val loggedInStudent by viewModel.loggedInStudent.collectAsStateWithLifecycle()
    val settings by viewModel.settingsFlow.collectAsStateWithLifecycle()

    val adminTab by viewModel.adminTab.collectAsStateWithLifecycle()
    val studentTab by viewModel.studentTab.collectAsStateWithLifecycle()

    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()
    val filteredStudents by viewModel.filteredStudents.collectAsStateWithLifecycle()
    val allAttendance by viewModel.allAttendance.collectAsStateWithLifecycle()
    val filteredAttendance by viewModel.filteredAttendance.collectAsStateWithLifecycle()

    val studentSearchQuery by viewModel.studentSearchQuery.collectAsStateWithLifecycle()
    val studentStatusFilter by viewModel.studentStatusFilter.collectAsStateWithLifecycle()

    val attendanceDateFilter by viewModel.attendanceDateFilter.collectAsStateWithLifecycle()
    val attendanceSearchQuery by viewModel.attendanceSearchQuery.collectAsStateWithLifecycle()
    val attendanceMethodFilter by viewModel.attendanceMethodFilter.collectAsStateWithLifecycle()

    val filteredPayments by viewModel.filteredPayments.collectAsStateWithLifecycle()
    val studentPayments by viewModel.studentPayments.collectAsStateWithLifecycle()
    val paymentFilterStatus by viewModel.paymentFilterStatus.collectAsStateWithLifecycle()
    val paymentSearchQuery by viewModel.paymentSearchQuery.collectAsStateWithLifecycle()

    val attendanceDialogState by viewModel.attendanceDialogState.collectAsStateWithLifecycle()
    val selectedStudentForQr by viewModel.selectedStudentForQr.collectAsStateWithLifecycle()
    val officialQrBitmap by viewModel.officialQrBitmap.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val syncStatusMessage by viewModel.syncStatusMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.statusMessage.value = null
        }
    }

    LaunchedEffect(syncStatusMessage) {
        syncStatusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.syncStatusMessage.value = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        when (userRole) {
            UserRole.NONE -> {
                LoginScreen(
                    onAdminLogin = { username, password, callback ->
                        viewModel.loginAsAdmin(username, password, callback)
                    },
                    onStudentLogin = { username, password, callback ->
                        viewModel.loginAsStudent(username, password, callback)
                    },
                    onSync = { viewModel.syncWithCloud() },
                    isSyncing = isSyncing
                )
            }

            UserRole.ADMIN -> {
                BackHandler {
                    if (adminTab != AdminTab.DASHBOARD) {
                        viewModel.setAdminTab(AdminTab.DASHBOARD)
                    } else {
                        viewModel.logout()
                    }
                }

                Scaffold(
                    topBar = {
                        LibraryHeaderBar(
                            title = settings.libraryName,
                            subtitle = settings.tagline,
                            roleLabel = "OWNER / ADMIN",
                            onSync = { viewModel.syncWithCloud() },
                            isSyncing = isSyncing,
                            onLogout = { viewModel.logout() }
                        )
                    },
                    bottomBar = {
                        AdminBottomNavigationBar(
                            currentTab = adminTab,
                            onTabSelected = { viewModel.setAdminTab(it) }
                        )
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (adminTab) {
                            AdminTab.DASHBOARD -> AdminDashboardScreen(
                                students = allStudents,
                                attendance = allAttendance,
                                onNavigateToAddStudent = { viewModel.setAdminTab(AdminTab.STUDENTS) },
                                onNavigateToQr = { viewModel.setAdminTab(AdminTab.QR_SYSTEM) },
                                onNavigateToAttendance = { viewModel.setAdminTab(AdminTab.ATTENDANCE) },
                                onNavigateToReports = { viewModel.setAdminTab(AdminTab.REPORTS) }
                            )

                            AdminTab.STUDENTS -> AdminStudentsScreen(
                                students = filteredStudents,
                                searchQuery = studentSearchQuery,
                                onSearchQueryChange = { viewModel.studentSearchQuery.value = it },
                                statusFilter = studentStatusFilter,
                                onStatusFilterChange = { viewModel.studentStatusFilter.value = it },
                                onAddOrUpdateStudent = { student, plainPassword, isEdit, cb ->
                                    viewModel.addOrUpdateStudent(student, plainPassword, isEdit, cb)
                                },
                                onDeleteStudent = { student, cb ->
                                    viewModel.deleteStudent(student, cb)
                                },
                                onResetStudentPassword = { studentId, newPass, cb ->
                                    viewModel.resetStudentPassword(studentId, newPass, cb)
                                },
                                onShowStudentQr = { student ->
                                    viewModel.selectedStudentForQr.value = student
                                },
                                onDownloadStudentPdf = { student ->
                                    viewModel.exportIndividualStudentPdf(student) { file ->
                                        if (file != null) {
                                            val intent = PdfReportGenerator.createSharePdfIntent(context, file)
                                            context.startActivity(Intent.createChooser(intent, "Share Student Dossier"))
                                        }
                                    }
                                },
                                defaultNextStudentId = viewModel.generateNextStudentId()
                            )

                            AdminTab.ATTENDANCE -> AdminAttendanceScreen(
                                attendanceList = filteredAttendance,
                                allStudents = allStudents,
                                selectedDate = attendanceDateFilter,
                                onDateChange = { viewModel.attendanceDateFilter.value = it },
                                searchQuery = attendanceSearchQuery,
                                onSearchChange = { viewModel.attendanceSearchQuery.value = it },
                                methodFilter = attendanceMethodFilter,
                                onMethodFilterChange = { viewModel.attendanceMethodFilter.value = it },
                                onMarkManualAttendance = { id, d, t, s, cb ->
                                    viewModel.markManualAttendance(id, d, t, s, cb)
                                },
                                onMarkCheckOut = { id -> viewModel.markCheckOut(id) },
                                onDeleteAttendance = { id -> viewModel.deleteAttendanceRecord(id) }
                            )

                            AdminTab.PAYMENTS -> AdminPaymentsScreen(
                                payments = filteredPayments,
                                settings = settings,
                                selectedFilter = paymentFilterStatus,
                                searchQuery = paymentSearchQuery,
                                onFilterChange = { viewModel.paymentFilterStatus.value = it },
                                onSearchChange = { viewModel.paymentSearchQuery.value = it },
                                onUpdateStatus = { id, status, note ->
                                    viewModel.updatePaymentStatus(id, status, note)
                                },
                                onUpdatePaytmSettings = { qr, fee, upi, instr ->
                                    viewModel.updatePaytmQrAndFee(qr, fee, upi, instr)
                                }
                            )

                            AdminTab.QR_SYSTEM -> AdminQrScreen(
                                settings = settings,
                                qrBitmap = officialQrBitmap,
                                onRegenerateToken = { cb ->
                                    viewModel.regenerateOfficialQrToken(cb)
                                },
                                onToggleActive = { active ->
                                    viewModel.toggleQrActive(active)
                                }
                            )

                            AdminTab.REPORTS -> AdminReportsScreen(
                                students = allStudents,
                                onExportAllStudentsPdf = { cb ->
                                    viewModel.exportAllStudentsPdf { file ->
                                        if (file != null) cb(file)
                                    }
                                },
                                onExportAttendancePdf = { date, cb ->
                                    viewModel.exportAttendanceReportPdf(date) { file ->
                                        if (file != null) cb(file)
                                    }
                                },
                                onExportIndividualPdf = { student, cb ->
                                    viewModel.exportIndividualStudentPdf(student) { file ->
                                        if (file != null) cb(file)
                                    }
                                }
                            )

                            AdminTab.SETTINGS -> AdminSettingsScreen(
                                settings = settings,
                                onSaveSettings = { updated, cb ->
                                    viewModel.updateLibrarySettings(updated) { success ->
                                        if (success) cb()
                                    }
                                }
                            )
                        }
                    }
                }
            }

            UserRole.STUDENT -> {
                val student = loggedInStudent
                if (student != null) {
                    val myAttendance = allAttendance.filter { it.studentId == student.id }

                    BackHandler {
                        if (studentTab != StudentTab.DASHBOARD) {
                            viewModel.setStudentTab(StudentTab.DASHBOARD)
                        } else {
                            viewModel.logout()
                        }
                    }

                    Scaffold(
                        topBar = {
                            LibraryHeaderBar(
                                title = settings.libraryName,
                                subtitle = "Student Portal • ${student.fullName}",
                                roleLabel = "STUDENT",
                                onSync = { viewModel.syncWithCloud() },
                                isSyncing = isSyncing,
                                onLogout = { viewModel.logout() }
                            )
                        },
                        bottomBar = {
                            StudentBottomNavigationBar(
                                currentTab = studentTab,
                                onTabSelected = { viewModel.setStudentTab(it) }
                            )
                        },
                        snackbarHost = { SnackbarHost(snackbarHostState) }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (studentTab) {
                                StudentTab.DASHBOARD -> StudentDashboardScreen(
                                    student = student,
                                    attendanceList = myAttendance,
                                    onNavigateToScanQr = { viewModel.setStudentTab(StudentTab.SCAN_QR) },
                                    onNavigateToAttendanceHistory = { viewModel.setStudentTab(StudentTab.MY_ATTENDANCE) },
                                    onNavigateToProfile = { viewModel.setStudentTab(StudentTab.MY_PROFILE) },
                                    onNavigateToPayment = { viewModel.setStudentTab(StudentTab.PAYMENT) }
                                )

                                StudentTab.SCAN_QR -> StudentScanQrScreen(
                                    student = student,
                                    officialTokenHint = settings.officialQrToken,
                                    onQrDetected = { scannedRaw ->
                                        viewModel.markAttendanceFromQr(scannedRaw, student.id)
                                    }
                                )

                                StudentTab.MY_ATTENDANCE -> StudentAttendanceScreen(
                                    student = student,
                                    attendanceList = myAttendance,
                                    onMarkCheckOut = { id -> viewModel.markCheckOut(id) }
                                )

                                StudentTab.PAYMENT -> StudentPaymentScreen(
                                    student = student,
                                    settings = settings,
                                    payments = studentPayments,
                                    onSubmitPayment = { amount, ref, screenshot, cb ->
                                        viewModel.submitStudentPayment(amount, ref, screenshot, cb)
                                    }
                                )

                                StudentTab.MY_PROFILE -> StudentProfileScreen(
                                    student = student,
                                    onDownloadPdf = {
                                        viewModel.exportIndividualStudentPdf(student) { file ->
                                            val intent = PdfReportGenerator.createSharePdfIntent(context, file)
                                            context.startActivity(Intent.createChooser(intent, "Share My Attendance Dossier"))
                                        }
                                    },
                                    onLogout = { viewModel.logout() },
                                    onUpdateProfile = { fullName, parentName, mobile, email, dob, address, course, emergency, frontBase64, backBase64, cb ->
                                        viewModel.updateStudentProfileAndDocuments(
                                            fullName, parentName, mobile, email, dob, address, course, emergency, frontBase64, backBase64, cb
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Attendance Result Dialog (Common for both Admin & Student flows)
        AttendanceResultDialog(
            isOpen = attendanceDialogState.isOpen,
            title = attendanceDialogState.title,
            message = attendanceDialogState.message,
            isSuccess = attendanceDialogState.isSuccess,
            attendance = attendanceDialogState.attendance,
            onDismiss = { viewModel.dismissAttendanceDialog() }
        )

        // Student's Personal QR Code Dialog (for Admin inspection)
        StudentQrCodeDialog(
            student = selectedStudentForQr,
            onDismiss = { viewModel.selectedStudentForQr.value = null },
            onShare = {}
        )
    }
}

@Composable
fun AdminBottomNavigationBar(
    currentTab: AdminTab,
    onTabSelected: (AdminTab) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = LibraryNavy,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentTab == AdminTab.DASHBOARD,
            onClick = { onTabSelected(AdminTab.DASHBOARD) },
            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
            label = { Text("Home", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = LibraryNavyDark,
                indicatorColor = Color(0xFFE0E7FF),
                unselectedIconColor = Slate500,
                selectedTextColor = LibraryNavyDark,
                unselectedTextColor = Slate500
            ),
            modifier = Modifier.testTag("nav_admin_dashboard")
        )

        NavigationBarItem(
            selected = currentTab == AdminTab.STUDENTS,
            onClick = { onTabSelected(AdminTab.STUDENTS) },
            icon = { Icon(Icons.Default.Group, contentDescription = "Students") },
            label = { Text("Students", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = LibraryNavyDark,
                indicatorColor = Color(0xFFE0E7FF),
                unselectedIconColor = Slate500,
                selectedTextColor = LibraryNavyDark,
                unselectedTextColor = Slate500
            ),
            modifier = Modifier.testTag("nav_admin_students")
        )

        NavigationBarItem(
            selected = currentTab == AdminTab.ATTENDANCE,
            onClick = { onTabSelected(AdminTab.ATTENDANCE) },
            icon = { Icon(Icons.Default.Assignment, contentDescription = "Attendance") },
            label = { Text("Attendance", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = LibraryNavyDark,
                indicatorColor = Color(0xFFE0E7FF),
                unselectedIconColor = Slate500,
                selectedTextColor = LibraryNavyDark,
                unselectedTextColor = Slate500
            ),
            modifier = Modifier.testTag("nav_admin_attendance")
        )

        NavigationBarItem(
            selected = currentTab == AdminTab.QR_SYSTEM,
            onClick = { onTabSelected(AdminTab.QR_SYSTEM) },
            icon = { Icon(Icons.Default.QrCode, contentDescription = "Library QR") },
            label = { Text("QR Code", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = LibraryNavyDark,
                indicatorColor = Color(0xFFE0E7FF),
                unselectedIconColor = Slate500,
                selectedTextColor = LibraryNavyDark,
                unselectedTextColor = Slate500
            ),
            modifier = Modifier.testTag("nav_admin_qr")
        )

        NavigationBarItem(
            selected = currentTab == AdminTab.REPORTS,
            onClick = { onTabSelected(AdminTab.REPORTS) },
            icon = { Icon(Icons.Default.Description, contentDescription = "PDF Reports") },
            label = { Text("Reports", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = LibraryNavyDark,
                indicatorColor = Color(0xFFE0E7FF),
                unselectedIconColor = Slate500,
                selectedTextColor = LibraryNavyDark,
                unselectedTextColor = Slate500
            ),
            modifier = Modifier.testTag("nav_admin_reports")
        )

        NavigationBarItem(
            selected = currentTab == AdminTab.SETTINGS,
            onClick = { onTabSelected(AdminTab.SETTINGS) },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = LibraryNavyDark,
                indicatorColor = Color(0xFFE0E7FF),
                unselectedIconColor = Slate500,
                selectedTextColor = LibraryNavyDark,
                unselectedTextColor = Slate500
            ),
            modifier = Modifier.testTag("nav_admin_settings")
        )
    }
}

@Composable
fun StudentBottomNavigationBar(
    currentTab: StudentTab,
    onTabSelected: (StudentTab) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = LibraryNavy,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentTab == StudentTab.DASHBOARD,
            onClick = { onTabSelected(StudentTab.DASHBOARD) },
            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
            label = { Text("Dashboard", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = LibraryNavyDark,
                indicatorColor = Color(0xFFE0E7FF),
                unselectedIconColor = Slate500,
                selectedTextColor = LibraryNavyDark,
                unselectedTextColor = Slate500
            ),
            modifier = Modifier.testTag("nav_student_dashboard")
        )

        NavigationBarItem(
            selected = currentTab == StudentTab.SCAN_QR,
            onClick = { onTabSelected(StudentTab.SCAN_QR) },
            icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR") },
            label = { Text("Scan QR", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = LibraryNavyDark,
                indicatorColor = Color(0xFFE0E7FF),
                unselectedIconColor = Slate500,
                selectedTextColor = LibraryNavyDark,
                unselectedTextColor = Slate500
            ),
            modifier = Modifier.testTag("nav_student_scan_qr")
        )

        NavigationBarItem(
            selected = currentTab == StudentTab.MY_ATTENDANCE,
            onClick = { onTabSelected(StudentTab.MY_ATTENDANCE) },
            icon = { Icon(Icons.Default.EventNote, contentDescription = "My Attendance") },
            label = { Text("Attendance", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = LibraryNavyDark,
                indicatorColor = Color(0xFFE0E7FF),
                unselectedIconColor = Slate500,
                selectedTextColor = LibraryNavyDark,
                unselectedTextColor = Slate500
            ),
            modifier = Modifier.testTag("nav_student_attendance")
        )

        NavigationBarItem(
            selected = currentTab == StudentTab.MY_PROFILE,
            onClick = { onTabSelected(StudentTab.MY_PROFILE) },
            icon = { Icon(Icons.Default.Person, contentDescription = "My Profile") },
            label = { Text("My Profile", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = LibraryNavyDark,
                indicatorColor = Color(0xFFE0E7FF),
                unselectedIconColor = Slate500,
                selectedTextColor = LibraryNavyDark,
                unselectedTextColor = Slate500
            ),
            modifier = Modifier.testTag("nav_student_profile")
        )
    }
}
