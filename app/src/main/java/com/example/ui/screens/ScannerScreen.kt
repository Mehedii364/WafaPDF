package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.Strings
import com.example.ui.components.CameraPermissionRationaleCard
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.PdfViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ScannerScreen(
  viewModel: PdfViewModel,
  onBack: () -> Unit,
  onNavigateToOcr: () -> Unit
) {
  val context = LocalContext.current
  val settings by viewModel.settings.collectAsState()
  val language = settings.language
  val scannedPages by viewModel.scannedPages.collectAsState()
  val isProcessing by viewModel.isProcessing.collectAsState()

  // Accompanist Permissions state for Camera Access
  val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

  var selectedProfile by remember { mutableStateOf("Document") }
  var selectedFilter by remember { mutableStateOf("ENHANCE") }
  var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
  var showSaveDialog by remember { mutableStateOf(false) }
  var docTitleInput by remember { mutableStateOf("Scan_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}") }

  val coroutineScope = rememberCoroutineScope()

  // Camera capture launcher
  val cameraLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicturePreview()
  ) { bmp: Bitmap? ->
    if (bmp != null) {
      coroutineScope.launch {
        val enhanced = viewModel.ocrEngine.enhanceImageForOcr(bmp, selectedFilter)
        val file = File(context.cacheDir, "scan_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
          enhanced.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        viewModel.addScannedPage(file.absolutePath)
        previewBitmap = enhanced
      }
    }
  }

  // Gallery image picker launcher
  val galleryLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri ->
    uri?.let {
      coroutineScope.launch {
        context.contentResolver.openInputStream(it)?.use { input ->
          val bmp = BitmapFactory.decodeStream(input)
          if (bmp != null) {
            val enhanced = viewModel.ocrEngine.enhanceImageForOcr(bmp, selectedFilter)
            val file = File(context.cacheDir, "scan_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
              enhanced.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            viewModel.addScannedPage(file.absolutePath)
            previewBitmap = enhanced
          }
        }
      }
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = Strings.get("action_scan", language),
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "${scannedPages.size} page(s) in batch",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("scanner_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          if (scannedPages.isNotEmpty()) {
            IconButton(onClick = { viewModel.clearScannedPages(); previewBitmap = null }) {
              Icon(Icons.Default.Delete, contentDescription = "Clear")
            }
            IconButton(onClick = { showSaveDialog = true }) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Save PDF",
                tint = MaterialTheme.colorScheme.primary
              )
            }
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
        .testTag("scanner_screen_content")
    ) {

      // Scan Profiles
      LazyRow(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val profiles = listOf("Document", "ID Card", "Receipt", "Book", "Business Card", "Notes")
        items(profiles.size) { idx ->
          val p = profiles[idx]
          FilterChip(
            selected = selectedProfile == p,
            onClick = { selectedProfile = p },
            label = { Text(p, style = MaterialTheme.typography.labelSmall) }
          )
        }
      }

      // Filter modes
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf(
          Pair("Color", "COLOR"),
          Pair("Enhanced", "ENHANCE"),
          Pair("Grayscale", "GRAYSCALE"),
          Pair("B&W", "BLACK_WHITE")
        ).forEach { (label, mode) ->
          FilterChip(
            selected = selectedFilter == mode,
            onClick = { selectedFilter = mode },
            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
          )
        }
      }

      // Viewfinder / Preview Area
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
      ) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          if (previewBitmap != null) {
            Image(
              bitmap = previewBitmap!!.asImageBitmap(),
              contentDescription = "Scan Preview",
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Fit
            )
          } else if (scannedPages.isNotEmpty()) {
            val lastPath = scannedPages.last()
            val bmp = remember(lastPath) { BitmapFactory.decodeFile(lastPath) }
            if (bmp != null) {
              Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "Last Scan",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
              )
            }
          } else if (!cameraPermissionState.status.isGranted) {
            CameraPermissionRationaleCard(
              title = "Camera Access for Scanner",
              message = "Wafa PDF requires camera permission to scan physical documents, detect edges, and capture high-quality pages.",
              shouldShowRationale = cameraPermissionState.status.shouldShowRationale,
              onRequestPermission = { cameraPermissionState.launchPermissionRequest() },
              onOpenSettings = {
                val intent = Intent(
                  Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                  Uri.fromParts("package", context.packageName, null)
                )
                context.startActivity(intent)
              }
            )
          } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.DocumentScanner,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "Document Scanner Ready",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Tap Camera or Photo below to scan",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          if (isProcessing) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)),
              contentAlignment = Alignment.Center
            ) {
              CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
          }
        }
      }

      // Scanned Pages Thumbnail Strip
      if (scannedPages.isNotEmpty()) {
        LazyRow(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          itemsIndexed(scannedPages) { index, path ->
            val bmp = remember(path) { BitmapFactory.decodeFile(path) }
            Card(
              modifier = Modifier
                .size(72.dp)
                .clickable { previewBitmap = bmp },
              shape = RoundedCornerShape(8.dp)
            ) {
              Box(modifier = Modifier.fillMaxSize()) {
                if (bmp != null) {
                  Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Page ${index + 1}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                  )
                }
                Text(
                  text = "${index + 1}",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White),
                  modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(topStart = 6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
          }
        }
      }

      // Capture controls
      Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Gallery import button
          IconButton(
            onClick = { galleryLauncher.launch("image/*") },
            modifier = Modifier.size(52.dp)
          ) {
            Icon(Icons.Default.Photo, contentDescription = "Gallery", modifier = Modifier.size(28.dp))
          }

          // Main Shutter / Camera Button
          Box(
            modifier = Modifier
              .size(72.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary)
              .clickable {
                if (cameraPermissionState.status.isGranted) {
                  cameraLauncher.launch(null)
                } else {
                  cameraPermissionState.launchPermissionRequest()
                }
              }
              .testTag("scanner_shutter_button"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.CameraAlt,
              contentDescription = "Capture",
              tint = Color.White,
              modifier = Modifier.size(34.dp)
            )
          }

          // OCR extract from scan
          IconButton(
            onClick = {
              if (scannedPages.isNotEmpty()) {
                onNavigateToOcr()
              } else {
                viewModel.showMessage("Scan or import an image first")
              }
            },
            modifier = Modifier.size(52.dp)
          ) {
            Icon(Icons.Default.TextFields, contentDescription = "OCR", modifier = Modifier.size(28.dp))
          }
        }
      }
    }
  }

  // Save Document Dialog
  if (showSaveDialog) {
    AlertDialog(
      onDismissRequest = { showSaveDialog = false },
      title = { Text("Save Scanned PDF") },
      text = {
        OutlinedTextField(
          value = docTitleInput,
          onValueChange = { docTitleInput = it },
          label = { Text("File Name") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )
      },
      confirmButton = {
        Button(
          onClick = {
            if (docTitleInput.isNotBlank()) {
              viewModel.saveScannedDocument(docTitleInput)
              showSaveDialog = false
            }
          }
        ) {
          Text("Save & Open")
        }
      },
      dismissButton = {
        TextButton(onClick = { showSaveDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
