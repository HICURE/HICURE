package com.example.hicure.utils

import android.util.Log
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object FirebaseCheckDate {

    private val userRef =
        Firebase.database("https://hicure-d5c99-default-rtdb.firebaseio.com/").getReference("users")

    fun updateDate(userId: String) {
        userRef.child(userId).child("lastAccessDate").get()
            .addOnSuccessListener { lastAccessSnapshot ->
                val lastVisited = lastAccessSnapshot.getValue(String::class.java)
                val now = LocalDate.now()
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val nowFormatted = now.format(dateFormatter)

                if (lastVisited == null || lastVisited != nowFormatted) {

                    userRef.child(userId).child("gapValue").get()
                        .addOnSuccessListener { gapValueSnapshot ->
                            val gapValue = gapValueSnapshot.getValue(Int::class.java) ?: 0

                            if (gapValue > 0) {
                                userRef.child(userId).child("score").get()
                                    .addOnSuccessListener { scoreSnapshot ->
                                        val currentScore = scoreSnapshot.getValue(Int::class.java) ?: 0
                                        val newScore = currentScore + gapValue

                                        userRef.child(userId).child("score").setValue(newScore)
                                            .addOnSuccessListener {
                                                Log.d("FirebaseCheckDate", "점수 갱신 성공: $newScore")

                                                userRef.child(userId).child("gapValue").setValue(0)
                                                    .addOnSuccessListener {
                                                        Log.d("FirebaseCheckDate", "gapValue 초기화 완료")
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Log.e("FirebaseCheckDate", "gapValue 초기화 실패", e)
                                                    }
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("FirebaseCheckDate", "점수 갱신 실패", e)
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("FirebaseCheckDate", "현재 점수 가져오기 실패", e)
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirebaseCheckDate", "gapValue 가져오기 실패", e)
                        }

                    // infoVisited 리셋 및 lastAccessDate 갱신
                    userRef.child(userId).child("infoVisited").setValue(0)
                        .addOnSuccessListener {
                            Log.d("FirebaseCheckDate", "InfoVisited 리셋 완료")
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirebaseCheckDate", "InfoVisited 리셋 실패", e)
                        }

                    userRef.child(userId).child("lastAccessDate").setValue(nowFormatted)
                        .addOnSuccessListener {
                            Log.d("FirebaseCheckDate", "LastAccessDate 갱신 완료")
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirebaseCheckDate", "LastAccessDate 갱신 실패", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseCheckDate", "Failed to get lastAccessDate value", e)
            }
    }

    fun updateStatus(userId: String, buttonIndex: Int) {
        if (buttonIndex < 0) {
            Log.e("FirebaseCheckDate", "Invalid buttonIndex: $buttonIndex")
            return
        }

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
