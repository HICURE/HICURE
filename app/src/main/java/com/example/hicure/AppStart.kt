package com.example.hicure

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.hicure.databinding.ActivityAppStartBinding
import com.example.hicure.databinding.CheckIdBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class AppStart : AppCompatActivity() {

    val binding: ActivityAppStartBinding by lazy { ActivityAppStartBinding.inflate(layoutInflater) }

    // load firebase realtime database
    val database = Firebase.database("https://hicure-d5c99-default-rtdb.firebaseio.com/")
    val userRef = database.getReference("users")
    private var isUserLoggedIn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // User data load
        loadUserFromPreferences()
    }

    // Touch Screen
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (isUserLoggedIn) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            showCustomDialog()
        }
        return super.onTouchEvent(event)
    }

    // If id is not checked, go to the check-id-activity
    private fun showCustomDialog() {

        // load check id xml ( binding )
        val dialogBinding = CheckIdBinding.inflate(LayoutInflater.from(this))
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogBinding.root)

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogBinding.Title.text = "어플 사용 전 확인"
        dialogBinding.editText.hint = "식별코드를 입력해주세요."
        dialogBinding.content.text = "식별코드는 안내지에 기입되어 있습니다!"
        dialogBinding.checkButton.text = "확인"

        dialogBinding.exitButton.setOnClickListener {
            alertDialog.dismiss()
        }

        // check id
        dialogBinding.checkButton.setOnClickListener {
            // need to add function for check id
            val idString = dialogBinding.editText.text.toString().trim()
            if (idString.isEmpty()) {
                dialogBinding.content.text = "식별코드가 입력되지 않았습니다."
                dialogBinding.content.setTextColor(Color.parseColor("#D1180B"))
            } else {
                if (idString.startsWith("add")) {
                    val newId = idString.removePrefix("add").trim()
                    val user = User().apply {
                        name = "New User"
                        age = 0
                        gender = "Unknown"
                        survey = false
                    }
                    userRef.child(newId).setValue(user)
                        .addOnSuccessListener {
                            Log.d("AppStart", "Add Success")
                        }
                        .addOnFailureListener { e ->
                            Log.d("AppStart", "Add Failure", e)
                        }
                } else {
                    // check User
                    checkUserId(idString, dialogBinding.content, alertDialog)
                }
            }
        }
    }

    private fun checkUserId(id: String, contentTextView: TextView, alertDialog: AlertDialog) {
        userRef.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    user?.let {
                        saveUserToPreferences(it, id)
                        val intent = Intent(this@AppStart, InitialSurvey::class.java)
                        alertDialog.dismiss()
                        startActivity(intent)
                        finish()
                    }
                } else {
                    contentTextView.text = "올바르지 않은 식별코드입니다."
                    contentTextView.setTextColor(Color.parseColor("#D1180B"))
                }
            }

            // database error log
            override fun onCancelled(error: DatabaseError) {
                Log.e("AppStart", "Database Error", error.toException())
            }
        })
    }

    private fun saveUserToPreferences(user: User, id: String) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("user_id", id)
            putString("user_name", user.name)
            putInt("user_age", user.age)
            putString("user_gender", user.gender)
            putBoolean("user_survey", user.survey)
            apply()
        }
    }

    private fun loadUserFromPreferences() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)

        if (userId != null) {
            // Check if the user ID exists in Firebase
            userRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user = snapshot.getValue(User::class.java)
                        user?.let {
                            // Update shared preferences with user data from Firebase
                            val editor = sharedPreferences.edit()
                            editor.putString("user_name", it.name)
                            editor.putInt("user_age", it.age)
                            editor.putString("user_gender", it.gender)
                            editor.putBoolean("user_survey", it.survey)
                            editor.apply()

                            if (it.survey) {
                                isUserLoggedIn = true
                            }
                        }
                    } else {
                        // User ID does not exist in Firebase
                        isUserLoggedIn = false
                    }
                }

                // Error database
                override fun onCancelled(error: DatabaseError) {
                    Log.e("AppStart", "Database Error", error.toException())
                    isUserLoggedIn = false
                }
            })
        } else {
            isUserLoggedIn = false
        }
    }
}