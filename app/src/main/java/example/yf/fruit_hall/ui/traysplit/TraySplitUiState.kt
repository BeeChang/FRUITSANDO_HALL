package example.yf.fruit_hall.ui.traysplit

import example.yf.fruit_hall.core.AllocationCandidate
import example.yf.fruit_hall.core.Level
import example.yf.fruit_hall.core.RoughSize
import example.yf.fruit_hall.core.RoundCapacity
import example.yf.fruit_hall.ui.traysplit.component.CandidateLabel

data class SpaceUi(
    val id: Long,
    val name: String,
    val isPrimaryLocation: Boolean = false,
    val capacity: Int? = null
)

data class SnackTypeUi(
    val id: Long,
    val name: String,
    val colorHex: String,
    val secondaryColorHex: String? = null,
    val isActive: Boolean = true
)

data class TrayItemUi(
    val snackTypeId: Long,
    val name: String,
    val colorHex: String,
    val secondaryColorHex: String? = null,
    val roughSize: RoughSize?,
    val exactQty: Int?,
    val sortOrder: Int = 0
)

data class TrayUi(
    val id: Long,
    val spaceId: Long,
    val items: List<TrayItemUi>,
    val pinnedRound: Int?
)

data class AllocationSettingsUi(
    val rounds: Int = 3,
    val capacity: List<RoundCapacity> = List(3) { RoundCapacity.Flexible(1.0) },
    val flexDeviation: Int = 2,
    val allowedMissingTypes: Int = 0,
    val primaryLocationSpaceId: Long? = null,
    val topN: Int = 5,
    val ilsIterations: Int = 6,
    val spreadStrength: Level = Level.MID,
    val orderStrictness: Level = Level.MID,
    val moveAversion: Level = Level.MID
)

data class TraySplitUiState(
    val spaces: List<SpaceUi> = emptyList(),
    val snackTypes: List<SnackTypeUi> = emptyList(),
    val trays: List<TrayUi> = emptyList(),
    val settings: AllocationSettingsUi = AllocationSettingsUi(),

    // 선택된 후보 + 수동편집이 반영된 현재 작업본 (trayId -> 차수)
    val currentAssignment: Map<Long, Int> = emptyMap(),
    val roundSizes: List<Int> = emptyList(),

    val isAllocating: Boolean = false,
    val attemptCount: Int = 0,
    val candidates: List<AllocationCandidate> = emptyList(),
    val candidateLabels: List<CandidateLabel> = emptyList(), // candidates와 동일 순서/길이
    val selectedCandidateIndex: Int? = null,

    val showResultDialog: Boolean = false,
    val candidateDetailIndex: Int? = null,
    val showOverallSummary: Boolean = false,

    val showSpaceDialog: Boolean = false,
    val showSnackTypeDialog: Boolean = false,
    val addTrayTargetSpaceId: Long? = null,
    val showSettingsDialog: Boolean = false,
    val pinSheetTrayId: Long? = null,
    val roundPickerTrayId: Long? = null,
    val renameSpaceTargetId: Long? = null,
    val snackbarMessage: TraySplitMessage? = null
)

sealed interface TraySplitMessage {
    data class AllocationSettingsError(val detail: String) : TraySplitMessage
    data object ResetDone : TraySplitMessage
    data object SaveDone : TraySplitMessage
}

sealed interface TraySplitEvent {
    data class AddSpace(val name: String) : TraySplitEvent
    data class DeleteSpace(val id: Long) : TraySplitEvent
    data object ShowSpaceDialog : TraySplitEvent
    data object HideSpaceDialog : TraySplitEvent
    data class ShowRenameSpaceDialog(val id: Long) : TraySplitEvent
    data object HideRenameSpaceDialog : TraySplitEvent
    data class RenameSpace(val id: Long, val name: String, val capacity: Int?) : TraySplitEvent
    data class SetPrimaryLocation(val spaceId: Long?) : TraySplitEvent

    data class AddSnackType(val name: String, val colorHex: String, val secondaryColorHex: String?) : TraySplitEvent
    data class DeleteSnackType(val id: Long) : TraySplitEvent
    data class ReorderSnackTypes(val orderedIds: List<Long>) : TraySplitEvent
    data class SetSnackTypeActive(val id: Long, val isActive: Boolean) : TraySplitEvent
    data object ShowSnackTypeDialog : TraySplitEvent
    data object HideSnackTypeDialog : TraySplitEvent

    data class QuickAddTray(val spaceId: Long) : TraySplitEvent
    data class DeleteTray(val id: Long) : TraySplitEvent
    data class AddItemToTray(val trayId: Long, val snackTypeId: Long, val roughSize: RoughSize?) : TraySplitEvent
    data class CycleItemSize(val trayId: Long, val snackTypeId: Long) : TraySplitEvent
    data class RemoveItemFromTray(val trayId: Long, val snackTypeId: Long) : TraySplitEvent
    data class ShowAddTrayDialog(val spaceId: Long) : TraySplitEvent
    data object HideAddTrayDialog : TraySplitEvent
    data class SubmitAddTrayDialog(val spaceId: Long, val snackTypeIds: List<Long>) : TraySplitEvent

    data class ShowPinSheet(val trayId: Long) : TraySplitEvent
    data object HidePinSheet : TraySplitEvent
    data class PinTray(val trayId: Long, val round: Int?) : TraySplitEvent

    data class UpdateSettings(val settings: AllocationSettingsUi) : TraySplitEvent
    data object ShowSettingsDialog : TraySplitEvent
    data object HideSettingsDialog : TraySplitEvent

    data object RunAllocation : TraySplitEvent
    data class SelectCandidate(val index: Int) : TraySplitEvent
    data class ShowCandidateDetail(val index: Int) : TraySplitEvent
    data object HideCandidateDetail : TraySplitEvent
    data object HideResultDialog : TraySplitEvent
    data object ShowOverallSummary : TraySplitEvent
    data object HideOverallSummary : TraySplitEvent

    data class ShowRoundPicker(val trayId: Long) : TraySplitEvent
    data object HideRoundPicker : TraySplitEvent
    data class MoveTrayToRound(val trayId: Long, val round: Int) : TraySplitEvent

    data object ResetDay : TraySplitEvent
    data object ConfirmAndSave : TraySplitEvent
    data object ClearSnackbar : TraySplitEvent
}
