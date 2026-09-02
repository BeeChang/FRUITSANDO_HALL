package example.yf.fruit_hall.ui.discord

import example.yf.fruit_hall.data.discord.DiscordWebhookTarget
import java.time.LocalDate

enum class DiscordSegmentType { CUSTOM, PHRASE, TAG, DATE }

data class DiscordSegmentUi(
    val id: Long,
    val type: DiscordSegmentType,
    val text: String,
    val date: LocalDate? = null,          // DATE 타입일 때만 사용
    val useFullWeekday: Boolean = false,  // DATE 타입일 때만 사용
    val suffix: String = "",              // DATE 타입일 때만 사용 — 날짜 뒤에 붙일 커스텀 문구
    val lineBreakAfter: Boolean = true    // false면 다음 조각과 줄바꿈 없이 공백으로 이어붙인다
)

data class DiscordTagUi(val id: Long, val text: String)

data class DiscordPhraseUi(val id: Long, val text: String)

data class DiscordPhrasePresetUi(val id: Long, val name: String, val items: List<String>)

data class DiscordSentMessageUi(
    val id: Long,
    val messageId: String,
    val webhookUrl: String,
    val webhookName: String,
    val content: String,
    val sentAt: Long
)

data class DiscordUiState(
    val segments: List<DiscordSegmentUi> = emptyList(),
    val savedTags: List<DiscordTagUi> = emptyList(),
    val savedPhrases: List<DiscordPhraseUi> = emptyList(),
    val phrasePresets: List<DiscordPhrasePresetUi> = emptyList(),

    val webhookTargets: List<DiscordWebhookTarget> = emptyList(),
    val selectedWebhookName: String = "",
    val isLoadingWebhookTargets: Boolean = false,

    val isSending: Boolean = false,
    val sendError: String? = null,
    val sendSuccess: Boolean = false,

    val showTagPicker: Boolean = false,
    val showPhrasePicker: Boolean = false,
    val showPhrasePresetPicker: Boolean = false,

    val sentMessages: List<DiscordSentMessageUi> = emptyList(),
    val editingSentMessageId: Long? = null,
    val editingText: String = "",
    val isEditingSending: Boolean = false,
    val editError: String? = null
) {
    val previewText: String get() = buildString {
        segments.forEachIndexed { index, seg ->
            append(seg.text)
            if (index != segments.lastIndex) append(if (seg.lineBreakAfter) "\n" else " ")
        }
    }
}
