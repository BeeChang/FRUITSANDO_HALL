package example.yf.fruit_hall.data.tray

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import example.yf.fruit_hall.data.discord.DiscordWebhookTarget
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraySplitPreferenceRepository @Inject constructor(
    @ApplicationContext context: Context,
    private val json: Json
) {
    private val prefs = context.getSharedPreferences("tray_split_prefs", Context.MODE_PRIVATE)

    var lastDiscordExtraText: String
        get() = prefs.getString(KEY_LAST_DISCORD_EXTRA_TEXT, "") ?: ""
        set(value) { prefs.edit().putString(KEY_LAST_DISCORD_EXTRA_TEXT, value).apply() }

    // 마지막으로 선택한 디스코드 전송 대상의 name(리모트 컨피그의 discord_hook_urls 목록과 매칭)
    var lastDiscordWebhookName: String
        get() = prefs.getString(KEY_LAST_DISCORD_WEBHOOK_NAME, "") ?: ""
        set(value) { prefs.edit().putString(KEY_LAST_DISCORD_WEBHOOK_NAME, value).apply() }

    // 리모트 컨피그에서 마지막으로 받아온 웹훅 목록 캐시 — 다이얼로그를 열 때 네트워크 응답을 기다리지 않도록 미리 화면 진입 시점에 채워둠
    var cachedDiscordWebhookTargets: List<DiscordWebhookTarget>
        get() = prefs.getString(KEY_CACHED_DISCORD_WEBHOOK_TARGETS, null)?.let {
            runCatching { json.decodeFromString<List<DiscordWebhookTarget>>(it) }.getOrDefault(emptyList())
        } ?: emptyList()
        set(value) { prefs.edit().putString(KEY_CACHED_DISCORD_WEBHOOK_TARGETS, json.encodeToString(value)).apply() }

    // 날짜 앞/뒤에 붙는 고정 문구(예: "산도 라인업") — 날짜 자체는 매번 자동 계산되므로 저장하지 않음
    var lastDiscordTitlePrefix: String
        get() = prefs.getString(KEY_LAST_DISCORD_TITLE_PREFIX, "") ?: ""
        set(value) { prefs.edit().putString(KEY_LAST_DISCORD_TITLE_PREFIX, value).apply() }

    var lastDiscordTitleSuffix: String
        get() = prefs.getString(KEY_LAST_DISCORD_TITLE_SUFFIX, "") ?: ""
        set(value) { prefs.edit().putString(KEY_LAST_DISCORD_TITLE_SUFFIX, value).apply() }

    // 차수 수가 늘어나도 그대로 대응 가능하도록 차수 번호 -> 마지막 입력 시간 문구를 통째로 저장
    var lastDiscordRoundTimes: Map<Int, String>
        get() = prefs.getString(KEY_LAST_DISCORD_ROUND_TIMES, null)?.let {
            runCatching { json.decodeFromString<Map<Int, String>>(it) }.getOrDefault(emptyMap())
        } ?: emptyMap()
        set(value) { prefs.edit().putString(KEY_LAST_DISCORD_ROUND_TIMES, json.encodeToString(value)).apply() }

    companion object {
        private const val KEY_LAST_DISCORD_EXTRA_TEXT = "last_discord_extra_text"
        private const val KEY_LAST_DISCORD_WEBHOOK_NAME = "last_discord_webhook_name"
        private const val KEY_CACHED_DISCORD_WEBHOOK_TARGETS = "cached_discord_webhook_targets"
        private const val KEY_LAST_DISCORD_TITLE_PREFIX = "last_discord_title_prefix"
        private const val KEY_LAST_DISCORD_TITLE_SUFFIX = "last_discord_title_suffix"
        private const val KEY_LAST_DISCORD_ROUND_TIMES = "last_discord_round_times"
    }
}
