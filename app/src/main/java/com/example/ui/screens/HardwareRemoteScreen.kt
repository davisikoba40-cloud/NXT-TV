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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.model.HardwareTunerConfig
import com.example.model.HardwareType

@Composable
fun HardwareRemoteScreen(
  config: HardwareTunerConfig,
  isTesting: Boolean,
  testResult: String?,
  onUpdateConfig: (HardwareTunerConfig) -> Unit,
  onTestHardware: (String, Int) -> Unit,
  onDismissTestResult: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedType by remember { mutableStateOf(config.hardwareType) }
  var ipInput by remember { mutableStateOf(config.remoteIp) }
  var portInput by remember { mutableStateOf(config.remotePort.toString()) }
  var gainInput by remember { mutableFloatStateOf(config.lnaGainDb.toFloat()) }
  var biasTee by remember { mutableStateOf(config.biasTee) }
  var directSampling by remember { mutableStateOf(config.directSampling) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Header Info Card
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
                  Icons.Default.Memory,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "HARDWARE & REMOTE TUNER",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary,
                  fontFamily = FontFamily.Monospace
                )
                Text(
                  text = "USB SDR / Network TCP Tuner Bridge",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Box(
              modifier = Modifier
                .background(
                  if (config.isConnected) Color(0xFF00E676).copy(alpha = 0.15f) else Color(0xFFFFB300).copy(alpha = 0.15f),
                  RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = if (config.isConnected) "LINK READY" else "STANDBY",
                color = if (config.isConnected) Color(0xFF00E676) else Color(0xFFFFB300),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = config.connectionStatus,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }

    // Hardware Type Selector
    item {
      Text(
        text = "ACTIVE TUNER HARDWARE INTERFACE",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        fontFamily = FontFamily.Monospace
      )
    }

    items(HardwareType.values()) { hwType ->
      val isSelected = selectedType == hwType
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .clickable {
            selectedType = hwType
            onUpdateConfig(config.copy(hardwareType = hwType))
          }
          .border(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
            RoundedCornerShape(10.dp)
          )
          .testTag("hw_type_${hwType.name}"),
        colors = CardDefaults.cardColors(
          containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
        )
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            when (hwType) {
              HardwareType.USB_RTL_SDR -> Icons.Default.Usb
              HardwareType.REMOTE_TCP_SDR -> Icons.Default.Router
              HardwareType.HDHOMERUN_TUNER -> Icons.Default.SettingsInputAntenna
              HardwareType.INTERNAL_DEMODULATOR -> Icons.Default.Memory
            },
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = hwType.label,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = when (hwType) {
                HardwareType.USB_RTL_SDR -> "OTG direct USB communication with real RTL2832U / R820T2 chipsets"
                HardwareType.REMOTE_TCP_SDR -> "Connect via Ethernet / WiFi to remote rtl_tcp SDR server endpoint"
                HardwareType.HDHOMERUN_TUNER -> "Network ATSC / DVB-T broadcast tuner discovery"
                HardwareType.INTERNAL_DEMODULATOR -> "Device integrated software-defined radio DSP demodulator"
              },
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    // Remote TCP SDR Configuration card
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "REMOTE HARDWARE CONNECTION (rtl_tcp / WebSDR)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary,
            fontFamily = FontFamily.Monospace
          )
          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(
              value = ipInput,
              onValueChange = { ipInput = it },
              label = { Text("Server IP Address") },
              singleLine = true,
              modifier = Modifier.weight(2f).testTag("input_remote_ip")
            )
            OutlinedTextField(
              value = portInput,
              onValueChange = { portInput = it },
              label = { Text("Port") },
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("input_remote_port")
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Button(
            onClick = {
              val port = portInput.toIntOrNull() ?: 1234
              onTestHardware(ipInput, port)
            },
            enabled = !isTesting,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().testTag("btn_test_socket")
          ) {
            if (isTesting) {
              CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Testing TCP Handshake...")
            } else {
              Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Test Remote Socket Handshake")
            }
          }

          if (testResult != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                .padding(10.dp)
            ) {
              Text(
                text = testResult,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = if (testResult.contains("Connected")) Color(0xFF00E676) else Color(0xFFFFB300)
              )
            }
          }
        }
      }
    }

    // RF Gain & Antenna Settings
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "RF LNA GAIN & HARDWARE TUNER PARAMETERS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            fontFamily = FontFamily.Monospace
          )

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("LNA RF Gain", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            Text("${gainInput.toInt()} dB", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
          }

          Slider(
            value = gainInput,
            onValueChange = {
              gainInput = it
              onUpdateConfig(config.copy(lnaGainDb = it.toInt()))
            },
            valueRange = 0f..49f,
            colors = SliderDefaults.colors(
              thumbColor = MaterialTheme.colorScheme.primary,
              activeTrackColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth().testTag("slider_lna_gain")
          )

          Spacer(modifier = Modifier.height(4.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("Antenna Bias-Tee (5V Active)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
              Text("Powers external LNA antenna amplifiers", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
              checked = biasTee,
              onCheckedChange = {
                biasTee = it
                onUpdateConfig(config.copy(biasTee = it))
              },
              colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary),
              modifier = Modifier.testTag("switch_bias_tee")
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("Direct Sampling Q-Branch", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
              Text("Enables HF bands below 28 MHz", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
              checked = directSampling,
              onCheckedChange = {
                directSampling = it
                onUpdateConfig(config.copy(directSampling = it))
              },
              colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary),
              modifier = Modifier.testTag("switch_direct_sampling")
            )
          }
        }
      }
    }

    // MobiLabs branding footer card
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
          .padding(14.dp)
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "POWERED BY MOBILABS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00E5FF),
            fontFamily = FontFamily.Monospace
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "NXT TV OFFLINE BROADCAST & REMOTE SDR ENGINE v2.4",
            fontSize = 10.sp,
            color = Color.LightGray
          )
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
