package com.example.hicure

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hicure.databinding.ActivityBleConnectBinding

class BleConnect : AppCompatActivity(), OnDeviceClickListener {

    private val binding: ActivityBleConnectBinding by lazy {
        ActivityBleConnectBinding.inflate(
            layoutInflater
        )
    }
    private lateinit var deviceAdapter: DeviceAdapter
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private val handler = Handler(Looper.getMainLooper())
    private var isScanning = false

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
        private const val SCAN_PERIOD: Long = 2000 // 2 seconds
        private const val TAG = "BleConnect"
    }

    @SuppressLint("ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        Log.d(TAG, "onCreate: Activity started")

        "기기 연결하기".also { binding.actionTitle.text = it }

        binding.actionTitle.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.actionTitle.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val actionTextWidth = binding.actionTitle.width
                binding.actionTitle.width = actionTextWidth + 10

                val layoutParams = binding.behindTitle.layoutParams
                layoutParams.width = actionTextWidth + 30
                binding.behindTitle.layoutParams = layoutParams
            }
        })

        binding.description.text = "PeakFlowMeter 기기에 연결하기"

        setupRecyclerView()

        binding.btnScan.setOnClickListener {
            if (checkAndRequestPermissions()) {
                if (isBluetoothEnabled()) {
                    deviceAdapter.clearDevices()
                    startScan()
                } else {
                    promptEnableBluetooth()
                }
            }
        }
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as? BluetoothManager

        if (bluetoothManager != null) {
            bluetoothLeScanner = bluetoothManager.adapter.bluetoothLeScanner
        } else {
            Log.e(TAG, "onCreate: BluetoothManager is null")
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_SHORT)
                .show()
            finish()
        }
    }

    private fun isBluetoothEnabled(): Boolean {
        val bluetoothAdapter = (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter
        return bluetoothAdapter.isEnabled
    }

    @SuppressLint("MissingPermission")
    private fun promptEnableBluetooth() {
        startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
    }

    private fun setupRecyclerView() {
        deviceAdapter = DeviceAdapter(this)
        binding.deviceList.layoutManager = LinearLayoutManager(this)
        binding.deviceList.adapter = deviceAdapter
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
        }

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
                PERMISSION_REQUEST_CODE
            )
            false
        } else {
            true
        }
    }

    private fun startScan() {
        deviceAdapter.clearDevices()
        scanLeDevice(true)
    }

    @SuppressLint("MissingPermission")
    private fun scanLeDevice(enable: Boolean) {
        when (enable) {
            true -> {
                handler.postDelayed({
                    isScanning = false
                    bluetoothLeScanner.stopScan(mScanCallback)
                    handler.post {
                        binding.description.text = "PeakFlowMeter 기기에 연결하기"
                    }
                }, SCAN_PERIOD)
                isScanning = true
                bluetoothLeScanner.startScan(mScanCallback)
                binding.description.text = "스캔 중..."
            }

            else -> {
                isScanning = false
                bluetoothLeScanner.stopScan(mScanCallback)
            }
        }
    }

    private val mScanCallback = object : ScanCallback() {
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
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startScan()
            } else {
                Toast.makeText(this, "권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onDeviceClick(device: ScanResult) {
        val isConnectable = device.isConnectable

        if (isConnectable) {
            Toast.makeText(this, "This device is connectable.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "This device is not connectable.", Toast.LENGTH_SHORT).show()
        }

        Log.d(TAG, "Device clicked: ${device.device.name ?: "Unknown Device"} - Connectable: $isConnectable")
    }
}