package com.maku.eyeonsite.ui.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.maku.eyeonsite.ui.theme.DeepForest
import com.maku.eyeonsite.ui.theme.Forest

@Composable
internal fun LogoMark() {
    Box(
        modifier =
            Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Forest),
        contentAlignment = Alignment.Center,
    ) {
        val dark = DeepForest
        Canvas(modifier = Modifier.size(64.dp)) {
            val w = size.width
            val h = size.height
            val scale = minOf(w / 120f, h / 160f)
            val ox = (w - 120f * scale) / 2f
            val oy = (h - 160f * scale) / 2f

            fun tx(x: Float) = x * scale + ox

            fun ty(y: Float) = y * scale + oy

            val drop =
                Path().apply {
                    moveTo(tx(60f), ty(4f))
                    cubicTo(tx(91f), ty(4f), tx(116f), ty(29f), tx(116f), ty(60f))
                    cubicTo(tx(116f), ty(91f), tx(60f), ty(152f), tx(60f), ty(152f))
                    cubicTo(tx(60f), ty(152f), tx(4f), ty(91f), tx(4f), ty(60f))
                    cubicTo(tx(4f), ty(29f), tx(29f), ty(4f), tx(60f), ty(4f))
                    close()
                }
            drawPath(drop, color = Color.White)

            val lens =
                Path().apply {
                    moveTo(tx(26f), ty(62f))
                    quadraticBezierTo(tx(60f), ty(32f), tx(94f), ty(62f))
                    quadraticBezierTo(tx(60f), ty(92f), tx(26f), ty(62f))
                    close()
                }
            drawPath(lens, color = dark)

            drawCircle(color = Color.White, radius = 16f * scale, center = Offset(tx(60f), ty(62f)))
            drawCircle(color = dark, radius = 8f * scale, center = Offset(tx(60f), ty(62f)))
        }
    }
}
