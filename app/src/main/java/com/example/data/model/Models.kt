package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SessionMode {
    FOCUS,
    SHORT_BREAK,
    LONG_BREAK
}

enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH
}

enum class TaskCategory {
    WORK,
    STUDY,
    CODING,
    READING,
    HEALTH,
    CREATIVE,
    OTHER
}

enum class AppBlockCategory {
    SOCIAL,
    GAMES,
    ENTERTAINMENT,
    MESSAGING,
    CUSTOM
}

enum class AppThemeSetting {
    ZEN_FOREST,      // Emerald / Sage / Moss
    SUNSET_GLOW,     // Warm Terracotta / Coral Red
    MIDNIGHT_NEON,   // Deep Obsidian / Neon Cyan & Violet
    SAKURA_ROSE,     // Blush Pink / Soft Mauve
    OCEAN_DEEP       // Arctic Teal / Ice Blue / Deep Navy
}

enum class DarkModeSetting {
    SYSTEM,
    LIGHT,
    DARK,
    OLED_BLACK
}

enum class AmbientSoundType {
    MUTE,
    ZEN_BELL,
    CLOCK_TICK,
    RAIN_WHISPER,
    WHITE_NOISE
}

@Entity(tableName = "tasks")
data class TaskItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val notes: String = "",
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val category: TaskCategory = TaskCategory.WORK,
    val estimatedPomodoros: Int = 1,
    val completedPomodoros: Int = 0,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

@Entity(tableName = "sessions")
data class PomodoroSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mode: SessionMode,
    val plannedDurationMinutes: Int,
    val actualSecondsFocused: Int,
    val taskId: Long? = null,
    val taskTitle: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val completed: Boolean = true,
    val distractionStrikes: Int = 0
)

@Entity(tableName = "blocked_apps")
data class BlockedAppEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val category: AppBlockCategory = AppBlockCategory.SOCIAL,
    val isBlocked: Boolean = true,
    val iconColorHex: String = "#FF5555",
    val timesBlockedCount: Int = 0
)

@Entity(tableName = "daily_stats")
data class DailyStatEntity(
    @PrimaryKey
    val dateString: String, // Format: YYYY-MM-DD
    val totalFocusMinutes: Int = 0,
    val completedSessions: Int = 0,
    val completedTasks: Int = 0,
    val distractionAttemptsResisted: Int = 0
)

data class ProductivityMetrics(
    val totalFocusMinutes: Int,
    val totalSessions: Int,
    val totalCompletedTasks: Int,
    val currentStreakDays: Int,
    val productivityScore: Int,
    val distractionsAvoided: Int,
    val dailyMinutesPastWeek: List<Pair<String, Int>> // Day name -> minutes
)
