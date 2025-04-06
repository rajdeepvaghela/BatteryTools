package com.rdapps.batterytools.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.rdapps.batterytools.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val latoFont = FontFamily(
    Font(
        googleFont = GoogleFont(name = "Lato"),
        fontProvider = provider
    ),
    Font(
        googleFont = GoogleFont(name = "Lato"),
        fontProvider = provider,
        weight = FontWeight.Bold
    ),
    Font(
        googleFont = GoogleFont(name = "Lato"),
        fontProvider = provider,
        weight = FontWeight.Black
    )
)

val wixMadeForFont = FontFamily(
    Font(
        googleFont = GoogleFont(name = "Wix Made For Text"),
        fontProvider = provider
    ),
    Font(
        googleFont = GoogleFont(name = "Wix Made For Text"),
        fontProvider = provider,
        weight = FontWeight.Bold
    ),
    Font(
        googleFont = GoogleFont(name = "Wix Made For Text"),
        fontProvider = provider,
        weight = FontWeight.ExtraBold
    )
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

// Set of Material typography styles to start with
val Typography = Typography(
    displayLarge = TextStyle(fontFamily = defaultFontFamily),
    displayMedium = TextStyle(fontFamily = defaultFontFamily),
    displaySmall = TextStyle(fontFamily = defaultFontFamily),
    headlineLarge = TextStyle(fontFamily = defaultFontFamily),
    headlineMedium = TextStyle(fontFamily = defaultFontFamily),
    headlineSmall = TextStyle(fontFamily = defaultFontFamily),
    titleLarge = TextStyle(fontFamily = defaultFontFamily),
    titleMedium = TextStyle(fontFamily = defaultFontFamily),
    titleSmall = TextStyle(fontFamily = defaultFontFamily),
    bodyLarge = TextStyle(fontFamily = defaultFontFamily),
    bodyMedium = TextStyle(fontFamily = defaultFontFamily),
    bodySmall = TextStyle(fontFamily = defaultFontFamily),
    labelLarge = TextStyle(fontFamily = defaultFontFamily),
    labelMedium = TextStyle(fontFamily = defaultFontFamily),
    labelSmall = TextStyle(fontFamily = defaultFontFamily)
)

//val Typography = Typography(
//    bodyLarge = TextStyle(
//        fontFamily = FontFamily.Default,
//        fontWeight = FontWeight.Normal,
//        fontSize = 16.sp,
//        lineHeight = 24.sp,
//        letterSpacing = 0.5.sp
//    ).copy(fontFamily = poppinsFont)
//    /* Other default text styles to override
//    titleLarge = TextStyle(
//        fontFamily = FontFamily.Default,
//        fontWeight = FontWeight.Normal,
//        fontSize = 22.sp,
//        lineHeight = 28.sp,
//        letterSpacing = 0.sp
//    ),
//    labelSmall = TextStyle(
//        fontFamily = FontFamily.Default,
//        fontWeight = FontWeight.Medium,
//        fontSize = 11.sp,
//        lineHeight = 16.sp,
//        letterSpacing = 0.5.sp
//    )
//    */
//)