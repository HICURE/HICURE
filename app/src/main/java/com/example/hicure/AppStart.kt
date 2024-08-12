package com.example.hicure

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
    private var isSurvey = false

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (checkAndRequestPermissions()) {
            promptEnableBluetooth()
        }


        // User data load
        loadUserFromPreferences {
            binding.root.setOnTouchListener { _, event ->
                handleTouchEvent(event)
                true
            }
        }

        binding.root.setOnClickListener {
            if (!isNetworkConnected(this)) {
                Toast.makeText(this, "네트워크에 연결되지 않았습니다. 인터넷 연결을 확인해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "LOADING", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun promptEnableBluetooth() {
        startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
    }

    private fun checkAndRequestPermissions(): Boolean {
        val permissionsNeeded = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT)
            }

            // Check for exact alarm permission (if needed)
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                // We cannot directly request this permission, but we should add a user prompt
                requestExactAlarmPermission()
            }
        }

        // Request notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && PackageManager.PERMISSION_DENIED == ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )
        ) {
            permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Request location permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        return if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsNeeded.toTypedArray(),
                AppStart.PERMISSION_REQUEST_CODE
            )
            false
        } else {
            true
        }
    }

    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlertDialog.Builder(this)
                .setTitle("정확한 알람 권한 요청")
                .setMessage("앱이 정확한 알람을 설정할 수 있도록 권한이 필요합니다.")
                .setPositiveButton("권한 설정하기") { _, _ ->
                    val intent = Intent().apply {
                        action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    }
                    startActivity(intent)
                }
                .setNegativeButton("취소", null)
                .show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == com.example.hicure.AppStart.PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                    if (!alarmManager.canScheduleExactAlarms()) {
                        requestExactAlarmPermission()
                    }
                }
            } else {
                Toast.makeText(this, "권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Touch Screen
    private fun handleTouchEvent(event: MotionEvent?) {

        if (isUserLoggedIn) {
            val nextActivity = if (isSurvey) MainActivity::class.java else InitialSurvey::class.java
            startActivity(Intent(this, nextActivity))
            finish()
        } else {
            showCustomDialog()
        }
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
                    userRef.child(newId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    Toast.makeText(
                                        this@AppStart,
                                        "이미 존재하는 계정입니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                } else {

                                    val user = User().apply {
                                        name = "New User"
                                        age = 0
                                        gender = "Unknown"
                                        survey = false
                                    }
                                    userRef.child(newId).setValue(user)
                                        .addOnSuccessListener {
                                            Log.d("AppStart", "Add Success")
                                            Toast.makeText(
                                                this@AppStart,
                                                "새로운 계정(${newId}) 생성 완료",
                                                Toast.LENGTH_SHORT
                                            )
                                                .show()

                                        }
                                        .addOnFailureListener { e ->
                                            Log.d("AppStart", "Add Failure", e)
                                        }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("AppStart", "Database Error", error.toException())
                            }
                        })
                } else {
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

                        userRef.child(id).child("survey").get()
                            .addOnSuccessListener { surveySnapshot ->
                                val isSurvey = surveySnapshot.getValue(Boolean::class.java) ?: false

                                val intent = if (isSurvey) {
                                    Intent(this@AppStart, MainActivity::class.java)
                                } else {
                                    Intent(this@AppStart, InitialSurvey::class.java)
                                }
                                alertDialog.dismiss()
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Log.e("AppStart", "Failed to get survey value", e)
                                // 에러 처리 로직 (필요한 경우)
                            }
                    }
                } else {
                    contentTextView.text = "올바르지 않은 식별코드입니다."
                    contentTextView.setTextColor(resources.getColor(R.color.warning, null))
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

    private fun loadUserFromPreferences(onComplete: () -> Unit) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)

        if (userId != null) {

            // Check if the user ID exists in Firebase
            userRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val editor = sharedPreferences.edit()

                    if (snapshot.exists()) {

                        isUserLoggedIn = true
                        val user = snapshot.getValue(User::class.java)
                        user?.let {
                            // Update shared preferences with user data from Firebase
                            editor.putString("user_name", it.name)
                            editor.putInt("user_age", it.age)
                            editor.putString("user_gender", it.gender)
                            editor.putBoolean("user_survey", it.survey)
                            editor.apply()

                            if (it.survey) {
                                isSurvey = true
                            }
                        }

                    } else {
                        // User ID does not exist in Firebase
                        editor.putString("user_id", "")
                    }
                    onComplete()
                }

                // Error database
                override fun onCancelled(error: DatabaseError) {
                    Log.e("AppStart", "Database Error", error.toException())
                    isUserLoggedIn = false
                    onComplete()
                }
            })
        } else {
            isUserLoggedIn = false
            onComplete()
        }
    }

    private fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }
}