package com.example.hicure

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hicure.databinding.ActivityBleConnectBinding

class BleConnect : AppCompatActivity() {

    private val binding: ActivityBleConnectBinding by lazy { ActivityBleConnectBinding.inflate(layoutInflater) }
    private lateinit var deviceAdapter: DeviceAdapter
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private val handler = Handler(Looper.getMainLooper())
    private var isScanning = false

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
        private const val SCAN_PERIOD: Long = 10000 // 10 seconds
        private const val TAG = "BleConnect"
    }

    @SuppressLint("ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        Log.d(TAG, "onCreate: Activity started")

        setupRecyclerView()
        updatePermissionState()

        binding.btnScan.setOnClickListener {
            if (checkAndRequestPermissions()) {
                startScan()
            }
        }

        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as? BluetoothManager
        if (bluetoothManager != null) {
            bluetoothLeScanner = bluetoothManager.adapter.bluetoothLeScanner
        } else {
            Log.e(TAG, "onCreate: BluetoothManager is null")
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupRecyclerView() {
        deviceAdapter = DeviceAdapter()
        binding.deviceList.layoutManager = LinearLayoutManager(this)
        binding.deviceList.adapter = deviceAdapter
    }

    private fun updatePermissionState() {
        val permissionsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }

        binding.permissionState.text = if (permissionsGranted) "권한 허용됨" else "권한 필요"
    }

    private fun checkAndRequestPermissions(): Boolean {
        val permissionsNeeded = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        return if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), PERMISSION_REQUEST_CODE)
            false
        } else {
            true
        }
    }

    private fun startScan() {
        scanLeDevice(true)
    }

    @SuppressLint("MissingPermission")
    private fun scanLeDevice(enable: Boolean){
        when(enable){
            true -> {
                handler.postDelayed({
                    isScanning = false
                    bluetoothLeScanner.stopScan(mScanCallback)
                    handler.post{
                        Toast.makeText(this,"스캔 종료", Toast.LENGTH_SHORT).show()
                    }
                }, SCAN_PERIOD)
                isScanning = true
                bluetoothLeScanner.startScan(mScanCallback)
                Toast.makeText(this, "스캔 시작", Toast.LENGTH_SHORT).show()
            }
            else -> {
                isScanning = false
                bluetoothLeScanner.stopScan(mScanCallback)
            }
        }
    }

    private val mScanCallback = object : ScanCallback(){
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            Log.d(TAG, "onScanResult result: $result")
            deviceAdapter.addDevice(result)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            Log.d(TAG, "onBatchScanResults: $results")
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d(TAG, "onScanResult errorCode: $errorCode")
            Toast.makeText(this@BleConnect, "스캔 실패: $errorCode", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            updatePermissionState()
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startScan()
            } else {
                Toast.makeText(this, "권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}