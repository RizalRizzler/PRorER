package com.example.prorer.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.prorer.data.model.Session
import com.example.prorer.data.model.SessionWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: Session): Long

    @Update
    suspend fun updateSession(session: Session)

    @Delete
    suspend fun deleteSession(session: Session)

    @Query("SELECT * FROM sessions ORDER BY dateMs DESC")
    fun getAllSessions(): Flow<List<Session>>

    @Query("SELECT * FROM sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: Long): Session?

    @Transaction
    @Query("SELECT * FROM sessions ORDER BY dateMs DESC")
    fun getAllSessionsWithDetails(): Flow<List<SessionWithDetails>>

    @Transaction
    @Query("SELECT * FROM sessions WHERE id = :sessionId LIMIT 1")
    fun getSessionWithDetailsById(sessionId: Long): Flow<SessionWithDetails?>

    //cascade delete
    @Query("DELETE FROM sets WHERE exerciseId IN (SELECT id FROM exercises WHERE sessionId = :sessionId)")
    suspend fun deleteSetsForSession(sessionId: Long)

    @Query("DELETE FROM exercises WHERE sessionId = :sessionId")
    suspend fun deleteExercisesForSession(sessionId: Long)

    @Query("DELETE FROM sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: Long)

    @Transaction
    suspend fun deleteFullSession(sessionId: Long) {
        deleteSetsForSession(sessionId)
        deleteExercisesForSession(sessionId)
        deleteSessionById(sessionId)
    }

}