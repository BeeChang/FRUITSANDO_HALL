package example.yf.fruit_hall.core

import kotlin.math.abs
import kotlin.math.max

// ═══════════════════ 도메인 모델 ═══════════════════

enum class RoughSize(val nominal: Int) { S(15), M(30), L(50) }

data class TrayItem(
    val typeId: String,
    val exactQty: Int? = null,
    val roughSize: RoughSize? = null
)

data class Tray(
    val id: String,
    val items: List<TrayItem>,       // 1~5종
    val pinnedRound: Int? = null,    // 1-based 차수 고정
    val location: String? = null     // 보관 위치. null=미상 → 절대 감점하지 않음
)

/** 설정 화면에 노출할 유일한 손잡이. 어떤 조합도 계층 벽을 못 깬다. */
enum class Level { LOW, MID, HIGH }
data class TuningPresets(
    val spreadStrength: Level = Level.MID,   // 분산 선호 강도
    val orderStrictness: Level = Level.MID,  // 종류 감소 엄격도
    val qtySensitivity: Level = Level.MID,   // 수량 밸런스 민감도
    val moveAversion: Level = Level.MID      // 운반 감점 강도
)

/** 차수별 용량: Fixed=판 수 고정, Flexible=잔여를 weight 비율로 나눠 목표±flexDeviation */
sealed interface RoundCapacity {
    data class Fixed(val trays: Int) : RoundCapacity
    data class Flexible(val weight: Double = 1.0) : RoundCapacity
}

data class AllocationConfig(
    val rounds: Int,                          // 차수 수 R (>=2)
    val capacity: List<RoundCapacity>,        // size == rounds
    val flexDeviation: Int = 2,               // Flexible 차수의 목표±허용 판 수
    val allowedMissingTypes: Int = 0,         // 1차 누락 허용 종류 수(보고 기준)
    val primaryLocation: String? = null,      // 최종 판매 위치. null=운반 항 비활성
    val primaryLocationCapacity: Int? = null, // 최종 판매 위치가 실제로 수용 가능한 판 수. null=제한 없음/미입력 (보고 기준, 탐색에는 관여 안 함)
    val topN: Int = 5,                        // 반환 후보 수 상한
    val presets: TuningPresets = TuningPresets(),
    val ilsIterations: Int = 6                // 결정론적 ILS(perturb→재climb) 반복 횟수
)

data class AllocationCandidate(
    val assignment: Map<String, Int>,  // trayId -> 1..R
    val roundSizes: List<Int>,
    val score: Int,
    val moveCount: Int,                // 1차 중 primaryLocation 밖 판 수(위치 아는 판만)
    val violations: List<String>
)

// ═══════════════════ 가중치 자동 파생 ═══════════════════
// invariant: 하위 계층 이론 최대 총점 < 상위 계층 위반 1건. 손 검산 대신
// 상향식으로 코드가 도출한다. K/R/N/수량이 바뀌어도 벽이 자동 재구축.
// ⚠ 이 파생 로직 수정 금지.
class DerivedWeights(
    val missing: Int,    // 계층1: 1차 누락 종류당
    val typeOrder: Int,  // 계층2: 종류수 비감소 역전당
    val spread: Int,     // 계층3: 분산 보상
    val clump: Int,      // 계층3: 같은 차수 중복 감점
    val movePen: Int,    // 계층3: 1차 비최종위치 판당 감점 (약함 — 원료 생성용)
    val devPen: Int,     // 계층3: Flexible 목표 편차² 당
    val qtyDivisor: Int, // 계층4: 수량 편차 ÷ divisor
    val maxPasses: Int = 64
) {
    companion object {
        fun derive(k: Int, r: Int, n: Int, totalQty: Int, flexDev: Int, p: TuningPresets): DerivedWeights {
            val clump = 50
            val spread = when (p.spreadStrength) { Level.LOW -> 50; Level.MID -> 100; Level.HIGH -> 200 }
            val movePen = when (p.moveAversion) { Level.LOW -> 10; Level.MID -> 25; Level.HIGH -> 40 }
            val devPen = spread * 2
            // 계층3 계열(분산+운반+편차) 이론 최대 절대값 합
            val tier3Max = k * (r - 1) * spread + n * movePen + r * flexDev * flexDev * devPen + 1
            val margin = when (p.orderStrictness) { Level.LOW -> 1.2; Level.MID -> 1.5; Level.HIGH -> 2.0 }
            val typeOrder = max((tier3Max * margin).toInt(), 1_000)
            val tier2Max = (r - 1) * (k + 1) * typeOrder
            val missing = ((tier2Max + tier3Max) * 1.5).toInt()
            // 계층4 최대(totalQty/div)가 계층3 최소 단위보다 작도록
            val minUnit = minOf(clump, movePen)
            val divBase = totalQty / minUnit + 1
            val qtyDivisor = when (p.qtySensitivity) {
                Level.LOW -> divBase * 4; Level.MID -> divBase * 2; Level.HIGH -> divBase
            }
            return DerivedWeights(missing, typeOrder, spread, clump, movePen, devPen, qtyDivisor)
        }
    }
}

// ═══════════════════ 배분 엔진 ═══════════════════
// 파이프라인:
//  ①컴파일(비트마스크) ②pin 고정 ③greedy set-cover(1차 전종류 seed)
//  ④현장 관행형 잔여 배치 ⑤hill climbing(swap + Flexible 밴드 내 move)
//  ⑥결정론적 ILS: 국소최적 스냅샷 → 강제 perturb → 재climb 반복으로 풀 수집
//  ⑦풀에서 점수+다양성+운반최소 보장으로 topN 선별
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
        require(config.capacity.size == config.rounds) { "capacity 크기 != rounds" }
        require(config.topN >= 1)
        require(config.ilsIterations >= 1) { "ilsIterations는 1 이상" }

        val c = compile(trays, config)
        val target = resolveTargets(c.n, config)
        val flexible = BooleanArray(config.rounds) { config.capacity[it] is RoundCapacity.Flexible }
        // 밴드: Fixed는 폭 0(=move 불가), Flexible은 target±dev, 1차는 최소 1판
        val bandLow = IntArray(config.rounds) { rd ->
            val lo = if (flexible[rd]) target[rd] - config.flexDeviation else target[rd]
            max(if (rd == 0) 1 else 0, lo)
        }
        val bandHigh = IntArray(config.rounds) { rd ->
            if (flexible[rd]) target[rd] + config.flexDeviation else target[rd]
        }
        val anyFlex = flexible.any { it }
        val w = DerivedWeights.derive(
            c.k, config.rounds, c.n, c.totalQtyEstimate,
            if (anyFlex) config.flexDeviation else 0, config.presets
        )
        val peakRound = target.indices.maxBy { target[it] }

        val st = State(c, config.rounds, target, peakRound, w)
        for (i in 0 until c.n) if (c.pin[i] >= 0) st.place(i, c.pin[i])   // Phase 2
        seedCover(st, c, target)                                          // Phase 3
        seedFieldHabit(st)                                                // Phase 4

        // Phase 5+6: climb + 결정론적 ILS로 풀 수집
        val pool = Pool()
        climb(st, c.pin, bandLow, bandHigh, anyFlex, onAttempt)
        pool.add(st.snapshot())
        for (iter in 1..config.ilsIterations) {
            perturb(st, c.pin, bandLow, bandHigh, anyFlex, iter)
            climb(st, c.pin, bandLow, bandHigh, anyFlex, onAttempt)
            pool.add(st.snapshot())
        }

        // Phase 7: 선별 → 후보 조립
        val picked = select(pool, config.topN)
        return picked.map { buildCandidate(it, c, config, target, bandLow, bandHigh, trays) }
    }

    // ── Phase 1: 컴파일. 종류집합→Long 비트마스크(집합연산=CPU 1명령),
    //    위치→"최종위치 밖" boolean (location null이면 false: 모름≠감점) ──
    internal class Compiled(
        val n: Int, val k: Int, val fullMask: Long,
        val masks: LongArray, val qty: IntArray, val pin: IntArray,
        val nonPrimary: BooleanArray,       // true = 1차 배정 시 운반 필요
        val allQtyKnown: Boolean, val totalQtyEstimate: Int,
        val typeNames: Array<String>
    )

    internal fun compile(trays: List<Tray>, config: AllocationConfig): Compiled {
        val typeIndex = HashMap<String, Int>()
        for (t in trays) for (item in t.items)
            typeIndex.getOrPut(item.typeId) { typeIndex.size }
        val k = typeIndex.size
        require(k in 1..64) { "종류는 1~64" }
        val n = trays.size
        val masks = LongArray(n); val qty = IntArray(n)
        val pin = IntArray(n) { -1 }; val nonPrimary = BooleanArray(n)
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
                require(it in 1..config.rounds) { "pin 차수 범위 오류: $it" }
                pin[i] = it - 1
            }
            // 위치 미상(null) 또는 primary 미설정이면 감점 대상 아님 — 안전 불변식
            nonPrimary[i] = config.primaryLocation != null &&
                trays[i].location != null && trays[i].location != config.primaryLocation
        }
        val names = arrayOfNulls<String>(k)
        for ((id, idx) in typeIndex) names[idx] = id
        @Suppress("UNCHECKED_CAST")
        return Compiled(n, k, if (k == 64) -1L else (1L shl k) - 1, masks, qty, pin,
            nonPrimary, allKnown, if (allKnown) qty.sum() else n * 40, names as Array<String>)
    }

    // ── 목표 판 수: Fixed 차감 후 잔여를 Flexible weight로 최대잉여법 배분 ──
    internal fun resolveTargets(n: Int, config: AllocationConfig): IntArray {
        val fixedSum = config.capacity.filterIsInstance<RoundCapacity.Fixed>().sumOf { it.trays }
        val flexIdx = config.capacity.indices.filter { config.capacity[it] is RoundCapacity.Flexible }
        val rest = n - fixedSum
        val target = IntArray(config.rounds)
        for (i in config.capacity.indices) {
            val cap = config.capacity[i]
            if (cap is RoundCapacity.Fixed) {
                require(cap.trays >= 0); target[i] = cap.trays
            }
        }
        if (flexIdx.isEmpty()) {
            require(rest == 0) { "Fixed 합($fixedSum) != 전체 판 수($n)" }
        } else {
            require(rest >= 0) { "Fixed 합($fixedSum)이 전체($n)를 초과" }
            val weights = flexIdx.map { (config.capacity[it] as RoundCapacity.Flexible).weight }
            require(weights.all { it >= 0 } && weights.sum() > 0) { "Flexible weight 오류" }
            val parts = apportion(rest, weights)
            flexIdx.forEachIndexed { j, idx -> target[idx] = parts[j] }
        }
        require(target[0] >= 1) { "1차는 최소 1판 (커버리지 차수)" }
        return target
    }

    /** 최대잉여법: 합=total 보장, 결정론적 */
    internal fun apportion(total: Int, weights: List<Double>): IntArray {
        val sum = weights.sum()
        val exact = DoubleArray(weights.size) { total * weights[it] / sum }
        val base = IntArray(weights.size) { exact[it].toInt() }
        var left = total - base.sum()
        val order = weights.indices.sortedWith(
            compareByDescending<Int> { exact[it] - base[it] }.thenBy { it })
        var i = 0
        while (left > 0) { base[order[i % order.size]]++; left--; i++ }
        return base
    }

    // ── Phase 3: greedy set-cover. 기준: 미커버 최다 신규 커버 > 종류 수 > index(결정론) ──
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
            if (best == -1) break // 불가(pin 충돌 등) → violations에서 보고
            st.place(best, 0); covered = covered or c.masks[best]
        }
    }

    // ── Phase 4: 현장 관행형 배치(유일 seed). 다종판→1차 잔여슬롯, 통판→뒤차수부터 ──
    private fun seedFieldHabit(st: State) {
        val rest = (0 until st.n).filter { st.assign[it] == -1 }
        val byDesc = rest.sortedWith(
            compareByDescending<Int> { java.lang.Long.bitCount(st.masks[it]) }.thenBy { it })
        val leftover = ArrayList<Int>()
        for (i in byDesc) if (st.roundSize[0] < st.target[0]) st.place(i, 0) else leftover += i
        leftover.sortWith(compareBy<Int> { java.lang.Long.bitCount(st.masks[it]) }.thenBy { it })
        for (i in leftover) {
            var placed = false
            for (rd in st.r - 1 downTo 0) if (st.roundSize[rd] < st.target[rd]) {
                st.place(i, rd); placed = true; break
            }
            if (!placed) st.place(i, (0 until st.r).minBy { st.roundSize[it] - st.target[it] })
        }
    }

    // ── Phase 5: hill climbing. swap=판 수 보존(항상 안전), move=밴드 내에서만
    //    (Fixed는 밴드 폭 0이라 자동 차단). strict(>) 수락 → 단조증가+유계 → 유한 종료 ──
    private fun climb(
        st: State,
        pin: IntArray,
        bandLow: IntArray,
        bandHigh: IntArray,
        anyFlex: Boolean,
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
            if (anyFlex) for (a in 0 until st.n) {
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

    // ── Phase 6: 결정론적 perturbation (ILS). 국소최적에서 강제로 2회 swap
    //    (+Flexible이면 밴드 내 move 1회)해 다른 골짜기로 밀어낸 뒤 재climb.
    //    iter 기반 소수(prime) 보폭 순회라 랜덤 없이 매번 다른 방향으로 찬다 ──
    private fun perturb(st: State, pin: IntArray, bandLow: IntArray, bandHigh: IntArray,
                        anyFlex: Boolean, iter: Int) {
        var kicks = 0
        var idx = (iter * 7) % st.n
        var guard = 0
        while (kicks < 2 && guard++ < st.n * st.n) {
            val a = idx % st.n
            val b = (idx / st.n + iter * 3 + 1) % st.n
            idx += 11
            if (a == b || pin[a] >= 0 || pin[b] >= 0) continue
            if (st.assign[a] == st.assign[b]) continue
            val ra = st.assign[a]; val rb = st.assign[b]
            st.move(a, rb); st.move(b, ra)   // 점수 무관 강제 수락
            kicks++
        }
        if (anyFlex) {
            var i = (iter * 5) % st.n; var g = 0
            while (g++ < st.n) {
                val a = i % st.n; i++
                if (pin[a] >= 0) continue
                val ra = st.assign[a]
                val rd = (ra + 1 + iter) % st.r
                if (rd == ra) continue
                if (st.roundSize[rd] + 1 > bandHigh[rd]) continue
                if (st.roundSize[ra] - 1 < bandLow[ra]) continue
                st.move(a, rd); break
            }
        }
    }

    // ── Phase 7: 풀 선별. 규칙:
    //    ①종합 점수 1위 필수 ②moveCount 최소(동률 시 고점) 필수 — 클라의
    //    "운반 최소형" 라벨 원료 보장 ③나머지는 점수순이되, 이미 뽑힌 것들과
    //    거리>0(판수 또는 종류구성이 실제로 다름)인 것 우선. 부족하면 거리 0 허용 ──
    internal class Snapshot(
        val assign: IntArray, val score: Int, val moveCount: Int,
        val roundSizes: IntArray, val roundTypeMasks: LongArray
    )

    private class Pool {
        val entries = ArrayList<Snapshot>()
        private val seen = HashSet<List<Int>>()
        fun add(s: Snapshot) { if (seen.add(s.assign.toList())) entries += s }
    }

    private fun distance(a: Snapshot, b: Snapshot): Int {
        var d = 0
        for (rd in a.roundSizes.indices) d += abs(a.roundSizes[rd] - b.roundSizes[rd])
        var typeDiff = 0
        for (rd in a.roundTypeMasks.indices)
            typeDiff += java.lang.Long.bitCount(a.roundTypeMasks[rd] xor b.roundTypeMasks[rd])
        return d * 1000 + typeDiff   // 판 수 차이 > 종류 구성 차이 (유저의 비교 기준 서열)
    }

    private fun select(pool: Pool, topN: Int): List<Snapshot> {
        val sorted = pool.entries.sortedWith(
            compareByDescending<Snapshot> { it.score }.thenBy { it.moveCount })
        if (sorted.isEmpty()) return emptyList()
        val picked = ArrayList<Snapshot>()
        picked += sorted[0]                                            // ① 종합 1위
        val minMove = sorted.minWith(                                  // ② 운반 최소 보장
            compareBy<Snapshot> { it.moveCount }.thenByDescending { it.score })
        if (minMove !== sorted[0] && picked.size < topN) picked += minMove
        for (s in sorted) {                                            // ③ 다양성 우선 충원
            if (picked.size >= topN) break
            if (picked.any { it === s }) continue
            if (picked.all { distance(it, s) > 0 }) picked += s
        }
        for (s in sorted) {                                            // 부족 시 거리 0 허용
            if (picked.size >= topN) break
            if (picked.none { it === s }) picked += s
        }
        return picked
    }

    private fun buildCandidate(
        s: Snapshot, c: Compiled, config: AllocationConfig,
        target: IntArray, bandLow: IntArray, bandHigh: IntArray, trays: List<Tray>
    ): AllocationCandidate {
        val r = config.rounds
        val distinct = IntArray(r) { java.lang.Long.bitCount(s.roundTypeMasks[it]) }
        val violations = buildList {
            val missing = (0 until c.k).filter { s.roundTypeMasks[0] and (1L shl it) == 0L }
            if (missing.size > config.allowedMissingTypes)
                add("1차 누락 ${missing.size}종(허용 ${config.allowedMissingTypes}): " +
                    missing.joinToString { c.typeNames[it] } + " — pin/1차 판 수 확인")
            for (rd in 1 until r) if (distinct[rd] >= distinct[rd - 1])
                add("종류 수 비감소: ${rd}차 ${distinct[rd - 1]}종 ≤ ${rd + 1}차 ${distinct[rd]}종")
            for (rd in 0 until r) {
                val sz = s.roundSizes[rd]
                if (sz < bandLow[rd] || sz > bandHigh[rd])
                    add("${rd + 1}차 판 수 이탈: $sz (허용 ${bandLow[rd]}~${bandHigh[rd]}, pin 충돌 가능성)")
            }
            val cap = config.primaryLocationCapacity
            if (cap != null && s.roundSizes[0] > cap)
                add("1차 총 ${s.roundSizes[0]}판이 최종 판매 위치 수용 한도(${cap}판)를 초과합니다 — 차수 수/판 수 설정을 조정하세요")
        }
        return AllocationCandidate(
            assignment = trays.indices.associate { trays[it].id to s.assign[it] + 1 },
            roundSizes = s.roundSizes.toList(),
            score = s.score,
            moveCount = s.moveCount,
            violations = violations
        )
    }

    // ── 증분 카운터 상태. score()가 배치 재스캔 없이 O(R·K), 핫루프 힙할당 0 ──
    internal class State(c: Compiled, val r: Int, val target: IntArray,
                         val peakRound: Int, val w: DerivedWeights) {
        val n = c.n; val k = c.k
        val masks = c.masks; val qty = c.qty
        private val nonPrimary = c.nonPrimary
        private val allQtyKnown = c.allQtyKnown

        val assign = IntArray(n) { -1 }
        val roundSize = IntArray(r)
        val qtySum = IntArray(r)
        val typeCnt = Array(r) { IntArray(k) }
        val distinct = IntArray(r)
        var movesInR1 = 0; private set   // 1차 배정된 비최종위치 판 수 (증분 유지)

        fun place(i: Int, rd: Int) {
            assign[i] = rd; roundSize[rd]++; qtySum[rd] += qty[i]
            if (rd == 0 && nonPrimary[i]) movesInR1++
            var m = masks[i]
            while (m != 0L) {
                val t = java.lang.Long.numberOfTrailingZeros(m)
                if (typeCnt[rd][t]++ == 0) distinct[rd]++
                m = m and (m - 1)
            }
        }
        fun remove(i: Int) {
            val rd = assign[i]
            assign[i] = -1; roundSize[rd]--; qtySum[rd] -= qty[i]
            if (rd == 0 && nonPrimary[i]) movesInR1--
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
            s -= (k - distinct[0]) * w.missing                          // 계층1: 1차 커버
            for (rd in 1 until r) if (distinct[rd] >= distinct[rd - 1]) // 계층2: 종류 감소
                s -= (distinct[rd] - distinct[rd - 1] + 1) * w.typeOrder
            for (t in 0 until k) {                                      // 계층3: 분산/중복
                var total = 0; var roundsWith = 0
                for (rd in 0 until r) { val cnt = typeCnt[rd][t]; total += cnt; if (cnt > 0) roundsWith++ }
                if (total < 2) continue
                s += (roundsWith - 1) * w.spread
                s -= (total - roundsWith) * w.clump
            }
            s -= movesInR1 * w.movePen                                  // 계층3: 운반(약)
            for (rd in 0 until r) {                                     // 계층3: 목표 편차²
                val d = roundSize[rd] - target[rd]; s -= d * d * w.devPen
            }
            if (allQtyKnown) for (rd in 0 until r)                      // 계층4: 수량 피크
                if (qtySum[rd] > qtySum[peakRound])
                    s -= (qtySum[rd] - qtySum[peakRound]) / w.qtyDivisor
            return s
        }

        fun snapshot(): Snapshot {
            val tm = LongArray(r)
            for (rd in 0 until r) {
                var m = 0L
                for (t in 0 until k) if (typeCnt[rd][t] > 0) m = m or (1L shl t)
                tm[rd] = m
            }
            return Snapshot(assign.copyOf(), score(), movesInR1, roundSize.copyOf(), tm)
        }
    }
}
