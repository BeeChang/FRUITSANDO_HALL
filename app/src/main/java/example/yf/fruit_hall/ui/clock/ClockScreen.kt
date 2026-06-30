package example.yf.fruit_hall.ui.clock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import example.yf.fruit_hall.ui.theme.AppTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ClockScreen() {
    var dateMonth by remember { mutableStateOf("") }
    var dateDay by remember { mutableStateOf("") }
    var timeText by remember { mutableStateOf("") }

    val monthFmt = remember { SimpleDateFormat("M", Locale.KOREA) }
    val dayFmt = remember { SimpleDateFormat("d", Locale.KOREA) }
    val timeFmt = remember { SimpleDateFormat("HH:mm:ss", Locale.KOREA) }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            dateMonth = monthFmt.format(now)
            dateDay = dayFmt.format(now)
            timeText = timeFmt.format(now)
            delay(1_000L)
        }
    }

    val primaryColor = AppTheme.colors.primary500

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = dateMonth,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = primaryColor.copy(alpha = 0.6f)
            )
            Text(
                text = "월",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = primaryColor.copy(alpha = 0.45f),
                modifier = Modifier.padding(start = 2.dp, bottom = 6.dp, end = 12.dp)
            )
            Text(
                text = dateDay,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = primaryColor.copy(alpha = 0.6f)
            )
            Text(
                text = "일",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = primaryColor.copy(alpha = 0.45f),
                modifier = Modifier.padding(start = 2.dp, bottom = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = timeText,
            style = MaterialTheme.typography.displayLarge,
            fontSize = 100.sp,
            fontWeight = FontWeight.Light,
            color = primaryColor
        )
    }
}
