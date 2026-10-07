package com.example.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.AttendanceEntity
import com.example.data.model.LibrarySettingsEntity
import com.example.data.model.StudentEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    private const val PAGE_WIDTH = 595 // Standard A4 width in points
    private const val PAGE_HEIGHT = 842 // Standard A4 height in points

    /**
     * Generates a PDF for all students in the library directory.
     */
    fun generateAllStudentsPdf(
        context: Context,
        settings: LibrarySettingsEntity,
        students: List<StudentEntity>
    ): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        drawHeader(canvas, settings, "ALL REGISTERED STUDENTS DIRECTORY")

        var y = 140f
        val paintText = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val paintBold = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintHeaderBg = Paint().apply {
            color = Color.parseColor("#0F1E36")
        }
        val paintHeaderTxt = Paint().apply {
            color = Color.WHITE
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintRowAlt = Paint().apply {
            color = Color.parseColor("#F1F5F9")
        }
        val paintBorder = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        // Table Header
        canvas.drawRect(25f, y, PAGE_WIDTH - 25f, y + 24f, paintHeaderBg)
        canvas.drawText("ID", 32f, y + 16f, paintHeaderTxt)
        canvas.drawText("STUDENT NAME", 95f, y + 16f, paintHeaderTxt)
        canvas.drawText("MOBILE", 225f, y + 16f, paintHeaderTxt)
        canvas.drawText("SEAT", 310f, y + 16f, paintHeaderTxt)
        canvas.drawText("COURSE", 380f, y + 16f, paintHeaderTxt)
        canvas.drawText("STATUS", 500f, y + 16f, paintHeaderTxt)
        y += 24f

        students.forEachIndexed { index, student ->
            if (y > PAGE_HEIGHT - 60) return@forEachIndexed // Single page fit or break
            if (index % 2 == 1) {
                canvas.drawRect(25f, y, PAGE_WIDTH - 25f, y + 20f, paintRowAlt)
            }
            canvas.drawRect(25f, y, PAGE_WIDTH - 25f, y + 20f, paintBorder)

            canvas.drawText(student.id, 32f, y + 14f, paintBold)
            val nameDisplay = if (student.fullName.length > 20) student.fullName.substring(0, 18) + ".." else student.fullName
            canvas.drawText(nameDisplay, 95f, y + 14f, paintText)
            canvas.drawText(student.mobileNumber, 225f, y + 14f, paintText)
            canvas.drawText(student.seatNumber, 310f, y + 14f, paintText)
            val courseDisplay = if (student.course.length > 18) student.course.substring(0, 16) + ".." else student.course
            canvas.drawText(courseDisplay, 380f, y + 14f, paintText)

            val statusPaint = Paint().apply {
                color = if (student.status == "Active") Color.parseColor("#059669") else Color.parseColor("#DC2626")
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText(student.status, 500f, y + 14f, statusPaint)

            y += 20f
        }

        // Summary footer
        y += 20f
        canvas.drawText("Total Registered Students: ${students.size} | Active: ${students.count { it.status == "Active" }}", 30f, y, paintBold)

        drawFooter(canvas, 1)

        document.finishPage(page)
        return savePdf(context, document, "RS_Library_Students_List.pdf")
    }

    /**
     * Generates a date-wise attendance report PDF
     */
    fun generateAttendanceReportPdf(
        context: Context,
        settings: LibrarySettingsEntity,
        date: String,
        records: List<AttendanceEntity>
    ): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        drawHeader(canvas, settings, "DAILY ATTENDANCE REPORT - $date")

        var y = 140f
        val paintText = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val paintBold = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintHeaderBg = Paint().apply {
            color = Color.parseColor("#0F1E36")
        }
        val paintHeaderTxt = Paint().apply {
            color = Color.WHITE
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintRowAlt = Paint().apply {
            color = Color.parseColor("#F1F5F9")
        }
        val paintBorder = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        // Table Header
        canvas.drawRect(25f, y, PAGE_WIDTH - 25f, y + 24f, paintHeaderBg)
        canvas.drawText("STUDENT ID", 32f, y + 16f, paintHeaderTxt)
        canvas.drawText("NAME", 130f, y + 16f, paintHeaderTxt)
        canvas.drawText("CHECK-IN", 270f, y + 16f, paintHeaderTxt)
        canvas.drawText("CHECK-OUT", 360f, y + 16f, paintHeaderTxt)
        canvas.drawText("METHOD", 445f, y + 16f, paintHeaderTxt)
        canvas.drawText("STATUS", 515f, y + 16f, paintHeaderTxt)
        y += 24f

        if (records.isEmpty()) {
            canvas.drawText("No attendance records found for $date.", 32f, y + 25f, paintText)
            y += 40f
        } else {
            records.forEachIndexed { index, item ->
                if (y > PAGE_HEIGHT - 60) return@forEachIndexed
                if (index % 2 == 1) {
                    canvas.drawRect(25f, y, PAGE_WIDTH - 25f, y + 20f, paintRowAlt)
                }
                canvas.drawRect(25f, y, PAGE_WIDTH - 25f, y + 20f, paintBorder)

                canvas.drawText(item.studentId, 32f, y + 14f, paintBold)
                val nameDisplay = if (item.studentName.length > 20) item.studentName.substring(0, 18) + ".." else item.studentName
                canvas.drawText(nameDisplay, 130f, y + 14f, paintText)
                canvas.drawText(item.checkInTime, 270f, y + 14f, paintText)
                canvas.drawText(item.checkOutTime ?: "--:--", 360f, y + 14f, paintText)
                canvas.drawText(item.method, 445f, y + 14f, paintText)

                val statusPaint = Paint().apply {
                    color = Color.parseColor("#059669")
                    textSize = 9f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                canvas.drawText(item.status, 515f, y + 14f, statusPaint)

                y += 20f
            }
        }

        y += 25f
        canvas.drawText("Total Present Today: ${records.size} | QR Check-in: ${records.count { it.method == "QR Scan" }} | Manual: ${records.count { it.method == "Manual" }}", 30f, y, paintBold)

        drawFooter(canvas, 1)

        document.finishPage(page)
        return savePdf(context, document, "RS_Library_Attendance_$date.pdf")
    }

    /**
     * Generates an individual student profile and attendance record PDF
     */
    fun generateIndividualStudentPdf(
        context: Context,
        settings: LibrarySettingsEntity,
        student: StudentEntity,
        records: List<AttendanceEntity>
    ): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        drawHeader(canvas, settings, "STUDENT RECORD DOSSIER")

        var y = 140f
        val paintLabel = Paint().apply {
            color = Color.parseColor("#475569")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintValue = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val paintCard = Paint().apply {
            color = Color.parseColor("#F8FAFC")
        }
        val paintCardBorder = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1.5f
            style = Paint.Style.STROKE
        }

        // Student Info Card
        canvas.drawRect(25f, y, PAGE_WIDTH - 25f, y + 130f, paintCard)
        canvas.drawRect(25f, y, PAGE_WIDTH - 25f, y + 130f, paintCardBorder)

        canvas.drawText("Student ID:", 35f, y + 25f, paintLabel)
        canvas.drawText(student.id, 120f, y + 25f, paintValue)

        canvas.drawText("Full Name:", 35f, y + 48f, paintLabel)
        canvas.drawText(student.fullName, 120f, y + 48f, paintValue)

        canvas.drawText("Parent's Name:", 35f, y + 71f, paintLabel)
        canvas.drawText(student.parentName, 120f, y + 71f, paintValue)

        canvas.drawText("Mobile:", 35f, y + 94f, paintLabel)
        canvas.drawText(student.mobileNumber, 120f, y + 94f, paintValue)

        canvas.drawText("Email:", 35f, y + 117f, paintLabel)
        canvas.drawText(student.email, 120f, y + 117f, paintValue)

        // Right side of card
        canvas.drawText("Assigned Seat:", 320f, y + 25f, paintLabel)
        canvas.drawText(student.seatNumber, 415f, y + 25f, paintValue)

        canvas.drawText("Course / Prep:", 320f, y + 48f, paintLabel)
        canvas.drawText(student.course, 415f, y + 48f, paintValue)

        canvas.drawText("Joining Date:", 320f, y + 71f, paintLabel)
        canvas.drawText(student.joiningDate, 415f, y + 71f, paintValue)

        canvas.drawText("Status:", 320f, y + 94f, paintLabel)
        val statusPaint = Paint().apply {
            color = if (student.status == "Active") Color.parseColor("#059669") else Color.parseColor("#DC2626")
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(student.status, 415f, y + 94f, statusPaint)

        canvas.drawText("Total Attendance:", 320f, y + 117f, paintLabel)
        canvas.drawText("${records.size} Days Logged", 415f, y + 117f, paintValue)

        y += 155f

        // Attendance History Section
        val paintSecTitle = Paint().apply {
            color = Color.parseColor("#0F1E36")
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("RECENT ATTENDANCE LOGS (${records.size})", 25f, y, paintSecTitle)
        y += 12f

        val paintHeaderBg = Paint().apply { color = Color.parseColor("#162A45") }
        val paintHeaderTxt = Paint().apply {
            color = Color.WHITE
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintRowAlt = Paint().apply { color = Color.parseColor("#F1F5F9") }
        val paintBorder = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        canvas.drawRect(25f, y, PAGE_WIDTH - 25f, y + 22f, paintHeaderBg)
        canvas.drawText("DATE", 35f, y + 15f, paintHeaderTxt)
        canvas.drawText("CHECK-IN TIME", 140f, y + 15f, paintHeaderTxt)
        canvas.drawText("CHECK-OUT TIME", 270f, y + 15f, paintHeaderTxt)
        canvas.drawText("ENTRY METHOD", 400f, y + 15f, paintHeaderTxt)
        canvas.drawText("STATUS", 495f, y + 15f, paintHeaderTxt)
        y += 22f

        records.take(22).forEachIndexed { index, item ->
            if (index % 2 == 1) {
                canvas.drawRect(25f, y, PAGE_WIDTH - 25f, y + 18f, paintRowAlt)
            }
            canvas.drawRect(25f, y, PAGE_WIDTH - 25f, y + 18f, paintBorder)

            val paintItem = Paint().apply { color = Color.BLACK; textSize = 9.5f }
            canvas.drawText(item.date, 35f, y + 13f, paintItem)
            canvas.drawText(item.checkInTime, 140f, y + 13f, paintItem)
            canvas.drawText(item.checkOutTime ?: "--:--", 270f, y + 13f, paintItem)
            canvas.drawText(item.method, 400f, y + 13f, paintItem)
            val stPaint = Paint().apply {
                color = Color.parseColor("#059669")
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText(item.status, 495f, y + 13f, stPaint)

            y += 18f
        }

        drawFooter(canvas, 1)

        document.finishPage(page)
        return savePdf(context, document, "RS_Library_Student_${student.id}.pdf")
    }

    private fun drawHeader(canvas: android.graphics.Canvas, settings: LibrarySettingsEntity, reportTitle: String) {
        val bannerPaint = Paint().apply {
            color = Color.parseColor("#0F1E36")
        }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 110f, bannerPaint)

        val goldBar = Paint().apply {
            color = Color.parseColor("#F59E0B")
        }
        canvas.drawRect(0f, 106f, PAGE_WIDTH.toFloat(), 110f, goldBar)

        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 22f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        canvas.drawText("R.S LIBRARY", 25f, 42f, titlePaint)

        val subPaint = Paint().apply {
            color = Color.parseColor("#FBBF24")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        canvas.drawText(settings.tagline, 25f, 58f, subPaint)

        val addressPaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            textSize = 8.5f
        }
        canvas.drawText("${settings.address} | Phone: ${settings.contactPhone}", 25f, 72f, addressPaint)

        // Document Report Title Badge
        val badgeBg = Paint().apply {
            color = Color.parseColor("#1E3E62")
        }
        canvas.drawRoundRect(25f, 82f, PAGE_WIDTH - 25f, 102f, 4f, 4f, badgeBg)

        val badgeText = Paint().apply {
            color = Color.WHITE
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("REPORT: $reportTitle", 35f, 96f, badgeText)

        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val dateText = "Generated: ${sdf.format(Date())}"
        val textWidth = badgeText.measureText(dateText)
        canvas.drawText(dateText, PAGE_WIDTH - 35f - textWidth, 96f, badgeText)
    }

    private fun drawFooter(canvas: android.graphics.Canvas, pageNumber: Int) {
        val footerPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val dividerPaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1f
        }
        canvas.drawLine(25f, PAGE_HEIGHT - 35f, PAGE_WIDTH - 25f, PAGE_HEIGHT - 35f, dividerPaint)
        canvas.drawText("R.S Library Official Document • Confidential Attendance System", 25f, PAGE_HEIGHT - 20f, footerPaint)
        canvas.drawText("Page $pageNumber", PAGE_WIDTH - 65f, PAGE_HEIGHT - 20f, footerPaint)
    }

    private fun savePdf(context: Context, document: PdfDocument, filename: String): File? {
        return try {
            val dir = File(context.cacheDir, "reports")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, filename)
            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)
            document.close()
            outputStream.flush()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
            null
        }
    }

    /**
     * Creates an Intent to share or open the generated PDF file using FileProvider.
     */
    fun createSharePdfIntent(context: Context, file: File): Intent {
        val uri = FileProvider.getUriForFile(context, "com.example.fileprovider", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "R.S Library Document: ${file.name}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun createViewPdfIntent(context: Context, file: File): Intent {
        val uri = FileProvider.getUriForFile(context, "com.example.fileprovider", file)
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
