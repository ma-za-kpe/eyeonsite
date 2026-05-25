package com.maku.eyeonsite.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import eyeonsite.app.shared.generated.resources.DmSans_Medium
import eyeonsite.app.shared.generated.resources.DmSans_Regular
import eyeonsite.app.shared.generated.resources.JetBrainsMono_Regular
import eyeonsite.app.shared.generated.resources.Res
import org.jetbrains.compose.resources.Font

@Composable
fun eyeOnSiteTypography(): Typography {
    val dmSans =
        FontFamily(
            Font(Res.font.DmSans_Regular, FontWeight.Normal),
            Font(Res.font.DmSans_Medium, FontWeight.Medium),
        )
    val jetBrainsMono =
        FontFamily(
            Font(Res.font.JetBrainsMono_Regular, FontWeight.Normal),
        )
    return Typography(
        displayLarge =
            TextStyle(
                fontFamily = dmSans,
                fontWeight = FontWeight.Medium,
                fontSize = 48.sp,
                lineHeight = 57.6.sp,
                letterSpacing = 0.sp,
            ),
        headlineLarge =
            TextStyle(
                fontFamily = dmSans,
                fontWeight = FontWeight.Medium,
                fontSize = 32.sp,
                lineHeight = 38.4.sp,
                letterSpacing = 0.sp,
            ),
        headlineMedium =
            TextStyle(
                fontFamily = dmSans,
                fontWeight = FontWeight.Medium,
                fontSize = 24.sp,
                lineHeight = 31.2.sp,
                letterSpacing = 0.sp,
            ),
        headlineSmall =
            TextStyle(
                fontFamily = dmSans,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                lineHeight = 25.2.sp,
            ),
        bodyLarge =
            TextStyle(
                fontFamily = dmSans,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 25.6.sp,
            ),
        bodyMedium =
            TextStyle(
                fontFamily = dmSans,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 21.sp,
            ),
        bodySmall =
            TextStyle(
                fontFamily = jetBrainsMono,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.8.sp,
            ),
        labelLarge =
            TextStyle(
                fontFamily = dmSans,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                letterSpacing = 0.5.sp,
            ),
        labelMedium =
            TextStyle(
                fontFamily = dmSans,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                letterSpacing = 0.5.sp,
            ),
        labelSmall =
            TextStyle(
                fontFamily = dmSans,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                letterSpacing = 0.5.sp,
            ),
    )
}

@Composable
fun inspectorTypography(): Typography {
    val dmSans =
        FontFamily(
            Font(Res.font.DmSans_Regular, FontWeight.Normal),
            Font(Res.font.DmSans_Medium, FontWeight.Medium),
        )
    val jetBrainsMono =
        FontFamily(
            Font(Res.font.JetBrainsMono_Regular, FontWeight.Normal),
        )
    return Typography(
        displayLarge =
            TextStyle(
                fontFamily = dmSans,
                fontWeight = FontWeight.Bold,
                fontSize = 48.sp,
                lineHeight = 52.8.sp,
                letterSpacing = (-0.5).sp,
            ),
        headlineLarge =
            TextStyle(
                fontFamily = dmSans,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                lineHeight = 35.2.sp,
                letterSpacing = 0.sp,
            ),
        headlineMedium =
            TextStyle(
                fontFamily = dmSans,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                lineHeight = 28.8.sp,
                letterSpacing = 0.sp,
            ),
        headlineSmall =
            TextStyle(
                fontFamily = dmSans,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                lineHeight = 21.6.sp,
            ),
        bodyLarge =
            TextStyle(
                fontFamily = dmSans,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
            ),
        bodyMedium =
            TextStyle(
                fontFamily = dmSans,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            ),
        bodySmall =
            TextStyle(
                fontFamily = jetBrainsMono,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.8.sp,
            ),
        labelLarge =
            TextStyle(
                fontFamily = dmSans,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = 1.sp,
            ),
        labelMedium =
            TextStyle(
                fontFamily = dmSans,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
            ),
        labelSmall =
            TextStyle(
                fontFamily = dmSans,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
            ),
    )
}
