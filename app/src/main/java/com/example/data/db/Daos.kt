package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BlockedAppEntity
import com.example.data.model.DailyStatEntity
import com.example.data.model.PomodoroSessionEntity
import com.example.data.model.TaskItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, priority DESC, id DESC")
    fun getAllTasksFlow(): Flow<List<TaskItemEntity>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY priority DESC, id DESC")
    fun getActiveTasksFlow(): Flow<List<TaskItemEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): TaskItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskItemEntity): Long

    @Update
    suspend fun updateTask(task: TaskItemEntity)

    @Delete
    suspend fun deleteTask(task: TaskItemEntity)

    @Query("UPDATE tasks SET completedPomodoros = completedPomodoros + 1 WHERE id = :id")
    suspend fun incrementPomodoro(id: Long)

    @Query("UPDATE tasks SET isCompleted = :completed, completedAt = :completedAt WHERE id = :id")
    suspend fun setCompleted(id: Long, completed: Boolean, completedAt: Long?)
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY timestamp DESC")
    fun getAllSessionsFlow(): Flow<List<PomodoroSessionEntity>>

    @Query("SELECT * FROM sessions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSessionsFlow(limit: Int = 30): Flow<List<PomodoroSessionEntity>>

    @Query("SELECT * FROM sessions WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getSessionsSinceFlow(sinceTimestamp: Long): Flow<List<PomodoroSessionEntity>>

    @Query("SELECT * FROM sessions WHERE timestamp >= :sinceTimestamp")
    suspend fun getSessionsSince(sinceTimestamp: Long): List<PomodoroSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PomodoroSessionEntity): Long

    @Query("SELECT COUNT(*) FROM sessions WHERE completed = 1 AND mode = 'FOCUS'")
    suspend fun getTotalFocusSessionsCount(): Int

    @Query("SELECT SUM(actualSecondsFocused) FROM sessions WHERE mode = 'FOCUS'")
    suspend fun getTotalSecondsFocused(): Long?
}

@Dao
interface BlockedAppDao {
    @Query("SELECT * FROM blocked_apps ORDER BY isBlocked DESC, appName ASC")
    fun getAllBlockedAppsFlow(): Flow<List<BlockedAppEntity>>

    @Query("SELECT * FROM blocked_apps WHERE isBlocked = 1")
    suspend fun getCurrentlyBlockedApps(): List<BlockedAppEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(apps: List<BlockedAppEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: BlockedAppEntity)

    @Update
    suspend fun updateApp(app: BlockedAppEntity)

    @Delete
    suspend fun deleteApp(app: BlockedAppEntity)

    @Query("UPDATE blocked_apps SET isBlocked = :blocked WHERE packageName = :packageName")
    suspend fun setBlockedStatus(packageName: String, blocked: Boolean)

    @Query("UPDATE blocked_apps SET timesBlockedCount = timesBlockedCount + 1 WHERE packageName = :packageName")
    suspend fun incrementBlockedCount(packageName: String)
}

@Dao
interface DailyStatDao {
    @Query("SELECT * FROM daily_stats ORDER BY dateString DESC LIMIT 30")
    fun getRecentDailyStatsFlow(): Flow<List<DailyStatEntity>>

    @Query("SELECT * FROM daily_stats WHERE dateString = :dateString LIMIT 1")
    suspend fun getStatForDate(dateString: String): DailyStatEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stat: DailyStatEntity)

    @Query("UPDATE daily_stats SET totalFocusMinutes = totalFocusMinutes + :minutes, completedSessions = completedSessions + 1 WHERE dateString = :dateString")
    suspend fun addFocusSession(dateString: String, minutes: Int)

    @Query("UPDATE daily_stats SET completedTasks = completedTasks + 1 WHERE dateString = :dateString")
    suspend fun incrementTaskCompleted(dateString: String)

    @Query("UPDATE daily_stats SET distractionAttemptsResisted = distractionAttemptsResisted + 1 WHERE dateString = :dateString")
    suspend fun incrementDistractionsResisted(dateString: String)
}
