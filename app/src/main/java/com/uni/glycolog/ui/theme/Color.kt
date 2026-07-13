package com.uni.glycolog.ui.theme

import androidx.compose.ui.graphics.Color
import com.uni.glycolog.util.GlucoseLevel
import com.uni.glycolog.util.GlucoseStats

// palette GlycoLog (mockup: tema scuro con accento verde)
val GreenPrimary = Color(0xFF2ECC71)
val GreenDark = Color(0xFF1E8449)
val YellowWarning = Color(0xFFF1C40F)
val RedAlert = Color(0xFFE74C3C)

val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val DarkBorder = Color(0xFF2A2A2A)
val GrayText = Color(0xFF9E9E9E)

val LightBackground = Color(0xFFF7F9F8)
val LightSurface = Color(0xFFFFFFFF)
val LightBorder = Color(0xFFE0E4E2)
val GrayTextLight = Color(0xFF6B6B6B)

// colore associato a un valore glicemico (indicatori nelle card, storico, grafico)
fun glucoseColor(value: Int): Color = when (GlucoseStats.classify(value)) {
    GlucoseLevel.LOW -> RedAlert
    GlucoseLevel.IN_RANGE -> GreenPrimary
    GlucoseLevel.WARNING -> YellowWarning
    GlucoseLevel.HIGH -> RedAlert
}
