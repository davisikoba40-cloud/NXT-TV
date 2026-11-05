package com.example

import com.example.model.BroadcastType
import com.example.model.FrequencyBand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TunerUnitTest {

  @Test
  fun testFrequencyBands() {
    assertTrue(FrequencyBand.FM_RADIO.minMhz < FrequencyBand.FM_RADIO.maxMhz)
    assertEquals(87.5, FrequencyBand.FM_RADIO.minMhz, 0.01)
    assertEquals(108.0, FrequencyBand.FM_RADIO.maxMhz, 0.01)
    assertEquals(470.0, FrequencyBand.UHF.minMhz, 0.01)
  }

  @Test
  fun testBroadcastTypes() {
    val tv = BroadcastType.TV
    val radio = BroadcastType.RADIO
    assertNotNull(tv)
    assertNotNull(radio)
  }
}
