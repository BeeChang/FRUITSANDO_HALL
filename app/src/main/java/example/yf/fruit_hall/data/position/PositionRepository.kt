package example.yf.fruit_hall.data.position

import example.yf.fruit_hall.data.position.dao.AssignmentDao
import example.yf.fruit_hall.data.position.dao.MemberDao
import example.yf.fruit_hall.data.position.dao.PositionDao
import example.yf.fruit_hall.data.position.dao.SlotSettingsDao
import example.yf.fruit_hall.data.position.dao.WorkDayDao
import example.yf.fruit_hall.data.position.entity.AssignmentEntity
import example.yf.fruit_hall.data.position.entity.MemberEntity
import example.yf.fruit_hall.data.position.entity.PositionEntity
import example.yf.fruit_hall.data.position.entity.SlotSettingsEntity
import example.yf.fruit_hall.data.position.entity.WeightDecayMode
import example.yf.fruit_hall.data.position.entity.WorkDayEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class PositionRepository @Inject constructor(
    private val memberDao: MemberDao,
    private val positionDao: PositionDao,
    private val workDayDao: WorkDayDao,
    private val slotSettingsDao: SlotSettingsDao,
    private val assignmentDao: AssignmentDao,
) {
    fun observeMembers(): Flow<List<MemberEntity>> = memberDao.observeAll()

    fun observePositions(): Flow<List<PositionEntity>> = positionDao.observeAll()

    fun observeSlotSettings(): Flow<SlotSettingsEntity> =
        slotSettingsDao.observe().map { it ?: SlotSettingsEntity() }

    suspend fun getOrCreateToday(): WorkDayEntity {
        val today = LocalDate.now().toString()
        return workDayDao.getByDate(today) ?: run {
            memberDao.resetAllWorking()
            val newDay = WorkDayEntity(date = today)
            workDayDao.upsert(newDay)
            newDay
        }
    }

    suspend fun getTodayAssignments(date: String): List<AssignmentEntity> =
        assignmentDao.getByDate(date)

    suspend fun getSlotAssignments(date: String, slot: Int): List<AssignmentEntity> =
        assignmentDao.getByDateAndSlot(date, slot)

    suspend fun confirmAssignments(
        date: String,
        slot: Int,
        assignments: List<Pair<Long, Long>>
    ) {
        assignmentDao.deleteByDateAndSlot(date, slot)
        val entities = assignments.map { (memberId, positionId) ->
            AssignmentEntity(
                date = date,
                slotNumber = slot,
                memberId = memberId,
                positionId = positionId
            )
        }
        assignmentDao.insertAll(entities)
    }

    suspend fun advanceSlot(date: String, currentSlot: Int) {
        workDayDao.upsert(WorkDayEntity(date = date, currentSlot = currentSlot + 1))
    }

    suspend fun setSlot(date: String, slot: Int) {
        workDayDao.upsert(WorkDayEntity(date = date, currentSlot = slot))
    }

    suspend fun resetToday(date: String) {
        assignmentDao.deleteByDate(date)
        workDayDao.deleteByDate(date)
    }

    suspend fun resetAll() {
        assignmentDao.deleteAll()
        workDayDao.deleteAll()
    }

    suspend fun reorderMembers(orderedIds: List<Long>) {
        orderedIds.forEachIndexed { index, id -> memberDao.updateSortOrder(id, index) }
    }

    suspend fun addMember(member: MemberEntity) = memberDao.insert(member)
    suspend fun updateMember(member: MemberEntity) = memberDao.update(member)
    suspend fun deleteMember(member: MemberEntity) = memberDao.delete(member)
    suspend fun setMemberWorking(id: Long, isWorking: Boolean) = memberDao.setWorking(id, isWorking)

    suspend fun addPosition(position: PositionEntity) = positionDao.insert(position)
    suspend fun updatePosition(position: PositionEntity) = positionDao.update(position)
    suspend fun deletePosition(position: PositionEntity) = positionDao.delete(position)
    suspend fun positionCount(): Int = positionDao.count()

    suspend fun updateSlotSettings(totalSlots: Int) =
        slotSettingsDao.upsert(SlotSettingsEntity(totalSlots = totalSlots))

    fun performDraw(
        members: List<MemberEntity>,
        positions: List<PositionEntity>,
        todayAssignments: List<AssignmentEntity>,
        currentSlot: Int
    ): List<Pair<Long, Long>> {
        if (members.isEmpty() || positions.isEmpty()) return emptyList()

        val previousAssignments = todayAssignments.filter { it.slotNumber < currentSlot }

        val sortedPositions = positions.sortedWith(
            compareBy({ it.isMultiPerson }, { it.sortOrder })
        )

        val fixedPositions = sortedPositions.filter { !it.isMultiPerson }
        val multiPositions = sortedPositions.filter { it.isMultiPerson }

        val slotCounts = distributeMembers(members.size, fixedPositions.size, multiPositions.size)

        val result = mutableListOf<Pair<Long, Long>>()
        val assignedMemberIds = mutableSetOf<Long>()
        val remainingMembers = members.shuffled().toMutableList()

        for ((index, position) in (fixedPositions + multiPositions).withIndex()) {
            val count = slotCounts[index]
            if (count == 0) continue

            val candidateMembers = remainingMembers.filter { it.id !in assignedMemberIds }
            if (candidateMembers.isEmpty()) break

            val picked = weightedPick(
                candidates = candidateMembers,
                position = position,
                previousAssignments = previousAssignments,
                count = minOf(count, candidateMembers.size)
            )

            picked.forEach { member ->
                result.add(member.id to position.id)
                assignedMemberIds.add(member.id)
                remainingMembers.remove(member)
            }
        }

        return result
    }

    private fun distributeMembers(
        memberCount: Int,
        fixedCount: Int,
        multiCount: Int
    ): List<Int> {
        val counts = mutableListOf<Int>()
        var remaining = memberCount

        val actualFixed = minOf(fixedCount, remaining)
        repeat(actualFixed) {
            counts.add(1)
            remaining--
        }
        repeat(fixedCount - actualFixed) { counts.add(0) }

        if (multiCount == 0) return counts

        if (remaining <= 0) {
            repeat(multiCount) { counts.add(0) }
            return counts
        }

        val basePerMulti = remaining / multiCount
        val extra = remaining % multiCount

        for (i in 0 until multiCount) {
            counts.add(basePerMulti + if (i < extra) 1 else 0)
        }

        return counts
    }

    private fun weightedPick(
        candidates: List<MemberEntity>,
        position: PositionEntity,
        previousAssignments: List<AssignmentEntity>,
        count: Int
    ): List<MemberEntity> {
        val weights = candidates.map { member ->
            member to computeWeight(member, position, previousAssignments)
        }

        val picked = mutableListOf<MemberEntity>()
        val remaining = weights.toMutableList()

        repeat(count) {
            if (remaining.isEmpty()) return@repeat
            val totalWeight = remaining.sumOf { it.second.toDouble() }
            val rand = Random.nextDouble(totalWeight)
            var cumulative = 0.0
            val selected = remaining.firstOrNull { (_, w) ->
                cumulative += w
                cumulative >= rand
            }?.first ?: remaining.last().first

            picked.add(selected)
            remaining.removeIf { it.first.id == selected.id }
        }

        return picked
    }

    private fun computeWeight(
        member: MemberEntity,
        position: PositionEntity,
        previousAssignments: List<AssignmentEntity>
    ): Float {
        if (!position.hasWeight) return 1.0f

        val memberAssignments = previousAssignments.filter {
            it.memberId == member.id && it.positionId == position.id
        }
        val timesAssigned = memberAssignments.size

        if (timesAssigned == 0) return 1.0f

        val strength = position.weightStrength
        val mode = runCatching { WeightDecayMode.valueOf(position.weightDecayMode) }
            .getOrDefault(WeightDecayMode.STAY_LOW)

        return when (mode) {
            WeightDecayMode.STAY_LOW -> {
                ((1f - strength).pow(timesAssigned)).coerceAtLeast(0.001f)
            }
            WeightDecayMode.RECOVER -> {
                val lastSlot = memberAssignments.maxOf { it.slotNumber }
                val currentSlotEstimate = (previousAssignments.maxOfOrNull { it.slotNumber } ?: 1) + 1
                val slotsSince = currentSlotEstimate - lastSlot
                val base = (1f - strength).pow(timesAssigned)
                minOf(1.0f, base + (slotsSince - 1) * strength * 0.3f).coerceAtLeast(0.001f)
            }
        }
    }

    fun computeWeightsForDisplay(
        members: List<MemberEntity>,
        positions: List<PositionEntity>,
        previousAssignments: List<AssignmentEntity>
    ): Map<Long, Map<Long, Float>> = members.associate { member ->
        member.id to positions.associate { position ->
            position.id to computeWeight(member, position, previousAssignments)
        }
    }

    private fun Float.pow(n: Int): Float {
        var result = 1f
        repeat(n) { result *= this }
        return result
    }
}
