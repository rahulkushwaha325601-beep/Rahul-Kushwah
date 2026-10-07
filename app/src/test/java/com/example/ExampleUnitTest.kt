package com.example

import com.example.data.security.SecurityUtils
import com.example.qr.ParsedQrResult
import com.example.qr.QrCodeGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testAdminCredentialsVerification() {
        assertEquals("Raman1998", SecurityUtils.ADMIN_USERNAME)
        assertTrue(SecurityUtils.verifyPassword("Rahul328650@#", SecurityUtils.ADMIN_PASSWORD_HASH))
        assertFalse(SecurityUtils.verifyPassword("WrongPassword123", SecurityUtils.ADMIN_PASSWORD_HASH))
        assertFalse(SecurityUtils.verifyPassword("admin", SecurityUtils.ADMIN_PASSWORD_HASH))
    }

    @Test
    fun testStudentPasswordHashingAndVerification() {
        val studentPassword = "secretPassword456!"
        val hash = SecurityUtils.hashPassword(studentPassword)
        assertTrue(SecurityUtils.verifyPassword(studentPassword, hash))
        assertFalse(SecurityUtils.verifyPassword("wrongPassword", hash))
    }

    @Test
    fun testOfficialQrPayloadFormattingAndParsing() {
        val token = "RSL-ATTENDANCE-TEST123"
        val libName = "R.S Library"
        val payload = QrCodeGenerator.formatOfficialReceptionQr(token, libName)
        assertEquals("RSLIBRARY:OFFICIAL:RSL-ATTENDANCE-TEST123:R.S Library", payload)

        val parsed = QrCodeGenerator.parseQrCode(payload)
        assertTrue(parsed is ParsedQrResult.OfficialReception)
        assertEquals(token, (parsed as ParsedQrResult.OfficialReception).token)
    }

    @Test
    fun testStudentQrPayloadFormattingAndParsing() {
        val studentId = "RSL-101"
        val studentName = "Aarav Sharma"
        val payload = QrCodeGenerator.formatStudentQr(studentId, studentName)
        assertEquals("RSLIBRARY:STUDENT:RSL-101:Aarav Sharma", payload)

        val parsed = QrCodeGenerator.parseQrCode(payload)
        assertTrue(parsed is ParsedQrResult.StudentBadge)
        val badge = parsed as ParsedQrResult.StudentBadge
        assertEquals(studentId, badge.studentId)
        assertEquals(studentName, badge.studentName)
    }

    @Test
    fun testDirectTokenParsing() {
        val directToken = "RSL-ATTENDANCE-ABCDEF12"
        val parsed = QrCodeGenerator.parseQrCode(directToken)
        assertTrue(parsed is ParsedQrResult.OfficialReception)
        assertEquals(directToken, (parsed as ParsedQrResult.OfficialReception).token)
    }

    @Test
    fun testStudentEntityAttributes() {
        val student = com.example.data.model.StudentEntity(
            id = "RSL-201",
            username = "student201",
            passwordHash = SecurityUtils.hashPassword("pass123"),
            fullName = "Rohit Kumar",
            parentName = "Suresh Kumar",
            mobileNumber = "9876543210",
            email = "rohit@example.com",
            address = "Civil Lines, Delhi",
            joiningDate = "2026-10-06",
            seatNumber = "Seat B-04",
            course = "SSC CGL",
            status = "Active"
        )
        assertEquals("RSL-201", student.id)
        assertEquals("student201", student.username)
        assertEquals("Rohit Kumar", student.fullName)
        assertEquals("Active", student.status)
        assertEquals("RSL-STUDENT-RSL-201", student.studentQrToken)
    }

    @Test
    fun testAttendanceEntityAttributes() {
        val attendance = com.example.data.model.AttendanceEntity(
            studentId = "RSL-201",
            studentName = "Rohit Kumar",
            date = "2026-10-06",
            checkInTime = "09:30 AM",
            status = "Present",
            method = "QR Scan"
        )
        assertEquals("RSL-201", attendance.studentId)
        assertEquals("Rohit Kumar", attendance.studentName)
        assertEquals("2026-10-06", attendance.date)
        assertEquals("09:30 AM", attendance.checkInTime)
        assertEquals("Present", attendance.status)
        assertEquals("QR Scan", attendance.method)
    }
}
