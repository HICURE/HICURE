// FirebaseUtils.kt
package com.example.hicure.utils

import android.util.Log
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object FirebaseCheckDate {
    private val userRef =
        Firebase.database("https://hicure-d5c99-default-rtdb.firebaseio.com/").getReference("users")

    fun checkAndUpdateDate(userId: String, buttonIndex: Int) {
        userRef.child(userId).child("lastAccessDate").get()
            .addOnSuccessListener { dateSnapshot ->
                val nowDate = dateSnapshot.getValue(String::class.java) ?: ""
                val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

                if (nowDate == date) {
                    Log.d("FirebaseUtils", "stay")
                } else {

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

                userRef.child(userId).child("infoVisited").get()
                    .addOnSuccessListener { visitedSnapshot ->
                        var visitedBitmask = visitedSnapshot.getValue(Int::class.java) ?: 0
                        val alreadyVisited = visitedBitmask and (1 shl buttonIndex) != 0

                        if (!alreadyVisited) {
                            // 버튼이 이전에 클릭되지 않은 경우에만 비트를 업데이트하고 점수 증가
                            visitedBitmask = visitedBitmask or (1 shl buttonIndex)

                            userRef.child(userId).child("infoVisited").setValue(visitedBitmask)
                                .addOnSuccessListener {
                                    Log.d(
                                        "FirebaseUtils",
                                        "Button $buttonIndex 클릭 기록 성공: $visitedBitmask"
                                    )
                                }
                                .addOnFailureListener { e ->
                                    Log.e("FirebaseUtils", "Button $buttonIndex 클릭 기록 실패", e)
                                }

                            // 점수 증가
                            userRef.child(userId).child("score").get()
                                .addOnSuccessListener { scoreSnapshot ->
                                    val currentScore = scoreSnapshot.getValue(Int::class.java) ?: 0
                                    userRef.child(userId).child("score").setValue(currentScore + 1)
                                        .addOnSuccessListener {
                                            Log.d("FirebaseUtils", "점수 증가 성공: ${currentScore + 1}")
                                        }
                                }
                        }
                    }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseUtils", "Failed to get nowDate value", e)
            }
    }
}