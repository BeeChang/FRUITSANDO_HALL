package example.yf.fruit_hall.ui.clock

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import example.yf.fruit_hall.data.ClockAlert
import example.yf.fruit_hall.ui.component.util.ClickShrinkEffect
import example.yf.fruit_hall.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    val monthFmt  = remember { SimpleDateFormat("M", Locale.KOREA) }
    val dayFmt    = remember { SimpleDateFormat("d", Locale.KOREA) }
    val timeFmt   = remember { SimpleDateFormat("HH:mm:ss", Locale.KOREA) }
    val fireKeyFmt = remember { SimpleDateFormat("yyyyMMdd-HH:mm", Locale.KOREA) }

    // ── 시간 알림 (화면 반짝임 + 다이얼로그) ──
    var showAlertDialog by remember { mutableStateOf(false) }
    var showNotificationSettings by remember { mutableStateOf(false) }
    var isFlashing by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }
    var lastFiredKey by remember { mutableStateOf("") }
    val flashAlpha = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val dismissAlert: () -> Unit = {
        showAlertDialog = false
        isFlashing = false
        scope.launch { flashAlpha.snapTo(0f) }
    }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            dateMonth = monthFmt.format(now)
            dateDay   = dayFmt.format(now)
            timeText  = timeFmt.format(now)

            val fireKey = fireKeyFmt.format(now)
            if (fireKey != lastFiredKey) {
                val calendar = java.util.Calendar.getInstance().apply { time = now }
                val hour     = calendar.get(java.util.Calendar.HOUR_OF_DAY)
                val minute   = calendar.get(java.util.Calendar.MINUTE)
                val matched  = viewModel.alerts.firstOrNull { it.enabled && it.hour == hour && it.minute == minute }
                if (matched != null) {
                    lastFiredKey = fireKey
                    alertMessage = matched.message
                    showAlertDialog = true
                    isFlashing = true
                }
            }

            delay(1_000L)
        }
    }

    val primaryColor = AppTheme.colors.primary500
    val context      = LocalContext.current

    // null = 닫힘 / true = 앱 없음 오류 포함 / false = 일반 설정
    var showDialogWithError by remember { mutableStateOf<Boolean?>(null) }

    val flashDurationSeconds = viewModel.flashDurationSeconds

    LaunchedEffect(isFlashing) {
        if (isFlashing) {
            val endTime = System.currentTimeMillis() + flashDurationSeconds * 1_000L
            while (System.currentTimeMillis() < endTime) {
                flashAlpha.animateTo(0.95f, animationSpec = tween(120))
                flashAlpha.animateTo(0f, animationSpec = tween(120))
            }
            isFlashing = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ── 시계 본문 ──
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val dateFontScale = viewModel.dateFontScale
            val timeFontScale = viewModel.timeFontScale

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text       = dateMonth,
                    style      = MaterialTheme.typography.displaySmall,
                    fontSize   = MaterialTheme.typography.displaySmall.fontSize * dateFontScale,
                    fontWeight = FontWeight.Bold,
                    color      = primaryColor.copy(alpha = 0.6f)
                )
                Text(
                    text     = "월",
                    style    = MaterialTheme.typography.titleMedium,
                    fontSize = MaterialTheme.typography.titleMedium.fontSize * dateFontScale,
                    fontWeight = FontWeight.Bold,
                    color    = primaryColor.copy(alpha = 0.45f),
                    modifier = Modifier.padding(start = 2.dp, bottom = 6.dp, end = 12.dp)
                )
                Text(
                    text       = dateDay,
                    style      = MaterialTheme.typography.displaySmall,
                    fontSize   = MaterialTheme.typography.displaySmall.fontSize * dateFontScale,
                    fontWeight = FontWeight.Bold,
                    color      = primaryColor.copy(alpha = 0.6f)
                )
                Text(
                    text     = "일",
                    style    = MaterialTheme.typography.titleMedium,
                    fontSize = MaterialTheme.typography.titleMedium.fontSize * dateFontScale,
                    fontWeight = FontWeight.Bold,
                    color    = primaryColor.copy(alpha = 0.45f),
                    modifier = Modifier.padding(start = 2.dp, bottom = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text       = timeText,
                style      = MaterialTheme.typography.displayLarge,
                fontSize   = 100.sp * timeFontScale,
                fontWeight = FontWeight.Light,
                color      = primaryColor
            )
        }

        // ── 글자 크기 조절 (우측 상단) ──
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(20.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FontScaleStepper(
                label      = "월일크기",
                tint       = primaryColor.copy(alpha = 0.6f),
                onDecrease = viewModel::decreaseDateFontScale,
                onIncrease = viewModel::increaseDateFontScale
            )
            FontScaleStepper(
                label      = "시계크기",
                tint       = primaryColor,
                onDecrease = viewModel::decreaseTimeFontScale,
                onIncrease = viewModel::increaseTimeFontScale
            )
            ClickShrinkEffect(
                shrinkFactor = 0.9f,
                onClick = { showNotificationSettings = true }
            ) {
                Text(
                    text     = "알림설정",
                    style    = MaterialTheme.typography.labelSmall,
                    color    = BtnTextColor,
                    modifier = Modifier
                        .background(color = BtnColor.copy(alpha = 0.25f), shape = RoundedCornerShape(50.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
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
                    text       = if (locked) "잠금" else "클릭시 캐치테이블 이동",
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color      = if (locked) colors.grey400 else BtnTextColor
                )
            }
        }

        // ── 화면 반짝임 오버레이 ──
        if (flashAlpha.value > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BtnColor.copy(alpha = flashAlpha.value))
            )
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

    // ── 알림 다이얼로그 (테스트) ──
    if (showAlertDialog) {
        Dialog(onDismissRequest = dismissAlert) {
            Card(
                shape     = RoundedCornerShape(20.dp),
                colors    = CardDefaults.cardColors(containerColor = AppTheme.colors.white),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text       = alertMessage,
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color      = AppTheme.colors.black
                    )
                    Button(
                        onClick = dismissAlert,
                        colors  = ButtonDefaults.buttonColors(containerColor = BtnColor)
                    ) {
                        Text("확인", color = BtnTextColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // ── 알림설정 다이얼로그 ──
    if (showNotificationSettings) {
        NotificationSettingsDialog(
            alerts                = viewModel.alerts,
            flashDurationSeconds  = viewModel.flashDurationSeconds,
            onAddAlert            = viewModel::addAlert,
            onRemoveAlert         = viewModel::removeAlert,
            onToggleAlert         = viewModel::toggleAlert,
            onDecreaseDuration    = viewModel::decreaseFlashDuration,
            onIncreaseDuration    = viewModel::increaseFlashDuration,
            onDismiss             = { showNotificationSettings = false }
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

@Composable
private fun NotificationSettingsDialog(
    alerts: List<ClockAlert>,
    flashDurationSeconds: Int,
    onAddAlert: (hour: Int, minute: Int, message: String) -> Unit,
    onRemoveAlert: (id: String) -> Unit,
    onToggleAlert: (id: String, enabled: Boolean) -> Unit,
    onDecreaseDuration: () -> Unit,
    onIncreaseDuration: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AppTheme.colors
    var newHour     by remember { mutableIntStateOf(18) }
    var newMinute   by remember { mutableIntStateOf(0) }
    var minuteInput by remember { mutableStateOf("00") }
    var newMessage  by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier  = Modifier.fillMaxWidth(0.7f),
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(containerColor = colors.white),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text       = "알림설정",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color      = colors.grey900
                    )
                    Text(
                        text  = "총 ${alerts.size}개",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.grey500
                    )
                }

                // ── 등록된 알림 목록 ──
                if (alerts.isEmpty()) {
                    Text(
                        text  = "등록된 알림이 없습니다",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.grey500
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        alerts.forEach { alert ->
                            key(alert.id) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(color = colors.grey50, shape = RoundedCornerShape(12.dp))
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text       = "%02d:%02d".format(alert.hour, alert.minute),
                                        style      = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color      = colors.grey900
                                    )
                                    Text(
                                        text     = alert.message,
                                        style    = MaterialTheme.typography.bodyMedium,
                                        color    = colors.grey700,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Switch(
                                        checked         = alert.enabled,
                                        onCheckedChange = { onToggleAlert(alert.id, it) }
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier.size(40.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        ClickShrinkEffect(shrinkFactor = 0.85f, onClick = { onRemoveAlert(alert.id) }) {
                                            Icon(
                                                imageVector        = Icons.Default.Close,
                                                contentDescription = "삭제",
                                                tint               = colors.grey400,
                                                modifier           = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ── 새 알림 추가 ──
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("알림 시간", style = MaterialTheme.typography.bodyLarge, color = colors.grey800)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FontScaleButton(icon = Icons.Default.Remove, tint = BtnTextColor, onClick = { newHour = (newHour - 1 + 24) % 24 })
                        Text(
                            text       = "%02d".format(newHour),
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color      = colors.grey900,
                            modifier   = Modifier.width(32.dp),
                            textAlign  = TextAlign.Center
                        )
                        FontScaleButton(icon = Icons.Default.Add, tint = BtnTextColor, onClick = { newHour = (newHour + 1) % 24 })

                        Text(":", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.grey900)

                        FontScaleButton(
                            icon    = Icons.Default.Remove,
                            tint    = BtnTextColor,
                            onClick = {
                                newMinute = (newMinute - 5 + 60) % 60
                                minuteInput = "%02d".format(newMinute)
                            }
                        )
                        OutlinedTextField(
                            value         = minuteInput,
                            onValueChange = { input ->
                                val rawDigits = input.filter { it.isDigit() }
                                val digits = collapseLeadingZeroInput(minuteInput, rawDigits).take(2)
                                minuteInput = digits
                                digits.toIntOrNull()?.let { newMinute = it.coerceIn(0, 59) }
                            },
                            singleLine      = true,
                            textStyle       = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                textAlign  = TextAlign.Center
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier        = Modifier.width(68.dp)
                        )
                        FontScaleButton(
                            icon    = Icons.Default.Add,
                            tint    = BtnTextColor,
                            onClick = {
                                newMinute = (newMinute + 5) % 60
                                minuteInput = "%02d".format(newMinute)
                            }
                        )
                    }

                    OutlinedTextField(
                        value         = newMessage,
                        onValueChange = { newMessage = it },
                        label         = { Text("알림 문구") },
                        placeholder   = { Text("비워두면 \"퇴근하세요\"", style = MaterialTheme.typography.bodySmall) },
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                onAddAlert(newHour, newMinute, newMessage.trim().ifBlank { "퇴근하세요" })
                                newMessage = ""
                            },
                            colors  = ButtonDefaults.buttonColors(containerColor = BtnColor)
                        ) {
                            Text("추가", color = BtnTextColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // ── 반짝임 시간 ──
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text("반짝임 시간", style = MaterialTheme.typography.bodyLarge, color = colors.grey800)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FontScaleButton(icon = Icons.Default.Remove, tint = BtnTextColor, onClick = onDecreaseDuration)
                        Text(
                            text       = "${flashDurationSeconds}초",
                            style      = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color      = colors.grey900
                        )
                        FontScaleButton(icon = Icons.Default.Add, tint = BtnTextColor, onClick = onIncreaseDuration)
                    }
                }

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("닫기", color = colors.grey500)
                    }
                }
            }
        }
    }
}

// "00" 상태에서 숫자를 입력하면 커서 위치와 무관하게 새 숫자만 남도록 기존 0을 제거
private fun collapseLeadingZeroInput(old: String, rawDigits: String): String {
    if ((old == "0" || old == "00") && rawDigits.length > old.length) {
        var zerosToRemove = old.length
        val result = StringBuilder()
        for (c in rawDigits) {
            if (c == '0' && zerosToRemove > 0) {
                zerosToRemove--
            } else {
                result.append(c)
            }
        }
        return result.toString().ifEmpty { "0" }
    }
    return rawDigits
}

@Composable
private fun FontScaleStepper(
    label: String,
    tint: Color,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = tint
        )
        FontScaleButton(icon = Icons.Default.Remove, tint = tint, onClick = onDecrease)
        FontScaleButton(icon = Icons.Default.Add, tint = tint, onClick = onIncrease)
    }
}

@Composable
private fun FontScaleButton(
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit,
) {
    ClickShrinkEffect(shrinkFactor = 0.85f, onClick = onClick) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(color = tint.copy(alpha = 0.15f), shape = CircleShape)
                .border(width = 1.dp, color = tint.copy(alpha = 0.4f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = tint,
                modifier           = Modifier.size(18.dp)
            )
        }
    }
}
