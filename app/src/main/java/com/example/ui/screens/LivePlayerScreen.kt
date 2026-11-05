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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.example.ui.components.VideoRasterDisplay

@Composable
fun LivePlayerScreen(
  currentChannel: BroadcastChannel?,
  currentFrequencyMhz: Double,
  activeType: BroadcastType,
  channels: List<BroadcastChannel>,
  isRecording: Boolean,
  recordingSeconds: Int,
  onTuneFrequency: (Double) -> Unit,
  onStepFrequency: (Double) -> Unit,
  onSelectChannel: (BroadcastChannel) -> Unit,
  onSetBroadcastType: (BroadcastType) -> Unit,
  onToggleFavorite: (String) -> Unit,
  onToggleRecording: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showDirectDialDialog by remember { mutableStateOf(false) }
  var manualInputMhz by remember { mutableStateOf("") }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Mode Switcher: Watch TV vs Listen to FM Radio
    item {
      TabRow(
        selectedTabIndex = if (activeType == BroadcastType.TV) 0 else 1,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
      ) {
        Tab(
          selected = activeType == BroadcastType.TV,
          onClick = { onSetBroadcastType(BroadcastType.TV) },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Tv, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Watch Live TV", fontWeight = FontWeight.Bold)
            }
          },
          modifier = Modifier.testTag("tab_watch_tv")
        )
        Tab(
          selected = activeType == BroadcastType.RADIO,
          onClick = { onSetBroadcastType(BroadcastType.RADIO) },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Radio, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Listen FM Radio", fontWeight = FontWeight.Bold)
            }
          },
          modifier = Modifier.testTag("tab_listen_radio")
        )
      }
    }

    // Video / Audio Raster Display
    item {
      VideoRasterDisplay(
        channel = currentChannel,
        frequencyMhz = currentFrequencyMhz,
        isRecording = isRecording,
        recordingSeconds = recordingSeconds
      )
    }

    // Direct Tuning & DVR Action Bar
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          // Top controls row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "TUNED FREQUENCY",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace
              )
              Row(verticalAlignment = Alignment.Bottom) {
                Text(
                  text = String.format("%.2f", currentFrequencyMhz),
                  fontSize = 24.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary,
                  fontFamily = FontFamily.Monospace
                )
                Text(
                  text = " MHz",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.padding(bottom = 2.dp)
                )
              }
            }

            // Save Favorite & DVR REC Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
              if (currentChannel != null) {
                IconButton(
                  onClick = { onToggleFavorite(currentChannel.id) },
                  modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .size(42.dp)
                    .testTag("btn_favorite_channel")
                ) {
                  Icon(
                    if (currentChannel.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Save Channel",
                    tint = if (currentChannel.isFavorite) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurface
                  )
                }
              }

              // Big DVR Record Button
              Button(
                onClick = onToggleRecording,
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (isRecording) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("btn_dvr_record")
              ) {
                Icon(
                  if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (isRecording) "STOP REC" else "DVR REC",
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Frequency Step Buttons (-1.0, -0.1, +0.1, +1.0 MHz)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            StepButton("-1.0", onClick = { onStepFrequency(-1.0) }, tag = "btn_step_minus_1")
            StepButton("-0.1", onClick = { onStepFrequency(-0.1) }, tag = "btn_step_minus_01")

            Button(
              onClick = { showDirectDialDialog = !showDirectDialDialog },
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("btn_direct_dial")
            ) {
              Icon(
                Icons.Default.Tune,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text("Dial MHz", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp)
            }

            StepButton("+0.1", onClick = { onStepFrequency(0.1) }, tag = "btn_step_plus_01")
            StepButton("+1.0", onClick = { onStepFrequency(1.0) }, tag = "btn_step_plus_1")
          }

          // Optional Direct Input Field
          if (showDirectDialDialog) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              OutlinedTextField(
                value = manualInputMhz,
                onValueChange = { manualInputMhz = it },
                label = { Text("Enter Frequency in MHz (e.g. 101.5)") },
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("input_mhz_field")
              )
              Button(
                onClick = {
                  val freq = manualInputMhz.toDoubleOrNull()
                  if (freq != null && freq in 40.0..950.0) {
                    onTuneFrequency(freq)
                    showDirectDialDialog = false
                    manualInputMhz = ""
                  }
                },
                modifier = Modifier.testTag("btn_tune_confirm")
              ) {
                Text("Tune")
              }
            }
          }
        }
      }
    }

    // Channels Section Header ("Tap to watch / listen")
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (activeType == BroadcastType.TV) "TV BROADCAST CHANNELS" else "FM RADIO PRESETS",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          fontFamily = FontFamily.Monospace
        )
        Text(
          text = "Tap to tune live",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // Filtered channels list
    val filteredChannels = channels.filter { it.type == activeType }
    items(filteredChannels) { channel ->
      ChannelItemCard(
        channel = channel,
        isCurrent = currentChannel?.id == channel.id,
        onSelect = { onSelectChannel(channel) },
        onToggleFavorite = { onToggleFavorite(channel.id) }
      )
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun StepButton(text: String, onClick: () -> Unit, tag: String) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(MaterialTheme.colorScheme.surface)
      .clickable { onClick() }
      .padding(horizontal = 10.dp, vertical = 6.dp)
      .testTag(tag),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text,
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}

@Composable
private fun ChannelItemCard(
  channel: BroadcastChannel,
  isCurrent: Boolean,
  onSelect: () -> Unit,
  onToggleFavorite: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .clickable { onSelect() }
      .border(
        width = if (isCurrent) 1.5.dp else 0.dp,
        color = if (isCurrent) MaterialTheme.colorScheme.primary else Color.Transparent,
        shape = RoundedCornerShape(10.dp)
      )
      .testTag("channel_card_${channel.id}"),
    colors = CardDefaults.cardColors(
      containerColor = if (isCurrent) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
      } else {
        MaterialTheme.colorScheme.surfaceVariant
      }
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .background(
              if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
              CircleShape
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            if (channel.type == BroadcastType.TV) Icons.Default.Tv else Icons.Default.Radio,
            contentDescription = null,
            tint = if (isCurrent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = channel.name,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = channel.callSign,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.secondary,
              fontFamily = FontFamily.Monospace
            )
          }
          Text(
            text = channel.programTitle,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "${channel.frequencyMhz} MHz",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = "${channel.rssiDbm} dBm",
            fontSize = 10.sp,
            color = if (channel.qualityScore > 85) Color(0xFF00E676) else Color(0xFFFFB300),
            fontFamily = FontFamily.Monospace
          )
        }

        IconButton(
          onClick = onToggleFavorite,
          modifier = Modifier.size(36.dp)
        ) {
          Icon(
            if (channel.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
            contentDescription = "Favorite",
            tint = if (channel.isFavorite) Color(0xFFFFB300) else MaterialTheme.colorScheme.outline
          )
        }
      }
    }
  }
}
