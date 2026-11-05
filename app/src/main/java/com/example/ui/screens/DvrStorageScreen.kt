package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.StorageStats
import com.example.model.BroadcastType
import com.example.model.DvrRecording
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DvrStorageScreen(
  recordings: List<DvrRecording>,
  storageStats: StorageStats,
  onPlayRecording: (DvrRecording) -> Unit,
  onDeleteRecording: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val usedMb = storageStats.usedBytes / (1024 * 1024)
  val freeGb = storageStats.freeBytes / (1024 * 1024 * 1024)
  val totalGb = storageStats.totalBytes / (1024 * 1024 * 1024)
  val dvrMb = storageStats.dvrBytes / (1024 * 1024)
  val usedFraction = if (storageStats.totalBytes > 0) {
    (storageStats.usedBytes.toFloat() / storageStats.totalBytes.toFloat()).coerceIn(0f, 1f)
  } else 0.35f

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Storage Module Hardware Status Card
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.VideoLibrary,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "DVR STORAGE MODULE",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary,
                  fontFamily = FontFamily.Monospace
                )
                Text(
                  text = "Flash Module / Internal NVMe",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Box(
              modifier = Modifier
                .background(Color(0xFF00E676).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "STATUS: HEALTHY",
                color = Color(0xFF00E676),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          LinearProgressIndicator(
            progress = { usedFraction },
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surface
          )

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "DVR Captures: $dvrMb MB ($recordings.size items)",
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Available: $freeGb GB free of $totalGb GB",
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    // Recordings List Section Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "OFFLINE DVR RECORDINGS",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          fontFamily = FontFamily.Monospace
        )
        Text(
          text = "Tap item to play offline",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    if (recordings.isEmpty()) {
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          shape = RoundedCornerShape(10.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              Icons.Default.VideoLibrary,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.outline,
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "No DVR Recordings Yet",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Press 'DVR REC' on any live TV channel or FM radio to capture offline.",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    } else {
      items(recordings) { rec ->
        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        val dateStr = dateFormat.format(Date(rec.recordedAt))
        val min = rec.durationSeconds / 60
        val sec = rec.durationSeconds % 60
        val mb = rec.fileSizeBytes / (1024 * 1024)

        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onPlayRecording(rec) }
            .testTag("dvr_item_${rec.id}"),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .background(
                    if (rec.type == BroadcastType.TV) MaterialTheme.colorScheme.primary else Color(0xFFFFB300),
                    CircleShape
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  if (rec.type == BroadcastType.TV) Icons.Default.Tv else Icons.Default.Radio,
                  contentDescription = null,
                  tint = Color.Black,
                  modifier = Modifier.size(20.dp)
                )
              }

              Spacer(modifier = Modifier.width(10.dp))

              Column {
                Text(
                  text = rec.title,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "${rec.channelName} • ${rec.frequencyMhz} MHz",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.primary
                )
                Text(
                  text = "$dateStr • ${String.format("%02d:%02d", min, sec)} • $mb MB",
                  fontSize = 10.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontFamily = FontFamily.Monospace
                )
              }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              IconButton(
                onClick = { onPlayRecording(rec) },
                modifier = Modifier.size(36.dp).testTag("play_recording_${rec.id}")
              ) {
                Icon(
                  Icons.Default.PlayCircle,
                  contentDescription = "Play Recording",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(28.dp)
                )
              }

              IconButton(
                onClick = { onDeleteRecording(rec.id) },
                modifier = Modifier.size(36.dp).testTag("delete_recording_${rec.id}")
              ) {
                Icon(
                  Icons.Default.Delete,
                  contentDescription = "Delete Recording",
                  tint = MaterialTheme.colorScheme.error,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
