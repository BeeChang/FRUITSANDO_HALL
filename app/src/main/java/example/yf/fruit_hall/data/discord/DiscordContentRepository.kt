package example.yf.fruit_hall.data.discord

import example.yf.fruit_hall.data.discord.local.dao.DiscordPhraseDao
import example.yf.fruit_hall.data.discord.local.dao.DiscordPhrasePresetDao
import example.yf.fruit_hall.data.discord.local.dao.DiscordSentMessageDao
import example.yf.fruit_hall.data.discord.local.dao.DiscordTagDao
import example.yf.fruit_hall.data.discord.local.entity.DiscordPhraseEntity
import example.yf.fruit_hall.data.discord.local.entity.DiscordPhrasePresetEntity
import example.yf.fruit_hall.data.discord.local.entity.DiscordSentMessageEntity
import example.yf.fruit_hall.data.discord.local.entity.DiscordTagEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiscordContentRepository @Inject constructor(
    private val tagDao: DiscordTagDao,
    private val phraseDao: DiscordPhraseDao,
    private val sentMessageDao: DiscordSentMessageDao,
    private val phrasePresetDao: DiscordPhrasePresetDao
) {
    fun observeTags(): Flow<List<DiscordTagEntity>> = tagDao.observeAll()
    suspend fun upsertTag(tag: DiscordTagEntity): Long = tagDao.upsert(tag)
    suspend fun deleteTag(tag: DiscordTagEntity) = tagDao.delete(tag)

    fun observePhrases(): Flow<List<DiscordPhraseEntity>> = phraseDao.observeAll()
    suspend fun upsertPhrase(phrase: DiscordPhraseEntity): Long = phraseDao.upsert(phrase)
    suspend fun deletePhrase(phrase: DiscordPhraseEntity) = phraseDao.delete(phrase)

    fun observeRecentSentMessages(): Flow<List<DiscordSentMessageEntity>> = sentMessageDao.observeRecent()
    suspend fun insertSentMessage(message: DiscordSentMessageEntity): Long = sentMessageDao.insert(message)
    suspend fun updateSentMessage(message: DiscordSentMessageEntity) = sentMessageDao.update(message)
    suspend fun deleteSentMessage(message: DiscordSentMessageEntity) = sentMessageDao.delete(message)

    fun observePhrasePresets(): Flow<List<DiscordPhrasePresetEntity>> = phrasePresetDao.observeAll()
    suspend fun upsertPhrasePreset(preset: DiscordPhrasePresetEntity): Long = phrasePresetDao.upsert(preset)
    suspend fun deletePhrasePreset(preset: DiscordPhrasePresetEntity) = phrasePresetDao.delete(preset)
}
