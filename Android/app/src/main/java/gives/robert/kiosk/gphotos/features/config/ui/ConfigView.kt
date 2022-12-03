package gives.robert.kiosk.gphotos.features.config.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gives.robert.kiosk.gphotos.R
import gives.robert.kiosk.gphotos.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

@Composable
fun ConfigView(onClick: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(
                    start = 16.dp,
                    bottom = 24.dp,
                    end = 16.dp
                ),
                text = "Import Photos from Google Photos",
                fontSize = 30.sp,
                lineHeight = 38.sp,
                textAlign = TextAlign.Center,
            )
            Text(
                modifier = Modifier.padding(
                    start = 24.dp,
                    bottom = 16.dp,
                    end = 24.dp
                ),
                text = "Connect to google Photos to import your existing photo libaries",
                textAlign = TextAlign.Center
            )
            ExtendedFloatingActionButton(
                text = { Text(text = "Connect to Google Photos") },
                onClick = onClick,
                icon = {
                    Icon(
                        painterResource(
                            id = R.drawable.google_photo_icon,
                        ),
                        tint = Color.Unspecified,
                        contentDescription = "Google Photo Icon"
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        ConfigView {
        }
    }
}
