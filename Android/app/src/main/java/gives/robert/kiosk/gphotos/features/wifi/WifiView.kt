package gives.robert.kiosk.gphotos.features.wifi

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import gives.robert.kiosk.gphotos.ui.theme.MyApplicationTheme
import gives.robert.kiosk.gphotos.utils.getActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun WifiView() {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize()) {

        Icon(
            modifier = Modifier
                .size(84.dp)
                .align(alignment = Alignment.Center)
                .height(50.dp)
                .fillMaxWidth(),
            imageVector = Icons.Filled.WifiOff,
            contentDescription = "wifi off"
        )
        Column(
            Modifier.align(alignment = Alignment.BottomCenter),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                modifier = Modifier
                    .layoutId("button")
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(Color.Magenta),
                shape = RectangleShape,
                contentPadding = PaddingValues(0.dp),
                onClick = {
                    coroutineScope.launch(Dispatchers.Main) {
                        context.getActivity()
                            ?.startActivity(Intent("android.settings.panel.action.INTERNET_CONNECTIVITY"))
                    }
                }
            ) {
                Text(
                    text = "Enabled Wifi"
                )

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        WifiView()
    }
}
