package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AppLanguage
import com.example.data.model.AppPreferencesManager
import com.example.data.model.PdfDocumentEntity
import com.example.data.local.WafaDatabase
import com.example.ui.Strings
import com.example.pdf.PdfEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `verify app name resource`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Wafa PDF", appName)
  }

  @Test
  fun `verify app preferences defaults and updates`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefsManager = AppPreferencesManager(context)
    val initial = prefsManager.settings.value
    assertEquals(AppLanguage.BANGLA, initial.language)

    prefsManager.updateLanguage(AppLanguage.ENGLISH)
    assertEquals(AppLanguage.ENGLISH, prefsManager.settings.value.language)

    prefsManager.updateTheme("DARK")
    assertEquals("DARK", prefsManager.settings.value.theme)
  }

  @Test
  fun `verify pdf database operations`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = WafaDatabase.getDatabase(context)
    val dao = db.pdfDao()

    val testDoc = PdfDocumentEntity(
      title = "Test_Document.pdf",
      filePath = "/tmp/test.pdf",
      fileSize = 1024L,
      pageCount = 3,
      folder = "Study"
    )

    val id = dao.insertDocument(testDoc)
    assertTrue(id > 0)

    val fetched = dao.getDocumentByIdDirect(id)
    assertNotNull(fetched)
    assertEquals("Test_Document.pdf", fetched?.title)
  }

  @Test
  fun `verify strings localization`() {
    val bnTitle = Strings.get("app_name", AppLanguage.BANGLA)
    val enTitle = Strings.get("app_name", AppLanguage.ENGLISH)
    val arTitle = Strings.get("app_name", AppLanguage.ARABIC)

    assertEquals("ওয়াফা পিডিএফ", bnTitle)
    assertEquals("Wafa PDF", enTitle)
    assertEquals("وفا بي دي إف", arTitle)

    val devCredit = Strings.get("dev_credit", AppLanguage.ENGLISH)
    assertTrue(devCredit.contains("Mehedi364"))
  }
}
