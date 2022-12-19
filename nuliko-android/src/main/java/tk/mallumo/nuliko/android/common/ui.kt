package tk.mallumo.nuliko.android.common

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*

@Composable
fun Space(horizontal: Dp, vertical: Dp = horizontal) {
    Spacer(modifier = Modifier.size(horizontal, vertical))
}
