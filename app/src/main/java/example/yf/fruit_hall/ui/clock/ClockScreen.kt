package example.yf.fruit_hall.ui.clock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
    var dateText by remember { mutableStateOf("") }
    var timeText by remember { mutableStateOf("") }

    val dateFmt = remember { SimpleDateFormat("M.d", Locale.KOREA) }
    val timeFmt = remember { SimpleDateFormat("HH:mm:ss", Locale.KOREA) }

    // 탭을 벗어나면 Composition에서 제거되어 코루틴이 자동 취소됨.
    // 탭으로 돌아오면 재실행.
    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            dateText = dateFmt.format(now)
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
        Text(
            text = dateText,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Normal,
            color = primaryColor.copy(alpha = 0.6f)
        )
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
