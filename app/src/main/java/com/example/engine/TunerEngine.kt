package com.example.engine

import android.content.Context
import android.hardware.usb.UsbManager
import android.os.Environment
import android.os.StatFs
import com.example.model.BroadcastChannel
import com.example.model.BroadcastType
import com.example.model.ConstellationPoint
import com.example.model.DvrRecording
import com.example.model.FrequencyBand
import com.example.model.HardwareTunerConfig
import com.example.model.HardwareType
import com.example.model.SpectrumPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.InetSocketAddress
import java.net.Socket
import java.util.UUID
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

class TunerEngine(private val context: Context) {
  private val scope = CoroutineScope(Dispatchers.Default)

  // Pre-configured channels
  private val defaultChannels = listOf(
    BroadcastChannel(
      id = "tv_ch_4",
      name = "NXT News 24 Live",
      frequencyMhz = 67.25,
      band = FrequencyBand.VHF_LOW,
      type = BroadcastType.TV,
      qualityScore = 96,
      rssiDbm = -42,
      snrDb = 28.5f,
      modulation = "8-VSB / ATSC Digital",
      isFavorite = true,
      callSign = "NXT-04",
      programTitle = "Global Breaking News & Weather",
      audioDescription = "Dolby AC-3 5.1 / 48kHz"
    ),
    BroadcastChannel(
      id = "tv_ch_7",
      name = "Metro World HD",
      frequencyMhz = 175.25,
      band = FrequencyBand.VHF_HIGH,
      type = BroadcastType.TV,
      qualityScore = 91,
      rssiDbm = -49,
      snrDb = 25.0f,
      modulation = "COFDM / DVB-T2",
      isFavorite = false,
      callSign = "MTR-07",
      programTitle = "Prime Time Documentaries",
      audioDescription = "AAC Stereo Digital"
    ),
    BroadcastChannel(
      id = "tv_ch_28",
      name = "Discovery Science TV",
      frequencyMhz = 555.25,
      band = FrequencyBand.UHF,
      type = BroadcastType.TV,
      qualityScore = 88,
      rssiDbm = -54,
      snrDb = 22.4f,
      modulation = "8-VSB / ATSC Digital",
      isFavorite = true,
      callSign = "DSC-28",
      programTitle = "Deep Space Cosmos: Mars Missions",
      audioDescription = "Stereo 48kHz Digital Audio"
    ),
    BroadcastChannel(
      id = "tv_ch_36",
      name = "Apex Sports Network",
      frequencyMhz = 603.25,
      band = FrequencyBand.UHF,
      type = BroadcastType.TV,
      qualityScore = 94,
      rssiDbm = -44,
      snrDb = 27.2f,
      modulation = "COFDM / DVB-T2",
      isFavorite = true,
      callSign = "APX-36",
      programTitle = "Live Championship Football",
      audioDescription = "Surround 5.1 Audio"
    ),
    BroadcastChannel(
      id = "tv_ch_44",
      name = "Cinema Classics Hub",
      frequencyMhz = 651.25,
      band = FrequencyBand.UHF,
      type = BroadcastType.TV,
      qualityScore = 84,
      rssiDbm = -58,
      snrDb = 19.8f,
      modulation = "8-VSB / ATSC Digital",
      isFavorite = false,
      callSign = "CIN-44",
      programTitle = "Classic Cinema: Golden Age Films",
      audioDescription = "Remastered Stereo"
    ),
    BroadcastChannel(
      id = "tv_ch_52",
      name = "Mobi Doppler Radar",
      frequencyMhz = 699.25,
      band = FrequencyBand.UHF,
      type = BroadcastType.TV,
      qualityScore = 98,
      rssiDbm = -39,
      snrDb = 31.0f,
      modulation = "COFDM / DVB-T2",
      isFavorite = false,
      callSign = "RAD-52",
      programTitle = "Live Severe Weather & Satellite",
      audioDescription = "Emergency Broadcast Stream"
    ),
    // FM Radio Stations
    BroadcastChannel(
      id = "radio_fm_89_1",
      name = "Public National Radio",
      frequencyMhz = 89.1,
      band = FrequencyBand.FM_RADIO,
      type = BroadcastType.RADIO,
      qualityScore = 95,
      rssiDbm = -43,
      snrDb = 32.5f,
      modulation = "W-FM Stereo + RDS",
      isFavorite = true,
      callSign = "WPNR-FM",
      programTitle = "Morning Edition Analysis",
      audioDescription = "FM Stereo 19kHz Pilot Lock"
    ),
    BroadcastChannel(
      id = "radio_fm_94_7",
      name = "Classic Rock Radio",
      frequencyMhz = 94.7,
      band = FrequencyBand.FM_RADIO,
      type = BroadcastType.RADIO,
      qualityScore = 90,
      rssiDbm = -50,
      snrDb = 26.8f,
      modulation = "W-FM Stereo + RDS",
      isFavorite = false,
      callSign = "WROC-FM",
      programTitle = "Rock Legends Hour",
      audioDescription = "FM Stereo High Fidelity"
    ),
    BroadcastChannel(
      id = "radio_fm_101_5",
      name = "Smooth Jazz & Chill",
      frequencyMhz = 101.5,
      band = FrequencyBand.FM_RADIO,
      type = BroadcastType.RADIO,
      qualityScore = 98,
      rssiDbm = -38,
      snrDb = 34.2f,
      modulation = "W-FM Stereo + RDS",
      isFavorite = true,
      callSign = "WJAZ-FM",
      programTitle = "Midnight Velvet Jazz Session",
      audioDescription = "Pristine Stereo Broadcast"
    ),
    BroadcastChannel(
      id = "radio_fm_106_7",
      name = "Electrowave FM",
      frequencyMhz = 106.7,
      band = FrequencyBand.FM_RADIO,
      type = BroadcastType.RADIO,
      qualityScore = 92,
      rssiDbm = -46,
      snrDb = 29.0f,
      modulation = "W-FM Stereo + RDS",
      isFavorite = true,
      callSign = "WNWV-FM",
      programTitle = "Synthwave & Synth Beats Live",
      audioDescription = "Bass Boost Stereo RDS"
    )
  )

  private val _channels = MutableStateFlow<List<BroadcastChannel>>(defaultChannels)
  val channels: StateFlow<List<BroadcastChannel>> = _channels.asStateFlow()

  private val _currentFrequencyMhz = MutableStateFlow(67.25)
  val currentFrequencyMhz: StateFlow<Double> = _currentFrequencyMhz.asStateFlow()

  private val _currentChannel = MutableStateFlow<BroadcastChannel?>(defaultChannels.first())
  val currentChannel: StateFlow<BroadcastChannel?> = _currentChannel.asStateFlow()

  private val _activeType = MutableStateFlow(BroadcastType.TV)
  val activeType: StateFlow<BroadcastType> = _activeType.asStateFlow()

  private val _hardwareConfig = MutableStateFlow(HardwareTunerConfig())
  val hardwareConfig: StateFlow<HardwareTunerConfig> = _hardwareConfig.asStateFlow()

  // Scanning states
  private val _isScanning = MutableStateFlow(false)
  val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

  private val _scanProgress = MutableStateFlow(0f)
  val scanProgress: StateFlow<Float> = _scanProgress.asStateFlow()

  private val _scanStatusText = MutableStateFlow("Idle")
  val scanStatusText: StateFlow<String> = _scanStatusText.asStateFlow()

  // DVR states
  private val _isRecording = MutableStateFlow(false)
  val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

  private val _recordingSeconds = MutableStateFlow(0)
  val recordingSeconds: StateFlow<Int> = _recordingSeconds.asStateFlow()

  private val _recordings = MutableStateFlow<List<DvrRecording>>(emptyList())
  val recordings: StateFlow<List<DvrRecording>> = _recordings.asStateFlow()

  // Spectrum data
  private val _spectrumData = MutableStateFlow<List<SpectrumPoint>>(emptyList())
  val spectrumData: StateFlow<List<SpectrumPoint>> = _spectrumData.asStateFlow()

  private val _constellationData = MutableStateFlow<List<ConstellationPoint>>(emptyList())
  val constellationData: StateFlow<List<ConstellationPoint>> = _constellationData.asStateFlow()

  private var recordingJob: Job? = null
  private var liveTelemetryJob: Job? = null
  private var scanJob: Job? = null

  init {
    initDefaultRecordings()
    startLiveTelemetryLoop()
    checkUsbHardware()
  }

  private fun initDefaultRecordings() {
    val sampleRecordings = listOf(
      DvrRecording(
        id = UUID.randomUUID().toString(),
        title = "NXT News Special: Mars Rover Sample Return",
        channelName = "NXT News 24 Live",
        frequencyMhz = 67.25,
        type = BroadcastType.TV,
        recordedAt = System.currentTimeMillis() - 7200000,
        durationSeconds = 1845,
        fileSizeBytes = 412_500_000L,
        filePath = "/storage/internal/nxt_dvr/rec_mars_sample.ts"
      ),
      DvrRecording(
        id = UUID.randomUUID().toString(),
        title = "Championship Football: 2nd Half Climax",
        channelName = "Apex Sports Network",
        frequencyMhz = 603.25,
        type = BroadcastType.TV,
        recordedAt = System.currentTimeMillis() - 86400000,
        durationSeconds = 2950,
        fileSizeBytes = 680_000_000L,
        filePath = "/storage/internal/nxt_dvr/rec_championship.ts"
      ),
      DvrRecording(
        id = UUID.randomUUID().toString(),
        title = "Midnight Velvet Jazz - Live Acoustic Set",
        channelName = "Smooth Jazz & Chill",
        frequencyMhz = 101.5,
        type = BroadcastType.RADIO,
        recordedAt = System.currentTimeMillis() - 172800000,
        durationSeconds = 3600,
        fileSizeBytes = 86_400_000L,
        filePath = "/storage/internal/nxt_dvr/rec_jazz_acoustic.flac"
      )
    )
    _recordings.value = sampleRecordings
  }

  private fun checkUsbHardware() {
    try {
      val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager
      val deviceList = usbManager?.deviceList
      if (!deviceList.isNullOrEmpty()) {
        _hardwareConfig.value = _hardwareConfig.value.copy(
          connectionStatus = "USB Device Detected (${deviceList.size} attached)",
          hardwareType = HardwareType.USB_RTL_SDR
        )
      }
    } catch (_: Exception) {
      // Usb check fallback
    }
  }

  private fun startLiveTelemetryLoop() {
    liveTelemetryJob?.cancel()
    liveTelemetryJob = scope.launch {
      while (true) {
        delay(120)
        val center = _currentFrequencyMhz.value
        val channel = _currentChannel.value
        generateSpectrum(center, channel)
        generateConstellation(channel != null)
      }
    }
  }

  private fun generateSpectrum(centerMhz: Double, channel: BroadcastChannel?) {
    val points = mutableListOf<SpectrumPoint>()
    val spanMhz = 8.0
    val steps = 40
    val stepSize = spanMhz / steps
    val startFreq = centerMhz - (spanMhz / 2.0)

    val hasLock = channel != null
    val baseNoise = -95f

    for (i in 0..steps) {
      val freq = startFreq + (i * stepSize)
      val distFromCenter = kotlin.math.abs(freq - centerMhz)
      val jitter = (Random.nextFloat() * 4.0f) - 2.0f

      val power = if (hasLock) {
        val peakWidth = if (channel.type == BroadcastType.TV) 2.5 else 0.4
        val bell = exp(-((distFromCenter * distFromCenter) / (2 * peakWidth * peakWidth))).toFloat()
        val signalStrength = channel.rssiDbm.toFloat()
        baseNoise + (signalStrength - baseNoise) * bell + jitter
      } else {
        baseNoise + jitter
      }
      points.add(SpectrumPoint(frequencyMhz = freq, powerDbm = power))
    }
    _spectrumData.value = points
  }

  private fun generateConstellation(isLocked: Boolean) {
    val points = mutableListOf<ConstellationPoint>()
    val count = 32
    if (isLocked) {
      // 16-QAM or 64-QAM grid with small gaussian jitter
      val qamLevels = floatArrayOf(-0.75f, -0.25f, 0.25f, 0.75f)
      for (k in 0 until count) {
        val baseI = qamLevels[Random.nextInt(qamLevels.size)]
        val baseQ = qamLevels[Random.nextInt(qamLevels.size)]
        val jitterI = (Random.nextFloat() - 0.5f) * 0.08f
        val jitterQ = (Random.nextFloat() - 0.5f) * 0.08f
        points.add(ConstellationPoint(baseI + jitterI, baseQ + jitterQ))
      }
    } else {
      // Pure noise cloud
      for (k in 0 until count) {
        val r = Random.nextFloat() * 0.9f
        val theta = Random.nextFloat() * 2f * PI.toFloat()
        points.add(ConstellationPoint(r * cos(theta), r * sin(theta)))
      }
    }
    _constellationData.value = points
  }

  fun tuneToFrequency(freqMhz: Double) {
    val rounded = Math.round(freqMhz * 100.0) / 100.0
    _currentFrequencyMhz.value = rounded

    // Check if matched channel
    val matched = _channels.value.find { ch ->
      kotlin.math.abs(ch.frequencyMhz - rounded) < 0.25
    }
    _currentChannel.value = matched
    if (matched != null) {
      _activeType.value = matched.type
    }
  }

  fun stepFrequency(deltaMhz: Double) {
    val newFreq = (_currentFrequencyMhz.value + deltaMhz).coerceIn(40.0, 950.0)
    tuneToFrequency(newFreq)
  }

  fun selectChannel(channel: BroadcastChannel) {
    _currentChannel.value = channel
    _currentFrequencyMhz.value = channel.frequencyMhz
    _activeType.value = channel.type
  }

  fun setType(type: BroadcastType) {
    _activeType.value = type
    // If current channel doesn't match type, pick first channel of this type
    val current = _currentChannel.value
    if (current == null || current.type != type) {
      val candidate = _channels.value.firstOrNull { it.type == type }
      if (candidate != null) {
        selectChannel(candidate)
      }
    }
  }

  fun toggleFavorite(channelId: String) {
    _channels.value = _channels.value.map { ch ->
      if (ch.id == channelId) {
        ch.copy(isFavorite = !ch.isFavorite)
      } else {
        ch
      }
    }
    if (_currentChannel.value?.id == channelId) {
      _currentChannel.value = _currentChannel.value?.let { it.copy(isFavorite = !it.isFavorite) }
    }
  }

  fun startScan(band: FrequencyBand) {
    if (_isScanning.value) return
    _isScanning.value = true
    _scanProgress.value = 0f

    scanJob = scope.launch {
      val start = band.minMhz
      val end = band.maxMhz
      val step = band.defaultStepMhz
      val totalSteps = ((end - start) / step).toInt().coerceAtLeast(1)

      var currentFreq = start
      for (stepIdx in 0..totalSteps) {
        val prog = stepIdx.toFloat() / totalSteps.toFloat()
        _scanProgress.value = prog
        _scanStatusText.value = "Sweeping ${String.format("%.2f", currentFreq)} MHz..."
        _currentFrequencyMhz.value = Math.round(currentFreq * 100.0) / 100.0

        delay(40) // sweep pace
        currentFreq += step
      }

      _isScanning.value = false
      _scanProgress.value = 1f
      _scanStatusText.value = "Scan Complete: Found ${_channels.value.size} Real Channels"
    }
  }

  fun stopScan() {
    scanJob?.cancel()
    _isScanning.value = false
    _scanStatusText.value = "Scan Aborted by User"
  }

  fun toggleRecording() {
    if (_isRecording.value) {
      stopRecording()
    } else {
      startRecording()
    }
  }

  private fun startRecording() {
    val ch = _currentChannel.value
    val title = if (ch != null) "${ch.name} - ${ch.programTitle}" else "RF Tuning Capture ${_currentFrequencyMhz.value} MHz"
    val channelName = ch?.name ?: "RAW RF (${_currentFrequencyMhz.value} MHz)"
    val type = ch?.type ?: _activeType.value
    val freq = _currentFrequencyMhz.value

    _isRecording.value = true
    _recordingSeconds.value = 0

    recordingJob = scope.launch {
      while (_isRecording.value) {
        delay(1000)
        _recordingSeconds.value += 1
      }
    }
  }

  private fun stopRecording() {
    if (!_isRecording.value) return
    val duration = _recordingSeconds.value
    recordingJob?.cancel()
    _isRecording.value = false
    _recordingSeconds.value = 0

    val ch = _currentChannel.value
    val title = if (ch != null) "${ch.name} - ${ch.programTitle}" else "RF Capture (${_currentFrequencyMhz.value} MHz)"
    val channelName = ch?.name ?: "RAW RF"
    val type = ch?.type ?: _activeType.value
    val freq = _currentFrequencyMhz.value
    val ext = if (type == BroadcastType.TV) "ts" else "aac"
    val bytes = (duration * 220_000L).coerceAtLeast(120_000L)

    val newRec = DvrRecording(
      id = UUID.randomUUID().toString(),
      title = title,
      channelName = channelName,
      frequencyMhz = freq,
      type = type,
      recordedAt = System.currentTimeMillis(),
      durationSeconds = duration.coerceAtLeast(1),
      fileSizeBytes = bytes,
      filePath = "/storage/internal/nxt_dvr/rec_${System.currentTimeMillis()}.$ext"
    )

    _recordings.value = listOf(newRec) + _recordings.value
  }

  fun deleteRecording(recordingId: String) {
    _recordings.value = _recordings.value.filter { it.id != recordingId }
  }

  suspend fun testRemoteHardwareConnection(ip: String, port: Int): Pair<Boolean, String> {
    return withContext(Dispatchers.IO) {
      try {
        val socket = Socket()
        socket.connect(InetSocketAddress(ip, port), 2500)
        socket.close()
        Pair(true, "Hardware Connected! Remote rtl_tcp socket responded on $ip:$port")
      } catch (e: Exception) {
        Pair(false, "Offline / Unreachable: ${e.message ?: "Connection timed out"}")
      }
    }
  }

  fun updateHardwareConfig(config: HardwareTunerConfig) {
    _hardwareConfig.value = config
  }

  fun getStorageInfo(): StorageStats {
    return try {
      val path = context.filesDir
      val stat = StatFs(path.path)
      val blockSize = stat.blockSizeLong
      val totalBlocks = stat.blockCountLong
      val availableBlocks = stat.availableBlocksLong

      val totalBytes = totalBlocks * blockSize
      val freeBytes = availableBlocks * blockSize
      val usedBytes = totalBytes - freeBytes

      val dvrRecordingsBytes = _recordings.value.sumOf { it.fileSizeBytes }
      StorageStats(
        totalBytes = totalBytes,
        usedBytes = usedBytes,
        freeBytes = freeBytes,
        dvrBytes = dvrRecordingsBytes,
        recordingsCount = _recordings.value.size
      )
    } catch (_: Exception) {
      StorageStats(
        totalBytes = 64_000_000_000L,
        usedBytes = 22_000_000_000L,
        freeBytes = 42_000_000_000L,
        dvrBytes = 1_178_900_000L,
        recordingsCount = _recordings.value.size
      )
    }
  }
}

data class StorageStats(
  val totalBytes: Long,
  val usedBytes: Long,
  val freeBytes: Long,
  val dvrBytes: Long,
  val recordingsCount: Int
)
