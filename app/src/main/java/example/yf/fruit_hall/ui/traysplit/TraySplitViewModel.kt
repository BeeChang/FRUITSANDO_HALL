package example.yf.fruit_hall.ui.traysplit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import example.yf.fruit_hall.core.Level
import example.yf.fruit_hall.core.RoughSize
import example.yf.fruit_hall.data.tray.TrayRepository
import example.yf.fruit_hall.data.tray.entity.AllocationSettingsEntity
import example.yf.fruit_hall.data.tray.entity.SnackTypeEntity
import example.yf.fruit_hall.data.tray.entity.SpaceEntity
import example.yf.fruit_hall.data.tray.entity.TrayEntity
import example.yf.fruit_hall.data.tray.entity.TrayItemEntity
import example.yf.fruit_hall.domain.tray.AllocateTraysUseCase
import example.yf.fruit_hall.ui.traysplit.component.labelCandidates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

private data class TraySplitSnapshot(
    val spaces: List<SpaceEntity>,
    val snackTypes: List<SnackTypeEntity>,
    val trays: List<TrayEntity>,
    val trayItems: List<TrayItemEntity>,
    val settings: AllocationSettingsEntity
)

@HiltViewModel
class TraySplitViewModel @Inject constructor(
    private val repository: TrayRepository,
    private val allocateTrays: AllocateTraysUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TraySplitUiState())
    val uiState: StateFlow<TraySplitUiState> = _uiState.asStateFlow()

    private var cachedSpaces: List<SpaceEntity> = emptyList()
    private var cachedSnackTypes: List<SnackTypeEntity> = emptyList()
    private var cachedTrays: List<TrayEntity> = emptyList()
    private var cachedTrayItems: List<TrayItemEntity> = emptyList()
    private var cachedSettings: AllocationSettingsEntity = AllocationSettingsEntity()

    init {
        viewModelScope.launch {
            combine(
                repository.observeSpaces(),
                repository.observeSnackTypes(),
                repository.observeTrays(),
                repository.observeTrayItems(),
                repository.observeSettings()
            ) { spaces, snackTypes, trays, trayItems, settings ->
                TraySplitSnapshot(spaces, snackTypes, trays, trayItems, settings)
            }.collect { snapshot ->
                cachedSpaces = snapshot.spaces
                cachedSnackTypes = snapshot.snackTypes
                cachedTrays = snapshot.trays
                cachedTrayItems = snapshot.trayItems
                cachedSettings = snapshot.settings

                val snackTypeMap = snapshot.snackTypes.associateBy { it.id }
                val itemsByTray = snapshot.trayItems.groupBy { it.trayId }

                _uiState.update { current ->
                    current.copy(
                        spaces = snapshot.spaces.map {
                            SpaceUi(
                                it.id, it.name,
                                isPrimaryLocation = it.id == snapshot.settings.primaryLocationSpaceId,
                                capacity = it.capacity
                            )
                        },
                        snackTypes = snapshot.snackTypes.map { SnackTypeUi(it.id, it.name, it.colorHex, it.secondaryColorHex, it.isActive) },
                        trays = snapshot.trays.map { tray ->
                            TrayUi(
                                id = tray.id,
                                spaceId = tray.spaceId,
                                pinnedRound = tray.pinnedRound,
                                items = itemsByTray[tray.id].orEmpty().mapNotNull { item ->
                                    val type = snackTypeMap[item.snackTypeId] ?: return@mapNotNull null
                                    TrayItemUi(
                                        snackTypeId = type.id,
                                        name = type.name,
                                        colorHex = type.colorHex,
                                        secondaryColorHex = type.secondaryColorHex,
                                        roughSize = item.roughSize?.let { RoughSize.valueOf(it) },
                                        exactQty = item.exactQty,
                                        sortOrder = type.sortOrder
                                    )
                                } // 판에 실제로 담은(드래그한) 순서를 그대로 유지 — 결과 화면에서만 등록순으로 재정렬함
                            )
                        },
                        settings = snapshot.settings.toUi()
                    )
                }
            }
        }
    }

    fun onEvent(event: TraySplitEvent) {
        when (event) {
            is TraySplitEvent.AddSpace -> addSpace(event.name)
            is TraySplitEvent.DeleteSpace -> deleteSpace(event.id)
            TraySplitEvent.ShowSpaceDialog -> _uiState.update { it.copy(showSpaceDialog = true) }
            TraySplitEvent.HideSpaceDialog -> _uiState.update { it.copy(showSpaceDialog = false) }
            is TraySplitEvent.ShowRenameSpaceDialog -> _uiState.update { it.copy(renameSpaceTargetId = event.id) }
            TraySplitEvent.HideRenameSpaceDialog -> _uiState.update { it.copy(renameSpaceTargetId = null) }
            is TraySplitEvent.RenameSpace -> renameSpace(event.id, event.name, event.capacity)
            is TraySplitEvent.SetPrimaryLocation -> setPrimaryLocation(event.spaceId)

            is TraySplitEvent.AddSnackType -> addSnackType(event.name, event.colorHex, event.secondaryColorHex)
            is TraySplitEvent.DeleteSnackType -> deleteSnackType(event.id)
            is TraySplitEvent.ReorderSnackTypes -> reorderSnackTypes(event.orderedIds)
            is TraySplitEvent.SetSnackTypeActive -> setSnackTypeActive(event.id, event.isActive)
            TraySplitEvent.ShowSnackTypeDialog -> _uiState.update { it.copy(showSnackTypeDialog = true) }
            TraySplitEvent.HideSnackTypeDialog -> _uiState.update { it.copy(showSnackTypeDialog = false) }

            is TraySplitEvent.QuickAddTray -> quickAddTray(event.spaceId)
            is TraySplitEvent.DeleteTray -> deleteTray(event.id)
            is TraySplitEvent.AddItemToTray -> addItemToTray(event.trayId, event.snackTypeId, event.roughSize)
            is TraySplitEvent.CycleItemSize -> cycleItemSize(event.trayId, event.snackTypeId)
            is TraySplitEvent.RemoveItemFromTray -> removeItem(event.trayId, event.snackTypeId)
            is TraySplitEvent.ShowAddTrayDialog -> _uiState.update { it.copy(addTrayTargetSpaceId = event.spaceId) }
            TraySplitEvent.HideAddTrayDialog -> _uiState.update { it.copy(addTrayTargetSpaceId = null) }
            is TraySplitEvent.SubmitAddTrayDialog -> submitAddTray(event.spaceId, event.snackTypeIds)

            is TraySplitEvent.ShowPinSheet -> _uiState.update { it.copy(pinSheetTrayId = event.trayId) }
            TraySplitEvent.HidePinSheet -> _uiState.update { it.copy(pinSheetTrayId = null) }
            is TraySplitEvent.PinTray -> pinTray(event.trayId, event.round)

            is TraySplitEvent.UpdateSettings -> updateSettings(event.settings)
            TraySplitEvent.ShowSettingsDialog -> _uiState.update { it.copy(showSettingsDialog = true) }
            TraySplitEvent.HideSettingsDialog -> _uiState.update { it.copy(showSettingsDialog = false) }

            TraySplitEvent.RunAllocation -> runAllocation()
            is TraySplitEvent.SelectCandidate -> selectCandidate(event.index)
            is TraySplitEvent.ShowCandidateDetail -> _uiState.update { it.copy(candidateDetailIndex = event.index) }
            TraySplitEvent.HideCandidateDetail -> _uiState.update { it.copy(candidateDetailIndex = null) }
            TraySplitEvent.HideResultDialog -> _uiState.update { it.copy(showResultDialog = false) }
            TraySplitEvent.ShowOverallSummary -> _uiState.update { it.copy(showOverallSummary = true) }
            TraySplitEvent.HideOverallSummary -> _uiState.update { it.copy(showOverallSummary = false) }

            is TraySplitEvent.ShowRoundPicker -> _uiState.update { it.copy(roundPickerTrayId = event.trayId) }
            TraySplitEvent.HideRoundPicker -> _uiState.update { it.copy(roundPickerTrayId = null) }
            is TraySplitEvent.MoveTrayToRound -> moveTrayToRound(event.trayId, event.round)

            TraySplitEvent.ResetDay -> resetDay()
            TraySplitEvent.ConfirmAndSave -> confirmAndSave()
            TraySplitEvent.ClearSnackbar -> _uiState.update { it.copy(snackbarMessage = null) }
        }
    }

    private fun addSpace(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addSpace(name.trim(), cachedSpaces.size)
            _uiState.update { it.copy(showSpaceDialog = false) }
        }
    }

    private fun deleteSpace(id: Long) {
        val space = cachedSpaces.find { it.id == id } ?: return
        viewModelScope.launch {
            repository.deleteSpace(space)
            if (cachedSettings.primaryLocationSpaceId == id) {
                repository.updateSettings(cachedSettings.copy(primaryLocationSpaceId = null))
            }
        }
    }

    private fun renameSpace(id: Long, name: String, capacity: Int?) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.renameSpace(id, name.trim())
            repository.setSpaceCapacity(id, capacity)
            _uiState.update { it.copy(renameSpaceTargetId = null) }
        }
    }

    private fun setPrimaryLocation(spaceId: Long?) {
        viewModelScope.launch {
            repository.updateSettings(cachedSettings.copy(primaryLocationSpaceId = spaceId))
        }
    }

    private fun addSnackType(name: String, colorHex: String, secondaryColorHex: String?) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addSnackType(name.trim(), colorHex, secondaryColorHex, cachedSnackTypes.size)
        }
    }

    private fun deleteSnackType(id: Long) {
        val type = cachedSnackTypes.find { it.id == id } ?: return
        viewModelScope.launch { repository.deleteSnackType(type) }
    }

    private fun reorderSnackTypes(orderedIds: List<Long>) {
        viewModelScope.launch { repository.reorderSnackTypes(orderedIds) }
    }

    private fun setSnackTypeActive(id: Long, isActive: Boolean) {
        viewModelScope.launch { repository.setSnackTypeActive(id, isActive) }
    }

    private fun quickAddTray(spaceId: Long) {
        viewModelScope.launch { repository.addTray(spaceId, cachedTrays.count { it.spaceId == spaceId }) }
    }

    private fun deleteTray(id: Long) {
        val tray = cachedTrays.find { it.id == id } ?: return
        viewModelScope.launch {
            repository.deleteTray(tray)
            _uiState.update { it.copy(currentAssignment = it.currentAssignment - id) }
        }
    }

    private fun addItemToTray(trayId: Long, snackTypeId: Long, roughSize: RoughSize?) {
        // 드래그 드롭 좌표가 낡아 이미 삭제된 trayId를 가리킬 수 있다 — FK 제약 크래시 방지용 가드
        if (cachedTrays.none { it.id == trayId } || cachedSnackTypes.none { it.id == snackTypeId }) return
        viewModelScope.launch { repository.addItemToTray(trayId, snackTypeId, roughSize) }
    }

    private fun cycleItemSize(trayId: Long, snackTypeId: Long) {
        val current = cachedTrayItems.find { it.trayId == trayId && it.snackTypeId == snackTypeId } ?: return
        val currentSize = current.roughSize?.let { RoughSize.valueOf(it) }
        val next = when (currentSize) {
            null -> RoughSize.S
            RoughSize.S -> RoughSize.M
            RoughSize.M -> RoughSize.L
            RoughSize.L -> RoughSize.S
        }
        viewModelScope.launch { repository.updateItemRoughSize(trayId, snackTypeId, next) }
    }

    private fun removeItem(trayId: Long, snackTypeId: Long) {
        viewModelScope.launch { repository.removeItemFromTray(trayId, snackTypeId) }
    }

    private fun submitAddTray(spaceId: Long, snackTypeIds: List<Long>) {
        viewModelScope.launch {
            val trayId = repository.addTray(spaceId, cachedTrays.count { it.spaceId == spaceId })
            snackTypeIds.forEach { snackTypeId -> repository.addItemToTray(trayId, snackTypeId, roughSize = null) }
            _uiState.update { it.copy(addTrayTargetSpaceId = null) }
        }
    }

    private fun pinTray(trayId: Long, round: Int?) {
        viewModelScope.launch {
            repository.setPinnedRound(trayId, round)
            _uiState.update { it.copy(pinSheetTrayId = null) }
        }
    }

    private fun updateSettings(settings: AllocationSettingsUi) {
        viewModelScope.launch {
            repository.updateSettings(settings.toEntity())
            _uiState.update { it.copy(showSettingsDialog = false) }
        }
    }

    private fun runAllocation() {
        val trays = cachedTrays
        if (trays.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isAllocating = true, attemptCount = 0) }
            var attempts = 0
            val engineTrays = repository.buildEngineTrays(trays, cachedTrayItems)
            val config = repository.buildAllocationConfig(cachedSettings, cachedSpaces)
            val candidates = try {
                allocateTrays(engineTrays, config) { attempts++ }
            } catch (e: IllegalArgumentException) {
                _uiState.update {
                    it.copy(isAllocating = false, snackbarMessage = TraySplitMessage.AllocationSettingsError(e.message.orEmpty()))
                }
                return@launch
            }
            _uiState.update {
                it.copy(
                    isAllocating = false,
                    attemptCount = attempts,
                    candidates = candidates,
                    candidateLabels = labelCandidates(candidates, primaryLocationSet = cachedSettings.primaryLocationSpaceId != null),
                    selectedCandidateIndex = null,
                    showResultDialog = true
                )
            }
        }
    }

    private fun selectCandidate(index: Int) {
        val candidate = _uiState.value.candidates.getOrNull(index) ?: return
        val assignment = candidate.assignment.entries.associate { it.key.toLong() to it.value }
        _uiState.update {
            it.copy(
                selectedCandidateIndex = index,
                currentAssignment = assignment,
                roundSizes = candidate.roundSizes,
                showResultDialog = false,
                candidateDetailIndex = null
            )
        }
    }

    private fun moveTrayToRound(trayId: Long, round: Int) {
        val from = _uiState.value.currentAssignment[trayId] ?: return
        if (from == round) {
            _uiState.update { it.copy(roundPickerTrayId = null) }
            return
        }
        viewModelScope.launch { repository.logManualEdit(trayId, from, round) }
        _uiState.update { current ->
            val newAssignment = current.currentAssignment + (trayId to round)
            val newSizes = MutableList(current.roundSizes.size) { 0 }
            newAssignment.values.forEach { r -> if (r - 1 in newSizes.indices) newSizes[r - 1]++ }
            current.copy(currentAssignment = newAssignment, roundSizes = newSizes, roundPickerTrayId = null)
        }
    }

    private fun resetDay() {
        viewModelScope.launch {
            repository.resetDay()
            _uiState.update {
                it.copy(
                    currentAssignment = emptyMap(),
                    roundSizes = emptyList(),
                    candidates = emptyList(),
                    candidateLabels = emptyList(),
                    selectedCandidateIndex = null,
                    snackbarMessage = TraySplitMessage.ResetDone
                )
            }
        }
    }

    private fun confirmAndSave() {
        val state = _uiState.value
        if (state.currentAssignment.isEmpty()) return
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val score = state.selectedCandidateIndex?.let { state.candidates.getOrNull(it)?.score } ?: 0
            repository.confirmAllocation(today, state.currentAssignment, state.roundSizes, score)
            _uiState.update { it.copy(snackbarMessage = TraySplitMessage.SaveDone) }
        }
    }

    private fun AllocationSettingsEntity.toUi() = AllocationSettingsUi(
        rounds = rounds,
        capacity = TrayRepository.parseCapacityCsv(capacityCsv, rounds),
        flexDeviation = flexDeviation,
        allowedMissingTypes = allowedMissingTypes,
        primaryLocationSpaceId = primaryLocationSpaceId,
        topN = topN,
        ilsIterations = ilsIterations,
        spreadStrength = runCatching { Level.valueOf(spreadStrength) }.getOrDefault(Level.MID),
        orderStrictness = runCatching { Level.valueOf(orderStrictness) }.getOrDefault(Level.MID),
        moveAversion = runCatching { Level.valueOf(moveAversion) }.getOrDefault(Level.MID)
    )

    private fun AllocationSettingsUi.toEntity() = AllocationSettingsEntity(
        id = 1,
        rounds = rounds,
        capacityCsv = TrayRepository.capacityCsv(capacity),
        flexDeviation = flexDeviation,
        allowedMissingTypes = allowedMissingTypes,
        primaryLocationSpaceId = primaryLocationSpaceId,
        topN = topN,
        ilsIterations = ilsIterations,
        spreadStrength = spreadStrength.name,
        orderStrictness = orderStrictness.name,
        moveAversion = moveAversion.name
    )
}
