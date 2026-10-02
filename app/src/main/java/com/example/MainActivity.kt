package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.data.model.AppLanguage
import com.example.ui.components.WafaBottomNavBar
import com.example.ui.screens.AiScreen
import com.example.ui.screens.FileManagerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OcrScreen
import com.example.ui.screens.ReaderScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.screens.VaultScreen
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.WafaPdfTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.PdfViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: PdfViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    handleIncomingIntent(intent)

    setContent {
      val settings by viewModel.settings.collectAsState()
      val currentScreen by viewModel.currentScreen.collectAsState()
      val statusMessage by viewModel.statusMessage.collectAsState()

      val themeMode = when (settings.theme) {
        "LIGHT" -> AppThemeMode.LIGHT
        "DARK" -> AppThemeMode.DARK
        "SEPIA" -> AppThemeMode.SEPIA
        else -> AppThemeMode.SYSTEM
      }

      val layoutDirection = if (settings.language == AppLanguage.ARABIC) {
        LayoutDirection.Rtl
      } else {
        LayoutDirection.Ltr
      }

      val snackbarHostState = remember { SnackbarHostState() }

      LaunchedEffect(statusMessage) {
        statusMessage?.let {
          snackbarHostState.showSnackbar(it)
          viewModel.clearMessage()
        }
      }

      CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        WafaPdfTheme(themeMode = themeMode) {
          Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
              val isPrimaryTab = currentScreen in listOf(
                AppScreen.HOME,
                AppScreen.FILES,
                AppScreen.SCAN,
                AppScreen.AI,
                AppScreen.SETTINGS
              )
              if (isPrimaryTab) {
                WafaBottomNavBar(
                  currentScreen = currentScreen,
                  language = settings.language,
                  onTabSelected = { viewModel.navigateTo(it) }
                )
              }
            }
          ) { innerPadding ->
            Box(
              modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
            ) {
              Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                when (screen) {
                  AppScreen.HOME -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateTo = { viewModel.navigateTo(it) }
                  )
                  AppScreen.FILES -> FileManagerScreen(
                    viewModel = viewModel,
                    onBack = { if (!viewModel.navigateBack()) viewModel.navigateTo(AppScreen.HOME) },
                    onOpenDoc = { viewModel.openDocument(it) }
                  )
                  AppScreen.SCAN -> ScannerScreen(
                    viewModel = viewModel,
                    onBack = { if (!viewModel.navigateBack()) viewModel.navigateTo(AppScreen.HOME) },
                    onNavigateToOcr = { viewModel.navigateTo(AppScreen.TOOL_OCR) }
                  )
                  AppScreen.AI -> AiScreen(
                    viewModel = viewModel,
                    onBack = { if (!viewModel.navigateBack()) viewModel.navigateTo(AppScreen.HOME) },
                    onNavigateToSettings = { viewModel.navigateTo(AppScreen.SETTINGS) }
                  )
                  AppScreen.SETTINGS -> SettingsScreen(
                    viewModel = viewModel,
                    onBack = { if (!viewModel.navigateBack()) viewModel.navigateTo(AppScreen.HOME) }
                  )
                  AppScreen.READER -> ReaderScreen(
                    viewModel = viewModel,
                    onBack = { if (!viewModel.navigateBack()) viewModel.navigateTo(AppScreen.HOME) },
                    onNavigateToAi = { viewModel.navigateTo(AppScreen.AI) }
                  )
                  AppScreen.TOOL_COMPRESS -> ToolsScreen(
                    initialTab = 0,
                    viewModel = viewModel,
                    onBack = { if (!viewModel.navigateBack()) viewModel.navigateTo(AppScreen.HOME) }
                  )
                  AppScreen.TOOL_WATERMARK -> ToolsScreen(
                    initialTab = 1,
                    viewModel = viewModel,
                    onBack = { if (!viewModel.navigateBack()) viewModel.navigateTo(AppScreen.HOME) }
                  )
                  AppScreen.TOOL_PRINT -> ToolsScreen(
                    initialTab = 2,
                    viewModel = viewModel,
                    onBack = { if (!viewModel.navigateBack()) viewModel.navigateTo(AppScreen.HOME) }
                  )
                  AppScreen.TOOL_MERGE -> ToolsScreen(
                    initialTab = 3,
                    viewModel = viewModel,
                    onBack = { if (!viewModel.navigateBack()) viewModel.navigateTo(AppScreen.HOME) }
                  )
                  AppScreen.TOOL_SPLIT -> ToolsScreen(
                    initialTab = 0,
                    viewModel = viewModel,
                    onBack = { if (!viewModel.navigateBack()) viewModel.navigateTo(AppScreen.HOME) }
                  )
                  AppScreen.TOOL_OCR -> OcrScreen(
                    viewModel = viewModel,
                    onBack = { if (!viewModel.navigateBack()) viewModel.navigateTo(AppScreen.SCAN) }
                  )
                  AppScreen.TOOL_FORMS -> ToolsScreen(
                    initialTab = 0,
                    viewModel = viewModel,
                    onBack = { if (!viewModel.navigateBack()) viewModel.navigateTo(AppScreen.HOME) }
                  )
                  AppScreen.VAULT -> VaultScreen(
                    viewModel = viewModel,
                    onBack = { if (!viewModel.navigateBack()) viewModel.navigateTo(AppScreen.HOME) }
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    handleIncomingIntent(intent)
  }

  private fun handleIncomingIntent(intent: Intent?) {
    if (intent == null) return
    val action = intent.action
    val type = intent.type

    when (action) {
      Intent.ACTION_VIEW -> {
        intent.data?.let { uri ->
          viewModel.importUri(uri)
        }
      }
      Intent.ACTION_SEND -> {
        val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        if (uri != null) {
          if (type?.startsWith("image/") == true) {
            // Add as scanned image
            viewModel.importUri(uri)
          } else {
            viewModel.importUri(uri)
          }
        }
      }
      Intent.ACTION_SEND_MULTIPLE -> {
        val uris = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
        uris?.forEach { uri ->
          viewModel.importUri(uri)
        }
      }
    }
  }
}
