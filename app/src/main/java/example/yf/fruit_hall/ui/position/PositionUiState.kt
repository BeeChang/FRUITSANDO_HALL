package example.yf.fruit_hall.ui.position

data class PositionUiState(
    val members: List<MemberUi> = emptyList(),
    val positions: List<PositionUi> = emptyList(),
    val enabledPositionIds: Set<Long> = emptySet(),
    val currentSlot: Int = 1,
    val totalSlots: Int = 3,
    val drawResult: List<DrawResultItem> = emptyList(),
    val isDrawDone: Boolean = false,
    val isConfirmedSlot: Boolean = false,
    val todayHistory: List<SlotHistoryUi> = emptyList(),
    val memberWeights: Map<Long, Map<Long, Float>> = emptyMap(),
    val showMemberDialog: Boolean = false,
    val showPositionDialog: Boolean = false,
    val showWeightDialog: Boolean = false,
    val showSlotDialog: Boolean = false,
    val showHistoryDialog: Boolean = false,
    val todayDate: String = "",
    val historyViewDate: String = "",
    val historyForViewDate: List<SlotHistoryUi> = emptyList(),
    val historyAvailableDates: Set<String> = emptySet(),
    val showHistoryDatePicker: Boolean = false
)

data class MemberUi(
    val id: Long,
    val name: String,
    val isWorking: Boolean,
    val colorHex: String = "#4D96FF"
)

data class PositionUi(
    val id: Long,
    val name: String,
    val isMultiPerson: Boolean,
    val hasWeight: Boolean,
    val weightStrength: Float,
    val weightDecayMode: String,
    val sortOrder: Int
)

data class DrawResultItem(
    val position: PositionUi,
    val members: MutableList<MemberUi>
)

data class SlotHistoryUi(
    val slotNumber: Int,
    val assignments: List<SlotAssignmentUi>
)

data class SlotAssignmentUi(
    val memberName: String,
    val positionName: String
)

sealed interface PositionEvent {
    data class ToggleWorking(val memberId: Long) : PositionEvent
    data object StartDraw : PositionEvent
    data class SwapMembers(
        val fromPositionId: Long,
        val memberId: Long,
        val toPositionId: Long
    ) : PositionEvent
    data object ConfirmDraw : PositionEvent
    data object CancelDraw : PositionEvent

    data class SelectSlot(val slotNumber: Int) : PositionEvent

    data object ResetToday : PositionEvent
    data object ResetAll : PositionEvent

    data class ReorderMembers(val orderedIds: List<Long>) : PositionEvent
    data class TogglePositionEnabled(val positionId: Long) : PositionEvent

    data class AddMember(val name: String, val colorHex: String) : PositionEvent
    data class UpdateMember(val id: Long, val name: String) : PositionEvent
    data class DeleteMember(val id: Long) : PositionEvent

    data class AddPosition(val name: String, val isMultiPerson: Boolean) : PositionEvent
    data class UpdatePosition(
        val id: Long,
        val name: String,
        val isMultiPerson: Boolean,
        val hasWeight: Boolean,
        val weightStrength: Float,
        val weightDecayMode: String
    ) : PositionEvent
    data class DeletePosition(val id: Long) : PositionEvent

    data class UpdateSlotSettings(val totalSlots: Int) : PositionEvent

    data object ShowMemberDialog : PositionEvent
    data object HideMemberDialog : PositionEvent
    data object ShowPositionDialog : PositionEvent
    data object HidePositionDialog : PositionEvent
    data object ShowWeightDialog : PositionEvent
    data object HideWeightDialog : PositionEvent
    data object ShowSlotDialog : PositionEvent
    data object HideSlotDialog : PositionEvent
    data object ShowHistoryDialog : PositionEvent
    data object HideHistoryDialog : PositionEvent
    data object ShowHistoryDatePicker : PositionEvent
    data object HideHistoryDatePicker : PositionEvent
    data class SelectHistoryDate(val date: String) : PositionEvent
}
