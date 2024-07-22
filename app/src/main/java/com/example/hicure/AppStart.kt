package com.example.hicure

import android.app.ActionBar
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.hicure.databinding.ActivityAppStartBinding

class AppStart : AppCompatActivity() {

    val binding: ActivityAppStartBinding by lazy {ActivityAppStartBinding.inflate(layoutInflater)}

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
        when (event?.action){
            MotionEvent.ACTION_DOWN ->{
                showCustomDialog()
            }
        }
        return super.onTouchEvent(event)
    }

    // If id is not checked, go to the check-id-activity

    private fun showCustomDialog(){
        val dialogView = LayoutInflater.from(this).inflate(R.layout.activity_check_id, null)

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val dialogTitle = dialogView.findViewById<TextView>(R.id.Title)
        val dialogContent = dialogView.findViewById<TextView>(R.id.content)
        val dialogEditText = dialogView.findViewById<TextView>(R.id.edit_text)
        val dialogExit = dialogView.findViewById<Button>(R.id.exit_button)
        val dialogButton = dialogView.findViewById<Button>(R.id.checkButton)

        dialogTitle.text = "어플 사용 전 확인"
        dialogEditText.hint = "식별코드를 입력해주세요."
        dialogContent.text = "식별코드는 안내지에 기입되어 있습니다!"
        dialogButton.text = "확인"

        dialogExit.setOnClickListener{
            alertDialog.dismiss()
        }

        dialogButton.setOnClickListener{
            // need to add function for check id
            alertDialog.dismiss()
            val intent = Intent(this, Survey::class.java)
            startActivity(intent)
            finish()
        }
    }

    // If id is checked, go to the main-activity

}