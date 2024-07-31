package com.example.hicure

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.Button
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

    val database = Firebase.database("https://hicure-d5c99-default-rtdb.firebaseio.com/")
    val myRef = database.getReference("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Splash Screen
        /*Handler().postDelayed(Runnable {
            val i = Intent(this@AppStart,MainActivity::class.java)
            startActivity(i)
            finish()
        }, 5000)*/
    }

    // Touch Screen
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                showCustomDialog()
            }
        }
        return super.onTouchEvent(event)
    }

    // If id is not checked, go to the check-id-activity

    private fun showCustomDialog() {

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

        dialogBinding.checkButton.setOnClickListener {
            // need to add function for check id
            val idString = dialogBinding.editText.text.toString().trim()
            if (idString.isEmpty()) {
                dialogBinding.content.text = "식별코드가 입력되지 않았습니다."
                dialogBinding.content.setTextColor(Color.parseColor("#D1180B"))
            } else {
                if (idString.startsWith("add")) {
                    val newId = idString.removePrefix("add").trim()
                    val user = User(newId, "", 0, "")
                    addItem(user)
                } else {
                    checkUserId(idString, dialogBinding.content, alertDialog)
                }
            }
        }
    }

    private fun addItem(user: User) {
        val keyId = myRef.push().key!!
        myRef.child(user.id).setValue(user)
            .addOnSuccessListener {
                Log.d("AppStart", "Add Success")
            }
            .addOnFailureListener { e ->
                Log.d("AppStart", "Add Failure")
            }
    }

    private fun checkUserId(id: String, contentTextView: TextView, alertDialog: AlertDialog) {
        myRef.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val intent = Intent(this@AppStart, InitialSurvey::class.java)
                    alertDialog.dismiss()
                    startActivity(intent)
                    finish()
                } else {
                    contentTextView.text = "올바르지 않은 식별코드입니다."
                    contentTextView.setTextColor(Color.parseColor("#D1180B"))
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("AppStart", "Database Error", error.toException())
            }
        })
    }
}