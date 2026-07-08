package example.yf.fruit_hall.core

import kotlin.math.max

// ═══════════════════ 도메인 모델 ═══════════════════

enum class RoughSize(val nominal: Int) { S(15), M(30), L(50) }

data class TrayItem(
    val typeId: String,
    val exactQty: Int? = null,        // 정확한 개수를 아는 드문 경우
    val roughSize: RoughSize? = null  // 대/중/소 러프 입력
)

data class Tray(
    val id: String,
    val items: List<TrayItem>,        // 1~5종
    val pinnedRound: Int? = null      // 1-based. 유저가 차수 고정한 판
)

/** 개발자 설정 화면에 노출할 유일한 튜닝 손잡이. 어떤 조합도 계층 벽을 못 깬다. */
enum class Level { LOW, MID, HIGH }
data class TuningPresets(
    val spreadStrength: Level = Level.MID,  // 분산 선호 강도
    val orderStrictness: Level = Level.MID, // 종류 감소 엄격도
    val qtySensitivity: Level = Level.MID   // 수량 밸런스 민감도
)

sealed interface CapacityMode {
    /** 차수별 판 수 고정. 정확히 하나만 null 허용 = 잔여(remainder) 차수 */
    data class Exact(val traysPerRound: List<Int?>) : CapacityMode
    /** 대략 비율(합 100 아니어도 정규화). maxDeviation = 목표±허용 판 수 */
    data class Ratio(val percents: List<Double>, val maxDeviation: Int = 2) : CapacityMode
}

data class AllocationConfig(
    val rounds: Int,                    // 차수 수 R
    val capacity: CapacityMode,
    val allowedMissingTypes: Int = 0,   // 1차 누락 허용 종류 수(보고 기준)
    val presets: TuningPresets = TuningPresets()
)

data class AllocationCandidate(
    val assignment: Map<String, Int>,   // trayId -> 1..R
    val roundSizes: List<Int>,
    val score: Int,
    val violations: List<String>,
    val strategyName: String            // UI 라벨용 ("현장 관행형" 등)
)

// ═══════════════════ 가중치 자동 파생 ═══════════════════
//
// 계층 invariant: "하위 계층 이론 최대 총점 < 상위 계층 위반 1건"
// 이걸 손 검산 대신 코드가 상향식으로 도출한다. 계층3 최대치를 계산 →
// 계층2 단가 = 그것 × 마진 → 계층2 최대 총합 → 계층1 단가, 순서.
// K/R/총수량이 바뀌어도 벽이 자동으로 다시 세워진다. ⚠ 이 파생 로직 수정 금지.
class DerivedWeights(
    val missing: Int,     // 계층1: 1차 누락 종류당
    val typeOrder: Int,   // 계층2: 종류수 비감소 역전당
    val spread: Int,      // 계층3: 분산 보상
    val clump: Int,       // 계층3: 같은 차수 중복 감점
    val ratioDev: Int,    // 계층3.5: 비율 모드 목표 편차(판²당). Exact에선 0
    val qtyDivisor: Int,  // 계층4: 수량 편차 ÷ divisor
    val maxPasses: Int = 64
) {
    companion object {
        fun derive(k: Int, r: Int, totalQty: Int, maxDev: Int, p: TuningPresets): DerivedWeights {
            val clump = 50
            val spread = when (p.spreadStrength) {
                Level.LOW -> 50; Level.MID -> 100; Level.HIGH -> 200
            }
            val ratioDev = spread * 2
            // 계층3(+비율편차) 이론 최대: 종류별 분산 보상 + 밴드 끝까지 편차
            val tier3Max = k * (r - 1) * spread + r * maxDev * maxDev * ratioDev + 1
            val margin = when (p.orderStrictness) {
                Level.LOW -> 1.2; Level.MID -> 1.5; Level.HIGH -> 2.0
            }
            val typeOrder = max((tier3Max * margin).toInt(), 1_000)
            // 계층2 이론 최대: 인접쌍 (R-1)개 × 최대 gap (K+1)
            val tier2Max = (r - 1) * (k + 1) * typeOrder
            val missing = ((tier2Max + tier3Max) * 1.5).toInt()
            // 계층4: 최대 수량편차/div < clump 이 되는 최소 div가 divBase.
            // 민감도는 divBase의 배수로만 조정 → 어떤 프리셋도 invariant 유지
            val divBase = totalQty / clump + 1
            val qtyDivisor = when (p.qtySensitivity) {
                Level.LOW -> divBase * 4; Level.MID -> divBase * 2; Level.HIGH -> divBase
            }
            return DerivedWeights(missing, typeOrder, spread, clump, ratioDev, qtyDivisor)
        }
    }
}

// ═══════════════════ 배분 ═══════════════════
//
// 파이프라인: ①컴파일(비트마스크화) ②pin 고정 ③greedy set-cover(1차 전종류 seed)
//            ④전략별 잔여 배치(multi-start) ⑤swap(+ratio면 move)
// 성능: 증분 카운터로 score O(R·K), 핫루프 힙할당 0회, 전체 sub-ms
//
// onAttempt: 순수 계측 훅. climb()의 매 score() 평가 시 1회 호출된다.
// 점수·선택 로직에는 관여하지 않는 부가 콜백이며, 기본값 no-op이라 호출부에
// 영향이 없다. (UI 진행 카운터 실측용으로 추가됨 — 알고리즘 로직 변경 없음)
class TrayAllocator {

    fun allocate(
        trays: List<Tray>,
        config: AllocationConfig,
        onAttempt: () -> Unit = {}
    ): List<AllocationCandidate> {
        require(trays.isNotEmpty()) { "판이 없습니다" }
        require(config.rounds >= 2) { "차수는 최소 2" }
        val c = compile(trays, config.rounds)
        val target = resolveTargets(trays.size, config)
        val maxDev = (config.capacity as? CapacityMode.Ratio)?.maxDeviation ?: 0
        val w = DerivedWeights.derive(c.k, config.rounds, c.totalQtyEstimate, maxDev, config.presets)
        val ratioMode = config.capacity is CapacityMode.Ratio

        // 비율 모드 밴드: [target-dev, target+dev], 단 1차(coverage 차수)는 최소 1판
        val bandLow = IntArray(config.rounds) { max(if (it == 0) 1 else 0, target[it] - maxDev) }
        val bandHigh = IntArray(config.rounds) { target[it] + maxDev }

        // 수량 피크 선호 차수 = 잔여/최대목표 차수 (동률 시 앞 차수)
        val peakRound = target.indices.maxBy { target[it] }

        // ── multi-start: 초기 전략 3종 → 각각 climbing → 후보 ──
        val strategies: List<Pair<String, (State, List<Int>) -> Unit>> = listOf(
            "현장 관행형" to ::seedFieldHabit,   // 다종→1차, 통판→뒤차수 (사람 요령)
            "균형 분배형" to ::seedBalanced,     // 남는 슬롯 많은 차수부터 고르게
            "수량 후반형" to ::seedQtyLate       // 수량 큰 판을 뒤 차수부터
        )
        val results = ArrayList<Pair<State, String>>(strategies.size)
        for ((name, seed) in strategies) {
            val st = State(c.n, c.k, config.rounds, c.masks, c.qty, c.allQtyKnown,
                           target, peakRound, ratioMode, w)
            for (i in 0 until c.n) if (c.pin[i] >= 0) st.place(i, c.pin[i]) // Phase 1
            seedCover(st, c, target)                                        // Phase 2
            val rest = (0 until c.n).filter { st.assign[it] == -1 }
            seed(st, rest)                                                  // Phase 3
            climb(st, c.pin, if (ratioMode) bandLow else null, bandHigh, onAttempt) // Phase 4
            results += st to name
        }

        // 중복 제거(동일 배치) → 점수 내림차순 → Exact 1개 / Ratio 최대 3개
        val seen = HashSet<List<Int>>()
        val limit = if (ratioMode) 3 else 1
        return results
            .filter { seen.add(it.first.assign.toList()) }
            .sortedByDescending { it.first.score() }
            .take(limit)
            .map { (st, name) -> buildCandidate(st, c, config, target, maxDev, name, trays) }
    }

    // ── Phase 0: 객체 → primitive. 종류집합을 Long 비트마스크로 ──
    internal class Compiled(
        val n: Int, val k: Int, val fullMask: Long,
        val masks: LongArray, val qty: IntArray, val pin: IntArray,
        val allQtyKnown: Boolean, val totalQtyEstimate: Int,
        val typeNames: Array<String>
    )

    internal fun compile(trays: List<Tray>, rounds: Int): Compiled {
        val typeIndex = HashMap<String, Int>()
        for (t in trays) for (item in t.items)
            typeIndex.getOrPut(item.typeId) { typeIndex.size }
        val k = typeIndex.size
        require(k in 1..64) { "종류는 1~64 (초과 시 LongArray 마스크 확장 필요)" }
        val n = trays.size
        val masks = LongArray(n); val qty = IntArray(n); val pin = IntArray(n) { -1 }
        var allKnown = true
        for (i in 0 until n) {
            var m = 0L; var q = 0; var known = true
            for (item in trays[i].items) {
                m = m or (1L shl typeIndex.getValue(item.typeId))
                val iq = item.exactQty ?: item.roughSize?.nominal
                if (iq == null) known = false else q += iq
            }
            masks[i] = m; qty[i] = if (known) q else 0
            if (!known) allKnown = false
            trays[i].pinnedRound?.let {
                require(it in 1..rounds) { "pin 차수 범위 오류: $it" }
                pin[i] = it - 1
            }
        }
        // 수량 미상 시 판당 40개 추정치로 가중치만 파생 (계층4 자체는 비활성)
        val totalEst = if (allKnown) qty.sum() else n * 40
        val names = arrayOfNulls<String>(k)
        for ((id, idx) in typeIndex) names[idx] = id
        @Suppress("UNCHECKED_CAST")
        return Compiled(n, k, if (k == 64) -1L else (1L shl k) - 1,
            masks, qty, pin, allKnown, totalEst, names as Array<String>)
    }

    // ── 목표 판 수 산출 ──
    internal fun resolveTargets(n: Int, config: AllocationConfig): IntArray =
        when (val cap = config.capacity) {
            is CapacityMode.Exact -> {
                require(cap.traysPerRound.size == config.rounds) { "차수 수 불일치" }
                val nulls = cap.traysPerRound.count { it == null }
                require(nulls <= 1) { "잔여(null) 차수는 최대 1개" }
                val fixed = cap.traysPerRound.sumOf { it ?: 0 }
                val t = IntArray(config.rounds) { i ->
                    cap.traysPerRound[i] ?: (n - fixed)
                }
                require(t.all { it >= 0 } && t.sum() == n) { "판 수 합($fixed)이 전체($n)와 안 맞음" }
                t
            }
            is CapacityMode.Ratio -> {
                require(cap.percents.size == config.rounds) { "차수 수 불일치" }
                require(cap.percents.all { it >= 0 } && cap.percents.sum() > 0) { "비율 오류" }
                apportion(n, cap.percents) // 합 100 아니어도 정규화됨
            }
        }

    /** 최대잉여법: 비율→정수 판 수, 합=n 보장, 결정론적 */
    internal fun apportion(n: Int, weights: List<Double>): IntArray {
        val sum = weights.sum()
        val exact = DoubleArray(weights.size) { n * weights[it] / sum }
        val base = IntArray(weights.size) { exact[it].toInt() }
        var left = n - base.sum()
        val order = (weights.indices).sortedWith(
            compareByDescending<Int> { exact[it] - base[it] }.thenBy { it })
        var i = 0
        while (left > 0) { base[order[i % order.size]]++; left--; i++ }
        return base
    }

    // ── Phase 2: Greedy "1차 전종류"는 NP-hard지만 K~10이라 greedy로 충분.
    //    기준: 미커버 종류 최다 신규 커버 > 종류 수 많은 판 > index 순(결정론) ──
    private fun seedCover(st: State, c: Compiled, target: IntArray) {
        var covered = 0L
        for (i in 0 until c.n) if (st.assign[i] == 0) covered = covered or c.masks[i]
        while (covered != c.fullMask && st.roundSize[0] < target[0]) {
            var best = -1; var bestNew = 0; var bestTotal = 0
            for (i in 0 until c.n) {
                if (st.assign[i] != -1) continue
                val nb = java.lang.Long.bitCount(c.masks[i] and covered.inv() and c.fullMask)
                if (nb == 0) continue
                val tot = java.lang.Long.bitCount(c.masks[i])
                if (nb > bestNew || (nb == bestNew && tot > bestTotal)) {
                    best = i; bestNew = nb; bestTotal = tot
                }
            }
            if (best == -1) break // 더 커버 불가(pin 충돌 등) → violations에서 보고됨
            st.place(best, 0); covered = covered or c.masks[best]
        }
    }

    // ── Phase 3 전략들. 전부 target까지만 채움(합=n이라 항상 정확히 소진) ──

    /** 전략A: 사람의 요령 그대로 — 다종판은 1차 잔여슬롯, 통판은 마지막 차수부터 */
    private fun seedFieldHabit(st: State, rest: List<Int>) {
        val byDesc = rest.sortedWith(
            compareByDescending<Int> { java.lang.Long.bitCount(st.masks[it]) }.thenBy { it })
        val leftover = ArrayList<Int>()
        for (i in byDesc) if (st.roundSize[0] < st.target[0]) st.place(i, 0) else leftover += i
        leftover.sortWith(compareBy<Int> { java.lang.Long.bitCount(st.masks[it]) }.thenBy { it })
        for (i in leftover) {
            var placed = false
            for (rd in st.r - 1 downTo 1) if (st.roundSize[rd] < st.target[rd]) {
                st.place(i, rd); placed = true; break
            }
            if (!placed) st.place(i, 0)
        }
    }

    /** 전략B: 빈 슬롯 최다 차수부터 고르게 (동률 시 앞 차수) */
    private fun seedBalanced(st: State, rest: List<Int>) {
        val byDesc = rest.sortedWith(
            compareByDescending<Int> { java.lang.Long.bitCount(st.masks[it]) }.thenBy { it })
        for (i in byDesc) {
            val rd = (0 until st.r).maxWith(
                compareBy<Int> { st.target[it] - st.roundSize[it] }.thenByDescending { it })
            st.place(i, if (st.roundSize[rd] < st.target[rd]) rd
                        else (0 until st.r).first { st.roundSize[it] < st.target[it] })
        }
    }

    /** 전략C: 수량 큰 판을 뒤 차수부터 (수량 미상 판은 종류 수로 대리) */
    private fun seedQtyLate(st: State, rest: List<Int>) {
        val order = rest.sortedWith(compareByDescending<Int> {
            if (st.qty[it] > 0) st.qty[it] else java.lang.Long.bitCount(st.masks[it]) * 10
        }.thenBy { it })
        for (i in order) {
            var placed = false
            for (rd in st.r - 1 downTo 0) if (st.roundSize[rd] < st.target[rd]) {
                st.place(i, rd); placed = true; break
            }
            if (!placed) st.place(i, 0)
        }
    }

    // ── Phase 4: hill climbing. swap은 판 수 보존(양 모드), move는 Ratio 밴드 내에서만.
    //    strict(>) 수락만 → score 단조증가+유계 → 유한 종료. maxPasses는 방어용 ──
    private fun climb(
        st: State,
        pin: IntArray,
        bandLow: IntArray?,
        bandHigh: IntArray,
        onAttempt: () -> Unit
    ) {
        var cur = st.score(); var pass = 0; var improved = true
        while (improved && pass++ < st.w.maxPasses) {
            improved = false
            for (a in 0 until st.n) {
                if (pin[a] >= 0) continue
                for (b in a + 1 until st.n) {
                    if (pin[b] >= 0) continue
                    val ra = st.assign[a]; val rb = st.assign[b]
                    if (ra == rb) continue
                    st.move(a, rb); st.move(b, ra)
                    val ns = st.score()
                    onAttempt()
                    if (ns > cur) { cur = ns; improved = true }
                    else { st.move(a, ra); st.move(b, rb) }
                }
            }
            if (bandLow != null) for (a in 0 until st.n) {   // Ratio 전용 move
                if (pin[a] >= 0) continue
                val ra = st.assign[a]
                for (rd in 0 until st.r) {
                    if (rd == ra) continue
                    if (st.roundSize[rd] + 1 > bandHigh[rd]) continue
                    if (st.roundSize[ra] - 1 < bandLow[ra]) continue
                    st.move(a, rd)
                    val ns = st.score()
                    onAttempt()
                    if (ns > cur) { cur = ns; improved = true; break }
                    else st.move(a, ra)
                }
            }
        }
    }

    private fun buildCandidate(
        st: State, c: Compiled, config: AllocationConfig,
        target: IntArray, maxDev: Int, name: String, trays: List<Tray>
    ): AllocationCandidate {
        val violations = buildList {
            val missing = (0 until c.k).filter { st.typeCnt[0][it] == 0 }
            if (missing.size > config.allowedMissingTypes)
                add("1차 누락 ${missing.size}종(허용 ${config.allowedMissingTypes}): " +
                    missing.joinToString { c.typeNames[it] } + " — pin/판 수 확인")
            for (rd in 1 until st.r)
                if (st.distinct[rd] >= st.distinct[rd - 1])
                    add("종류 수 비감소: ${rd}차 ${st.distinct[rd - 1]}종 ≤ ${rd + 1}차 ${st.distinct[rd]}종")
            for (rd in 0 until st.r) {
                val diff = st.roundSize[rd] - target[rd]
                if (config.capacity is CapacityMode.Exact && diff != 0)
                    add("${rd + 1}차 판 수 불일치: ${st.roundSize[rd]}/${target[rd]} (pin 충돌 가능성)")
                if (config.capacity is CapacityMode.Ratio && kotlin.math.abs(diff) > maxDev)
                    add("${rd + 1}차 목표 이탈: ${st.roundSize[rd]} (목표 ${target[rd]}±$maxDev)")
            }
        }
        return AllocationCandidate(
            assignment = trays.indices.associate { trays[it].id to st.assign[it] + 1 },
            roundSizes = st.roundSize.toList(),
            score = st.score(),
            violations = violations,
            strategyName = name
        )
    }

    // ── 증분 카운터 상태: score()가 배치 재스캔 없이 O(R·K), 핫루프 힙할당 0 ──
    internal class State(
        val n: Int, val k: Int, val r: Int,
        val masks: LongArray, val qty: IntArray, val allQtyKnown: Boolean,
        val target: IntArray, val peakRound: Int, val ratioMode: Boolean,
        val w: DerivedWeights
    ) {
        val assign = IntArray(n) { -1 }
        val roundSize = IntArray(r)
        val qtySum = IntArray(r)
        val typeCnt = Array(r) { IntArray(k) }
        val distinct = IntArray(r)

        fun place(i: Int, rd: Int) {
            assign[i] = rd; roundSize[rd]++; qtySum[rd] += qty[i]
            var m = masks[i]
            while (m != 0L) {
                val t = java.lang.Long.numberOfTrailingZeros(m)
                if (typeCnt[rd][t]++ == 0) distinct[rd]++   // 0→1 경계만 갱신
                m = m and (m - 1)                            // 최하위 set bit 제거
            }
        }
        fun remove(i: Int) {
            val rd = assign[i]
            assign[i] = -1; roundSize[rd]--; qtySum[rd] -= qty[i]
            var m = masks[i]
            while (m != 0L) {
                val t = java.lang.Long.numberOfTrailingZeros(m)
                if (--typeCnt[rd][t] == 0) distinct[rd]--
                m = m and (m - 1)
            }
        }
        fun move(i: Int, rd: Int) { remove(i); place(i, rd) }

        fun score(): Int {
            var s = 0
            s -= (k - distinct[0]) * w.missing                        // 계층1: 1차 커버
            for (rd in 1 until r)                                     // 계층2: 종류 감소 체인
                if (distinct[rd] >= distinct[rd - 1])
                    s -= (distinct[rd] - distinct[rd - 1] + 1) * w.typeOrder
            for (t in 0 until k) {                                    // 계층3: 분산/중복
                var total = 0; var roundsWith = 0
                for (rd in 0 until r) {
                    val cnt = typeCnt[rd][t]; total += cnt
                    if (cnt > 0) roundsWith++
                }
                if (total < 2) continue
                s += (roundsWith - 1) * w.spread
                s -= (total - roundsWith) * w.clump
            }
            if (ratioMode) for (rd in 0 until r) {                    // 계층3.5: 목표 편차²
                val d = roundSize[rd] - target[rd]
                s -= d * d * w.ratioDev
            }
            if (allQtyKnown) for (rd in 0 until r)                    // 계층4: 수량 피크
                if (qtySum[rd] > qtySum[peakRound])
                    s -= (qtySum[rd] - qtySum[peakRound]) / w.qtyDivisor
            return s
        }
    }
}
