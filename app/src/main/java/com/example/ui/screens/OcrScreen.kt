package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.pdf.PdfEngine
import com.example.ui.Strings
import com.example.ui.viewmodel.PdfViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrScreen(
  viewModel: PdfViewModel,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  val settings by viewModel.settings.collectAsState()
  val language = settings.language
  val scannedPages by viewModel.scannedPages.collectAsState()
  val activeDoc by viewModel.activeDocument.collectAsState()

  BackHandler { onBack() }

  var ocrLanguage by remember { mutableStateOf(AppLanguage.BANGLA) }
  var ocrResultText by remember { mutableStateOf("") }
  var isProcessing by remember { mutableStateOf(false) }
  var isSpeaking by remember { mutableStateOf(false) }
  var searchFilter by remember { mutableStateOf("") }

  val coroutineScope = rememberCoroutineScope()

  fun performOcr() {
    coroutineScope.launch {
      isProcessing = true
      // Grab bitmap from scanned page or active document page
      val bmp = if (scannedPages.isNotEmpty()) {
        BitmapFactory.decodeFile(scannedPages.last())
      } else if (activeDoc != null) {
        PdfEngine.renderPageToBitmap(File(activeDoc!!.filePath), 0, 1.5f)
      } else null

      if (bmp != null) {
        val result = viewModel.ocrEngine.processOcr(bmp, ocrLanguage)
        ocrResultText = result
      } else {
        // Fallback default demonstration
        val sampleBmp = android.graphics.Bitmap.createBitmap(800, 1200, android.graphics.Bitmap.Config.ARGB_8888)
        ocrResultText = viewModel.ocrEngine.processOcr(sampleBmp, ocrLanguage)
      }
      isProcessing = false
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = Strings.get("action_ocr", language),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("ocr_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(16.dp)
        .testTag("ocr_screen_content")
    ) {

      // Language Selector Chips (Bangla, English, Arabic)
      Text(
        text = "Select Recognition Language (OCR)",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
      )
      Spacer(modifier = Modifier.height(8.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AppLanguage.entries.forEach { lang ->
          FilterChip(
            selected = ocrLanguage == lang,
            onClick = {
              ocrLanguage = lang
              if (ocrResultText.isNotBlank()) performOcr()
            },
            label = { Text(lang.displayName, fontSize = 12.sp) }
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Button(
        onClick = { performOcr() },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(Icons.Default.TextFields, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Extract Text with ${ocrLanguage.name} Engine")
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Search inside OCR text
      if (ocrResultText.isNotBlank()) {
        OutlinedTextField(
          value = searchFilter,
          onValueChange = { searchFilter = it },
          placeholder = { Text("Search text...") },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
      }

      // OCR Results Box
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.fillMaxSize()) {
          // Action Bar
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
              .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (ocrResultText.isNotBlank()) "${ocrResultText.split("\\s+".toRegex()).size} Words" else "No text extracted yet",
              style = MaterialTheme.typography.labelSmall
            )

            Row {
              if (ocrResultText.isNotBlank()) {
                // Read Aloud / Stop
                IconButton(onClick = {
                  if (isSpeaking) {
                    viewModel.ocrEngine.stopReading()
                    isSpeaking = false
                  } else {
                    viewModel.ocrEngine.readAloud(ocrResultText, ocrLanguage)
                    isSpeaking = true
                  }
                }) {
                  Icon(
                    imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                    contentDescription = "Read Aloud",
                    tint = if (isSpeaking) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                  )
                }

                // Copy
                IconButton(onClick = {
                  val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                  cm.setPrimaryClip(ClipData.newPlainText("OCR Text", ocrResultText))
                  viewModel.showMessage("Copied to clipboard!")
                }) {
                  Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                }

                // Save as new PDF
                IconButton(onClick = {
                  val title = "OCR_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}"
                  viewModel.createPdfFromText(title, ocrResultText)
                }) {
                  Icon(Icons.Default.PictureAsPdf, contentDescription = "Save PDF")
                }
              }
            }
          }

          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(14.dp)
          ) {
            if (isProcessing) {
              Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
              ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(10.dp))
                Text("Analyzing character blocks...")
              }
            } else if (ocrResultText.isNotBlank()) {
              LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                  Text(
                    text = ocrResultText,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }
              }
            } else {
              Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
              ) {
                Icon(
                  imageVector = Icons.Default.TextFields,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                  modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = "Scan or select a document, then tap Extract Text",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }
    }
  }
}
