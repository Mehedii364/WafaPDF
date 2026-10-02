package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppLanguage
import com.example.ui.Strings
import com.example.ui.viewmodel.AppScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WafaTopAppBar(
  title: String,
  subtitle: String? = null,
  showBack: Boolean = false,
  language: AppLanguage = AppLanguage.BANGLA,
  onBackClick: () -> Unit = {},
  onSearchClick: () -> Unit = {},
  onSettingsClick: () -> Unit = {},
  onVaultClick: () -> Unit = {}
) {
  var showMenu by remember { mutableStateOf(false) }

  TopAppBar(
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        if (!showBack) {
          Image(
            painter = painterResource(id = R.drawable.img_wafa_logo_1790900970624),
            contentDescription = "Wafa PDF Logo",
            modifier = Modifier
              .size(36.dp)
              .clip(RoundedCornerShape(8.dp))
              .padding(end = 6.dp)
          )
        }
        androidx.compose.foundation.layout.Column {
          Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
          )
          if (!subtitle.isNullOrBlank()) {
            Text(
              text = subtitle,
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    },
    navigationIcon = {
      if (showBack) {
        IconButton(
          onClick = onBackClick,
          modifier = Modifier.testTag("top_bar_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    },
    actions = {
      IconButton(
        onClick = onSearchClick,
        modifier = Modifier.testTag("top_bar_search_button")
      ) {
        Icon(
          imageVector = Icons.Default.Search,
          contentDescription = "Search",
          tint = MaterialTheme.colorScheme.onSurface
        )
      }
      IconButton(
        onClick = onSettingsClick,
        modifier = Modifier.testTag("top_bar_settings_button")
      ) {
        Icon(
          imageVector = Icons.Default.Settings,
          contentDescription = "Settings",
          tint = MaterialTheme.colorScheme.onSurface
        )
      }
      Box {
        IconButton(
          onClick = { showMenu = true },
          modifier = Modifier.testTag("top_bar_menu_button")
        ) {
          Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "More Options",
            tint = MaterialTheme.colorScheme.onSurface
          )
        }
        DropdownMenu(
          expanded = showMenu,
          onDismissRequest = { showMenu = false }
        ) {
          DropdownMenuItem(
            text = { Text(Strings.get("action_vault", language)) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            onClick = {
              showMenu = false
              onVaultClick()
            }
          )
          DropdownMenuItem(
            text = { Text(Strings.get("tab_settings", language)) },
            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
            onClick = {
              showMenu = false
              onSettingsClick()
            }
          )
        }
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.surface
    )
  )
}

@Composable
fun WafaBottomNavBar(
  currentScreen: AppScreen,
  language: AppLanguage,
  onTabSelected: (AppScreen) -> Unit
) {
  NavigationBar(
    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
    containerColor = MaterialTheme.colorScheme.surface,
    tonalElevation = 8.dp
  ) {
    val items = listOf(
      Triple(AppScreen.HOME, Strings.get("tab_home", language), Icons.Default.Home),
      Triple(AppScreen.FILES, Strings.get("tab_files", language), Icons.Default.Description),
      Triple(AppScreen.SCAN, Strings.get("tab_scan", language), Icons.Default.DocumentScanner),
      Triple(AppScreen.AI, Strings.get("tab_ai", language), Icons.Default.AutoAwesome),
      Triple(AppScreen.SETTINGS, Strings.get("tab_settings", language), Icons.Default.Settings)
    )

    items.forEach { (screen, label, icon) ->
      val selected = currentScreen == screen
      NavigationBarItem(
        selected = selected,
        onClick = { onTabSelected(screen) },
        icon = {
          Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp)
          )
        },
        label = {
          Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
          )
        },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = MaterialTheme.colorScheme.primary,
          selectedTextColor = MaterialTheme.colorScheme.primary,
          indicatorColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
      )
    }
  }
}
