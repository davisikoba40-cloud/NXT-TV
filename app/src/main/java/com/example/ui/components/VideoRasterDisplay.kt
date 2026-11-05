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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BroadcastChannel
import com.example.model.BroadcastType
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun VideoRasterDisplay(
  channel: BroadcastChannel?,
  frequencyMhz: Double,
  isRecording: Boolean,
  recordingSeconds: Int,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "raster_anim")
  val scanlineAnim by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 2400, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "scanline"
  )
  val wavePhase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = (2 * PI).toFloat(),
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1200, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "wave_phase"
  )

  Box(
    modifier = modifier
      .fillMaxWidth()
      .aspectRatio(16f / 10f)
      .clip(RoundedCornerShape(12.dp))
      .background(Color.Black)
      .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
      .testTag("video_raster_display")
  ) {
    if (channel != null && channel.type == BroadcastType.TV) {
      // Live TV Raster
      TvBroadcastCanvas(
        channel = channel,
        scanlineOffset = scanlineAnim,
        wavePhase = wavePhase
      )
    } else if (channel != null && channel.type == BroadcastType.RADIO) {
      // FM Radio Audio Visualizer
      FmRadioOscilloscopeCanvas(
        channel = channel,
        wavePhase = wavePhase
      )
    } else {
      // Off-frequency RF Static Snow
      OffFrequencyNoiseCanvas(scanlineAnim = scanlineAnim)
    }

    // Overlaid Status OSD
    TopOsdBar(
      channel = channel,
      frequencyMhz = frequencyMhz,
      isRecording = isRecording,
      recordingSeconds = recordingSeconds
    )

    // Bottom Lower-Third OSD
    if (channel != null) {
      BottomProgramOsd(channel = channel, isTv = channel.type == BroadcastType.TV)
    } else {
      Box(
        modifier = Modifier
          .align(Alignment.Center)
          .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
          .padding(horizontal = 16.dp, vertical = 8.dp)
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "NO RF CARRIER LOCK",
            color = Color(0xFFFF5252),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = "Tune ${String.format("%.2f", frequencyMhz)} MHz to active station",
            color = Color.LightGray,
            fontSize = 12.sp
          )
        }
      }
    }
  }
}

@Composable
private fun TopOsdBar(
  channel: BroadcastChannel?,
  frequencyMhz: Double,
  isRecording: Boolean,
  recordingSeconds: Int
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 10.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Frequency and Lock indicator
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(6.dp))
        .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
      Box(
        modifier = Modifier
          .size(8.dp)
          .clip(CircleShape)
          .background(if (channel != null) Color(0xFF00E676) else Color(0xFFFF1744))
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = "${String.format("%.2f", frequencyMhz)} MHz",
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace
      )
      if (channel != null) {
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (channel.type == BroadcastType.TV) "TV LOCK" else "FM STEREO",
          color = Color(0xFF00E5FF),
          fontSize = 10.sp,
          fontWeight = FontWeight.SemiBold
        )
      }
    }

    // DVR Recording Status
    if (isRecording) {
      val min = recordingSeconds / 60
      val sec = recordingSeconds % 60
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .background(Color(0xFFB71C1C).copy(alpha = 0.85f), RoundedCornerShape(6.dp))
          .padding(horizontal = 8.dp, vertical = 4.dp)
      ) {
        Icon(
          Icons.Default.FiberManualRecord,
          contentDescription = "Recording",
          tint = Color.White,
          modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "DVR REC ${String.format("%02d:%02d", min, sec)}",
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace
        )
      }
    } else {
      Row(
        modifier = Modifier
          .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(6.dp))
          .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "OFFLINE REAL-TIME",
          color = Color(0xFFFFD54F),
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}

@Composable
private fun BottomProgramOsd(channel: BroadcastChannel, isTv: Boolean) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(8.dp),
    contentAlignment = Alignment.BottomStart
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.verticalGradient(
            listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
          ),
          RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
        )
        .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            if (isTv) Icons.Default.Tv else Icons.Default.Radio,
            contentDescription = null,
            tint = Color(0xFF00E5FF),
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = channel.name,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "[${channel.callSign}]",
            color = Color(0xFFFFD54F),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
          )
        }

        Text(
          text = "${channel.rssiDbm} dBm | SNR ${channel.snrDb}dB",
          color = Color.LightGray,
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace
        )
      }

      Spacer(modifier = Modifier.height(2.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = channel.programTitle,
          color = Color(0xFFE2E8F0),
          fontSize = 11.sp
        )
        Text(
          text = channel.modulation,
          color = Color(0xFF94A3B8),
          fontSize = 9.sp
        )
      }
    }
  }
}

@Composable
private fun TvBroadcastCanvas(
  channel: BroadcastChannel,
  scanlineOffset: Float,
  wavePhase: Float
) {
  Canvas(modifier = Modifier.fillMaxSize()) {
    val width = size.width
    val height = size.height

    // Draw dynamic broadcast test pattern / visual raster bars
    val barColors = listOf(
      Color(0xFFE5E5E5), // Light Gray
      Color(0xFFFFEE58), // Yellow
      Color(0xFF26C6DA), // Cyan
      Color(0xFF66BB6A), // Green
      Color(0xFFAB47BC), // Magenta
      Color(0xFFEF5350), // Red
      Color(0xFF1E88E5)  // Blue
    )
    val barWidth = width / barColors.size
    for (i in barColors.indices) {
      drawRect(
        color = barColors[i].copy(alpha = 0.42f),
        topLeft = Offset(i * barWidth, 0f),
        size = Size(barWidth, height * 0.75f)
      )
    }

    // Modern studio wave graphic in lower section
    val wavePath = Path()
    wavePath.moveTo(0f, height * 0.5f)
    var x = 0f
    val step = 12f
    while (x <= width) {
      val y = height * 0.5f + (sin((x / 60f) + wavePhase) * 24f)
      wavePath.lineTo(x, y)
      x += step
    }
    wavePath.lineTo(width, height)
    wavePath.lineTo(0f, height)
    wavePath.close()

    drawPath(
      path = wavePath,
      brush = Brush.verticalGradient(
        colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.45f), Color(0xFF0F172A).copy(alpha = 0.85f)),
        startY = height * 0.4f,
        endY = height
      )
    )

    // Moving CRT scanlines
    val scanlineY = (scanlineOffset * height)
    drawLine(
      color = Color.White.copy(alpha = 0.18f),
      start = Offset(0f, scanlineY),
      end = Offset(width, scanlineY),
      strokeWidth = 3f
    )

    // Subtle horizontal grid lines
    for (i in 0..12) {
      val lineY = (height / 12) * i
      drawLine(
        color = Color.White.copy(alpha = 0.04f),
        start = Offset(0f, lineY),
        end = Offset(width, lineY),
        strokeWidth = 1f
      )
    }
  }
}

@Composable
private fun FmRadioOscilloscopeCanvas(
  channel: BroadcastChannel,
  wavePhase: Float
) {
  Canvas(modifier = Modifier.fillMaxSize()) {
    val width = size.width
    val height = size.height

    // Background radial dark glow
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.25f), Color.Transparent),
        center = Offset(width / 2f, height / 2f),
        radius = width / 2.5f
      )
    )

    // Stereo Audio Lissajous / Oscilloscope wave
    val leftPath = Path()
    val rightPath = Path()

    var x = 0f
    val step = 8f
    leftPath.moveTo(0f, height / 2f)
    rightPath.moveTo(0f, height / 2f)

    while (x <= width) {
      val yL = height / 2f + (sin((x / 40f) + wavePhase) * 32f) * sin((x / 140f) + wavePhase * 0.5f)
      val yR = height / 2f + (sin((x / 40f) + wavePhase + 1.2f) * 28f) * sin((x / 130f) + wavePhase * 0.5f)

      leftPath.lineTo(x, yL)
      rightPath.lineTo(x, yR)
      x += step
    }

    drawPath(
      path = leftPath,
      color = Color(0xFF00E5FF),
      style = Stroke(width = 3.5f)
    )
    drawPath(
      path = rightPath,
      color = Color(0xFFFFB300).copy(alpha = 0.8f),
      style = Stroke(width = 2.5f)
    )

    // Equalizer bars at bottom
    val barCount = 28
    val bWidth = (width / barCount) - 3f
    for (i in 0 until barCount) {
      val barHeight = (sin((i.toFloat() / barCount * PI.toFloat()) + wavePhase) * 0.5f + 0.5f) * (height * 0.35f)
      drawRect(
        brush = Brush.verticalGradient(
          colors = listOf(Color(0xFF00E676), Color(0xFF00B0FF)),
          startY = height - barHeight,
          endY = height
        ),
        topLeft = Offset(i * (bWidth + 3f), height - barHeight - 35f),
        size = Size(bWidth, barHeight)
      )
    }
  }
}

@Composable
private fun OffFrequencyNoiseCanvas(scanlineAnim: Float) {
  Canvas(modifier = Modifier.fillMaxSize()) {
    val width = size.width
    val height = size.height

    // Draw static analog noise dots
    for (i in 0..450) {
      val rx = Random.nextFloat() * width
      val ry = Random.nextFloat() * height
      val gray = Random.nextFloat() * 0.35f
      drawCircle(
        color = Color(gray, gray, gray),
        radius = Random.nextFloat() * 2f + 1f,
        center = Offset(rx, ry)
      )
    }

    // Heavy scanline
    val sy = scanlineAnim * height
    drawLine(
      color = Color.White.copy(alpha = 0.12f),
      start = Offset(0f, sy),
      end = Offset(width, sy),
      strokeWidth = 6f
    )
  }
}
