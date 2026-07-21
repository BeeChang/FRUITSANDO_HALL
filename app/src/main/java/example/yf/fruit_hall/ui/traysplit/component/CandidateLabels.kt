package example.yf.fruit_hall.ui.traysplit.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import example.yf.fruit_hall.R
import example.yf.fruit_hall.core.AllocationCandidate

sealed interface CandidateLabel {
    data object Best : CandidateLabel
    data object BestAndMinMove : CandidateLabel
    data object MinMove : CandidateLabel
    data class Alternative(val number: Int) : CandidateLabel
}

@Composable
fun CandidateLabel.resolve(): String = when (this) {
    CandidateLabel.Best -> stringResource(R.string.tray_candidate_label_best)
    CandidateLabel.BestAndMinMove -> stringResource(R.string.tray_candidate_label_best_and_min_move)
    CandidateLabel.MinMove -> stringResource(R.string.tray_candidate_label_min_move)
    is CandidateLabel.Alternative -> stringResource(R.string.tray_candidate_fallback_label, number)
}

/**
 * 후보 리스트(엔진 선별 순서, 점수순 아님)에 화면 표시용 라벨을 붙인다.
 * 규칙: score 최고 후보=Best, moveCount 최소 후보=MinMove(동일 후보면 BestAndMinMove로 병합),
 * 나머지=Alternative(N). primaryLocationSet=false면 운반 라벨 자체를 생략.
 */
fun labelCandidates(candidates: List<AllocationCandidate>, primaryLocationSet: Boolean): List<CandidateLabel> {
    if (candidates.isEmpty()) return emptyList()
    val bestScoreIdx = candidates.indices.maxBy { candidates[it].score }
    val bestMoveIdx = if (primaryLocationSet) candidates.indices.minBy { candidates[it].moveCount } else -1
    var altCounter = 0
    return candidates.indices.map { i ->
        when {
            i == bestScoreIdx && i == bestMoveIdx -> CandidateLabel.BestAndMinMove
            i == bestScoreIdx -> CandidateLabel.Best
            i == bestMoveIdx -> CandidateLabel.MinMove
            else -> { altCounter++; CandidateLabel.Alternative(altCounter) }
        }
    }
}
