package com.example.prorer.data.db

import androidx.room.*
import com.example.prorer.data.model.SetEntry
import com.example.prorer.data.model.SetWithDate
import kotlinx.coroutines.flow.Flow

@Dao
interface SetEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(setEntry: SetEntry): Long

    @Update
    suspend fun updateSet(setEntry: SetEntry)

    @Delete
    suspend fun deleteSet(setEntry: SetEntry)

    @Query("SELECT * FROM sets WHERE exerciseId = :exerciseId ORDER BY setNumber ASC")
    fun getSetsForExercise(exerciseId: Long): Flow<List<SetEntry>>

    //get max
    @Query("""
        SELECT MAX(sets.weightKg) FROM sets 
        INNER JOIN exercises ON sets.exerciseId = exercises.id 
        WHERE exercises.name = :exerciseName AND sets.isCompleted = 1
    """)
    suspend fun getMaxWeightForExercise(exerciseName: String): Float?

    //recent sets
    @Query("""
        SELECT sets.* FROM sets 
        INNER JOIN exercises ON sets.exerciseId = exercises.id 
        INNER JOIN sessions ON exercises.sessionId = sessions.id
        WHERE exercises.name = :exerciseName AND sets.isCompleted = 1
        ORDER BY sessions.dateMs DESC LIMIT 5
    """)
    suspend fun getGhostSetsForExercise(exerciseName: String): List<SetEntry>


    @Query("SELECT DISTINCT name FROM exercises ORDER BY name ASC")
    fun getDistinctExerciseNames(): Flow<List<String>>

    @Query("""
        SELECT sets.weightKg, sets.reps, sessions.dateMs 
        FROM sets 
        INNER JOIN exercises ON sets.exerciseId = exercises.id 
        INNER JOIN sessions ON exercises.sessionId = sessions.id
        WHERE exercises.name = :exerciseName AND sets.isCompleted = 1
        ORDER BY sessions.dateMs DESC
    """)
    fun getHistoryForExercise(exerciseName: String): Flow<List<SetWithDate>>
}