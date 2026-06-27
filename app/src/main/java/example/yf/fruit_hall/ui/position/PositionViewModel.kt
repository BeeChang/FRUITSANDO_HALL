package example.yf.fruit_hall.ui.position

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import example.yf.fruit_hall.data.position.PositionRepository
import example.yf.fruit_hall.data.position.entity.AssignmentEntity
import example.yf.fruit_hall.data.position.entity.MemberEntity
import example.yf.fruit_hall.data.position.entity.PositionEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class PositionViewModel @Inject constructor(
    private val repository: PositionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PositionUiState())
    val uiState: StateFlow<PositionUiState> = _uiState.asStateFlow()

    private var animationJob: Job? = null

    private var cachedMembers: List<MemberEntity> = emptyList()
    private var cachedPositions: List<PositionEntity> = emptyList()

    init {
        viewModelScope.launch {
            combine(
                repository.observeMembers(),
                repository.observePositions(),
                repository.observeSlotSettings()
            ) { members, positions, settings ->
                Triple(members, positions, settings)
            }.collect { (members, positions, settings) ->
                cachedMembers = members
                cachedPositions = positions

                val today = LocalDate.now().toString()
                val workDay = repository.getOrCreateToday()
                val todayAssignments = repository.getTodayAssignments(today)

                val memberWeights = repository.computeWeightsForDisplay(
                    members, positions, todayAssignments
                )
                val history = buildHistory(todayAssignments, members, positions)

                _uiState.update { current ->
                    current.copy(
                        members = members.map { it.toUi() },
                        positions = positions.map { it.toUi() },
                        totalSlots = settings.totalSlots,
                        currentSlot = workDay.currentSlot,
                        memberWeights = memberWeights,
                        todayHistory = history,
                        todayDate = today
                    )
                }
            }
        }
    }

    fun onEvent(event: PositionEvent) {
        when (event) {
            is PositionEvent.ToggleWorking -> toggleWorking(event.memberId)
            is PositionEvent.StartDraw -> startDraw()
            is PositionEvent.SkipAnimation -> skipAnimation()
            is PositionEvent.SwapMembers -> swapMembers(event.fromPositionId, event.memberId, event.toPositionId)
            is PositionEvent.ConfirmDraw -> confirmDraw()
            is PositionEvent.CancelDraw -> cancelDraw()
            is PositionEvent.RedrawPosition -> redrawPosition(event.positionId)

            is PositionEvent.ResetToday -> resetToday()
            is PositionEvent.ResetAll -> resetAll()

            is PositionEvent.AddMember -> addMember(event.name)
            is PositionEvent.UpdateMember -> updateMember(event.id, event.name)
            is PositionEvent.DeleteMember -> deleteMember(event.id)

            is PositionEvent.AddPosition -> addPosition(event.name, event.isMultiPerson)
            is PositionEvent.UpdatePosition -> updatePosition(event)
            is PositionEvent.DeletePosition -> deletePosition(event.id)

            is PositionEvent.UpdateSlotSettings -> updateSlotSettings(event.totalSlots)

            is PositionEvent.ShowMemberDialog -> _uiState.update { it.copy(showMemberDialog = true) }
            is PositionEvent.HideMemberDialog -> _uiState.update { it.copy(showMemberDialog = false) }
            is PositionEvent.ShowPositionDialog -> _uiState.update { it.copy(showPositionDialog = true) }
            is PositionEvent.HidePositionDialog -> _uiState.update { it.copy(showPositionDialog = false) }
            is PositionEvent.ShowWeightDialog -> _uiState.update { it.copy(showWeightDialog = true) }
            is PositionEvent.HideWeightDialog -> _uiState.update { it.copy(showWeightDialog = false) }
            is PositionEvent.ShowSlotDialog -> _uiState.update { it.copy(showSlotDialog = true) }
            is PositionEvent.HideSlotDialog -> _uiState.update { it.copy(showSlotDialog = false) }
            is PositionEvent.ShowHistoryDialog -> _uiState.update { it.copy(showHistoryDialog = true) }
            is PositionEvent.HideHistoryDialog -> _uiState.update { it.copy(showHistoryDialog = false) }
            is PositionEvent.ShowDayResetConfirm -> _uiState.update { it.copy(showDayResetConfirm = true) }
            is PositionEvent.HideDayResetConfirm -> _uiState.update { it.copy(showDayResetConfirm = false) }
            is PositionEvent.ShowFullResetConfirm -> _uiState.update { it.copy(showFullResetConfirm = true) }
            is PositionEvent.HideFullResetConfirm -> _uiState.update { it.copy(showFullResetConfirm = false) }
        }
    }

    private fun toggleWorking(memberId: Long) {
        val member = cachedMembers.find { it.id == memberId } ?: return
        viewModelScope.launch {
            repository.setMemberWorking(memberId, !member.isWorking)
        }
    }

    private fun startDraw() {
        val workingMembers = cachedMembers.filter { it.isWorking }
        if (workingMembers.isEmpty()) return

        val state = _uiState.value
        val today = LocalDate.now().toString()

        viewModelScope.launch {
            val todayAssignments = repository.getTodayAssignments(today)
            val drawPairs = repository.performDraw(
                members = workingMembers,
                positions = cachedPositions,
                todayAssignments = todayAssignments,
                currentSlot = state.currentSlot
            )

            val resultItems = buildDrawResult(drawPairs, workingMembers, cachedPositions)

            _uiState.update { it.copy(drawResult = resultItems, isAnimating = true) }

            animationJob = launch {
                delay(2000)
                _uiState.update { it.copy(isAnimating = false, isDrawDone = true) }
            }
        }
    }

    private fun skipAnimation() {
        animationJob?.cancel()
        animationJob = null
        _uiState.update { it.copy(isAnimating = false, isDrawDone = true) }
    }

    private fun swapMembers(fromPositionId: Long, memberId: Long, toPositionId: Long) {
        if (fromPositionId == toPositionId) return

        val currentResult = _uiState.value.drawResult.map { item ->
            item.copy(members = item.members.toMutableList())
        }.toMutableList()

        val fromItem = currentResult.find { it.position.id == fromPositionId } ?: return
        val toItem = currentResult.find { it.position.id == toPositionId } ?: return
        val memberToMove = fromItem.members.find { it.id == memberId } ?: return

        if (!toItem.position.isMultiPerson && toItem.members.isNotEmpty()) {
            val displaced = toItem.members[0]
            toItem.members[0] = memberToMove
            val fromIndex = fromItem.members.indexOfFirst { it.id == memberId }
            fromItem.members[fromIndex] = displaced
        } else {
            fromItem.members.remove(memberToMove)
            toItem.members.add(memberToMove)
        }

        _uiState.update { it.copy(drawResult = currentResult) }
    }

    private fun confirmDraw() {
        val state = _uiState.value
        val today = state.todayDate.ifEmpty { LocalDate.now().toString() }

        viewModelScope.launch {
            val assignments = state.drawResult.flatMap { item ->
                item.members.map { member -> member.id to item.position.id }
            }
            repository.confirmAssignments(today, state.currentSlot, assignments)

            if (state.currentSlot < state.totalSlots) {
                repository.advanceSlot(today, state.currentSlot)
            }

            _uiState.update { it.copy(isDrawDone = false, drawResult = emptyList()) }
        }
    }

    private fun cancelDraw() {
        animationJob?.cancel()
        _uiState.update { it.copy(isDrawDone = false, isAnimating = false, drawResult = emptyList()) }
    }

    private fun redrawPosition(positionId: Long) {
        val state = _uiState.value
        val today = state.todayDate.ifEmpty { LocalDate.now().toString() }
        val workingMembers = cachedMembers.filter { it.isWorking }

        viewModelScope.launch {
            val todayAssignments = repository.getTodayAssignments(today)
            val targetPosition = cachedPositions.find { it.id == positionId } ?: return@launch

            val alreadyAssigned = state.drawResult
                .filter { it.position.id != positionId }
                .flatMap { it.members }
                .map { it.id }
                .toSet()

            val available = workingMembers.filter { it.id !in alreadyAssigned }
            if (available.isEmpty()) return@launch

            val count = state.drawResult.find { it.position.id == positionId }?.members?.size ?: 1
            val newPairs = repository.performDraw(
                members = available,
                positions = listOf(targetPosition),
                todayAssignments = todayAssignments,
                currentSlot = state.currentSlot
            )

            val newMembers = newPairs.take(count).mapNotNull { (memberId, _) ->
                workingMembers.find { it.id == memberId }?.toUi()
            }

            val updated = state.drawResult.map { item ->
                if (item.position.id == positionId) {
                    item.copy(members = newMembers.toMutableList())
                } else item
            }

            _uiState.update { it.copy(drawResult = updated) }
        }
    }

    private fun resetToday() {
        val today = LocalDate.now().toString()
        viewModelScope.launch {
            repository.resetToday(today)
            _uiState.update {
                it.copy(
                    currentSlot = 1,
                    isDrawDone = false,
                    isAnimating = false,
                    drawResult = emptyList(),
                    todayHistory = emptyList(),
                    showDayResetConfirm = false
                )
            }
        }
    }

    private fun resetAll() {
        viewModelScope.launch {
            repository.resetAll()
            _uiState.update {
                it.copy(
                    currentSlot = 1,
                    isDrawDone = false,
                    isAnimating = false,
                    drawResult = emptyList(),
                    todayHistory = emptyList(),
                    showFullResetConfirm = false
                )
            }
        }
    }

    private fun addMember(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addMember(MemberEntity(name = name.trim(), sortOrder = cachedMembers.size))
        }
    }

    private fun updateMember(id: Long, name: String) {
        if (name.isBlank()) return
        val member = cachedMembers.find { it.id == id } ?: return
        viewModelScope.launch {
            repository.updateMember(member.copy(name = name.trim()))
        }
    }

    private fun deleteMember(id: Long) {
        val member = cachedMembers.find { it.id == id } ?: return
        viewModelScope.launch {
            repository.deleteMember(member)
        }
    }

    private fun addPosition(name: String, isMultiPerson: Boolean) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addPosition(
                PositionEntity(
                    name = name.trim(),
                    isMultiPerson = isMultiPerson,
                    sortOrder = cachedPositions.size
                )
            )
        }
    }

    private fun updatePosition(event: PositionEvent.UpdatePosition) {
        val position = cachedPositions.find { it.id == event.id } ?: return
        viewModelScope.launch {
            repository.updatePosition(
                position.copy(
                    name = event.name,
                    isMultiPerson = event.isMultiPerson,
                    hasWeight = event.hasWeight,
                    weightStrength = event.weightStrength,
                    weightDecayMode = event.weightDecayMode
                )
            )
        }
    }

    private fun deletePosition(id: Long) {
        val position = cachedPositions.find { it.id == id } ?: return
        viewModelScope.launch {
            repository.deletePosition(position)
        }
    }

    private fun updateSlotSettings(totalSlots: Int) {
        viewModelScope.launch {
            repository.updateSlotSettings(totalSlots)
            _uiState.update { it.copy(showSlotDialog = false) }
        }
    }

    private fun buildDrawResult(
        pairs: List<Pair<Long, Long>>,
        members: List<MemberEntity>,
        positions: List<PositionEntity>
    ): List<DrawResultItem> {
        val positionMap = positions.associateBy { it.id }
        val memberMap = members.associateBy { it.id }

        val grouped = pairs.groupBy { it.second }
        return grouped.mapNotNull { (positionId, memberPairs) ->
            val position = positionMap[positionId]?.toUi() ?: return@mapNotNull null
            val memberUis = memberPairs.mapNotNull { (memberId, _) ->
                memberMap[memberId]?.toUi()
            }.toMutableList()
            DrawResultItem(position = position, members = memberUis)
        }.sortedBy { it.position.sortOrder }
    }

    private suspend fun buildHistory(
        assignments: List<AssignmentEntity>,
        members: List<MemberEntity>,
        positions: List<PositionEntity>
    ): List<SlotHistoryUi> {
        val memberMap = members.associateBy { it.id }
        val positionMap = positions.associateBy { it.id }

        return assignments
            .groupBy { it.slotNumber }
            .entries
            .sortedBy { it.key }
            .map { (slot, slotAssignments) ->
                SlotHistoryUi(
                    slotNumber = slot,
                    assignments = slotAssignments.map { a ->
                        SlotAssignmentUi(
                            memberName = memberMap[a.memberId]?.name ?: "?",
                            positionName = positionMap[a.positionId]?.name ?: "?"
                        )
                    }
                )
            }
    }

    private fun MemberEntity.toUi() = MemberUi(id = id, name = name, isWorking = isWorking)
    private fun PositionEntity.toUi() = PositionUi(
        id = id,
        name = name,
        isMultiPerson = isMultiPerson,
        hasWeight = hasWeight,
        weightStrength = weightStrength,
        weightDecayMode = weightDecayMode,
        sortOrder = sortOrder
    )
}