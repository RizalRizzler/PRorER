package com.example.prorer.data.model

import androidx.room.Embedded
import androidx.room.Relation

// movements in relation to its sets
data class ExerciseWithSets(
    @Embedded val exercise: Exercise,
    @Relation(
        parentColumn = "id",
        entityColumn = "exerciseId"
    )
    val sets: List<SetEntry>
)

//declaring a sesh and all of its contents
data class SessionWithDetails(
    @Embedded val session: Session,
    @Relation(
        entity = Exercise::class,
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val exercises: List<ExerciseWithSets>
)