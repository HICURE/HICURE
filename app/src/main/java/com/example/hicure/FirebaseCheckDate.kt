package com.example.hicure.utils

import android.util.Log
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object FirebaseCheckDate {

    private val userRef = Firebase.database("https://hicure-d5c99-default-rtdb.firebaseio.com/").getReference("users")

    fun updateVisitedStatus(userId: String, buttonIndex: Int) {
        userRef.child(userId).child("infoVisited").get()
            .addOnSuccessListener { visitedSnapshot ->
                var visitedBitmask = visitedSnapshot.getValue(Int::class.java) ?: 0
                val alreadyVisited = visitedBitmask and (1 shl buttonIndex) != 0

                if (!alreadyVisited) {
                    visitedBitmask = visitedBitmask or (1 shl buttonIndex)
                    userRef.child(userId).child("infoVisited").setValue(visitedBitmask)
                        .addOnSuccessListener {
                            Log.d("FirebaseCheckDate", "Button $buttonIndex 클릭 기록 성공: $visitedBitmask")
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirebaseCheckDate", "Button $buttonIndex 클릭 기록 실패", e)
                        }

                    userRef.child(userId).child("score").get()
                        .addOnSuccessListener { scoreSnapshot ->
                            val currentScore = scoreSnapshot.getValue(Int::class.java) ?: 0
                            userRef.child(userId).child("score").setValue(currentScore + 1)
                                .addOnSuccessListener {
                                    Log.d("FirebaseCheckDate", "점수 증가 성공: ${currentScore + 1}")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("FirebaseCheckDate", "점수 증가 실패", e)
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirebaseCheckDate", "Failed to get score value", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseCheckDate", "Failed to get infoVisited value", e)
            }
    }

    fun getInfoVisited(userId: String, callback: (Int) -> Unit) {
        userRef.child(userId).child("infoVisited").get()
            .addOnSuccessListener { visitedSnapshot ->
                val visitedBitmask = visitedSnapshot.getValue(Int::class.java) ?: 0
                callback(visitedBitmask)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseCheckDate", "Failed to get infoVisited value", e)
                callback(0)
            }
    }
}
