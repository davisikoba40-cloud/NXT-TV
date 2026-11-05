package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.BroadcastType
import com.example.model.DvrRecording
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun OfflineDvrPlayerDialog(
  recording: DvrRecording,
  onDismiss: () -> Unit,
  onDelete: (String) -> Unit
) {
  var isPlaying by remember { mutableStateOf(true) }
  var currentPositionSeconds by remember { mutableFloatStateOf(0f) }
  val totalDuration = recording.durationSeconds.toFloat().coerceAtLeast(1f)

  // Simulation timer for playback scrub
  LaunchedEffect(isPlaying, recording.id) {
    while (isPlaying) {
      delay(500)
      if (currentPositionSeconds < totalDuration) {
        currentPositionSeconds = (currentPositionSeconds + 0.5f).coerceAtMost(totalDuration)
      } else {
        isPlaying = false
      }
    }
  }

  val infiniteTransition = rememberInfiniteTransition(label = "dvr_play_anim")
  val wavePhase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = (2 * PI).toFloat(),
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1400, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "dvr_wave"
  )

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .clip(RoundedCornerShape(16.dp))
        .background(MaterialTheme.colorScheme.surface)
        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
        .testTag("offline_dvr_player_dialog"),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        // Dialog Top Bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .background(Color(0xFF00E5FF).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "OFFLINE DVR PLAYBACK",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(32.dp).testTag("close_dvr_player")
          ) {
            Icon(Icons.Default.Close, contentDescription = "Close Player")
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Playback Screen Canvas
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black)
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
        ) {
          Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Background
            drawRect(
              brush = Brush.verticalGradient(
                listOf(Color(0xFF0F172A), Color(0xFF020617))
              )
            )

            if (recording.type == BroadcastType.TV) {
              // TV Playback graphics
              val wavePath = Path()
              wavePath.moveTo(0f, height * 0.55f)
              var x = 0f
              while (x <= width) {
                val y = height * 0.55f + sin((x / 50f) + wavePhase) * 20f
                wavePath.lineTo(x, y)
                x += 10f
              }
              wavePath.lineTo(width, height)
              wavePath.lineTo(0f, height)
              wavePath.close()

              drawPath(
                path = wavePath,
                brush = Brush.verticalGradient(
                  colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.35f), Color.Transparent),
                  startY = height * 0.5f,
                  endY = height
                )
              )
            } else {
              // Radio Playback graphics
              val audioWave = Path()
              audioWave.moveTo(0f, height / 2f)
              var x = 0f
              while (x <= width) {
                val y = height / 2f + sin((x / 30f) + wavePhase) * 35f
                audioWave.lineTo(x, y)
                x += 8f
              }
              drawPath(
                path = audioWave,
                color = Color(0xFFFFB300),
                style = Stroke(width = 3f)
              )
            }
          }

          // Top Info inside player
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                if (recording.type == BroadcastType.TV) Icons.Default.Tv else Icons.Default.Radio,
                contentDescription = null,
                tint = Color(0xFF00E5FF),
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "${recording.channelName} (${recording.frequencyMhz} MHz)",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }

            Text(
              text = "${recording.fileSizeBytes / (1024 * 1024)} MB | STORAGE",
              color = Color.LightGray,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace
            )
          }

          // Center Pause Indicator
          if (!isPlaying) {
            Box(
              modifier = Modifier
                .align(Alignment.Center)
                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
                .padding(12.dp)
            ) {
              Icon(
                Icons.Default.PlayArrow,
                contentDescription = "Paused",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Recording Title and Metadata
        Text(
          text = recording.title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "Local Storage: ${recording.filePath}",
          fontSize = 10.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Scrubbing Slider & Time
        val curMin = currentPositionSeconds.toInt() / 60
        val curSec = currentPositionSeconds.toInt() % 60
        val totMin = recording.durationSeconds / 60
        val totSec = recording.durationSeconds % 60

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = String.format("%02d:%02d", curMin, curSec),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = String.format("%02d:%02d", totMin, totSec),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Slider(
          value = currentPositionSeconds,
          onValueChange = { currentPositionSeconds = it },
          valueRange = 0f..totalDuration,
          colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.primary,
            activeTrackColor = MaterialTheme.colorScheme.primary
          ),
          modifier = Modifier.fillMaxWidth()
        )

        // Player Controls (Rewind, Play/Pause, Fast Forward, Delete)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = {
              currentPositionSeconds = (currentPositionSeconds - 10f).coerceAtLeast(0f)
            }
          ) {
            Icon(Icons.Default.FastRewind, contentDescription = "Rewind 10s")
          }

          IconButton(
            onClick = { isPlaying = !isPlaying },
            modifier = Modifier
              .size(54.dp)
              .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(27.dp))
          ) {
            Icon(
              if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
              contentDescription = if (isPlaying) "Pause" else "Play",
              tint = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.size(28.dp)
            )
          }

          IconButton(
            onClick = {
              currentPositionSeconds = (currentPositionSeconds + 10f).coerceAtMost(totalDuration)
            }
          ) {
            Icon(Icons.Default.FastForward, contentDescription = "Forward 10s")
          }

          IconButton(
            onClick = {
              onDelete(recording.id)
            }
          ) {
            Icon(
              Icons.Default.Delete,
              contentDescription = "Delete Recording",
              tint = MaterialTheme.colorScheme.error
            )
          }
        }
      }
    }
  }
}
