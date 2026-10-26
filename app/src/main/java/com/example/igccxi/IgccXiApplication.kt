package com.example.igccxi

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class IgccXiApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
                Log.d("IgccXiApplication", "FirebaseApp successfully initialized in Application.onCreate")
            }
        } catch (t: Throwable) {
            Log.w("IgccXiApplication", "FirebaseApp initialization handled safely: ${t.message}")
        }
    }
}
