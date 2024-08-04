package com.example.hicure

import android.Manifest
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class NewMeasume : AppCompatActivity() {
    private var scanResults: ArrayList<BluetoothDevice>? = ArrayList()
    private var bleAdapter: BluetoothAdapter? = BluetoothRepository
    private var bleGatt: BluetoothGatt? = null
    private val TAG = "NewMeasume"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_measume)
    }

    override fun onResume() {
        super.onResume()
        // finish app if the BLE is not supported
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            finish()
        }else{
            Log.d("NewMeasume", "BLE 지원 잘됩니다!")
        }
    }
    fun setBLEAdapter(){
        // ble manager
        val bleManager: BluetoothManager? = MyApplication.applicationContext().getSystemService( BLUETOOTH_SERVICE ) as BluetoothManager
        // set ble adapter
        bleAdapter?= bleManager?.adapter
    }
}