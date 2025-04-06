package com.rdapps.wearable.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.wear.compose.material.Typography
import com.rdapps.wearable.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val poppinsFont = FontFamily(
    Font(
        googleFont = GoogleFont(name = "Poppins"),
        fontProvider = provider
    ),
    Font(
        googleFont = GoogleFont(name = "Poppins"),
        fontProvider = provider,
        weight = FontWeight.Medium
    ),
    Font(
        googleFont = GoogleFont(name = "Poppins"),
        fontProvider = provider,
        weight = FontWeight.SemiBold
    ),
    Font(
        googleFont = GoogleFont(name = "Poppins"),
        fontProvider = provider,
        weight = FontWeight.Bold
    ),
    Font(
        googleFont = GoogleFont(name = "Poppins"),
        fontProvider = provider,
        weight = FontWeight.ExtraBold
    )
)

private val defaultFontFamily = poppinsFont

val Typography = Typography(
    defaultFontFamily = defaultFontFamily
//    displayLarge = TextStyle(fontFamily = defaultFontFamily),
//    displayMedium = TextStyle(fontFamily = defaultFontFamily),
//    displaySmall = TextStyle(fontFamily = defaultFontFamily),
//    headlineLarge = TextStyle(fontFamily = defaultFontFamily),
//    headlineMedium = TextStyle(fontFamily = defaultFontFamily),
//    headlineSmall = TextStyle(fontFamily = defaultFontFamily),
//    titleLarge = TextStyle(fontFamily = defaultFontFamily),
//    titleMedium = TextStyle(fontFamily = defaultFontFamily),
//    titleSmall = TextStyle(fontFamily = defaultFontFamily),
//    bodyLarge = TextStyle(fontFamily = defaultFontFamily),
//    bodyMedium = TextStyle(fontFamily = defaultFontFamily),
//    bodySmall = TextStyle(fontFamily = defaultFontFamily),
//    labelLarge = TextStyle(fontFamily = defaultFontFamily),
//    labelMedium = TextStyle(fontFamily = defaultFontFamily),
//    labelSmall = TextStyle(fontFamily = defaultFontFamily)
)