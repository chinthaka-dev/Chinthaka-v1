package com.chinthaka.chinthaka_beta.repositories

import android.util.Log
import dagger.hilt.android.scopes.ActivityScoped
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import java.text.SimpleDateFormat
import java.util.*

@ActivityScoped
class MetricRepository {

    private val firebaseFirestore = FirebaseFirestore.getInstance()

    private val todaysDateAsString: String
        private get() {
            val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
            val calendar = Calendar.getInstance()
            return simpleDateFormat.format(calendar.time)
        }

    fun recordDailyLogin() {
        val todaysDate = todaysDateAsString
        val docRef = firebaseFirestore.collection("metrics").document(todaysDate)
        docRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (!document.exists()) {
                    val loginsObjectMap: MutableMap<String, Any> = HashMap()
                    val logins: MutableList<String?> = ArrayList()
                    logins.add(FirebaseAuth.getInstance().currentUser!!.displayName)
                    loginsObjectMap["logins"] = logins
                    firebaseFirestore.collection("metrics").document(todaysDate)
                        .set(loginsObjectMap)
                }
            } else {
                Log.d("Firebase Firestore", "get failed with ", task.exception)
            }
        }
        docRef.update(
            "logins", FieldValue.arrayUnion(
                FirebaseAuth.getInstance().currentUser!!.displayName
            )
        )
    }

    fun recordClicksOnMetric(metricName: String) {
        val todaysDate = todaysDateAsString
        val docRef = firebaseFirestore.collection("metrics").document(todaysDate)
        docRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (!document.exists()) {
                    val clicksOnViewSolutionsMap: MutableMap<String, Any> = HashMap()
                    clicksOnViewSolutionsMap[metricName] = 1
                    firebaseFirestore.collection("metrics").document(todaysDate)
                        .set(clicksOnViewSolutionsMap)
                }
            } else {
                Log.d("Firebase Firestore", "get failed with ", task.exception)
            }
        }
        docRef.update(metricName, FieldValue.increment(1))
    }
}