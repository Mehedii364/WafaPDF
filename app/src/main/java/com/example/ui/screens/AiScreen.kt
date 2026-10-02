package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.Strings
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.PdfViewModel
import java.text.SimpleDateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiScreen(
  viewModel: PdfViewModel,
  onBack: () -> Unit,
  onNavigateToSettings: () -> Unit
) {
  val context = LocalContext.current
  val settings by viewModel.settings.collectAsState()
  val language = settings.language
  val activeDoc by viewModel.activeDocument.collectAsState()
  val recentDocs by viewModel.recentDocuments.collectAsState()
  val aiResult by viewModel.aiResult.collectAsState()
  val aiLoading by viewModel.aiLoading.collectAsState()

  BackHandler { onBack() }

  val targetDoc = activeDoc ?: recentDocs.firstOrNull()

  var selectedTask by remember { mutableStateOf("SUMMARIZE") }
  var questionInput by remember { mutableStateOf("") }
  var showDocPickerMenu by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = Color(0xFFE91E63),
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = Strings.get("tab_ai", language) + " - " + Strings.get("ask_pdf_title", language),
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("ai_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(onClick = onNavigateToSettings) {
            Icon(Icons.Default.Settings, contentDescription = "AI Settings")
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
        .testTag("ai_screen_content")
    ) {

      // Document Context Header
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
              imageVector = Icons.Default.Description,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "Target Document",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = targetDoc?.title ?: "No Document Available",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          Box {
            TextButton(onClick = { showDocPickerMenu = true }) {
              Text("Switch")
            }
            DropdownMenu(
              expanded = showDocPickerMenu,
              onDismissRequest = { showDocPickerMenu = false }
            ) {
              recentDocs.forEach { doc ->
                DropdownMenuItem(
                  text = { Text(doc.title) },
                  onClick = {
                    viewModel.openDocument(doc)
                    showDocPickerMenu = false
                  }
                )
              }
            }
          }
        }
      }

      // AI Tasks Selector
      LazyRow(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val tasks = listOf(
          Pair(Strings.get("ai_summarize", language), "SUMMARIZE"),
          Pair(Strings.get("ai_ask", language), "ASK"),
          Pair(Strings.get("ai_explain", language), "EXPLAIN"),
          Pair(Strings.get("ai_translate", language), "TRANSLATE"),
          Pair(Strings.get("ai_notes", language), "NOTES"),
          Pair(Strings.get("ai_mcq", language), "MCQ"),
          Pair(Strings.get("ai_flashcards", language), "FLASHCARDS"),
          Pair(Strings.get("ai_keypoints", language), "KEYPOINTS"),
          Pair("Table", "TABLE"),
          Pair("Report", "REPORT")
        )

        items(tasks) { (label, task) ->
          FilterChip(
            selected = selectedTask == task,
            onClick = {
              selectedTask = task
              if (task != "ASK") {
                viewModel.executeAiTask(task)
              }
            },
            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
          )
        }
      }

      // Prompt input for "ASK" mode
      if (selectedTask == "ASK") {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = questionInput,
            onValueChange = { questionInput = it },
            placeholder = { Text("Ask anything about this document...") },
            modifier = Modifier.weight(1f),
            singleLine = true,
            shape = RoundedCornerShape(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          IconButton(
            onClick = {
              if (questionInput.isNotBlank()) {
                viewModel.executeAiTask("ASK", questionInput)
                questionInput = ""
              }
            },
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary)
          ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
          }
        }
      }

      // AI Output Card
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .padding(16.dp)
          .testTag("ai_result_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.fillMaxSize()) {
          // Card Header with Badge
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
              .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(Color(0xFFE91E63))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = "AI INSIGHT",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 9.sp
                  )
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Model: ${settings.aiModel.substringAfterLast("/")}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            if (aiResult.isNotBlank()) {
              Row {
                IconButton(onClick = {
                  val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                  cm.setPrimaryClip(ClipData.newPlainText("AI Insight", aiResult))
                  viewModel.showMessage("Copied to clipboard!")
                }) {
                  Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = {
                  // Export as PDF note
                  val title = "AI_Note_${SimpleDateFormat("yyyyMMdd_HHmm", java.util.Locale.getDefault()).format(java.util.Date())}"
                  viewModel.createPdfFromText(title, aiResult)
                }) {
                  Icon(Icons.Default.PictureAsPdf, contentDescription = "Export as PDF", modifier = Modifier.size(18.dp))
                }
              }
            }
          }

          // Card Body
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(16.dp)
          ) {
            if (aiLoading) {
              Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
              ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Analyzing document content...", style = MaterialTheme.typography.bodyMedium)
              }
            } else if (aiResult.isNotBlank()) {
              LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                  Text(
                    text = aiResult,
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
                  imageVector = Icons.Default.AutoAwesome,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                  modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = "Choose an AI action above to analyze the document",
                  style = MaterialTheme.typography.bodyMedium,
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
