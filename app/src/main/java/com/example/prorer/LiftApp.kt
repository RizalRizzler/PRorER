package com.example.prorer

import android.app.Application
import com.example.prorer.data.db.AppDatabase
import com.example.prorer.data.repository.LiftRepository

class LiftApp : Application() {


    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy {
        LiftRepository(
            database.sessionDao(),
            database.exerciseDao(),
            database.setEntryDao()
        )
    }
}