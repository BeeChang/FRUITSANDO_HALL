package example.yf.fruit_hall.core.rotation

import kotlin.random.Random

// ⚠ 이 클래스를 빼먹으면 매일 똑같은 표가 나온다 (명세 §7-4).
// 부채 동점 시 인덱스 순으로 뽑거나, 포지션을 항상 같은 순서로 순회하면
// "포지션 개설순위 × 멤버 등록순서"가 그대로 결과가 되어버린다.
//
// 하나의 Random(seed)을 여러 호출부가 공유하면 호출 순서가 바뀔 때마다 결과 전체가
// 달라져 재현이 불안정해진다. 대신 seed + 호출 지점 태그 + salt로 매번 독립된 파생 시드를
// 만든다 — 같은 시드+같은 입력이면 항상 같은 결과, 슬롯마다는 다른 순열이 나온다.
class SeededShuffle(private val seed: Long) {

    fun <T> shuffled(items: List<T>, tag: String, salt: Int): List<T> {
        if (items.size <= 1) return items
        return items.shuffled(Random(derive(tag, salt)))
    }

    private fun derive(tag: String, salt: Int): Long {
        var h = seed
        h = h * 1000003L xor tag.hashCode().toLong()
        h = h * 1000003L xor salt.toLong()
        return h
    }
}
