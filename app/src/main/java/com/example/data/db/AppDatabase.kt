package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AppBlockCategory
import com.example.data.model.BlockedAppEntity
import com.example.data.model.DailyStatEntity
import com.example.data.model.PomodoroSessionEntity
import com.example.data.model.SessionMode
import com.example.data.model.TaskCategory
import com.example.data.model.TaskItemEntity
import com.example.data.model.TaskPriority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Converters {
    @TypeConverter
    fun fromSessionMode(mode: SessionMode): String = mode.name
    @TypeConverter
    fun toSessionMode(value: String): SessionMode = try { SessionMode.valueOf(value) } catch (e: Exception) { SessionMode.FOCUS }

    @TypeConverter
    fun fromTaskPriority(priority: TaskPriority): String = priority.name
    @TypeConverter
    fun toTaskPriority(value: String): TaskPriority = try { TaskPriority.valueOf(value) } catch (e: Exception) { TaskPriority.MEDIUM }

    @TypeConverter
    fun fromTaskCategory(category: TaskCategory): String = category.name
    @TypeConverter
    fun toTaskCategory(value: String): TaskCategory = try { TaskCategory.valueOf(value) } catch (e: Exception) { TaskCategory.WORK }

    @TypeConverter
    fun fromAppBlockCategory(category: AppBlockCategory): String = category.name
    @TypeConverter
    fun toAppBlockCategory(value: String): AppBlockCategory = try { AppBlockCategory.valueOf(value) } catch (e: Exception) { AppBlockCategory.SOCIAL }
}

@Database(
    entities = [
        TaskItemEntity::class,
        PomodoroSessionEntity::class,
        BlockedAppEntity::class,
        DailyStatEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun sessionDao(): SessionDao
    abstract fun blockedAppDao(): BlockedAppDao
    abstract fun dailyStatDao(): DailyStatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aurafocus_database"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Prepopulate default distracting apps and sample tasks
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getDatabase(context)
                            prepopulateData(database)
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun prepopulateData(db: AppDatabase) {
            val defaultBlockedApps = listOf(
                BlockedAppEntity(
                    packageName = "com.whatsapp",
                    appName = "WhatsApp",
                    category = AppBlockCategory.MESSAGING,
                    isBlocked = true,
                    iconColorHex = "#25D366"
                ),
                BlockedAppEntity(
                    packageName = "com.instagram.android",
                    appName = "Instagram",
                    category = AppBlockCategory.SOCIAL,
                    isBlocked = true,
                    iconColorHex = "#E1306C"
                ),
                BlockedAppEntity(
                    packageName = "com.zhiliaoapp.musically",
                    appName = "TikTok",
                    category = AppBlockCategory.ENTERTAINMENT,
                    isBlocked = true,
                    iconColorHex = "#EE1D52"
                ),
                BlockedAppEntity(
                    packageName = "com.google.android.youtube",
                    appName = "YouTube",
                    category = AppBlockCategory.ENTERTAINMENT,
                    isBlocked = true,
                    iconColorHex = "#FF0000"
                ),
                BlockedAppEntity(
                    packageName = "com.twitter.android",
                    appName = "X (Twitter)",
                    category = AppBlockCategory.SOCIAL,
                    isBlocked = true,
                    iconColorHex = "#1DA1F2"
                ),
                BlockedAppEntity(
                    packageName = "com.reddit.frontpage",
                    appName = "Reddit",
                    category = AppBlockCategory.SOCIAL,
                    isBlocked = true,
                    iconColorHex = "#FF4500"
                ),
                BlockedAppEntity(
                    packageName = "com.netflix.mediaclient",
                    appName = "Netflix",
                    category = AppBlockCategory.ENTERTAINMENT,
                    isBlocked = true,
                    iconColorHex = "#E50914"
                ),
                BlockedAppEntity(
                    packageName = "com.dts.freefireth",
                    appName = "Free Fire & Mobile Games",
                    category = AppBlockCategory.GAMES,
                    isBlocked = true,
                    iconColorHex = "#F39C12"
                ),
                BlockedAppEntity(
                    packageName = "com.facebook.katana",
                    appName = "Facebook",
                    category = AppBlockCategory.SOCIAL,
                    isBlocked = true,
                    iconColorHex = "#1877F2"
                ),
                BlockedAppEntity(
                    packageName = "com.snapchat.android",
                    appName = "Snapchat",
                    category = AppBlockCategory.SOCIAL,
                    isBlocked = false,
                    iconColorHex = "#FFFC00"
                ),
                BlockedAppEntity(
                    packageName = "com.discord",
                    appName = "Discord",
                    category = AppBlockCategory.MESSAGING,
                    isBlocked = false,
                    iconColorHex = "#5865F2"
                )
            )
            db.blockedAppDao().insertAll(defaultBlockedApps)

            val initialTasks = listOf(
                TaskItemEntity(
                    title = "Deep Focus: Complete project architecture",
                    notes = "Map out high-accuracy timer & offline state sync",
                    priority = TaskPriority.HIGH,
                    category = TaskCategory.CODING,
                    estimatedPomodoros = 4,
                    completedPomodoros = 1,
                    isCompleted = false
                ),
                TaskItemEntity(
                    title = "Read 25 pages of cognitive psychology",
                    notes = "Flow states and attention span optimization",
                    priority = TaskPriority.MEDIUM,
                    category = TaskCategory.READING,
                    estimatedPomodoros = 2,
                    completedPomodoros = 2,
                    isCompleted = true
                ),
                TaskItemEntity(
                    title = "Review Weekly Productivity Report",
                    notes = "Examine distraction trends and pacing",
                    priority = TaskPriority.LOW,
                    category = TaskCategory.STUDY,
                    estimatedPomodoros = 1,
                    completedPomodoros = 0,
                    isCompleted = false
                )
            )
            for (t in initialTasks) {
                db.taskDao().insertTask(t)
            }

            // Seed sample past daily stats for immediate visual richness in weekly chart
            val now = System.currentTimeMillis()
            val dayMs = 24 * 60 * 60 * 1000L
            val sampleDays = listOf(
                Triple("2026-10-15", 75, 3),
                Triple("2026-10-16", 125, 5),
                Triple("2026-10-17", 100, 4),
                Triple("2026-10-18", 150, 6),
                Triple("2026-10-19", 90, 3),
                Triple("2026-10-20", 125, 5),
                Triple("2026-10-21", 50, 2)
            )
            for ((date, mins, sessions) in sampleDays) {
                db.dailyStatDao().insertOrUpdate(
                    DailyStatEntity(
                        dateString = date,
                        totalFocusMinutes = mins,
                        completedSessions = sessions,
                        completedTasks = sessions / 2,
                        distractionAttemptsResisted = (sessions * 1.5).toInt()
                    )
                )
            }
        }
    }
}
