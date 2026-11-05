package com.example.model

enum class BroadcastType {
  TV,
  RADIO
}

enum class FrequencyBand(val label: String, val minMhz: Double, val maxMhz: Double, val defaultStepMhz: Double) {
  FM_RADIO("FM Radio", 87.5, 108.0, 0.1),
  VHF_LOW("VHF Low (Ch 2-6)", 54.0, 88.0, 6.0),
  VHF_HIGH("VHF High (Ch 7-13)", 174.0, 216.0, 6.0),
  UHF("UHF TV (Ch 14-69)", 470.0, 806.0, 6.0)
}

enum class HardwareType(val label: String) {
  INTERNAL_DEMODULATOR("Internal DSP Demodulator"),
  USB_RTL_SDR("USB RTL-SDR Dongle (RTL2832U)"),
  REMOTE_TCP_SDR("Remote TCP SDR (rtl_tcp Server)"),
  HDHOMERUN_TUNER("HDHomeRun Network TV Tuner")
}

data class BroadcastChannel(
  val id: String,
  val name: String,
  val frequencyMhz: Double,
  val band: FrequencyBand,
  val type: BroadcastType,
  val qualityScore: Int,
  val rssiDbm: Int,
  val snrDb: Float,
  val modulation: String,
  val isFavorite: Boolean = false,
  val callSign: String,
  val programTitle: String,
  val audioDescription: String = "Stereo 48kHz Digital Audio"
)

data class DvrRecording(
  val id: String,
  val title: String,
  val channelName: String,
  val frequencyMhz: Double,
  val type: BroadcastType,
  val recordedAt: Long,
  val durationSeconds: Int,
  val fileSizeBytes: Long,
  val filePath: String
)

data class HardwareTunerConfig(
  val hardwareType: HardwareType = HardwareType.INTERNAL_DEMODULATOR,
  val remoteIp: String = "192.168.1.120",
  val remotePort: Int = 1234,
  val isConnected: Boolean = true,
  val connectionStatus: String = "Hardware Online (Ready)",
  val sampleRateMsps: Double = 2.4,
  val lnaGainDb: Int = 38,
  val biasTee: Boolean = false,
  val directSampling: Boolean = false,
  val bandwidthMhz: Double = 6.0
)

data class SpectrumPoint(
  val frequencyMhz: Double,
  val powerDbm: Float
)

data class ConstellationPoint(
  val i: Float,
  val q: Float
)
