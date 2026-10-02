package com.example.ui.components

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

/**
 * Reusable Accompanist Permissions wrapper for Camera Access.
 * Handles permission request lifecycle, rationale explanations,
 * and graceful fallback to system settings if permanently denied.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPermissionWrapper(
  modifier: Modifier = Modifier,
  rationaleTitle: String = "Camera Access Required",
  rationaleMessage: String = "Wafa PDF requires camera permission to scan documents, detect paper edges, and capture high-resolution pages offline.",
  onPermissionGrantedContent: @Composable () -> Unit
) {
  val context = LocalContext.current
  val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

  if (cameraPermissionState.status.isGranted) {
    onPermissionGrantedContent()
  } else {
    CameraPermissionRationaleCard(
      modifier = modifier,
      title = rationaleTitle,
      message = rationaleMessage,
      shouldShowRationale = cameraPermissionState.status.shouldShowRationale,
      onRequestPermission = {
        cameraPermissionState.launchPermissionRequest()
      },
      onOpenSettings = {
        val intent = Intent(
          Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
          Uri.fromParts("package", context.packageName, null)
        )
        context.startActivity(intent)
      }
    )
  }
}

@Composable
fun CameraPermissionRationaleCard(
  modifier: Modifier = Modifier,
  title: String,
  message: String,
  shouldShowRationale: Boolean,
  onRequestPermission: () -> Unit,
  onOpenSettings: () -> Unit
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(16.dp)
      .testTag("camera_permission_rationale_card"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp),
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
          imageVector = Icons.Default.CameraAlt,
          contentDescription = "Camera Permission",
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(36.dp)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp
        ),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(20.dp))

      Button(
        onClick = onRequestPermission,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("grant_camera_permission_button")
      ) {
        Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Grant Camera Permission")
      }

      if (!shouldShowRationale) {
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
          onClick = onOpenSettings,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = "Open App Settings")
        }
      }
    }
  }
}
