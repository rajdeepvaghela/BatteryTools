package com.rdapps.batterytools.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.text.TextPaint
import androidx.annotation.FontRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.TextUnit
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.core.util.TypedValueCompat.spToPx
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext

fun Context.textAsBitmap(
    text: String,
    fontSize: TextUnit,
    color: Color = Color.Black,
    font: Int,
    letterSpacing: Float = 0f
): Bitmap {
    val paint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    paint.textSize = spToPx(fontSize.value, resources.displayMetrics)
    paint.color = color.toArgb()
    paint.letterSpacing = letterSpacing
    paint.typeface = ResourcesCompat.getFont(this, font)

    val baseline = -paint.ascent()
    val width = (paint.measureText(text)).toInt()
    val height = (baseline + paint.descent()).toInt()
    val image = createBitmap(width, height)
    val canvas = Canvas(image)
    canvas.drawText(text, 0f, baseline, paint)
    return image
}

@Composable
fun GlanceText(
    text: String,
    @FontRes font: Int,
    fontSize: TextUnit,
    modifier: GlanceModifier = GlanceModifier,
    color: Color = Color.Black,
    letterSpacing: Float = 0f,
) {
    Image(
        modifier = modifier,
        provider = ImageProvider(
            LocalContext.current.textAsBitmap(
                text = text,
                fontSize = fontSize,
                color = color,
                font = font,
                letterSpacing = letterSpacing
            )
        ),
        contentDescription = null,
    )
}