package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BroadcastChannel
import com.example.model.BroadcastType
import com.example.model.ConstellationPoint
import com.example.model.FrequencyBand
import com.example.model.SpectrumPoint
import com.example.ui.components.SpectrumAnalyzerView

@Composable
fun SpectrumScannerScreen(
  spectrumPoints: List<SpectrumPoint>,
  constellationPoints: List<ConstellationPoint>,
  centerFrequencyMhz: Double,
  currentChannel: BroadcastChannel?,
  channels: List<BroadcastChannel>,
  isScanning: Boolean,
  scanProgress: Float,
  scanStatusText: String,
  onStartScan: (FrequencyBand) -> Unit,
  onStopScan: () -> Unit,
  onTuneChannel: (BroadcastChannel) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedBand by remember { mutableStateOf(FrequencyBand.UHF) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Band Selector chips
    item {
      Column {
        Text(
          text = "SELECT FREQUENCY BAND TO SCAN",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(FrequencyBand.values()) { band ->
            val isSelected = selectedBand == band
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                  if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                )
                .clickable {
                  if (!isScanning) selectedBand = band
                }
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .testTag("band_chip_${band.name}")
            ) {
              Text(
                text = band.label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }
    }

    // Scan Control Banner
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "FREQUENCY SCANNER ENGINE",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace
              )
              Text(
                text = "${selectedBand.minMhz} MHz - ${selectedBand.maxMhz} MHz",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Monospace
              )
            }

            Button(
              onClick = {
                if (isScanning) onStopScan() else onStartScan(selectedBand)
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isScanning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
              ),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("btn_trigger_scan")
            ) {
              Icon(
                if (isScanning) Icons.Default.Stop else Icons.Default.Radar,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(if (isScanning) "STOP SCAN" else "START SWEEP")
            }
          }

          if (isScanning || scanProgress > 0f) {
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
              progress = { scanProgress },
              modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
              color = MaterialTheme.colorScheme.primary,
              trackColor = MaterialTheme.colorScheme.surface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = scanStatusText,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary
              )
              Text(
                text = "${(scanProgress * 100).toInt()}%",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }
    }

    // Spectrum Analyzer Visualizer & Constellation Graph
    item {
      SpectrumAnalyzerView(
        spectrumPoints = spectrumPoints,
        constellationPoints = constellationPoints,
        centerFrequencyMhz = centerFrequencyMhz,
        channel = currentChannel
      )
    }

    // Discovered Frequencies & Channels
    item {
      Text(
        text = "DISCOVERED BROADCAST CHANNELS IN SPECTRUM",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        fontFamily = FontFamily.Monospace
      )
    }

    items(channels) { channel ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .border(
            1.dp,
            if (currentChannel?.id == channel.id) MaterialTheme.colorScheme.primary else Color.Transparent,
            RoundedCornerShape(10.dp)
          ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              if (channel.type == BroadcastType.TV) Icons.Default.Tv else Icons.Default.Radio,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = channel.name,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "${channel.frequencyMhz} MHz | ${channel.band.label} | ${channel.modulation}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Button(
            onClick = { onTuneChannel(channel) },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.testTag("tune_found_${channel.id}")
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Tune", fontSize = 11.sp)
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
