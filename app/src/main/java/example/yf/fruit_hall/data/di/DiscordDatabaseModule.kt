package example.yf.fruit_hall.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import example.yf.fruit_hall.data.discord.local.DiscordContentDatabase
import example.yf.fruit_hall.data.discord.local.dao.DiscordPhraseDao
import example.yf.fruit_hall.data.discord.local.dao.DiscordPhrasePresetDao
import example.yf.fruit_hall.data.discord.local.dao.DiscordSentMessageDao
import example.yf.fruit_hall.data.discord.local.dao.DiscordTagDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DiscordDatabaseModule {

    // 전송 기록(수정·삭제용 messageId 보관) 테이블 추가
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS discord_sent_messages (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "messageId TEXT NOT NULL, " +
                    "webhookUrl TEXT NOT NULL, " +
                    "webhookName TEXT NOT NULL, " +
                    "content TEXT NOT NULL, " +
                    "sentAt INTEGER NOT NULL)"
            )
        }
    }

    // 문구 여러 개를 순서대로 묶어 한 번에 불러오는 프리셋 테이블 추가
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS discord_phrase_presets (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "itemsJson TEXT NOT NULL, " +
                    "sortOrder INTEGER NOT NULL)"
            )
        }
    }

    @Provides
    @Singleton
    fun provideDiscordContentDatabase(@ApplicationContext context: Context): DiscordContentDatabase =
        Room.databaseBuilder(context, DiscordContentDatabase::class.java, "discord_content_database")
            // destructive fallback 없음: 스키마를 바꿀 땐 반드시 여기에 실제 Migration을 추가해야 한다.
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()

    @Provides
    @Singleton
    fun provideDiscordTagDao(db: DiscordContentDatabase): DiscordTagDao = db.tagDao()

    @Provides
    @Singleton
    fun provideDiscordPhraseDao(db: DiscordContentDatabase): DiscordPhraseDao = db.phraseDao()

    @Provides
    @Singleton
    fun provideDiscordSentMessageDao(db: DiscordContentDatabase): DiscordSentMessageDao = db.sentMessageDao()

    @Provides
    @Singleton
    fun provideDiscordPhrasePresetDao(db: DiscordContentDatabase): DiscordPhrasePresetDao = db.phrasePresetDao()
}
