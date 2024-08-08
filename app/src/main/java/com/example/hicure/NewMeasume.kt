package com.example.hicure

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.hicure.databinding.ActivityNewMeasumeBinding
import java.util.UUID

class NewMeasume : AppCompatActivity() {

    private lateinit var binding: ActivityNewMeasumeBinding

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothGatt: BluetoothGatt? = null
    private val batteryLevelUuid = UUID.fromString("00002A19-0000-1000-8000-00805F9B34FB")
    private val descriptorUuid = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")

    companion object {
        private const val TAG = "NewMeasume"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewMeasumeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val deviceName = intent.getStringExtra("EXTRA_DEVICE_NAME")
        val deviceAddress = intent.getStringExtra("EXTRA_DEVICE_ADDRESS")

        binding.bleName.text = deviceName ?: "Unknown Device"
        binding.bleAddress.text = deviceAddress ?: "Unknown Address"

        if (deviceAddress != null) {
            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothAdapter = bluetoothManager.adapter

            val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
            connectToDevice(device)
        } else {
            Log.e(TAG, "Device address is null")
            binding.connectStatus.text = "Device address is null"
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {
        bluetoothGatt = device.connectGatt(this, false, gattCallback)
    }

    @SuppressLint("MissingPermission")
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            runOnUiThread {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i(TAG, "Connected to GATT server.")
                    binding.connectStatus.text = "CONNECT"
                    bluetoothGatt?.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.i(TAG, "Disconnected from GATT server.")
                    binding.connectStatus.text = "DISCONNECT"
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            runOnUiThread {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i(TAG, "Services discovered.")
                    startBatteryLevelUpdates()
                } else {
                    Log.w(TAG, "onServicesDiscovered received: $status")
                }
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            // This method is not used for notifications
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            runOnUiThread {
                if (characteristic?.uuid == batteryLevelUuid) {
                    val batteryLevel = characteristic!!.value?.get(0)?.toInt() ?: -1
                    Log.i(TAG, "Battery level changed: $batteryLevel")
                    binding.batteryLevel.text = "Battery Level: $batteryLevel%"
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startBatteryLevelUpdates() {
        bluetoothGatt?.let { gatt ->
            val batteryService: BluetoothGattService? = gatt.services.find { service ->
                service.characteristics.any { characteristic ->
                    characteristic.uuid == batteryLevelUuid
                }
            }
            batteryService?.getCharacteristic(batteryLevelUuid)?.let { characteristic ->
                // Enable notifications
                gatt.setCharacteristicNotification(characteristic, true)
                val descriptor = characteristic.getDescriptor(descriptorUuid)
                descriptor?.let {
                    it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(it)
                }
            } ?: Log.w(TAG, "Battery level characteristic not found")
        } ?: Log.w(TAG, "Battery service not found")
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        bluetoothGatt?.let { gatt ->
            gatt.services.forEach { service ->
                service.characteristics.forEach { characteristic ->
                    if (characteristic.uuid == batteryLevelUuid) {
                        gatt.setCharacteristicNotification(characteristic, false)
                    }
                }
            }
            bluetoothGatt?.close()
        }
        bluetoothGatt = null
    }
}