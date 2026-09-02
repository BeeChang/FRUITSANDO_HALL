package example.yf.fruit_hall.ui.discord

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import example.yf.fruit_hall.data.discord.DiscordContentRepository
import example.yf.fruit_hall.data.discord.DiscordPreferenceRepository
import example.yf.fruit_hall.data.discord.DiscordWebhookRepository
import example.yf.fruit_hall.data.discord.local.entity.DiscordPhraseEntity
import example.yf.fruit_hall.data.discord.local.entity.DiscordPhrasePresetEntity
import example.yf.fruit_hall.data.discord.local.entity.DiscordSentMessageEntity
import example.yf.fruit_hall.data.discord.local.entity.DiscordTagEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

private val shortWeekdayFormatter = DateTimeFormatter.ofPattern("M/d일(E)", Locale.KOREAN)
private val fullWeekdayFormatter = DateTimeFormatter.ofPattern("M/d일(EEEE)", Locale.KOREAN)
private fun formatDiscordDate(date: LocalDate, useFullWeekday: Boolean): String =
    date.format(if (useFullWeekday) fullWeekdayFormatter else shortWeekdayFormatter)

@HiltViewModel
class DiscordViewModel @Inject constructor(
    private val contentRepository: DiscordContentRepository,
    private val webhookRepository: DiscordWebhookRepository,
    private val preferenceRepository: DiscordPreferenceRepository,
    private val json: Json
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscordUiState())
    val uiState: StateFlow<DiscordUiState> = _uiState.asStateFlow()

    private var nextSegmentId = 1L

    init {
        viewModelScope.launch {
            combine(contentRepository.observeTags(), contentRepository.observePhrases()) { tags, phrases ->
                tags.map { DiscordTagUi(it.id, it.text) } to phrases.map { DiscordPhraseUi(it.id, it.text) }
            }.collect { (tags, phrases) ->
                _uiState.update { it.copy(savedTags = tags, savedPhrases = phrases) }
            }
        }
        viewModelScope.launch {
            contentRepository.observeRecentSentMessages().collect { messages ->
                _uiState.update {
                    it.copy(sentMessages = messages.map { m -> DiscordSentMessageUi(m.id, m.messageId, m.webhookUrl, m.webhookName, m.content, m.sentAt) })
                }
            }
        }
        viewModelScope.launch {
            contentRepository.observePhrasePresets().collect { presets ->
                _uiState.update {
                    it.copy(phrasePresets = presets.map { p ->
                        val items = runCatching { json.decodeFromString<List<String>>(p.itemsJson) }.getOrDefault(emptyList())
                        DiscordPhrasePresetUi(p.id, p.name, items)
                    })
                }
            }
        }
        refreshWebhookTargets()
    }

    fun refreshWebhookTargets() = viewModelScope.launch {
        _uiState.update { it.copy(isLoadingWebhookTargets = true) }
        val targets = runCatching { webhookRepository.fetchWebhookTargets() }.getOrDefault(emptyList())
        _uiState.update { current ->
            current.copy(
                webhookTargets = targets,
                isLoadingWebhookTargets = false,
                selectedWebhookName = current.selectedWebhookName
                    .takeIf { name -> targets.any { it.name == name } }
                    ?: targets.firstOrNull { it.name == preferenceRepository.lastWebhookName }?.name
                    ?: targets.firstOrNull()?.name.orEmpty()
            )
        }
    }

    fun selectWebhook(name: String) {
        preferenceRepository.lastWebhookName = name
        _uiState.update { it.copy(selectedWebhookName = name) }
    }

    fun addCustomSegment(text: String) {
        if (text.isEmpty()) return
        appendSegment(DiscordSegmentType.CUSTOM, text)
    }

    fun addPhraseSegment(phrase: DiscordPhraseUi) = appendSegment(DiscordSegmentType.PHRASE, phrase.text)

    fun addTagSegment(tag: DiscordTagUi) = appendSegment(DiscordSegmentType.TAG, tag.text)

    fun addDateSegment() {
        val date = LocalDate.now().plusDays(1)
        val segment = DiscordSegmentUi(
            id = nextSegmentId++, type = DiscordSegmentType.DATE,
            text = formatDiscordDate(date, useFullWeekday = false), date = date, useFullWeekday = false
        )
        _uiState.update { it.copy(segments = it.segments + segment) }
    }

    fun updateDateSegment(id: Long, date: LocalDate? = null, useFullWeekday: Boolean? = null, suffix: String? = null) {
        _uiState.update { state ->
            state.copy(
                segments = state.segments.map { seg ->
                    if (seg.id != id || seg.type != DiscordSegmentType.DATE) return@map seg
                    val newDate = date ?: seg.date ?: LocalDate.now()
                    val newFull = useFullWeekday ?: seg.useFullWeekday
                    val newSuffix = suffix ?: seg.suffix
                    val formatted = formatDiscordDate(newDate, newFull)
                    val combined = if (newSuffix.isBlank()) formatted else "$formatted $newSuffix"
                    seg.copy(date = newDate, useFullWeekday = newFull, suffix = newSuffix, text = combined)
                }
            )
        }
    }

    fun toggleLineBreakAfter(id: Long) {
        _uiState.update { state ->
            state.copy(segments = state.segments.map { if (it.id == id) it.copy(lineBreakAfter = !it.lineBreakAfter) else it })
        }
    }

    private fun appendSegment(type: DiscordSegmentType, text: String) {
        val segment = DiscordSegmentUi(id = nextSegmentId++, type = type, text = text)
        _uiState.update { it.copy(segments = it.segments + segment) }
    }

    fun updateSegmentText(id: Long, text: String) {
        _uiState.update { state -> state.copy(segments = state.segments.map { if (it.id == id) it.copy(text = text) else it }) }
    }

    fun removeSegment(id: Long) {
        _uiState.update { it.copy(segments = it.segments.filterNot { seg -> seg.id == id }) }
    }

    fun moveSegment(id: Long, delta: Int) {
        _uiState.update { state ->
            val list = state.segments.toMutableList()
            val index = list.indexOfFirst { it.id == id }
            val target = index + delta
            if (index < 0 || target !in list.indices) return@update state
            list[index] = list[target].also { list[target] = list[index] }
            state.copy(segments = list)
        }
    }

    fun saveTag(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch { contentRepository.upsertTag(DiscordTagEntity(text = text.trim())) }
    }

    fun deleteTag(tag: DiscordTagUi) = viewModelScope.launch {
        contentRepository.deleteTag(DiscordTagEntity(id = tag.id, text = tag.text))
    }

    fun savePhrase(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch { contentRepository.upsertPhrase(DiscordPhraseEntity(text = text.trim())) }
    }

    fun deletePhrase(phrase: DiscordPhraseUi) = viewModelScope.launch {
        contentRepository.deletePhrase(DiscordPhraseEntity(id = phrase.id, text = phrase.text))
    }

    fun setDialog(showTagPicker: Boolean? = null, showPhrasePicker: Boolean? = null, showPhrasePresetPicker: Boolean? = null) {
        _uiState.update {
            it.copy(
                showTagPicker = showTagPicker ?: it.showTagPicker,
                showPhrasePicker = showPhrasePicker ?: it.showPhrasePicker,
                showPhrasePresetPicker = showPhrasePresetPicker ?: it.showPhrasePresetPicker
            )
        }
    }

    // ── 문구 프리셋: 여러 문구를 순서대로 묶어 한 번에 불러온다 ──
    fun saveCurrentAsPhrasePreset(name: String) {
        if (name.isBlank()) return
        val texts = _uiState.value.segments.map { it.text }
        if (texts.isEmpty()) return
        viewModelScope.launch {
            contentRepository.upsertPhrasePreset(
                DiscordPhrasePresetEntity(name = name.trim(), itemsJson = json.encodeToString(texts), sortOrder = _uiState.value.phrasePresets.size)
            )
        }
    }

    fun loadPhrasePreset(preset: DiscordPhrasePresetUi) {
        val newSegments = preset.items.map { text -> DiscordSegmentUi(id = nextSegmentId++, type = DiscordSegmentType.PHRASE, text = text) }
        _uiState.update { it.copy(segments = it.segments + newSegments) }
    }

    fun deletePhrasePreset(preset: DiscordPhrasePresetUi) = viewModelScope.launch {
        contentRepository.deletePhrasePreset(DiscordPhrasePresetEntity(id = preset.id, name = preset.name, itemsJson = json.encodeToString(preset.items)))
    }

    fun dismissSendResult() = _uiState.update { it.copy(sendError = null, sendSuccess = false) }

    fun send() {
        val state = _uiState.value
        val target = state.webhookTargets.firstOrNull { it.name == state.selectedWebhookName }
        if (target == null) {
            _uiState.update { it.copy(sendError = "전송 대상을 선택해주세요") }
            return
        }
        val message = state.previewText.trim()
        if (message.isBlank()) {
            _uiState.update { it.copy(sendError = "보낼 내용이 없습니다") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, sendError = null, sendSuccess = false) }
            webhookRepository.sendMessage(message, target.url)
                .onSuccess { messageId ->
                    contentRepository.insertSentMessage(
                        DiscordSentMessageEntity(
                            messageId = messageId, webhookUrl = target.url, webhookName = target.name,
                            content = message, sentAt = System.currentTimeMillis()
                        )
                    )
                    _uiState.update { it.copy(isSending = false, sendSuccess = true, segments = emptyList()) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSending = false, sendError = e.message ?: "전송에 실패했습니다") }
                }
        }
    }

    // ── 최근 전송 기록: 다시 웹훅에 수정·삭제 요청 ──
    fun startEditSentMessage(message: DiscordSentMessageUi) {
        _uiState.update { it.copy(editingSentMessageId = message.id, editingText = message.content, editError = null) }
    }

    fun updateEditingText(text: String) = _uiState.update { it.copy(editingText = text) }

    fun cancelEditSentMessage() = _uiState.update { it.copy(editingSentMessageId = null, editingText = "", editError = null) }

    fun submitEditSentMessage() {
        val state = _uiState.value
        val id = state.editingSentMessageId ?: return
        val message = state.sentMessages.firstOrNull { it.id == id } ?: return
        val newContent = state.editingText.trim()
        if (newContent.isBlank()) {
            _uiState.update { it.copy(editError = "내용을 입력해주세요") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isEditingSending = true, editError = null) }
            webhookRepository.editMessage(message.webhookUrl, message.messageId, newContent)
                .onSuccess {
                    contentRepository.updateSentMessage(
                        DiscordSentMessageEntity(message.id, message.messageId, message.webhookUrl, message.webhookName, newContent, message.sentAt)
                    )
                    _uiState.update { it.copy(isEditingSending = false, editingSentMessageId = null, editingText = "") }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isEditingSending = false, editError = e.message ?: "수정에 실패했습니다") }
                }
        }
    }

    fun deleteSentMessage(message: DiscordSentMessageUi) {
        viewModelScope.launch {
            webhookRepository.deleteMessage(message.webhookUrl, message.messageId)
                .onSuccess {
                    contentRepository.deleteSentMessage(
                        DiscordSentMessageEntity(message.id, message.messageId, message.webhookUrl, message.webhookName, message.content, message.sentAt)
                    )
                }
                .onFailure { e -> _uiState.update { it.copy(editError = e.message ?: "삭제에 실패했습니다") } }
        }
    }
}
