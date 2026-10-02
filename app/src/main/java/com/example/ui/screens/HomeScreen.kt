package com.example.ui.screens

import android.text.format.Formatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.PdfDocumentEntity
import com.example.ui.Strings
import com.example.ui.components.WafaTopAppBar
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.PdfViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  viewModel: PdfViewModel,
  onNavigateTo: (AppScreen) -> Unit
) {
  val context = LocalContext.current
  val settings by viewModel.settings.collectAsState()
  val language = settings.language
  val recentDocs by viewModel.recentDocuments.collectAsState()
  val folders by viewModel.allFolders.collectAsState()

  var showCreateSheet by remember { mutableStateOf(false) }
  var showCreateTextDialog by remember { mutableStateOf(false) }
  var showNewFolderDialog by remember { mutableStateOf(false) }
  var newFolderName by remember { mutableStateOf("") }
  var textPdfTitle by remember { mutableStateOf("") }
  var textPdfBody by remember { mutableStateOf("") }

  // PDF Document Picker
  val pdfPicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
  ) { uri ->
    uri?.let { viewModel.importUri(it) }
  }

  // Multi-Image Picker
  val imagePicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetMultipleContents()
  ) { uris ->
    if (uris.isNotEmpty()) {
      // copy images and create PDF
      val paths = uris.mapNotNull { u ->
        try {
          val f = java.io.File(context.cacheDir, "img_${System.currentTimeMillis()}_${(0..999).random()}.jpg")
          context.contentResolver.openInputStream(u)?.use { input ->
            java.io.FileOutputStream(f).use { out -> input.copyTo(out) }
          }
          f.absolutePath
        } catch (e: Exception) {
          null
        }
      }
      if (paths.isNotEmpty()) {
        paths.forEach { viewModel.addScannedPage(it) }
        viewModel.saveScannedDocument("Images_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}")
      }
    }
  }

  Scaffold(
    topBar = {
      WafaTopAppBar(
        title = Strings.get("app_name", language),
        subtitle = Strings.get("app_subtitle", language),
        language = language,
        onSearchClick = { onNavigateTo(AppScreen.FILES) },
        onSettingsClick = { onNavigateTo(AppScreen.SETTINGS) },
        onVaultClick = { onNavigateTo(AppScreen.VAULT) }
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showCreateSheet = true },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.testTag("fab_create_button")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Add, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = Strings.get("fab_create", language),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
          )
        }
      }
    }
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .testTag("home_screen_content"),
      contentPadding = PaddingValues(bottom = 96.dp)
    ) {

      // 1. Search Bar Card
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onNavigateTo(AppScreen.FILES) }
            .testTag("home_search_bar"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = Strings.get("search_hint", language),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      // 2. Quick Actions Grid (8 primary tools)
      item {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
          val quickActions = listOf(
            Triple(Strings.get("action_camera", language), Icons.Default.CameraAlt) { onNavigateTo(AppScreen.SCAN) },
            Triple(Strings.get("action_scan", language), Icons.Default.DocumentScanner) { onNavigateTo(AppScreen.SCAN) },
            Triple(Strings.get("action_capture", language), Icons.Default.PhotoCamera) { imagePicker.launch("image/*") },
            Triple(Strings.get("action_create_pdf", language), Icons.Default.Description) { showCreateTextDialog = true },
            Triple(Strings.get("action_compress", language), Icons.Default.LinearScale) { onNavigateTo(AppScreen.TOOL_COMPRESS) },
            Triple(Strings.get("action_edit", language), Icons.Default.Edit) {
              val first = recentDocs.firstOrNull()
              if (first != null) viewModel.openDocument(first) else showCreateTextDialog = true
            },
            Triple(Strings.get("action_convert", language), Icons.Default.Image) { imagePicker.launch("image/*") },
            Triple(Strings.get("action_watermark", language), Icons.Default.WaterDrop) { onNavigateTo(AppScreen.TOOL_WATERMARK) }
          )

          Text(
            text = "Quick Tools",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
          )

          // 2 rows of 4 items
          for (row in 0 until 2) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              for (col in 0 until 4) {
                val index = row * 4 + col
                if (index < quickActions.size) {
                  val (title, icon, action) = quickActions[index]
                  QuickActionButton(
                    title = title,
                    icon = icon,
                    onClick = action,
                    modifier = Modifier.weight(1f)
                  )
                }
              }
            }
            if (row == 0) Spacer(modifier = Modifier.height(12.dp))
          }
        }
      }

      // 3. Continue Reading Card
      val mostRecent = recentDocs.firstOrNull()
      if (mostRecent != null) {
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 10.dp)
              .testTag("continue_reading_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(38.dp)
                      .clip(CircleShape)
                      .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.PictureAsPdf,
                      contentDescription = null,
                      tint = Color.White,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(
                      text = Strings.get("continue_reading", language),
                      style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                      color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                      text = mostRecent.title,
                      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                }
                Button(
                  onClick = { viewModel.openDocument(mostRecent) },
                  colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                  shape = RoundedCornerShape(12.dp),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                  Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(Strings.get("continue_btn", language), style = MaterialTheme.typography.labelSmall)
                }
              }

              Spacer(modifier = Modifier.height(12.dp))
              val progress = (mostRecent.lastPageRead.toFloat() / maxOf(1, mostRecent.pageCount)).coerceIn(0f, 1f)
              LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(6.dp)
                  .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
              )
              Spacer(modifier = Modifier.height(6.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = "Page ${mostRecent.lastPageRead} of ${mostRecent.pageCount}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "${(progress * 100).toInt()}% completed",
                  style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }
          }
        }
      }

      // 4. AI PDF Card with Hero Banner
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onNavigateTo(AppScreen.AI) }
            .testTag("ai_pdf_banner_card"),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
            ) {
              Image(
                painter = painterResource(id = R.drawable.img_hero_banner_1790900985909),
                contentDescription = "AI PDF Banner",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
              )
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .background(Color.Black.copy(alpha = 0.45f))
              )
              Column(
                modifier = Modifier
                  .align(Alignment.BottomStart)
                  .padding(16.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFFFD54F),
                    modifier = Modifier.size(20.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = Strings.get("ask_pdf_title", language),
                    style = MaterialTheme.typography.titleMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = Color.White
                    )
                  )
                }
                Text(
                  text = Strings.get("ask_pdf_subtitle", language),
                  style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFF1F5F9))
                )
              }
            }

            // Quick AI Chips
            LazyRow(
              modifier = Modifier.padding(12.dp),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              val aiChips = listOf(
                Pair(Strings.get("ai_summarize", language), "SUMMARIZE"),
                Pair(Strings.get("ai_explain", language), "EXPLAIN"),
                Pair(Strings.get("ai_translate", language), "TRANSLATE"),
                Pair(Strings.get("ai_notes", language), "NOTES"),
                Pair(Strings.get("ai_mcq", language), "MCQ"),
                Pair(Strings.get("ai_flashcards", language), "FLASHCARDS")
              )
              items(aiChips) { (label, task) ->
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                  modifier = Modifier.clickable {
                    viewModel.executeAiTask(task)
                    onNavigateTo(AppScreen.AI)
                  }
                ) {
                  Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }
              }
            }
          }
        }
      }

      // 5. Folders Section
      item {
        Column(modifier = Modifier.padding(top = 10.dp)) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = Strings.get("folders_title", language),
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(onClick = { showNewFolderDialog = true }) {
              Text("+ New Folder", style = MaterialTheme.typography.labelSmall)
            }
          }

          LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            items(folders) { folder ->
              Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                  .width(110.dp)
                  .clickable { onNavigateTo(AppScreen.FILES) }
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Box(
                    modifier = Modifier
                      .size(32.dp)
                      .clip(CircleShape)
                      .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.Folder,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                  Spacer(modifier = Modifier.height(8.dp))
                  Text(
                    text = folder.name,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }
            }
          }
        }
      }

      // 6. Recent Documents List
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = Strings.get("recent_documents", language),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          TextButton(onClick = { onNavigateTo(AppScreen.FILES) }) {
            Text("View All", style = MaterialTheme.typography.labelSmall)
          }
        }
      }

      if (recentDocs.isEmpty()) {
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = Strings.get("empty_documents", language),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      } else {
        items(recentDocs.take(6)) { doc ->
          DocumentItemRow(
            doc = doc,
            onOpen = { viewModel.openDocument(doc) },
            onFavorite = { viewModel.toggleFavorite(doc) },
            onCompress = {
              viewModel.selectDocumentForTools(doc)
              onNavigateTo(AppScreen.TOOL_COMPRESS)
            },
            onWatermark = {
              viewModel.selectDocumentForTools(doc)
              onNavigateTo(AppScreen.TOOL_WATERMARK)
            },
            onVault = { viewModel.moveToVault(doc) },
            onTrash = { viewModel.moveToTrash(doc) }
          )
        }
      }
    }
  }

  // Create Bottom Sheet Dialog
  if (showCreateSheet) {
    ModalBottomSheet(
      onDismissRequest = { showCreateSheet = false },
      sheetState = rememberModalBottomSheetState()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 10.dp)
      ) {
        Text(
          text = Strings.get("fab_create", language),
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          modifier = Modifier.padding(bottom = 16.dp)
        )

        val creationOptions = listOf(
          Triple(Strings.get("fab_blank_pdf", language), Icons.Default.Description) {
            showCreateSheet = false
            showCreateTextDialog = true
          },
          Triple(Strings.get("fab_scan_doc", language), Icons.Default.DocumentScanner) {
            showCreateSheet = false
            onNavigateTo(AppScreen.SCAN)
          },
          Triple(Strings.get("fab_images_to_pdf", language), Icons.Default.Image) {
            showCreateSheet = false
            imagePicker.launch("image/*")
          },
          Triple(Strings.get("fab_text_to_pdf", language), Icons.Default.TextFields) {
            showCreateSheet = false
            showCreateTextDialog = true
          },
          Triple(Strings.get("fab_import_pdf", language), Icons.Default.PictureAsPdf) {
            showCreateSheet = false
            pdfPicker.launch(arrayOf("application/pdf"))
          }
        )

        creationOptions.forEach { (label, icon, action) ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { action() }
              .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center
            ) {
              Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
              text = label,
              style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
          }
        }
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }

  // Dialog: Create Text PDF
  if (showCreateTextDialog) {
    AlertDialog(
      onDismissRequest = { showCreateTextDialog = false },
      title = { Text(Strings.get("fab_text_to_pdf", language)) },
      text = {
        Column {
          OutlinedTextField(
            value = textPdfTitle,
            onValueChange = { textPdfTitle = it },
            label = { Text("Document Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = textPdfBody,
            onValueChange = { textPdfBody = it },
            label = { Text("Document Content") },
            modifier = Modifier
              .fillMaxWidth()
              .height(150.dp)
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (textPdfTitle.isNotBlank()) {
              viewModel.createPdfFromText(textPdfTitle, textPdfBody)
              textPdfTitle = ""
              textPdfBody = ""
              showCreateTextDialog = false
            }
          }
        ) {
          Text(Strings.get("save", language))
        }
      },
      dismissButton = {
        TextButton(onClick = { showCreateTextDialog = false }) {
          Text(Strings.get("cancel", language))
        }
      }
    )
  }

  // Dialog: New Folder
  if (showNewFolderDialog) {
    AlertDialog(
      onDismissRequest = { showNewFolderDialog = false },
      title = { Text("Create Folder") },
      text = {
        OutlinedTextField(
          value = newFolderName,
          onValueChange = { newFolderName = it },
          label = { Text("Folder Name") },
          modifier = Modifier.fillMaxWidth()
        )
      },
      confirmButton = {
        Button(
          onClick = {
            if (newFolderName.isNotBlank()) {
              viewModel.createFolder(newFolderName)
              newFolderName = ""
              showNewFolderDialog = false
            }
          }
        ) {
          Text(Strings.get("save", language))
        }
      },
      dismissButton = {
        TextButton(onClick = { showNewFolderDialog = false }) {
          Text(Strings.get("cancel", language))
        }
      }
    )
  }
}

@Composable
fun QuickActionButton(
  title: String,
  icon: ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .clickable { onClick() }
      .padding(4.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier
        .size(52.dp)
        .clip(RoundedCornerShape(16.dp))
        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = title,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(24.dp)
      )
    }
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = title,
      style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

@Composable
fun DocumentItemRow(
  doc: PdfDocumentEntity,
  onOpen: () -> Unit,
  onFavorite: () -> Unit,
  onCompress: () -> Unit,
  onWatermark: () -> Unit,
  onVault: () -> Unit,
  onTrash: () -> Unit
) {
  val context = LocalContext.current
  var showMenu by remember { mutableStateOf(false) }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 4.dp)
      .clickable { onOpen() }
      .testTag("doc_item_${doc.id}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.PictureAsPdf,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(24.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = doc.title,
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "${doc.pageCount} pgs • ${Formatter.formatFileSize(context, doc.fileSize)}",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(doc.lastOpenedTimestamp)),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      IconButton(onClick = onFavorite) {
        Icon(
          imageVector = if (doc.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
          contentDescription = "Favorite",
          tint = if (doc.isFavorite) Color(0xFFE91E63) else MaterialTheme.colorScheme.outline
        )
      }

      Box {
        IconButton(onClick = { showMenu = true }) {
          Icon(Icons.Default.MoreVert, contentDescription = "More")
        }
        DropdownMenu(
          expanded = showMenu,
          onDismissRequest = { showMenu = false }
        ) {
          DropdownMenuItem(
            text = { Text("Open") },
            onClick = {
              showMenu = false
              onOpen()
            }
          )
          DropdownMenuItem(
            text = { Text("Compress") },
            onClick = {
              showMenu = false
              onCompress()
            }
          )
          DropdownMenuItem(
            text = { Text("Watermark") },
            onClick = {
              showMenu = false
              onWatermark()
            }
          )
          DropdownMenuItem(
            text = { Text("Move to Vault") },
            onClick = {
              showMenu = false
              onVault()
            }
          )
          DropdownMenuItem(
            text = { Text("Move to Trash") },
            onClick = {
              showMenu = false
              onTrash()
            }
          )
        }
      }
    }
  }
}
