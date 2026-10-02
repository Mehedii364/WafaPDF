package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PdfDocumentEntity
import com.example.pdf.WatermarkConfig
import com.example.ui.Strings
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.PdfViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
  initialTab: Int = 0,
  viewModel: PdfViewModel,
  onBack: () -> Unit
) {
  var selectedTab by remember { mutableIntStateOf(initialTab) }
  val settings by viewModel.settings.collectAsState()
  val language = settings.language
  val activeDoc by viewModel.activeDocument.collectAsState()
  val recentDocs by viewModel.recentDocuments.collectAsState()
  val isProcessing by viewModel.isProcessing.collectAsState()

  BackHandler { onBack() }

  val doc = activeDoc ?: recentDocs.firstOrNull()

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = when (selectedTab) {
              0 -> Strings.get("tool_compress_title", language)
              1 -> Strings.get("tool_watermark_title", language)
              2 -> Strings.get("tool_print_title", language)
              3 -> Strings.get("action_merge", language)
              else -> "PDF Tools"
            },
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("tools_back_button")) {
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
        .testTag("tools_screen_content")
    ) {

      // Tabs Header
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = { Text("Compress") },
          icon = { Icon(Icons.Default.LinearScale, contentDescription = null, modifier = Modifier.size(18.dp)) }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = { Text("Watermark") },
          icon = { Icon(Icons.Default.WaterDrop, contentDescription = null, modifier = Modifier.size(18.dp)) }
        )
        Tab(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          text = { Text("Ready Print") },
          icon = { Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp)) }
        )
        Tab(
          selected = selectedTab == 3,
          onClick = { selectedTab = 3 },
          text = { Text("Merge") },
          icon = { Icon(Icons.Default.CallMerge, contentDescription = null, modifier = Modifier.size(18.dp)) }
        )
      }

      if (isProcessing) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Processing PDF operation...")
          }
        }
        return@Scaffold
      }

      when (selectedTab) {
        0 -> CompressToolView(doc = doc, viewModel = viewModel)
        1 -> WatermarkToolView(doc = doc, viewModel = viewModel)
        2 -> PrintToolView(doc = doc, viewModel = viewModel)
        3 -> MergeToolView(allDocs = recentDocs, viewModel = viewModel)
      }
    }
  }
}

@Composable
fun CompressToolView(doc: PdfDocumentEntity?, viewModel: PdfViewModel) {
  var selectedPreset by remember { mutableStateOf("Balanced") }
  var qualitySlider by remember { mutableFloatStateOf(75f) }
  var toGrayscale by remember { mutableStateOf(false) }

  if (doc == null) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text("No document available to compress")
    }
    return
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(text = "Target File: ${doc.title}", fontWeight = FontWeight.Bold)
          Text(text = "Original Size: ${doc.fileSize / 1024} KB • ${doc.pageCount} Pages", fontSize = 12.sp)
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
      Text("Optimization Presets", fontWeight = FontWeight.SemiBold)
      Spacer(modifier = Modifier.height(8.dp))

      val presets = listOf("Minimize Size", "Balanced", "High Quality", "WhatsApp Optimize", "Web Optimize")
      presets.forEach { preset ->
        FilterChip(
          selected = selectedPreset == preset,
          onClick = {
            selectedPreset = preset
            when (preset) {
              "Minimize Size" -> { qualitySlider = 45f; toGrayscale = true }
              "Balanced" -> { qualitySlider = 70f; toGrayscale = false }
              "High Quality" -> { qualitySlider = 90f; toGrayscale = false }
              "WhatsApp Optimize" -> { qualitySlider = 50f; toGrayscale = false }
              "Web Optimize" -> { qualitySlider = 60f; toGrayscale = false }
            }
          },
          label = { Text(preset) },
          modifier = Modifier.padding(end = 6.dp)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))
      Text("JPEG Quality: ${qualitySlider.toInt()}%")
      Slider(
        value = qualitySlider,
        onValueChange = { qualitySlider = it },
        valueRange = 25f..95f
      )

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Convert to Grayscale (B&W)")
        Switch(checked = toGrayscale, onCheckedChange = { toGrayscale = it })
      }

      Spacer(modifier = Modifier.height(24.dp))
      Button(
        onClick = {
          val scale = when (selectedPreset) {
            "Minimize Size" -> 0.55f
            "WhatsApp Optimize" -> 0.65f
            "High Quality" -> 0.9f
            else -> 0.75f
          }
          viewModel.compressDocument(
            doc = doc,
            scaleFactor = scale,
            quality = qualitySlider.toInt(),
            toGrayscale = toGrayscale
          )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(Icons.Default.LinearScale, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Start Compression")
      }
    }
  }
}

@Composable
fun WatermarkToolView(doc: PdfDocumentEntity?, viewModel: PdfViewModel) {
  var watermarkText by remember { mutableStateOf("CONFIDENTIAL") }
  var opacitySlider by remember { mutableFloatStateOf(0.35f) }
  var rotationSlider by remember { mutableFloatStateOf(-35f) }
  var isTiled by remember { mutableStateOf(false) }
  var includeDate by remember { mutableStateOf(true) }
  var includePage by remember { mutableStateOf(true) }

  if (doc == null) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text("No document available to watermark")
    }
    return
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    item {
      OutlinedTextField(
        value = watermarkText,
        onValueChange = { watermarkText = it },
        label = { Text("Watermark Text") },
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(16.dp))
      Text("Watermark Presets")
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("CONFIDENTIAL", "DRAFT", "APPROVED", "VERIFIED").forEach { p ->
          FilterChip(
            selected = watermarkText == p,
            onClick = { watermarkText = p },
            label = { Text(p, fontSize = 11.sp) }
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
      Text("Opacity: ${(opacitySlider * 100).toInt()}%")
      Slider(value = opacitySlider, onValueChange = { opacitySlider = it }, valueRange = 0.1f..0.9f)

      Text("Rotation: ${rotationSlider.toInt()}°")
      Slider(value = rotationSlider, onValueChange = { rotationSlider = it }, valueRange = -90f..90f)

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Tiled Watermark Pattern")
        Switch(checked = isTiled, onCheckedChange = { isTiled = it })
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Append Current Date")
        Switch(checked = includeDate, onCheckedChange = { includeDate = it })
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Append Page Number")
        Switch(checked = includePage, onCheckedChange = { includePage = it })
      }

      Spacer(modifier = Modifier.height(24.dp))
      Button(
        onClick = {
          viewModel.watermarkDocument(
            doc = doc,
            config = WatermarkConfig(
              text = watermarkText,
              opacity = opacitySlider,
              rotation = rotationSlider,
              isTiled = isTiled,
              includeDate = includeDate,
              includePageNumber = includePage
            )
          )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(Icons.Default.WaterDrop, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Apply Watermark & Export")
      }
    }
  }
}

@Composable
fun PrintToolView(doc: PdfDocumentEntity?, viewModel: PdfViewModel) {
  var selectedLayout by remember { mutableIntStateOf(2) } // 2-up or 4-up
  var paperSize by remember { mutableStateOf("A4") }

  if (doc == null) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text("No document available for printing")
    }
    return
  }

  Column(modifier = Modifier
    .fillMaxSize()
    .padding(16.dp)) {
    Text("Print Layout Mode", fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      FilterChip(
        selected = selectedLayout == 2,
        onClick = { selectedLayout = 2 },
        label = { Text("2 Pages Per Sheet (2-Up)") }
      )
      FilterChip(
        selected = selectedLayout == 4,
        onClick = { selectedLayout = 4 },
        label = { Text("4 Pages (4-Up)") }
      )
    }

    Spacer(modifier = Modifier.height(16.dp))
    Text("Paper Size", fontWeight = FontWeight.Bold)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      listOf("A4", "A5", "Letter", "Legal").forEach { sz ->
        FilterChip(
          selected = paperSize == sz,
          onClick = { paperSize = sz },
          label = { Text(sz) }
        )
      }
    }

    Spacer(modifier = Modifier.height(24.dp))
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text("Ready Print Features:", fontWeight = FontWeight.SemiBold)
        Text("• Consolidates multiple document pages per physical sheet", fontSize = 12.sp)
        Text("• Fits standard margins with duplex/booklet orientation", fontSize = 12.sp)
        Text("• Produces high-resolution print-ready PDF", fontSize = 12.sp)
      }
    }

    Spacer(modifier = Modifier.weight(1f))
    Button(
      onClick = { viewModel.preparePrintNUp(doc, selectedLayout) },
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp)
    ) {
      Icon(Icons.Default.Print, contentDescription = null)
      Spacer(modifier = Modifier.width(8.dp))
      Text("Generate Print PDF ($selectedLayout-up)")
    }
  }
}

@Composable
fun MergeToolView(allDocs: List<PdfDocumentEntity>, viewModel: PdfViewModel) {
  val selectedDocs = remember { mutableStateListOf<PdfDocumentEntity>() }
  var outputTitle by remember { mutableStateOf("Merged_Document") }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    OutlinedTextField(
      value = outputTitle,
      onValueChange = { outputTitle = it },
      label = { Text("Merged Output File Name") },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true
    )

    Spacer(modifier = Modifier.height(12.dp))
    Text(
      text = "Select Documents to Merge (${selectedDocs.size} selected):",
      fontWeight = FontWeight.Bold
    )

    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .padding(vertical = 8.dp)
    ) {
      items(allDocs) { doc ->
        val isChecked = selectedDocs.contains(doc)
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
              if (isChecked) selectedDocs.remove(doc) else selectedDocs.add(doc)
            },
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
          )
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Checkbox(
              checked = isChecked,
              onCheckedChange = { check ->
                if (check) selectedDocs.add(doc) else selectedDocs.remove(doc)
              }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(text = doc.title, fontWeight = FontWeight.SemiBold)
              Text(text = "${doc.pageCount} pages", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
      }
    }

    Button(
      onClick = {
        if (selectedDocs.size >= 2) {
          viewModel.mergeDocuments(selectedDocs.toList(), outputTitle)
        } else {
          viewModel.showMessage("Please select at least 2 PDFs to merge")
        }
      },
      modifier = Modifier.fillMaxWidth(),
      enabled = selectedDocs.size >= 2,
      shape = RoundedCornerShape(12.dp)
    ) {
      Icon(Icons.Default.CallMerge, contentDescription = null)
      Spacer(modifier = Modifier.width(8.dp))
      Text("Merge ${selectedDocs.size} Documents")
    }
  }
}
