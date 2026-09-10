package example.yf.fruit_hall.ui.rotation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import example.yf.fruit_hall.R
import example.yf.fruit_hall.ui.rotation.RotationRowUi
import example.yf.fruit_hall.ui.theme.AppTheme

/** §10-4. 시간축 꺾은선 — 사람별 라인 + 목표선. 셀 수정 즉시 갱신되어야 하므로 상위 상태를 그대로 그린다. */
@Composable
fun FairnessChartPanel(
    debtCurve: Map<Long, List<Double>>,
    targetCurve: List<Double>,
    rows: List<RotationRowUi>,
    modifier: Modifier = Modifier
) {
    val appColors = AppTheme.colors
    if (debtCurve.isEmpty() || targetCurve.isEmpty()) return

    val maxValue = (debtCurve.values.flatten() + targetCurve).maxOrNull()?.coerceAtLeast(1.0) ?: 1.0
    val nameByMember = rows.associateBy { it.memberId }

    Column(modifier.fillMaxWidth().padding(8.dp)) {
        Text(stringResource(R.string.rotation_chart_title), style = MaterialTheme.typography.titleSmall)
        Canvas(Modifier.fillMaxWidth().height(160.dp)) {
            val w = size.width
            val h = size.height
            fun pointsFor(curve: List<Double>): List<Offset> {
                if (curve.size < 2) return emptyList()
                return curve.mapIndexed { i, v ->
                    Offset(x = w * i / (curve.size - 1), y = h - (v / maxValue * h).toFloat())
                }
            }
            // 목표선 (점선)
            val targetPoints = pointsFor(targetCurve)
            for (i in 0 until targetPoints.size - 1) {
                drawLine(
                    color = appColors.grey600, start = targetPoints[i], end = targetPoints[i + 1],
                    strokeWidth = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                )
            }
            // 사람별 부채 곡선
            debtCurve.forEach { (memberId, curve) ->
                val color = nameByMember[memberId]?.colorHex.toRotationColor(Color.Gray)
                val points = pointsFor(curve)
                for (i in 0 until points.size - 1) {
                    drawLine(color = color, start = points[i], end = points[i + 1], strokeWidth = 4f)
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
            rows.forEach { row ->
                Row(Modifier.padding(end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp)) {
                        Canvas(Modifier.size(10.dp)) {
                            drawCircle(color = row.colorHex.toRotationColor(Color.Gray))
                        }
                    }
                    Text(" ${row.name}", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
