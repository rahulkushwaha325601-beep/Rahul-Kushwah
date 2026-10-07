package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.qr.QrCodeGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("R.S Library", appName)
  }

  @Test
  fun `qr bitmap generation in robolectric`() {
    val bitmap = QrCodeGenerator.generateQrBitmap("RSLIBRARY:TEST", 256, 256)
    assertNotNull(bitmap)
    assertEquals(256, bitmap?.width)
    assertEquals(256, bitmap?.height)
  }
}
