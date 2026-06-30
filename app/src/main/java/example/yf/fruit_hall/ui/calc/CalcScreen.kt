package example.yf.fruit_hall.ui.calc

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import example.yf.fruit_hall.ui.component.util.ClickShrinkEffect
import example.yf.fruit_hall.ui.theme.AppColor
import example.yf.fruit_hall.ui.theme.AppTheme

private val CalcAccent     = Color(0xFFFF8C42)   // 메인 오렌지 (= 버튼)
private val CalcAccentSoft = Color(0xFFFFF3E8)   // 연한 오렌지 (연산자 버튼 배경)
private val CalcAccentText = Color(0xFFC25000)   // 진한 오렌지 (연산자 버튼 텍스트)
private val Scale085Bg     = Color(0xFFE8F5E9)   // 연한 그린 (×0.85 전용)
private val Scale085Text   = Color(0xFF2E7D32)   // 진한 그린 (×0.85 전용)
private val AcBg           = Color(0xFFFDECEC)   // 연한 레드 (AC 버튼)
private val AcText         = Color(0xFFBF2020)   // 진한 레드 (AC 버튼)

// 세로모드 버튼 행 고정 높이 — 가로모드는 weight(1f)로 남은 공간을 채운다
private val PORTRAIT_ROW_HEIGHT = 68.dp

@Composable
fun CalcScreen(viewModel: CalcViewModel = hiltViewModel()) {
    val uiState    by viewModel.uiState.collectAsStateWithLifecycle()
    val colors      = AppTheme.colors
    val isPortrait  = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.grey50),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                // 세로모드: 컨텐츠 높이만큼만 차지해 화면 중앙에 정렬
                // 가로모드: 가용 높이를 모두 채워 버튼을 균등 배분
                .then(if (isPortrait) Modifier else Modifier.fillMaxHeight())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DisplayCard(uiState = uiState, colors = colors, isPortrait = isPortrait)
            KeypadCard(
                viewModel  = viewModel,
                colors     = colors,
                isPortrait = isPortrait,
                modifier   = if (isPortrait) Modifier else Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DisplayCard(uiState: CalcUiState, colors: AppColor, isPortrait: Boolean) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = colors.white),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 28.dp,
                    vertical   = if (isPortrait) 14.dp else 20.dp
                ),
            horizontalAlignment = Alignment.End
        ) {
            // 수식 표시 (예: "450 × 0.85 =")
            Text(
                text      = uiState.expression.ifEmpty { " " },
                style     = MaterialTheme.typography.bodyMedium,
                color     = colors.grey400,
                textAlign = TextAlign.End,
                maxLines  = 1,
                overflow  = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            // 현재 숫자 표시
            Text(
                text       = uiState.display,
                style      = if (isPortrait) MaterialTheme.typography.displaySmall
                             else MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color      = if (uiState.display == "오류") AcText else colors.grey900,
                textAlign  = TextAlign.End,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis,
                modifier   = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun KeypadCard(
    viewModel: CalcViewModel,
    colors: AppColor,
    isPortrait: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = colors.white),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier
                .then(if (isPortrait) Modifier else Modifier.fillMaxSize())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ColumnScope 안에서 정의해야 weight(1f) 사용 가능
            val rowMod = if (isPortrait)
                Modifier.fillMaxWidth().height(PORTRAIT_ROW_HEIGHT)
            else
                Modifier.fillMaxWidth().weight(1f)

            // Row 1: AC  ⌫  ×0.85  ÷
            Row(rowMod, Arrangement.spacedBy(8.dp)) {
                CalcBtn("AC", AcBg,          AcText,          Modifier.weight(1f)) { viewModel.onClear() }
                CalcBtn("⌫", colors.grey100, colors.grey700,  Modifier.weight(1f)) { viewModel.onBackspace() }
                Scale085Btn(Modifier.weight(1f))                                    { viewModel.onScale085() }
                CalcBtn("÷", CalcAccentSoft, CalcAccentText,  Modifier.weight(1f)) { viewModel.onOperator("÷") }
            }
            // Row 2: 7  8  9  ×
            Row(rowMod, Arrangement.spacedBy(8.dp)) {
                listOf("7", "8", "9").forEach { d ->
                    CalcBtn(d, colors.grey50, colors.grey800, Modifier.weight(1f))  { viewModel.onDigit(d) }
                }
                CalcBtn("×", CalcAccentSoft, CalcAccentText, Modifier.weight(1f))  { viewModel.onOperator("×") }
            }
            // Row 3: 4  5  6  −
            Row(rowMod, Arrangement.spacedBy(8.dp)) {
                listOf("4", "5", "6").forEach { d ->
                    CalcBtn(d, colors.grey50, colors.grey800, Modifier.weight(1f))  { viewModel.onDigit(d) }
                }
                CalcBtn("−", CalcAccentSoft, CalcAccentText, Modifier.weight(1f))  { viewModel.onOperator("−") }
            }
            // Row 4: 1  2  3  +
            Row(rowMod, Arrangement.spacedBy(8.dp)) {
                listOf("1", "2", "3").forEach { d ->
                    CalcBtn(d, colors.grey50, colors.grey800, Modifier.weight(1f))  { viewModel.onDigit(d) }
                }
                CalcBtn("+", CalcAccentSoft, CalcAccentText, Modifier.weight(1f))  { viewModel.onOperator("+") }
            }
            // Row 5: 0(2칸)  .  =
            Row(rowMod, Arrangement.spacedBy(8.dp)) {
                CalcBtn("0", colors.grey50, colors.grey800, Modifier.weight(2f))    { viewModel.onDigit("0") }
                CalcBtn(".", colors.grey50, colors.grey800, Modifier.weight(1f))    { viewModel.onDigit(".") }
                CalcBtn("=", CalcAccent,   Color.White,    Modifier.weight(1f))     { viewModel.onEquals() }
            }
        }
    }
}

// ×0.85 전용 버튼: 라벨과 보조 텍스트를 두 줄로 표시
@Composable
private fun Scale085Btn(modifier: Modifier = Modifier, onClick: () -> Unit) {
    ClickShrinkEffect(modifier = modifier.fillMaxHeight(), shrinkFactor = 0.92f, onClick = onClick) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Scale085Bg, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text       = "×0.85",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = Scale085Text
                )
                Text(
                    text  = "85%",
                    style = MaterialTheme.typography.labelSmall,
                    color = Scale085Text.copy(alpha = 0.65f)
                )
            }
        }
    }
}

@Composable
private fun CalcBtn(
    label: String,
    bg: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ClickShrinkEffect(modifier = modifier.fillMaxHeight(), shrinkFactor = 0.92f, onClick = onClick) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bg, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = label,
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = textColor
            )
        }
    }
}
