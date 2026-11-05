package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.StorageStats
import com.example.engine.TunerEngine
import com.example.model.BroadcastChannel
import com.example.model.BroadcastType
import com.example.model.ConstellationPoint
import com.example.model.DvrRecording
import com.example.model.FrequencyBand
import com.example.model.HardwareTunerConfig
import com.example.model.SpectrumPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
  LIVE("Live Tuner"),
  SPECTRUM("Analyzer & Scan"),
  DVR("DVR Storage"),
  HARDWARE("Hardware")
}

class TunerViewModel(application: Application) : AndroidViewModel(application) {
  val engine = TunerEngine(application)

  private val _currentTab = MutableStateFlow(AppTab.LIVE)
  val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

  private val _isDarkTheme = MutableStateFlow(true)
  val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

  private val _playingRecording = MutableStateFlow<DvrRecording?>(null)
  val playingRecording: StateFlow<DvrRecording?> = _playingRecording.asStateFlow()

  private val _hardwareTestResult = MutableStateFlow<String?>(null)
  val hardwareTestResult: StateFlow<String?> = _hardwareTestResult.asStateFlow()

  private val _isTestingHardware = MutableStateFlow(false)
  val isTestingHardware: StateFlow<Boolean> = _isTestingHardware.asStateFlow()

  // Engine exposures
  val channels: StateFlow<List<BroadcastChannel>> = engine.channels
  val currentFrequencyMhz: StateFlow<Double> = engine.currentFrequencyMhz
  val currentChannel: StateFlow<BroadcastChannel?> = engine.currentChannel
  val activeType: StateFlow<BroadcastType> = engine.activeType
  val hardwareConfig: StateFlow<HardwareTunerConfig> = engine.hardwareConfig
  val isScanning: StateFlow<Boolean> = engine.isScanning
  val scanProgress: StateFlow<Float> = engine.scanProgress
  val scanStatusText: StateFlow<String> = engine.scanStatusText
  val isRecording: StateFlow<Boolean> = engine.isRecording
  val recordingSeconds: StateFlow<Int> = engine.recordingSeconds
  val recordings: StateFlow<List<DvrRecording>> = engine.recordings
  val spectrumData: StateFlow<List<SpectrumPoint>> = engine.spectrumData
  val constellationData: StateFlow<List<ConstellationPoint>> = engine.constellationData

  private val _storageStats = MutableStateFlow(engine.getStorageInfo())
  val storageStats: StateFlow<StorageStats> = _storageStats.asStateFlow()

  fun setTab(tab: AppTab) {
    _currentTab.value = tab
    if (tab == AppTab.DVR) {
      refreshStorageStats()
    }
  }

  fun toggleTheme() {
    _isDarkTheme.value = !_isDarkTheme.value
  }

  fun tuneToFrequency(freqMhz: Double) {
    engine.tuneToFrequency(freqMhz)
  }

  fun stepFrequency(deltaMhz: Double) {
    engine.stepFrequency(deltaMhz)
  }

  fun selectChannel(channel: BroadcastChannel) {
    engine.selectChannel(channel)
  }

  fun setBroadcastType(type: BroadcastType) {
    engine.setType(type)
  }

  fun toggleFavorite(channelId: String) {
    engine.toggleFavorite(channelId)
  }

  fun startScan(band: FrequencyBand) {
    engine.startScan(band)
  }

  fun stopScan() {
    engine.stopScan()
  }

  fun toggleRecording() {
    engine.toggleRecording()
    refreshStorageStats()
  }

  fun deleteRecording(recordingId: String) {
    engine.deleteRecording(recordingId)
    refreshStorageStats()
    if (_playingRecording.value?.id == recordingId) {
      _playingRecording.value = null
    }
  }

  fun playOfflineRecording(recording: DvrRecording) {
    _playingRecording.value = recording
  }

  fun closeRecordingPlayer() {
    _playingRecording.value = null
  }

  fun refreshStorageStats() {
    _storageStats.value = engine.getStorageInfo()
  }

  fun updateHardwareConfig(config: HardwareTunerConfig) {
    engine.updateHardwareConfig(config)
  }

  fun testRemoteHardware(ip: String, port: Int) {
    viewModelScope.launch {
      _isTestingHardware.value = true
      _hardwareTestResult.value = null
      val (success, message) = engine.testRemoteHardwareConnection(ip, port)
      _isTestingHardware.value = false
      _hardwareTestResult.value = message
      if (success) {
        engine.updateHardwareConfig(
          engine.hardwareConfig.value.copy(
            remoteIp = ip,
            remotePort = port,
            isConnected = true,
            connectionStatus = "Connected to Remote Hardware via TCP ($ip:$port)"
          )
        )
      }
    }
  }

  fun dismissHardwareTestResult() {
    _hardwareTestResult.value = null
  }
}
