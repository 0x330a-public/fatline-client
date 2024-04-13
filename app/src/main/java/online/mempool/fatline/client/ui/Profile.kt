package online.mempool.fatline.client.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.UiMode
import androidx.compose.ui.unit.dp
import online.mempool.fatline.client.ui.theme.FatlineTheme

@Composable
fun ProfileBanner(modifier: Modifier = Modifier) {

    Row(
        modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .padding(32.dp)
                .size(48.dp, 48.dp)
                .clip(CircleShape)
        ) {

        }
    }

}


@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun PreviewProfile() {
    FatlineTheme {
        Box(Modifier.fillMaxSize()) {
            ProfileBanner()
        }
    }
}