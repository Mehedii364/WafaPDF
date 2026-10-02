package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.Strings
import com.example.ui.viewmodel.PdfViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
  viewModel: PdfViewModel,
  onBack: () -> Unit
) {
  val settings by viewModel.settings.collectAsState()
  val language = settings.language
  val vaultDocs by viewModel.vaultDocuments.collectAsState()

  BackHandler { onBack() }

  var isUnlocked by remember { mutableStateOf(false) }
  var enteredPin by remember { mutableStateOf("") }
  var pinError by remember { mutableStateOf(false) }
  var showChangePinDialog by remember { mutableStateOf(false) }
  var newPinInput by remember { mutableStateOf("") }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = Strings.get("action_vault", language),
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("vault_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          if (isUnlocked) {
            IconButton(onClick = { showChangePinDialog = true }) {
              Icon(Icons.Default.VpnKey, contentDescription = "Change PIN")
            }
            IconButton(onClick = { isUnlocked = false; enteredPin = "" }) {
              Icon(Icons.Default.Lock, contentDescription = "Lock")
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .testTag("vault_screen_content")
    ) {
      if (!isUnlocked) {
        // PIN Entry Lock Screen
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Box(
            modifier = Modifier
              .size(72.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(36.dp)
            )
          }

          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = "Enter Vault PIN",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = "Default PIN is 1234",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(20.dp))
          OutlinedTextField(
            value = enteredPin,
            onValueChange = {
              if (it.length <= 6) {
                enteredPin = it
                pinError = false
              }
            },
            label = { Text("PIN") },
            visualTransformation = PasswordVisualTransformation(),
            isError = pinError,
            singleLine = true,
            modifier = Modifier.width(200.dp)
          )

          if (pinError) {
            Text(
              text = "Incorrect PIN. Try again.",
              color = MaterialTheme.colorScheme.error,
              style = MaterialTheme.typography.bodySmall,
              modifier = Modifier.padding(top = 4.dp)
            )
          }

          Spacer(modifier = Modifier.height(20.dp))
          Button(
            onClick = {
              if (enteredPin == settings.vaultPin) {
                isUnlocked = true
                pinError = false
              } else {
                pinError = true
              }
            },
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.LockOpen, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Unlock Vault")
          }
        }
      } else {
        // Vault Unlocked Document List
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
        ) {
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(12.dp)
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Documents here are encrypted and hidden from the standard file manager.",
                style = MaterialTheme.typography.bodySmall
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          if (vaultDocs.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
              Text("No documents currently stored in Private Vault.")
            }
          } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
              items(vaultDocs) { doc ->
                Card(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { viewModel.openDocument(doc) },
                  shape = RoundedCornerShape(12.dp),
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                      Text(text = doc.title, fontWeight = FontWeight.SemiBold)
                      Text(text = "${doc.pageCount} pgs", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { viewModel.restoreFromVault(doc) }) {
                      Icon(Icons.Default.Restore, contentDescription = "Restore")
                    }
                    IconButton(onClick = { viewModel.moveToTrash(doc) }) {
                      Icon(Icons.Default.DeleteForever, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // Change PIN Dialog
  if (showChangePinDialog) {
    AlertDialog(
      onDismissRequest = { showChangePinDialog = false },
      title = { Text("Change Vault PIN") },
      text = {
        OutlinedTextField(
          value = newPinInput,
          onValueChange = { if (it.length <= 6) newPinInput = it },
          label = { Text("Enter New PIN (4-6 digits)") },
          visualTransformation = PasswordVisualTransformation(),
          singleLine = true
        )
      },
      confirmButton = {
        Button(
          onClick = {
            if (newPinInput.length >= 4) {
              viewModel.prefsManager.updateVaultPin(newPinInput)
              viewModel.showMessage("Vault PIN updated successfully")
              newPinInput = ""
              showChangePinDialog = false
            }
          }
        ) {
          Text("Save PIN")
        }
      },
      dismissButton = {
        TextButton(onClick = { showChangePinDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
