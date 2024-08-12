// FirebaseUtils.kt
package com.example.hicure.utils

import android.util.Log
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object FirebaseCheckDate {
    private val userRef = Firebase.database("https://hicure-d5c99-default-rtdb.firebaseio.com/").getReference("users")

    fun checkAndUpdateDate(userId: String) {
        userRef.child(userId).child("lastAccessDate").get()
            .addOnSuccessListener { dateSnapshot ->
                val nowDate = dateSnapshot.getValue(String::class.java) ?: ""
                val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

                if (nowDate == date) {
                    Log.d("FirebaseUtils", "stay")
                } else {
                    // 날짜가 다를 경우 업데이트
                    userRef.child(userId).child("lastAccessDate").setValue(date)
                        .addOnSuccessListener {
                            Log.d("FirebaseUtils", "lastAccessDate 업데이트 성공")
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirebaseUtils", "lastAccessDate 업데이트 실패", e)
                        }

                    userRef.child(userId).child("infoVisited").setValue(0)
                        .addOnSuccessListener {
                            Log.d("FirebaseUtils", "infoVisited 업데이트 성공")
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirebaseUtils", "infoVisited 업데이트 실패", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseUtils", "Failed to get nowDate value", e)
            }
    }
}