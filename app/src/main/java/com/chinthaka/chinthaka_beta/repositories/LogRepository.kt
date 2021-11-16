package com.chinthaka.chinthaka_beta.repositories

import android.media.MediaDrm
import android.util.Log
import dagger.hilt.android.scopes.ActivityScoped
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import java.text.SimpleDateFormat
import java.util.*

@ActivityScoped
class LogRepository {

    private val firebaseFirestore = FirebaseFirestore.getInstance()

    private val todaysDateAsString: String
        private get() {
            val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
            val calendar = Calendar.getInstance()
            return simpleDateFormat.format(calendar.time)
        }

    fun recordLog(logMessage: String) {
        val todaysDate = todaysDateAsString
        val docRef = firebaseFirestore.collection("logs").document(todaysDate)
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userName: String = currentUser?.displayName ?: "TempUser"
        docRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (!document.exists()) {
                    val loginsObjectMap: MutableMap<String, Any> = HashMap()
                    val logs: MutableList<String?> = ArrayList()
                    logs.add(logMessage)
                    loginsObjectMap[userName] = logs
                    firebaseFirestore.collection("logs").document(todaysDate)
                        .set(loginsObjectMap)
                }
            } else {
                Log.d("Firebase Firestore", "get failed with ", task.exception)
            }
        }
        docRef.update(userName, FieldValue.arrayUnion(
            logMessage
        ))
    }
}