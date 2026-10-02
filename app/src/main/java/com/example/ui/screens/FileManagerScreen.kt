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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PdfDocumentEntity
import com.example.ui.Strings
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.PdfViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileManagerScreen(
  viewModel: PdfViewModel,
  onBack: () -> Unit,
  onOpenDoc: (PdfDocumentEntity) -> Unit
) {
  val settings by viewModel.settings.collectAsState()
  val language = settings.language
  val allDocs by viewModel.allDocuments.collectAsState()
  val recentDocs by viewModel.recentDocuments.collectAsState()
  val favDocs by viewModel.favoriteDocuments.collectAsState()
  val folders by viewModel.allFolders.collectAsState()

  BackHandler { onBack() }

  var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: Favorites, 2: Folders, 3: Trash
  var selectedFolder by remember { mutableStateOf("Documents") }
  var searchQuery by remember { mutableStateOf("") }
  var sortBy by remember { mutableStateOf("DATE") } // DATE, NAME, SIZE
  var showSortMenu by remember { mutableStateOf(false) }

  val displayedDocs = remember(selectedTab, selectedFolder, searchQuery, sortBy, allDocs, favDocs, recentDocs) {
    var list = when (selectedTab) {
      0 -> allDocs
      1 -> favDocs
      2 -> allDocs.filter { it.folder.equals(selectedFolder, ignoreCase = true) }
      else -> allDocs
    }

    if (searchQuery.isNotBlank()) {
      list = list.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
          (it.extractedText?.contains(searchQuery, ignoreCase = true) == true)
      }
    }

    when (sortBy) {
      "NAME" -> list.sortedBy { it.title.lowercase() }
      "SIZE" -> list.sortedByDescending { it.fileSize }
      else -> list.sortedByDescending { it.lastOpenedTimestamp }
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = Strings.get("tab_files", language),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("file_manager_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          Box {
            IconButton(onClick = { showSortMenu = true }) {
              Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
            }
            DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
              DropdownMenuItem(text = { Text("Sort by Date") }, onClick = { sortBy = "DATE"; showSortMenu = false })
              DropdownMenuItem(text = { Text("Sort by Name") }, onClick = { sortBy = "NAME"; showSortMenu = false })
              DropdownMenuItem(text = { Text("Sort by Size") }, onClick = { sortBy = "SIZE"; showSortMenu = false })
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
        .testTag("file_manager_screen_content")
    ) {

      // Search Bar
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text(Strings.get("search_hint", language)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
      )

      // Tabs Row
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface
      ) {
        Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("All PDFs") })
        Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Favorites") })
        Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Folders") })
      }

      // Folders Chip Bar if tab 2
      if (selectedTab == 2) {
        LazyRow(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(folders) { f ->
            FilterChip(
              selected = selectedFolder == f.name,
              onClick = { selectedFolder = f.name },
              label = { Text(f.name) },
              leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
          }
        }
      }

      // List of documents
      if (displayedDocs.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.Description,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
              modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = Strings.get("empty_documents", language),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(vertical = 8.dp)
        ) {
          items(displayedDocs) { doc ->
            DocumentItemRow(
              doc = doc,
              onOpen = { onOpenDoc(doc) },
              onFavorite = { viewModel.toggleFavorite(doc) },
              onCompress = {
                viewModel.selectDocumentForTools(doc)
                viewModel.navigateTo(AppScreen.TOOL_COMPRESS)
              },
              onWatermark = {
                viewModel.selectDocumentForTools(doc)
                viewModel.navigateTo(AppScreen.TOOL_WATERMARK)
              },
              onVault = { viewModel.moveToVault(doc) },
              onTrash = { viewModel.moveToTrash(doc) }
            )
          }
        }
      }
    }
  }
}
