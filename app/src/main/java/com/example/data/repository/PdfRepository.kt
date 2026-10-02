package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.local.PdfDao
import com.example.data.model.AnnotationEntity
import com.example.data.model.BookmarkEntity
import com.example.data.model.FolderEntity
import com.example.data.model.PdfDocumentEntity
import com.example.pdf.PdfEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfRepository(
  private val context: Context,
  private val pdfDao: PdfDao
) {

  val recentDocuments: Flow<List<PdfDocumentEntity>> = pdfDao.getRecentDocuments()
  val allDocuments: Flow<List<PdfDocumentEntity>> = pdfDao.getAllDocuments()
  val favoriteDocuments: Flow<List<PdfDocumentEntity>> = pdfDao.getFavoriteDocuments()
  val vaultDocuments: Flow<List<PdfDocumentEntity>> = pdfDao.getVaultDocuments()
  val trashDocuments: Flow<List<PdfDocumentEntity>> = pdfDao.getTrashDocuments()
  val allFolders: Flow<List<FolderEntity>> = pdfDao.getAllFolders()

  fun getDocumentById(id: Long): Flow<PdfDocumentEntity?> = pdfDao.getDocumentById(id)
  suspend fun getDocumentByIdDirect(id: Long): PdfDocumentEntity? = pdfDao.getDocumentByIdDirect(id)

  fun getDocumentsByFolder(folder: String): Flow<List<PdfDocumentEntity>> =
    pdfDao.getDocumentsByFolder(folder)

  fun searchDocuments(query: String): Flow<List<PdfDocumentEntity>> =
    pdfDao.searchDocuments(query)

  suspend fun insertDocument(doc: PdfDocumentEntity): Long = pdfDao.insertDocument(doc)
  suspend fun updateDocument(doc: PdfDocumentEntity) = pdfDao.updateDocument(doc)
  suspend fun deleteDocument(doc: PdfDocumentEntity) = pdfDao.deleteDocument(doc)
  suspend fun deleteDocumentById(id: Long) = pdfDao.deleteDocumentById(id)

  // Folders
  suspend fun createFolder(name: String, colorHex: String = "#D32F2F"): Long {
    return pdfDao.insertFolder(FolderEntity(name = name, colorHex = colorHex))
  }
  suspend fun deleteFolder(id: Long) = pdfDao.deleteFolder(id)

  // Bookmarks
  fun getBookmarks(docId: Long): Flow<List<BookmarkEntity>> =
    pdfDao.getBookmarksForDocument(docId)

  suspend fun addBookmark(docId: Long, page: Int, title: String, snippet: String? = null): Long =
    pdfDao.insertBookmark(BookmarkEntity(documentId = docId, pageNumber = page, title = title, snippet = snippet))

  suspend fun removeBookmark(id: Long) = pdfDao.deleteBookmark(id)

  // Annotations
  fun getAnnotations(docId: Long, page: Int): Flow<List<AnnotationEntity>> =
    pdfDao.getAnnotationsForPage(docId, page)

  suspend fun addAnnotation(annotation: AnnotationEntity): Long =
    pdfDao.insertAnnotation(annotation)

  suspend fun removeAnnotation(id: Long) = pdfDao.deleteAnnotation(id)

  suspend fun initializeDefaultContentIfNeeded() = withContext(Dispatchers.IO) {
    val existing = pdfDao.getDocumentByIdDirect(1)
    if (existing == null) {
      // Create standard folders
      val defaultFolders = listOf("Study", "Documents", "Work", "Personal", "Scans")
      defaultFolders.forEach { fName ->
        pdfDao.insertFolder(FolderEntity(name = fName))
      }

      // Generate the official Wafa PDF User Guide document
      val guideFile = File(context.filesDir, "Wafa_PDF_User_Guide.pdf")
      val guideText = """
        WELCOME TO WAFA PDF
        The Comprehensive Offline & AI PDF Toolkit
        Made with ❤️ by Mehedi364

        1. INTRODUCTION
        Wafa PDF is a high-performance Android document workstation engineered for speed, privacy, and extensive functionality. Every core feature operates offline on your device, ensuring total privacy.

        2. CORE CAPABILITIES
        • PDF Reader: Continuous scroll, single-page, and book views with customizable light, dark, and warm sepia reading modes.
        • Document Scanner: Multi-page batch camera capture with edge enhancement and filters.
        • PDF Compressor: Presets for Web, Print, WhatsApp, and custom DPI targeting.
        • Watermark Studio: Apply confidential, date, author, or customized tiled stamps.
        • Ready Print: Automatic 2-up, 4-up, and booklet preparation for A4 and Letter sizes.
        • Full OCR: Text recognition for Bangla, English, and Arabic with Text-to-Speech read aloud.
        • AI PDF Assistant: Summary, translation, study notes, quiz/MCQ, and interactive document chat.

        3. PRIVACY & SECURITY
        Files saved in your Private Vault are locked with your master PIN. No documents are uploaded to cloud servers without your explicit AI action.

        Enjoy a seamless document experience!
      """.trimIndent()

      PdfEngine.createPdfFromText(
        context = context,
        text = guideText,
        title = "Wafa PDF User Guide & Toolkit Overview",
        outputFile = guideFile
      )

      val pageCount = PdfEngine.getPageCount(guideFile).coerceAtLeast(1)
      val sampleDoc = PdfDocumentEntity(
        id = 1,
        title = "Wafa PDF User Guide.pdf",
        filePath = guideFile.absolutePath,
        fileSize = guideFile.length(),
        pageCount = pageCount,
        folder = "Documents",
        tags = "Guide, Official",
        isFavorite = true,
        extractedText = guideText
      )
      pdfDao.insertDocument(sampleDoc)
    }
  }

  suspend fun importPdfFromUri(uri: Uri, displayName: String? = null): PdfDocumentEntity? = withContext(Dispatchers.IO) {
    try {
      val name = displayName ?: "Imported_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
      val cleanName = if (name.endsWith(".pdf", ignoreCase = true)) name else "$name.pdf"
      val destFile = File(context.filesDir, cleanName)

      context.contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(destFile).use { output ->
          input.copyTo(output)
        }
      }

      val pageCount = PdfEngine.getPageCount(destFile).coerceAtLeast(1)
      val entity = PdfDocumentEntity(
        title = cleanName,
        filePath = destFile.absolutePath,
        fileSize = destFile.length(),
        pageCount = pageCount,
        folder = "Documents"
      )
      val id = pdfDao.insertDocument(entity)
      entity.copy(id = id)
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }
}
