package online.mempool.fatline.client.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import online.mempool.fatline.client.R


// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)

private val CourierRegular = Font(R.font.courier_prime_regular, weight = FontWeight.Normal)
private val CourierBold = Font(R.font.courier_prime_bold, weight = FontWeight.Bold)
private val CourierItalic = Font(R.font.courier_prime_italic, weight = FontWeight.Normal)
private val CourierBoldItalic = Font(R.font.courier_prime_bold_italic, weight = FontWeight.Bold)

val MonospaceRegular = FontFamily(CourierRegular, CourierBold)
val MonospaceItalic = FontFamily(CourierItalic, CourierBoldItalic)

val MonospaceStyle = TextStyle(
    fontFamily = MonospaceRegular
)

val MonospaceItalicStyle = TextStyle(
    fontFamily = MonospaceItalic
)