package com.example.prorer.data.repository

import com.example.prorer.data.db.ExerciseDao
import com.example.prorer.data.db.SessionDao
import com.example.prorer.data.db.SetEntryDao
import com.example.prorer.data.model.Exercise
import com.example.prorer.data.model.Session
import com.example.prorer.data.model.SetEntry

class LiftRepository(
    private val sessionDao: SessionDao,
    private val exerciseDao: ExerciseDao,
    private val setEntryDao: SetEntryDao
) {
    //sesh
    val allSessions = sessionDao.getAllSessions()
    suspend fun insertSession(session: Session) = sessionDao.insertSession(session)
    suspend fun deleteSession(session: Session) = sessionDao.deleteSession(session)

    //movements
    fun getExercisesForSession(sessionId: Long) = exerciseDao.getExercisesForSession(sessionId)
    suspend fun insertExercise(exercise: Exercise) = exerciseDao.insertExercise(exercise)
    suspend fun deleteExercise(exercise: Exercise) = exerciseDao.deleteExercise(exercise)

    //sets
    fun getSetsForExercise(exerciseId: Long) = setEntryDao.getSetsForExercise(exerciseId)
    suspend fun insertSet(setEntry: SetEntry) = setEntryDao.insertSet(setEntry)
    suspend fun updateSet(setEntry: SetEntry) = setEntryDao.updateSet(setEntry)
    suspend fun deleteSet(setEntry: SetEntry) = setEntryDao.deleteSet(setEntry)

    suspend fun getMaxWeightForExercise(name: String) = setEntryDao.getMaxWeightForExercise(name)
    suspend fun getGhostSetsForExercise(name: String) = setEntryDao.getGhostSetsForExercise(name)
//  pass-through properties/functions
    val allSessionsWithDetails = sessionDao.getAllSessionsWithDetails()

    fun getSessionWithDetailsById(sessionId: Long) = sessionDao.getSessionWithDetailsById(sessionId)

    suspend fun deleteFullSession(sessionId: Long) = sessionDao.deleteFullSession(sessionId)

    val distinctExerciseNames = setEntryDao.getDistinctExerciseNames()
    fun getHistoryForExercise(name: String) = setEntryDao.getHistoryForExercise(name)
}