package example.yf.fruit_hall.ui.clock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import example.yf.fruit_hall.ui.component.util.ClickShrinkEffect
import example.yf.fruit_hall.ui.theme.AppTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val BtnColor     = Color(0xFFFFD580)   // 시계탭 앰버 — 화면 컬러와 통일
private val BtnTextColor = Color(0xFF8C6200)   // 진한 앰버

@Composable
fun ClockScreen(viewModel: ClockViewModel = hiltViewModel()) {
    var dateMonth by remember { mutableStateOf("") }
    var dateDay   by remember { mutableStateOf("") }
    var timeText  by remember { mutableStateOf("") }

    val monthFmt = remember { SimpleDateFormat("M", Locale.KOREA) }
    val dayFmt   = remember { SimpleDateFormat("d", Locale.KOREA) }
    val timeFmt  = remember { SimpleDateFormat("HH:mm:ss", Locale.KOREA) }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            dateMonth = monthFmt.format(now)
            dateDay   = dayFmt.format(now)
            timeText  = timeFmt.format(now)
            delay(1_000L)
        }
    }

    val primaryColor = AppTheme.colors.primary500
    val context      = LocalContext.current

    // null = 닫힘 / true = 앱 없음 오류 포함 / false = 일반 설정
    var showDialogWithError by remember { mutableStateOf<Boolean?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // ── 시계 본문 ──
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text       = dateMonth,
                    style      = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color      = primaryColor.copy(alpha = 0.6f)
                )
                Text(
                    text     = "월",
                    style    = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color    = primaryColor.copy(alpha = 0.45f),
                    modifier = Modifier.padding(start = 2.dp, bottom = 6.dp, end = 12.dp)
                )
                Text(
                    text       = dateDay,
                    style      = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color      = primaryColor.copy(alpha = 0.6f)
                )
                Text(
                    text     = "일",
                    style    = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color    = primaryColor.copy(alpha = 0.45f),
                    modifier = Modifier.padding(start = 2.dp, bottom = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text       = timeText,
                style      = MaterialTheme.typography.displayLarge,
                fontSize   = 100.sp,
                fontWeight = FontWeight.Light,
                color      = primaryColor
            )
        }

        // ── 우하단 바로가기 버튼 ──
        val colors = AppTheme.colors
        val locked = viewModel.isLocked
        ClickShrinkEffect(
            modifier     = Modifier
                .align(Alignment.BottomEnd)
                .padding(28.dp),
            shrinkFactor = 0.88f,
            onClick = {
                if (locked) {
                    showDialogWithError = false
                } else {
                    val intent = context.packageManager.getLaunchIntentForPackage(viewModel.packageName)
                    if (intent != null) context.startActivity(intent)
                    else showDialogWithError = true
                }
            }
        ) {
            Row(
                modifier = Modifier
                    .background(
                        color = if (locked) colors.grey100 else BtnColor.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(50.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector        = if (locked) Icons.Default.Lock else Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint               = if (locked) colors.grey400 else BtnTextColor,
                )
                Text(
                    text       = if (locked) "잠금" else "캐치테이블",
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color      = if (locked) colors.grey400 else BtnTextColor
                )
            }
        }
    }

    // ── 설정 다이얼로그 ──
    showDialogWithError?.let { hasError ->
        ExternalAppDialog(
            packageName  = viewModel.packageName,
            isLocked     = viewModel.isLocked,
            showAppError = hasError,
            onDismiss    = { showDialogWithError = null },
            onSave       = { pkg, locked ->
                viewModel.saveSettings(pkg, locked)
                showDialogWithError = null
            }
        )
    }
}

@Composable
private fun ExternalAppDialog(
    packageName: String,
    isLocked: Boolean,
    showAppError: Boolean,
    onDismiss: () -> Unit,
    onSave: (pkg: String, locked: Boolean) -> Unit,
) {
    val colors      = AppTheme.colors
    var inputPkg    by remember(packageName) { mutableStateOf(packageName) }
    var inputLocked by remember(isLocked)    { mutableStateOf(isLocked) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier  = Modifier.fillMaxWidth(0.65f),
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(containerColor = colors.white),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (showAppError) {
                    Text(
                        text  = "앱을 찾을 수 없습니다. 패키지명을 확인해 주세요.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFBF2020)
                    )
                }

                Text(
                    text       = "바로가기 설정",
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color      = colors.grey900
                )

                OutlinedTextField(
                    value         = inputPkg,
                    onValueChange = { inputPkg = it },
                    label         = { Text("앱 패키지명") },
                    placeholder   = { Text("예) co.kr.catchtable.waiting", style = MaterialTheme.typography.bodySmall) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text("버튼 잠금", style = MaterialTheme.typography.bodyLarge, color = colors.grey800)
                    Switch(checked = inputLocked, onCheckedChange = { inputLocked = it })
                }

                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("취소", color = colors.grey500)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(inputPkg.trim(), inputLocked) },
                        enabled = inputPkg.isNotBlank(),
                        colors  = ButtonDefaults.buttonColors(containerColor = BtnColor)
                    ) {
                        Text("저장", color = BtnTextColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
