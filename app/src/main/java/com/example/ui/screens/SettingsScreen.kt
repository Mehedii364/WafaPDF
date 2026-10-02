package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppLanguage
import com.example.ui.Strings
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.PdfViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  viewModel: PdfViewModel,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  val settings by viewModel.settings.collectAsState()
  val language = settings.language

  BackHandler { onBack() }

  var showAiKeyDialog by remember { mutableStateOf(false) }
  var apiKeyInput by remember { mutableStateOf(settings.aiApiKey) }
  var showAboutDialog by remember { mutableStateOf(false) }
  var showLicensesDialog by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = Strings.get("tab_settings", language),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    }
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(horizontal = 16.dp)
        .testTag("settings_screen_content"),
      contentPadding = PaddingValues(vertical = 12.dp)
    ) {

      // SECTION 1: Appearance
      item {
        SettingsSectionHeader("Appearance & Language")

        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            // Language Picker
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Text("App Language", fontWeight = FontWeight.SemiBold)
              }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              AppLanguage.entries.forEach { lang ->
                FilterChip(
                  selected = settings.language == lang,
                  onClick = { viewModel.prefsManager.updateLanguage(lang) },
                  label = { Text(lang.displayName, fontSize = 11.sp) }
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Theme Picker
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Text("App Theme", fontWeight = FontWeight.SemiBold)
              }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              listOf("SYSTEM", "LIGHT", "DARK", "SEPIA").forEach { mode ->
                FilterChip(
                  selected = settings.theme == mode,
                  onClick = { viewModel.prefsManager.updateTheme(mode) },
                  label = { Text(mode.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 11.sp) }
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Keep Screen Awake
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Visibility, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text("Keep Screen Awake", fontWeight = FontWeight.SemiBold)
                  Text("Prevent screen timeout while reading", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }
              Switch(
                checked = settings.keepScreenAwake,
                onCheckedChange = { viewModel.prefsManager.updateKeepScreenAwake(it) }
              )
            }
          }
        }
      }

      // SECTION 2: AI Provider Settings
      item {
        Spacer(modifier = Modifier.height(16.dp))
        SettingsSectionHeader("AI PDF Provider")

        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text("AI API Configuration", fontWeight = FontWeight.SemiBold)
                  Text("OpenRouter / Gemini Model Integration", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }
              Button(onClick = { apiKeyInput = settings.aiApiKey; showAiKeyDialog = true }) {
                Text(if (settings.aiApiKey.isBlank()) "Set Key" else "Edit Key")
              }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "Status: ${if (settings.aiApiKey.isBlank()) "Offline Mode (Local Synthetic Intelligence)" else "Active (Live Cloud Inference)"}",
              fontSize = 12.sp,
              color = if (settings.aiApiKey.isBlank()) MaterialTheme.colorScheme.primary else Color(0xFF10B981)
            )
          }
        }
      }

      // SECTION 3: Storage & Privacy
      item {
        Spacer(modifier = Modifier.height(16.dp))
        SettingsSectionHeader("Storage & Security")

        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  viewModel.navigateTo(AppScreen.VAULT)
                }
                .padding(vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text("Private Vault", fontWeight = FontWeight.SemiBold)
                Text("PIN-protected encrypted storage for sensitive documents", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  // Clear cache
                  context.cacheDir.deleteRecursively()
                  viewModel.showMessage("Cache cleared successfully!")
                }
                .padding(vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.CleaningServices, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text("Clear Cache & Temporary Files", fontWeight = FontWeight.SemiBold)
                Text("Frees up storage without deleting saved PDFs", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          }
        }
      }

      // SECTION 4: About
      item {
        Spacer(modifier = Modifier.height(16.dp))
        SettingsSectionHeader("About & Developer Credit")

        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Image(
                painter = painterResource(id = R.drawable.img_wafa_logo_1790900970624),
                contentDescription = null,
                modifier = Modifier
                  .size(48.dp)
                  .clip(RoundedCornerShape(10.dp))
              )
              Spacer(modifier = Modifier.width(14.dp))
              Column {
                Text(text = "Wafa PDF", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text(text = "Version 1.0.0 (Release Build)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                  text = "Made with ❤️ by Mehedi364",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium,
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              TextButton(onClick = { showAboutDialog = true }) {
                Text("About Wafa PDF")
              }
              TextButton(onClick = { showLicensesDialog = true }) {
                Text("Open Source Licenses")
              }
            }
          }
        }
      }
    }
  }

  // Dialog: AI API Key configuration
  if (showAiKeyDialog) {
    AlertDialog(
      onDismissRequest = { showAiKeyDialog = false },
      title = { Text("AI Provider Settings") },
      text = {
        Column {
          Text("Configure OpenRouter API Key for live AI PDF chat, translation, and summary.", fontSize = 12.sp)
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = apiKeyInput,
            onValueChange = { apiKeyInput = it },
            label = { Text("OpenRouter API Key") },
            placeholder = { Text("sk-or-v1-...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
        }
      },
      confirmButton = {
        Button(onClick = {
          viewModel.prefsManager.updateAiConfig(
            provider = "OpenRouter",
            apiKey = apiKeyInput.trim(),
            model = settings.aiModel
          )
          viewModel.showMessage("AI settings saved")
          showAiKeyDialog = false
        }) {
          Text("Save")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAiKeyDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Dialog: About App
  if (showAboutDialog) {
    AlertDialog(
      onDismissRequest = { showAboutDialog = false },
      title = { Text("Wafa PDF") },
      text = {
        Column {
          Text("PDF & Document Toolkit by Mehedi364\n", fontWeight = FontWeight.Bold)
          Text("Wafa PDF is engineered for speed, privacy, and full offline autonomy. Built using modern Android architecture, Jetpack Compose, and native graphics PDF engines.")
          Spacer(modifier = Modifier.height(8.dp))
          Text("Features: PDF Reader, Document Scanner, Compressor, Watermark Studio, Ready Print, Multilingual OCR (Bangla, English, Arabic), and AI Document Assistant.")
          Spacer(modifier = Modifier.height(8.dp))
          Text("Developer: Mehedi364", fontWeight = FontWeight.SemiBold)
        }
      },
      confirmButton = {
        Button(onClick = { showAboutDialog = false }) { Text("OK") }
      }
    )
  }

  // Dialog: Licenses
  if (showLicensesDialog) {
    AlertDialog(
      onDismissRequest = { showLicensesDialog = false },
      title = { Text("Open Source Licenses") },
      text = {
        Column {
          Text("• Android Jetpack & Compose (Apache 2.0)")
          Text("• Kotlin Coroutines & Flow (Apache 2.0)")
          Text("• Room Persistence Library (Apache 2.0)")
          Text("• Coil Image Loader (Apache 2.0)")
          Text("• OkHttp & Retrofit (Apache 2.0)")
        }
      },
      confirmButton = {
        Button(onClick = { showLicensesDialog = false }) { Text("Close") }
      }
    )
  }
}

@Composable
fun SettingsSectionHeader(title: String) {
  Text(
    text = title,
    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
    color = MaterialTheme.colorScheme.primary,
    modifier = Modifier.padding(bottom = 6.dp, top = 4.dp)
  )
}
