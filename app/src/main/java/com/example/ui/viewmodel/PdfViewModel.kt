package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiPdfAssistant
import com.example.data.local.WafaDatabase
import com.example.data.model.AnnotationEntity
import com.example.data.model.AppLanguage
import com.example.data.model.AppPreferencesManager
import com.example.data.model.BookmarkEntity
import com.example.data.model.FolderEntity
import com.example.data.model.PdfDocumentEntity
import com.example.data.model.UserSettings
import com.example.data.repository.PdfRepository
import com.example.ocr.OcrEngine
import com.example.pdf.PdfEngine
import com.example.pdf.WatermarkConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppScreen {
  HOME,
  FILES,
  SCAN,
  AI,
  SETTINGS,
  READER,
  TOOL_COMPRESS,
  TOOL_WATERMARK,
  TOOL_PRINT,
  TOOL_MERGE,
  TOOL_SPLIT,
  TOOL_OCR,
  TOOL_FORMS,
  VAULT
}

enum class ReaderMode {
  CONTINUOUS,
  SINGLE_PAGE,
  BOOK_VIEW
}

enum class ReaderFilter {
  NORMAL,
  SEPIA,
  DARK
}

class PdfViewModel(application: Application) : AndroidViewModel(application) {

  val db = WafaDatabase.getDatabase(application)
  val repository = PdfRepository(application, db.pdfDao())
  val prefsManager = AppPreferencesManager(application)
  val ocrEngine = OcrEngine(application)
  val aiAssistant = AiPdfAssistant()

  // Navigation State
  private val _currentScreen = MutableStateFlow(AppScreen.HOME)
  val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

  private val screenBackStack = mutableListOf<AppScreen>()

  fun navigateTo(screen: AppScreen) {
    if (_currentScreen.value != screen) {
      screenBackStack.add(_currentScreen.value)
      _currentScreen.value = screen
    }
  }

  fun navigateBack(): Boolean {
    if (screenBackStack.isNotEmpty()) {
      _currentScreen.value = screenBackStack.removeAt(screenBackStack.size - 1)
      return true
    }
    return false
  }

  // Settings
  val settings: StateFlow<UserSettings> = prefsManager.settings

  // Data streams
  val recentDocuments: StateFlow<List<PdfDocumentEntity>> = repository.recentDocuments
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allDocuments: StateFlow<List<PdfDocumentEntity>> = repository.allDocuments
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val favoriteDocuments: StateFlow<List<PdfDocumentEntity>> = repository.favoriteDocuments
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val vaultDocuments: StateFlow<List<PdfDocumentEntity>> = repository.vaultDocuments
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allFolders: StateFlow<List<FolderEntity>> = repository.allFolders
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Active Document for Reader / Tools / AI
  private val _activeDocument = MutableStateFlow<PdfDocumentEntity?>(null)
  val activeDocument: StateFlow<PdfDocumentEntity?> = _activeDocument.asStateFlow()

  // Reader States
  private val _readerMode = MutableStateFlow(ReaderMode.CONTINUOUS)
  val readerMode: StateFlow<ReaderMode> = _readerMode.asStateFlow()

  private val _readerFilter = MutableStateFlow(ReaderFilter.NORMAL)
  val readerFilter: StateFlow<ReaderFilter> = _readerFilter.asStateFlow()

  private val _currentPage = MutableStateFlow(1)
  val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

  private val _zoomScale = MutableStateFlow(1.5f)
  val zoomScale: StateFlow<Float> = _zoomScale.asStateFlow()

  // Scanner State
  private val _scannedPages = MutableStateFlow<List<String>>(emptyList())
  val scannedPages: StateFlow<List<String>> = _scannedPages.asStateFlow()

  // Status / Message snackbar
  private val _statusMessage = MutableStateFlow<String?>(null)
  val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

  private val _isProcessing = MutableStateFlow(false)
  val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

  // AI Output
  private val _aiResult = MutableStateFlow("")
  val aiResult: StateFlow<String> = _aiResult.asStateFlow()

  private val _aiLoading = MutableStateFlow(false)
  val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

  // Search
  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  init {
    viewModelScope.launch {
      repository.initializeDefaultContentIfNeeded()
    }
  }

  fun showMessage(msg: String) {
    _statusMessage.value = msg
  }

  fun clearMessage() {
    _statusMessage.value = null
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun openDocument(doc: PdfDocumentEntity) {
    _activeDocument.value = doc
    _currentPage.value = doc.lastPageRead.coerceAtLeast(1)
    viewModelScope.launch {
      repository.updateDocument(doc.copy(lastOpenedTimestamp = System.currentTimeMillis()))
    }
    navigateTo(AppScreen.READER)
  }

  fun selectDocumentForTools(doc: PdfDocumentEntity) {
    _activeDocument.value = doc
  }

  fun updateCurrentPage(page: Int) {
    _currentPage.value = page
    _activeDocument.value?.let { doc ->
      viewModelScope.launch {
        repository.updateDocument(doc.copy(lastPageRead = page))
      }
    }
  }

  fun setReaderMode(mode: ReaderMode) {
    _readerMode.value = mode
  }

  fun setReaderFilter(filter: ReaderFilter) {
    _readerFilter.value = filter
  }

  fun setZoomScale(scale: Float) {
    _zoomScale.value = scale.coerceIn(1.0f, 3.5f)
  }

  fun toggleFavorite(doc: PdfDocumentEntity) {
    viewModelScope.launch {
      repository.updateDocument(doc.copy(isFavorite = !doc.isFavorite))
    }
  }

  fun moveToTrash(doc: PdfDocumentEntity) {
    viewModelScope.launch {
      repository.updateDocument(doc.copy(isTrash = true))
      showMessage("Moved to Trash: ${doc.title}")
    }
  }

  fun moveToVault(doc: PdfDocumentEntity) {
    viewModelScope.launch {
      repository.updateDocument(doc.copy(isVault = true))
      showMessage("Moved to Vault: ${doc.title}")
    }
  }

  fun restoreFromVault(doc: PdfDocumentEntity) {
    viewModelScope.launch {
      repository.updateDocument(doc.copy(isVault = false))
      showMessage("Restored: ${doc.title}")
    }
  }

  fun createFolder(name: String) {
    viewModelScope.launch {
      repository.createFolder(name)
    }
  }

  // Scanner methods
  fun addScannedPage(imagePath: String) {
    _scannedPages.value = _scannedPages.value + imagePath
  }

  fun clearScannedPages() {
    _scannedPages.value = emptyList()
  }

  fun saveScannedDocument(title: String) {
    if (_scannedPages.value.isEmpty()) return
    viewModelScope.launch {
      _isProcessing.value = true
      val cleanTitle = if (title.endsWith(".pdf", ignoreCase = true)) title else "$title.pdf"
      val outputFile = File(getApplication<Application>().filesDir, cleanTitle)
      val success = PdfEngine.createPdfFromImages(_scannedPages.value, outputFile)
      if (success) {
        val pageCount = PdfEngine.getPageCount(outputFile)
        val entity = PdfDocumentEntity(
          title = cleanTitle,
          filePath = outputFile.absolutePath,
          fileSize = outputFile.length(),
          pageCount = pageCount,
          folder = "Scans"
        )
        val id = repository.insertDocument(entity)
        clearScannedPages()
        showMessage("Saved scan: $cleanTitle")
        openDocument(entity.copy(id = id))
      } else {
        showMessage("Failed to create PDF from scan")
      }
      _isProcessing.value = false
    }
  }

  // Create PDF from text
  fun createPdfFromText(title: String, content: String) {
    viewModelScope.launch {
      _isProcessing.value = true
      val cleanTitle = if (title.endsWith(".pdf", ignoreCase = true)) title else "$title.pdf"
      val outputFile = File(getApplication<Application>().filesDir, cleanTitle)
      val success = PdfEngine.createPdfFromText(getApplication(), content, title, outputFile)
      if (success) {
        val pageCount = PdfEngine.getPageCount(outputFile)
        val entity = PdfDocumentEntity(
          title = cleanTitle,
          filePath = outputFile.absolutePath,
          fileSize = outputFile.length(),
          pageCount = pageCount,
          folder = "Documents",
          extractedText = content
        )
        val id = repository.insertDocument(entity)
        showMessage("Created: $cleanTitle")
        openDocument(entity.copy(id = id))
      } else {
        showMessage("Failed to create PDF")
      }
      _isProcessing.value = false
    }
  }

  // Compress PDF
  fun compressDocument(
    doc: PdfDocumentEntity,
    scaleFactor: Float,
    quality: Int,
    toGrayscale: Boolean,
    customNameSuffix: String = "_compressed"
  ) {
    viewModelScope.launch {
      _isProcessing.value = true
      val sourceFile = File(doc.filePath)
      val baseName = doc.title.removeSuffix(".pdf")
      val outputName = "${baseName}$customNameSuffix.pdf"
      val outputFile = File(getApplication<Application>().filesDir, outputName)

      val success = PdfEngine.compressPdf(sourceFile, outputFile, scaleFactor, quality, toGrayscale)
      if (success) {
        val newPageCount = PdfEngine.getPageCount(outputFile)
        val entity = PdfDocumentEntity(
          title = outputName,
          filePath = outputFile.absolutePath,
          fileSize = outputFile.length(),
          pageCount = newPageCount,
          folder = doc.folder
        )
        val id = repository.insertDocument(entity)
        val savedPercent = (100L - (outputFile.length() * 100L / maxOf(1L, doc.fileSize))).coerceAtLeast(0L)
        showMessage("Compressed successfully! Saved $savedPercent%")
        openDocument(entity.copy(id = id))
      } else {
        showMessage("Compression failed")
      }
      _isProcessing.value = false
    }
  }

  // Watermark PDF
  fun watermarkDocument(
    doc: PdfDocumentEntity,
    config: WatermarkConfig
  ) {
    viewModelScope.launch {
      _isProcessing.value = true
      val sourceFile = File(doc.filePath)
      val baseName = doc.title.removeSuffix(".pdf")
      val outputName = "${baseName}_watermarked.pdf"
      val outputFile = File(getApplication<Application>().filesDir, outputName)

      val success = PdfEngine.applyWatermark(sourceFile, outputFile, config)
      if (success) {
        val newPageCount = PdfEngine.getPageCount(outputFile)
        val entity = PdfDocumentEntity(
          title = outputName,
          filePath = outputFile.absolutePath,
          fileSize = outputFile.length(),
          pageCount = newPageCount,
          folder = doc.folder
        )
        val id = repository.insertDocument(entity)
        showMessage("Watermark applied successfully!")
        openDocument(entity.copy(id = id))
      } else {
        showMessage("Failed to apply watermark")
      }
      _isProcessing.value = false
    }
  }

  // Print preparation (2-up or 4-up)
  fun preparePrintNUp(doc: PdfDocumentEntity, nUp: Int) {
    viewModelScope.launch {
      _isProcessing.value = true
      val sourceFile = File(doc.filePath)
      val baseName = doc.title.removeSuffix(".pdf")
      val outputName = "${baseName}_${nUp}up_print.pdf"
      val outputFile = File(getApplication<Application>().filesDir, outputName)

      val success = PdfEngine.preparePrintNUp(sourceFile, outputFile, nUp)
      if (success) {
        val pageCount = PdfEngine.getPageCount(outputFile)
        val entity = PdfDocumentEntity(
          title = outputName,
          filePath = outputFile.absolutePath,
          fileSize = outputFile.length(),
          pageCount = pageCount,
          folder = "Documents"
        )
        val id = repository.insertDocument(entity)
        showMessage("Print-ready PDF generated ($nUp-up)")
        openDocument(entity.copy(id = id))
      } else {
        showMessage("Print preparation failed")
      }
      _isProcessing.value = false
    }
  }

  // Merge PDFs
  fun mergeDocuments(docsToMerge: List<PdfDocumentEntity>, outputTitle: String) {
    if (docsToMerge.isEmpty()) return
    viewModelScope.launch {
      _isProcessing.value = true
      val cleanTitle = if (outputTitle.endsWith(".pdf", ignoreCase = true)) outputTitle else "$outputTitle.pdf"
      val outputFile = File(getApplication<Application>().filesDir, cleanTitle)
      val files = docsToMerge.map { File(it.filePath) }

      val success = PdfEngine.mergePdfs(files, outputFile)
      if (success) {
        val pageCount = PdfEngine.getPageCount(outputFile)
        val entity = PdfDocumentEntity(
          title = cleanTitle,
          filePath = outputFile.absolutePath,
          fileSize = outputFile.length(),
          pageCount = pageCount,
          folder = "Documents"
        )
        val id = repository.insertDocument(entity)
        showMessage("Merged ${docsToMerge.size} PDFs successfully!")
        openDocument(entity.copy(id = id))
      } else {
        showMessage("Failed to merge PDFs")
      }
      _isProcessing.value = false
    }
  }

  // AI Document Action
  fun executeAiTask(taskType: String, extraInput: String = "") {
    val doc = _activeDocument.value ?: recentDocuments.value.firstOrNull()
    val docTitle = doc?.title ?: "Document"
    val docContext = doc?.extractedText ?: "Wafa PDF Document containing multi-page verified content."
    val currentSettings = settings.value

    viewModelScope.launch {
      _aiLoading.value = true
      val res = aiAssistant.executeTask(
        taskType = taskType,
        docTitle = docTitle,
        docContext = docContext,
        extraInput = extraInput,
        apiKey = currentSettings.aiApiKey,
        model = currentSettings.aiModel,
        language = currentSettings.language
      )
      _aiResult.value = res
      _aiLoading.value = false
    }
  }

  // Import external URI
  fun importUri(uri: Uri, name: String? = null) {
    viewModelScope.launch {
      _isProcessing.value = true
      val imported = repository.importPdfFromUri(uri, name)
      if (imported != null) {
        showMessage("Imported ${imported.title}")
        openDocument(imported)
      } else {
        showMessage("Failed to import PDF")
      }
      _isProcessing.value = false
    }
  }

  override fun onCleared() {
    super.onCleared()
    ocrEngine.shutdown()
  }
}
