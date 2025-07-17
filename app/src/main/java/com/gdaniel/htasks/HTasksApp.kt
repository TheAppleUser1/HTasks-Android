package com.gdaniel.htasks

import android.app.Application
import com.google.firebase.FirebaseApp

class HTasksApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
} 