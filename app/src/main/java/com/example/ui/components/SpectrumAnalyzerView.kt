package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.model.ConstellationPoint
import com.example.model.SpectrumPoint

@Composable
fun SpectrumAnalyzerView(
  spectrumPoints: List<SpectrumPoint>,
  constellationPoints: List<ConstellationPoint>,
  centerFrequencyMhz: Double,
  channel: BroadcastChannel?,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .testTag("spectrum_analyzer_view")
  ) {
    // Header telemetry metrics
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "RF SPECTRUM ANALYZER (FFT)",
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          fontFamily = FontFamily.Monospace
        )
        Text(
          text = "Center: ${String.format("%.2f", centerFrequencyMhz)} MHz | Span: 8.0 MHz",
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 11.sp
        )
      }

      val peakPower = spectrumPoints.maxOfOrNull { it.powerDbm } ?: -95f
      Box(
        modifier = Modifier
          .background(
            if (channel != null) Color(0xFF00E676).copy(alpha = 0.15f) else Color(0xFFFF1744).copy(alpha = 0.15f),
            RoundedCornerShape(6.dp)
          )
          .border(
            1.dp,
            if (channel != null) Color(0xFF00E676) else Color(0xFFFF1744),
            RoundedCornerShape(6.dp)
          )
          .padding(horizontal = 8.dp, vertical = 4.dp)
      ) {
        Text(
          text = if (channel != null) "LOCK: ${String.format("%.1f", peakPower)} dBm" else "UNLOCKED",
          color = if (channel != null) Color(0xFF00E676) else Color(0xFFFF1744),
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace
        )
      }
    }

    // FFT Spectrum Graph Canvas
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(180.dp)
        .background(Color(0xFF080D1A), RoundedCornerShape(10.dp))
        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
        .padding(6.dp)
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // dBm Horizontal Grid lines (-30, -50, -70, -90, -110 dBm)
        val dbmLabels = listOf(-30f, -50f, -70f, -90f, -110f)
        for (dbm in dbmLabels) {
          val normalizedY = (dbm - (-30f)) / (-110f - (-30f))
          val y = normalizedY * height
          drawLine(
            color = Color(0xFF1E293B),
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1f
          )
        }

        // Center Frequency Marker (Vertical dashed / line)
        val centerX = width / 2f
        drawLine(
          color = Color(0xFFFFB300).copy(alpha = 0.7f),
          start = Offset(centerX, 0f),
          end = Offset(centerX, height),
          strokeWidth = 1.5f
        )

        // Draw Channel Bandwidth shaded box if tuned to a channel
        if (channel != null) {
          val bwWidth = width * 0.45f
          drawRect(
            color = Color(0xFF00E5FF).copy(alpha = 0.08f),
            topLeft = Offset(centerX - (bwWidth / 2f), 0f),
            size = Size(bwWidth, height)
          )
        }

        // Plot Spectrum Points
        if (spectrumPoints.isNotEmpty()) {
          val curvePath = Path()
          val fillPath = Path()

          val minDbm = -110f
          val maxDbm = -25f

          val firstPoint = spectrumPoints.first()
          val firstNormY = 1f - ((firstPoint.powerDbm - minDbm) / (maxDbm - minDbm)).coerceIn(0f, 1f)
          val firstY = firstNormY * height

          curvePath.moveTo(0f, firstY)
          fillPath.moveTo(0f, height)
          fillPath.lineTo(0f, firstY)

          val stepX = width / (spectrumPoints.size - 1).coerceAtLeast(1)

          for (i in 1 until spectrumPoints.size) {
            val pt = spectrumPoints[i]
            val normY = 1f - ((pt.powerDbm - minDbm) / (maxDbm - minDbm)).coerceIn(0f, 1f)
            val px = i * stepX
            val py = normY * height
            curvePath.lineTo(px, py)
            fillPath.lineTo(px, py)
          }

          fillPath.lineTo(width, height)
          fillPath.close()

          // Draw Gradient Fill under curve
          drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
              colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.35f), Color.Transparent),
              startY = 0f,
              endY = height
            )
          )

          // Draw outline curve
          drawPath(
            path = curvePath,
            color = Color(0xFF00E5FF),
            style = Stroke(width = 2.5f)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Lower row: Constellation diagram + RF Metrics Card
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Constellation Box
      Card(
        modifier = Modifier.weight(1f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(10.dp)
      ) {
        Column(
          modifier = Modifier.padding(10.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "CONSTELLATION (I/Q)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            fontFamily = FontFamily.Monospace
          )
          Spacer(modifier = Modifier.height(6.dp))
          Box(
            modifier = Modifier
              .size(100.dp)
              .background(Color(0xFF080D1A), RoundedCornerShape(8.dp))
              .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
              .padding(4.dp)
          ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
              val w = size.width
              val h = size.height
              val midX = w / 2f
              val midY = h / 2f

              // Axes
              drawLine(Color(0xFF334155), Offset(midX, 0f), Offset(midX, h), 1f)
              drawLine(Color(0xFF334155), Offset(0f, midY), Offset(w, midY), 1f)

              // Points
              for (pt in constellationPoints) {
                val px = midX + (pt.i * (w * 0.42f))
                val py = midY - (pt.q * (h * 0.42f))
                drawCircle(
                  color = if (channel != null) Color(0xFF00E5FF) else Color(0xFFFF5252),
                  radius = 2.2f,
                  center = Offset(px, py)
                )
              }
            }
          }
        }
      }

      // RF Telemetry Stats Card
      Card(
        modifier = Modifier.weight(1.3f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(10.dp)
      ) {
        Column(
          modifier = Modifier.padding(10.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
            text = "RF SIGNAL METRICS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary,
            fontFamily = FontFamily.Monospace
          )

          MetricRow(label = "Modulation", value = channel?.modulation ?: "Unmodulated / Noise")
          MetricRow(label = "Signal Power", value = if (channel != null) "${channel.rssiDbm} dBm" else "-98 dBm")
          MetricRow(label = "SNR Quality", value = if (channel != null) "${channel.snrDb} dB" else "1.2 dB")
          MetricRow(label = "Bit Error Rate", value = if (channel != null) "< 1.2e-6" else "1.00 (No Sync)")
          MetricRow(label = "DSP Demod", value = if (channel != null) "Real-Time Locked" else "Searching")
        }
      }
    }
  }
}

@Composable
private fun MetricRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = label,
      fontSize = 10.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      text = value,
      fontSize = 10.sp,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurface,
      fontFamily = FontFamily.Monospace
    )
  }
}
