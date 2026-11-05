package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppTab
import com.example.ui.TunerViewModel
import com.example.ui.components.OfflineDvrPlayerDialog
import com.example.ui.screens.DvrStorageScreen
import com.example.ui.screens.HardwareRemoteScreen
import com.example.ui.screens.LivePlayerScreen
import com.example.ui.screens.SpectrumScannerScreen
import com.example.ui.theme.NXTTVTheme

class MainActivity : ComponentActivity() {
  private val viewModel: TunerViewModel by viewModels()

  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
      val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
      val currentChannel by viewModel.currentChannel.collectAsStateWithLifecycle()
      val currentFrequencyMhz by viewModel.currentFrequencyMhz.collectAsStateWithLifecycle()
      val activeType by viewModel.activeType.collectAsStateWithLifecycle()
      val channels by viewModel.channels.collectAsStateWithLifecycle()
      val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
      val recordingSeconds by viewModel.recordingSeconds.collectAsStateWithLifecycle()
      val recordings by viewModel.recordings.collectAsStateWithLifecycle()
      val storageStats by viewModel.storageStats.collectAsStateWithLifecycle()
      val spectrumData by viewModel.spectrumData.collectAsStateWithLifecycle()
      val constellationData by viewModel.constellationData.collectAsStateWithLifecycle()
      val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
      val scanProgress by viewModel.scanProgress.collectAsStateWithLifecycle()
      val scanStatusText by viewModel.scanStatusText.collectAsStateWithLifecycle()
      val hardwareConfig by viewModel.hardwareConfig.collectAsStateWithLifecycle()
      val isTestingHardware by viewModel.isTestingHardware.collectAsStateWithLifecycle()
      val hardwareTestResult by viewModel.hardwareTestResult.collectAsStateWithLifecycle()
      val playingRecording by viewModel.playingRecording.collectAsStateWithLifecycle()

      NXTTVTheme(darkTheme = isDarkTheme) {
        Scaffold(
          modifier = Modifier.fillMaxSize(),
          topBar = {
            TopAppBar(
              colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
              ),
              title = {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                  modifier = Modifier.fillMaxWidth().padding(end = 8.dp)
                ) {
                  Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(
                        text = "NXT TV",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace
                      )
                      Spacer(modifier = Modifier.width(6.dp))
                      Box(
                        modifier = Modifier
                          .clip(RoundedCornerShape(4.dp))
                          .background(Color(0xFF00E5FF).copy(alpha = 0.2f))
                          .padding(horizontal = 6.dp, vertical = 2.dp)
                      ) {
                        Text(
                          text = "OFFLINE",
                          color = MaterialTheme.colorScheme.primary,
                          fontSize = 10.sp,
                          fontWeight = FontWeight.Bold,
                          fontFamily = FontFamily.Monospace
                        )
                      }
                    }
                    Text(
                      text = "POWERED BY MOBILABS",
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.secondary,
                      fontFamily = FontFamily.Monospace
                    )
                  }

                  // Hardware & Dark Mode Toggle Controls
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    // Lock indicator
                    Box(
                      modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (currentChannel != null) Color(0xFF00E676) else Color(0xFFFF1744))
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                      onClick = { viewModel.toggleTheme() },
                      modifier = Modifier.testTag("btn_toggle_dark_mode")
                    ) {
                      Icon(
                        if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Light/Dark Mode",
                        tint = MaterialTheme.colorScheme.primary
                      )
                    }
                  }
                }
              }
            )
          },
          bottomBar = {
            NavigationBar(
              containerColor = MaterialTheme.colorScheme.surface,
              contentColor = MaterialTheme.colorScheme.onSurface
            ) {
              NavigationBarItem(
                selected = currentTab == AppTab.LIVE,
                onClick = { viewModel.setTab(AppTab.LIVE) },
                icon = { Icon(Icons.Default.Tv, contentDescription = "Live Tuner") },
                label = { Text("Live Tuner", fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                  indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                  selectedIconColor = MaterialTheme.colorScheme.primary,
                  selectedTextColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag("nav_tab_live")
              )
              NavigationBarItem(
                selected = currentTab == AppTab.SPECTRUM,
                onClick = { viewModel.setTab(AppTab.SPECTRUM) },
                icon = { Icon(Icons.Default.Radar, contentDescription = "Analyzer & Scan") },
                label = { Text("Analyzer", fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                  indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                  selectedIconColor = MaterialTheme.colorScheme.primary,
                  selectedTextColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag("nav_tab_spectrum")
              )
              NavigationBarItem(
                selected = currentTab == AppTab.DVR,
                onClick = { viewModel.setTab(AppTab.DVR) },
                icon = { Icon(Icons.Default.VideoLibrary, contentDescription = "DVR Storage") },
                label = { Text("DVR (${recordings.size})", fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                  indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                  selectedIconColor = MaterialTheme.colorScheme.primary,
                  selectedTextColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag("nav_tab_dvr")
              )
              NavigationBarItem(
                selected = currentTab == AppTab.HARDWARE,
                onClick = { viewModel.setTab(AppTab.HARDWARE) },
                icon = { Icon(Icons.Default.Memory, contentDescription = "Hardware") },
                label = { Text("Hardware", fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                  indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                  selectedIconColor = MaterialTheme.colorScheme.primary,
                  selectedTextColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag("nav_tab_hardware")
              )
            }
          }
        ) { paddingValues ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(paddingValues)
              .background(MaterialTheme.colorScheme.background)
          ) {
            when (currentTab) {
              AppTab.LIVE -> {
                LivePlayerScreen(
                  currentChannel = currentChannel,
                  currentFrequencyMhz = currentFrequencyMhz,
                  activeType = activeType,
                  channels = channels,
                  isRecording = isRecording,
                  recordingSeconds = recordingSeconds,
                  onTuneFrequency = { viewModel.tuneToFrequency(it) },
                  onStepFrequency = { viewModel.stepFrequency(it) },
                  onSelectChannel = { viewModel.selectChannel(it) },
                  onSetBroadcastType = { viewModel.setBroadcastType(it) },
                  onToggleFavorite = { viewModel.toggleFavorite(it) },
                  onToggleRecording = { viewModel.toggleRecording() }
                )
              }
              AppTab.SPECTRUM -> {
                SpectrumScannerScreen(
                  spectrumPoints = spectrumData,
                  constellationPoints = constellationData,
                  centerFrequencyMhz = currentFrequencyMhz,
                  currentChannel = currentChannel,
                  channels = channels,
                  isScanning = isScanning,
                  scanProgress = scanProgress,
                  scanStatusText = scanStatusText,
                  onStartScan = { viewModel.startScan(it) },
                  onStopScan = { viewModel.stopScan() },
                  onTuneChannel = { viewModel.selectChannel(it); viewModel.setTab(AppTab.LIVE) }
                )
              }
              AppTab.DVR -> {
                DvrStorageScreen(
                  recordings = recordings,
                  storageStats = storageStats,
                  onPlayRecording = { viewModel.playOfflineRecording(it) },
                  onDeleteRecording = { viewModel.deleteRecording(it) }
                )
              }
              AppTab.HARDWARE -> {
                HardwareRemoteScreen(
                  config = hardwareConfig,
                  isTesting = isTestingHardware,
                  testResult = hardwareTestResult,
                  onUpdateConfig = { viewModel.updateHardwareConfig(it) },
                  onTestHardware = { ip, port -> viewModel.testRemoteHardware(ip, port) },
                  onDismissTestResult = { viewModel.dismissHardwareTestResult() }
                )
              }
            }

            // Offline DVR Player Dialog
            playingRecording?.let { rec ->
              OfflineDvrPlayerDialog(
                recording = rec,
                onDismiss = { viewModel.closeRecordingPlayer() },
                onDelete = {
                  viewModel.deleteRecording(it)
                  viewModel.closeRecordingPlayer()
                }
              )
            }
          }
        }
      }
    }
  }
}
