package com.example.electricitips

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FirebaseHelper () : android.app.Application() {
    override fun onCreate() {
        super.onCreate()
        Firebase.database("https://electricitips-default-rtdb.asia-southeast1.firebasedatabase.app/").setPersistenceEnabled(true)
    }
}