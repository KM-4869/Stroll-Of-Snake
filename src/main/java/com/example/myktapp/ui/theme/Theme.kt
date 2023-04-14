package com.example.myktapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColors(
    primary = Purple200,
    primaryVariant = Purple700,
    secondary = Teal200
)

private val LightColorPalette = lightColors(
    primary = Blue1,
    primaryVariant = Purple700,
    secondary = Teal200,
    background = SkyBlue,
    onBackground = Lavender
    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

private val KMColorPalette = lightColors(
    primary = KM5,
    primaryVariant = KM8,
    secondary = KM6,
    secondaryVariant = KM3,
    background = KM1,
)

@Composable
fun MyKtAppTheme(ThemeOption: Int, content: @Composable () -> Unit) {
    val colors = when(ThemeOption) {
        LightMode -> LightColorPalette
        DarkMode -> DarkColorPalette
        else -> KMColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

const val LightMode = 0
const val DarkMode = 1
const val KMMode = 2

//@Composable
//fun MyKtAppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
//    val colors = if (darkTheme) {
//        DarkColorPalette
//    } else {
//        LightColorPalette
//    }
//
//    MaterialTheme(
//        colors = colors,
//        typography = Typography,
//        shapes = Shapes,
//        content = content
//    )
//}