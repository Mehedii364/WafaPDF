package com.example.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.model.PdfDocumentEntity
import com.example.pdf.PdfEngine
import com.example.ui.Strings
import com.example.ui.theme.SepiaBackground
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.PdfViewModel
import com.example.ui.viewmodel.ReaderFilter
import com.example.ui.viewmodel.ReaderMode
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
  viewModel: PdfViewModel,
  onBack: () -> Unit,
  onNavigateToAi: () -> Unit
) {
  val context = LocalContext.current
  val activeDoc by viewModel.activeDocument.collectAsState()
  val settings by viewModel.settings.collectAsState()
  val readerMode by viewModel.readerMode.collectAsState()
  val readerFilter by viewModel.readerFilter.collectAsState()
  val currentPage by viewModel.currentPage.collectAsState()
  val zoomScale by viewModel.zoomScale.collectAsState()

  BackHandler { onBack() }

  // Keep screen awake handling
  DisposableEffect(settings.keepScreenAwake) {
    val activity = context as? ComponentActivity
    if (settings.keepScreenAwake) {
      activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
    onDispose {
      activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
  }

  if (activeDoc == null) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text("No document selected")
    }
    return
  }

  val doc = activeDoc!!
  val file = remember(doc.filePath) { File(doc.filePath) }
  val totalPages = remember(doc.pageCount) { maxOf(1, doc.pageCount) }

  // Page Bitmaps Cache
  val pageBitmaps = remember { mutableStateMapOf<Int, Bitmap>() }
  val coroutineScope = rememberCoroutineScope()

  // Preload current page and surrounding pages
  fun loadPage(index: Int) {
    if (index in 0 until totalPages && !pageBitmaps.containsKey(index)) {
      coroutineScope.launch {
        val bmp = PdfEngine.renderPageToBitmap(file, index, zoomScale)
        if (bmp != null) {
          pageBitmaps[index] = bmp
        }
      }
    }
  }

  LaunchedEffect(doc.id, zoomScale) {
    pageBitmaps.clear()
    loadPage(currentPage - 1)
    loadPage(currentPage)
    loadPage(currentPage - 2)
  }

  // Dialogs & Sheets
  var showGoToPageDialog by remember { mutableStateOf(false) }
  var goToPageInput by remember { mutableStateOf("") }
  var showAnnotationsBar by remember { mutableStateOf(false) }
  var activeAnnotationTool by remember { mutableStateOf<String?>(null) }
  var isBookmarked by remember { mutableStateOf(false) }
  var showSearchOverlay by remember { mutableStateOf(false) }
  var searchQuery by remember { mutableStateOf("") }
  var showMoreMenu by remember { mutableStateOf(false) }

  // Background and filter colors
  val filterBgColor = when (readerFilter) {
    ReaderFilter.NORMAL -> MaterialTheme.colorScheme.background
    ReaderFilter.SEPIA -> SepiaBackground
    ReaderFilter.DARK -> Color(0xFF121212)
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = doc.title,
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = "Page $currentPage / $totalPages",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("reader_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(onClick = { isBookmarked = !isBookmarked }) {
            Icon(
              imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
              contentDescription = "Bookmark",
              tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
          }
          IconButton(onClick = { showAnnotationsBar = !showAnnotationsBar }) {
            Icon(Icons.Default.Draw, contentDescription = "Annotate")
          }
          IconButton(onClick = { onNavigateToAi() }) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = "AI Assistant",
              tint = Color(0xFFE91E63)
            )
          }
          IconButton(onClick = {
            // Share PDF via FileProvider
            try {
              val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
              val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
              }
              context.startActivity(Intent.createChooser(shareIntent, "Share PDF"))
            } catch (e: Exception) {
              viewModel.showMessage("Share error: ${e.message}")
            }
          }) {
            Icon(Icons.Default.Share, contentDescription = "Share")
          }
          Box {
            IconButton(onClick = { showMoreMenu = true }) {
              Icon(Icons.Default.MoreVert, contentDescription = "More")
            }
            DropdownMenu(expanded = showMoreMenu, onDismissRequest = { showMoreMenu = false }) {
              DropdownMenuItem(
                text = { Text("Go to Page...") },
                onClick = {
                  showMoreMenu = false
                  showGoToPageDialog = true
                }
              )
              DropdownMenuItem(
                text = { Text("First Page") },
                onClick = {
                  showMoreMenu = false
                  viewModel.updateCurrentPage(1)
                  loadPage(0)
                }
              )
              DropdownMenuItem(
                text = { Text("Last Page") },
                onClick = {
                  showMoreMenu = false
                  viewModel.updateCurrentPage(totalPages)
                  loadPage(totalPages - 1)
                }
              )
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    bottomBar = {
      // Bottom Controls Bar (Modes, Filter, Page navigation)
      Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
          // Annotation tools row if opened
          if (showAnnotationsBar) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.SpaceAround,
              verticalAlignment = Alignment.CenterVertically
            ) {
              val tools = listOf("Pen", "Highlight", "Underline", "Note", "Signature")
              tools.forEach { tool ->
                val active = activeAnnotationTool == tool
                FilterChip(
                  selected = active,
                  onClick = {
                    activeAnnotationTool = if (active) null else tool
                    viewModel.showMessage("Selected $tool tool")
                  },
                  label = { Text(tool, style = MaterialTheme.typography.labelSmall) }
                )
              }
            }
          }

          // Main bottom row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Reader Modes Toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
              IconButton(onClick = { viewModel.setReaderMode(ReaderMode.CONTINUOUS) }) {
                Icon(
                  imageVector = Icons.Default.Tune,
                  contentDescription = "Continuous",
                  tint = if (readerMode == ReaderMode.CONTINUOUS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              IconButton(onClick = { viewModel.setReaderMode(ReaderMode.SINGLE_PAGE) }) {
                Icon(
                  imageVector = Icons.Default.Fullscreen,
                  contentDescription = "Single Page",
                  tint = if (readerMode == ReaderMode.SINGLE_PAGE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              IconButton(onClick = { viewModel.setReaderMode(ReaderMode.BOOK_VIEW) }) {
                Icon(
                  imageVector = Icons.Default.MenuBook,
                  contentDescription = "Book View",
                  tint = if (readerMode == ReaderMode.BOOK_VIEW) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            // Filter mode (Light, Sepia, Dark)
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(24.dp)
                  .clip(CircleShape)
                  .background(Color.White)
                  .border(1.dp, Color.Gray, CircleShape)
                  .clickable { viewModel.setReaderFilter(ReaderFilter.NORMAL) }
              )
              Spacer(modifier = Modifier.width(6.dp))
              Box(
                modifier = Modifier
                  .size(24.dp)
                  .clip(CircleShape)
                  .background(SepiaBackground)
                  .border(1.dp, Color(0xFFDEC5A2), CircleShape)
                  .clickable { viewModel.setReaderFilter(ReaderFilter.SEPIA) }
              )
              Spacer(modifier = Modifier.width(6.dp))
              Box(
                modifier = Modifier
                  .size(24.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF1E293B))
                  .border(1.dp, Color.Gray, CircleShape)
                  .clickable { viewModel.setReaderFilter(ReaderFilter.DARK) }
              )
            }

            // Zoom controls
            Row(verticalAlignment = Alignment.CenterVertically) {
              IconButton(onClick = { viewModel.setZoomScale(zoomScale - 0.25f) }) {
                Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out")
              }
              Text(
                text = "${(zoomScale * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
              )
              IconButton(onClick = { viewModel.setZoomScale(zoomScale + 0.25f) }) {
                Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In")
              }
            }
          }
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .background(filterBgColor)
    ) {
      when (readerMode) {
        ReaderMode.CONTINUOUS -> {
          val listState = rememberLazyListState()

          LaunchedEffect(currentPage) {
            if (currentPage - 1 != listState.firstVisibleItemIndex) {
              listState.scrollToItem((currentPage - 1).coerceIn(0, totalPages - 1))
            }
          }

          LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 16.dp)
          ) {
            items(totalPages) { pageIdx ->
              LaunchedEffect(pageIdx) {
                loadPage(pageIdx)
              }
              val bmp = pageBitmaps[pageIdx]

              Card(
                modifier = Modifier
                  .padding(horizontal = 16.dp, vertical = 8.dp)
                  .testTag("page_card_$pageIdx"),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
              ) {
                if (bmp != null) {
                  Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Page ${pageIdx + 1}",
                    modifier = Modifier.fillMaxWidth()
                  )
                } else {
                  Box(
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(480.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                  }
                }
              }
            }
          }
        }

        ReaderMode.SINGLE_PAGE -> {
          val pageIdx = currentPage - 1
          LaunchedEffect(pageIdx) { loadPage(pageIdx) }
          val bmp = pageBitmaps[pageIdx]

          Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Card(
              modifier = Modifier
                .padding(16.dp)
                .weight(1f),
              shape = RoundedCornerShape(8.dp),
              colors = CardDefaults.cardColors(containerColor = Color.White),
              elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
              Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (bmp != null) {
                  Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Page $currentPage",
                    modifier = Modifier.fillMaxSize()
                  )
                } else {
                  CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
              }
            }

            // Single page navigation buttons
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Button(
                onClick = {
                  if (currentPage > 1) {
                    viewModel.updateCurrentPage(currentPage - 1)
                  }
                },
                enabled = currentPage > 1
              ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Previous")
              }

              Text(
                text = "$currentPage / $totalPages",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )

              Button(
                onClick = {
                  if (currentPage < totalPages) {
                    viewModel.updateCurrentPage(currentPage + 1)
                  }
                },
                enabled = currentPage < totalPages
              ) {
                Text("Next")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
              }
            }
          }
        }

        ReaderMode.BOOK_VIEW -> {
          // 2 pages side-by-side
          val leftIdx = currentPage - 1
          val rightIdx = currentPage
          LaunchedEffect(leftIdx, rightIdx) {
            loadPage(leftIdx)
            if (rightIdx < totalPages) loadPage(rightIdx)
          }
          val leftBmp = pageBitmaps[leftIdx]
          val rightBmp = if (rightIdx < totalPages) pageBitmaps[rightIdx] else null

          Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(4.dp)
              ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                  if (leftBmp != null) {
                    Image(bitmap = leftBmp.asImageBitmap(), contentDescription = "Page ${leftIdx + 1}")
                  } else {
                    CircularProgressIndicator()
                  }
                }
              }

              Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(4.dp)
              ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                  if (rightBmp != null) {
                    Image(bitmap = rightBmp.asImageBitmap(), contentDescription = "Page ${rightIdx + 1}")
                  } else {
                    Text("End of Document", style = MaterialTheme.typography.bodySmall)
                  }
                }
              }
            }

            // Navigation
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Button(
                onClick = { if (currentPage > 2) viewModel.updateCurrentPage(currentPage - 2) },
                enabled = currentPage > 2
              ) {
                Text("Prev 2 Pgs")
              }
              Text(
                text = "${leftIdx + 1} - ${minOf(rightIdx + 1, totalPages)} of $totalPages",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
              )
              Button(
                onClick = { if (currentPage + 2 <= totalPages) viewModel.updateCurrentPage(currentPage + 2) },
                enabled = currentPage + 2 <= totalPages
              ) {
                Text("Next 2 Pgs")
              }
            }
          }
        }
      }
    }
  }

  // Go to Page Dialog
  if (showGoToPageDialog) {
    AlertDialog(
      onDismissRequest = { showGoToPageDialog = false },
      title = { Text("Go to Page") },
      text = {
        OutlinedTextField(
          value = goToPageInput,
          onValueChange = { goToPageInput = it },
          label = { Text("Page Number (1 to $totalPages)") },
          singleLine = true
        )
      },
      confirmButton = {
        Button(
          onClick = {
            val p = goToPageInput.toIntOrNull()
            if (p != null && p in 1..totalPages) {
              viewModel.updateCurrentPage(p)
              loadPage(p - 1)
              showGoToPageDialog = false
            }
          }
        ) {
          Text("Go")
        }
      },
      dismissButton = {
        TextButton(onClick = { showGoToPageDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
