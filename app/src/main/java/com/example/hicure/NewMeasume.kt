package com.example.hicure

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import com.example.hicure.databinding.ActivityNewMeasumeBinding
import java.util.UUID

class NewMeasume : AppCompatActivity() {

    private lateinit var binding: ActivityNewMeasumeBinding

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothGatt: BluetoothGatt? = null

    private val writeUuid = UUID.fromString("BEF8D6C9-9C21-4C9E-B632-BD58C1009F9F")
    private val vitalCapacityUuid = UUID.fromString("CBA1D466-344C-4BE3-AB3F-189F80DD7518")
    private val batteryLevelUuid = UUID.fromString("00002A19-0000-1000-8000-00805F9B34FB")

    companion object {
        private const val TAG = "NewMeasume"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewMeasumeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val deviceName = intent.getStringExtra("EXTRA_DEVICE_NAME")
        val deviceAddress = intent.getStringExtra("EXTRA_DEVICE_ADDRESS")

        if (deviceAddress != null) {
            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothAdapter = bluetoothManager.adapter

            val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
            connectToDevice(device)
        } else {
            Log.e(TAG, "Device address is null")
            binding.connectStatus.text = "Device address is null"
        }

        binding.start.setOnClickListener {
            writeToCharacteristic("START".toByteArray())
        }

        binding.stop.setOnClickListener {
            writeToCharacteristic("STOP".toByteArray())
        }

        "$deviceName".also { binding.actionTitle.text = it }

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
                    binding.connectStatus.text = "연결된 상태입니다."
                    binding.connectStatus.setTextColor(resources.getColor(R.color.edge_blue, null))
                    bluetoothGatt?.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.i(TAG, "Disconnected from GATT server.")
                    binding.connectStatus.text = "연결이 해제되었습니다."
                    binding.connectStatus.setTextColor(resources.getColor(R.color.warning, null))
                    binding.line.setBackgroundColor(resources.getColor(R.color.warning, null))
                    binding.batteryBar.progress = 0
                    binding.batteryLevel.text = "???"
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            runOnUiThread {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i(TAG, "Services discovered.")
                    startBatteryLevelUpdates()
                    startVitalCapacityUpdates()
                } else {
                    Log.w(TAG, "onServicesDiscovered received: $status")
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            runOnUiThread {
                when (characteristic?.uuid) {
                    batteryLevelUuid -> {
                        val batteryLevel = characteristic?.value?.get(0)?.toInt() ?: -1
                        Log.i(TAG, "Battery level changed: $batteryLevel")
                        binding.batteryBar.progress = batteryLevel
                        binding.batteryLevel.text = "$batteryLevel%"
                    }
                    vitalCapacityUuid -> {
                        val vcValue = characteristic?.value?.let { String(it) }
                        Log.i(TAG, "Vital Capacity changed: $vcValue")
                        binding.VC.text = "Vital Capacity: $vcValue"
                    }
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
                gatt.setCharacteristicNotification(characteristic, true)
                val descriptor = characteristic.getDescriptor(batteryLevelUuid)
                descriptor?.let {
                    it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(it)
                }
            } ?: Log.w(TAG, "Battery level characteristic not found")
        } ?: Log.w(TAG, "Battery service not found")
    }

    @SuppressLint("MissingPermission")
    private fun startVitalCapacityUpdates() {
        bluetoothGatt?.let { gatt ->
            val vcService: BluetoothGattService? = gatt.services.find { service ->
                service.characteristics.any { characteristic ->
                    characteristic.uuid == vitalCapacityUuid
                }
            }
            vcService?.getCharacteristic(vitalCapacityUuid)?.let { characteristic ->
                gatt.setCharacteristicNotification(characteristic, true)
                val descriptor = characteristic.getDescriptor(vitalCapacityUuid)
                descriptor?.let {
                    it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(it)
                }
            } ?: Log.w(TAG, "Vital Capacity characteristic not found")
        } ?: Log.w(TAG, "Vital Capacity service not found")
    }

    @SuppressLint("MissingPermission")
    private fun writeToCharacteristic(value: ByteArray) {
        bluetoothGatt?.let { gatt ->
            val writeService: BluetoothGattService? = gatt.services.find { service ->
                service.characteristics.any { characteristic ->
                    characteristic.uuid == writeUuid
                }
            }
            writeService?.getCharacteristic(writeUuid)?.let { characteristic ->
                characteristic.value = value
                gatt.writeCharacteristic(characteristic)
            } ?: Log.w(TAG, "Write characteristic not found")
        } ?: Log.w(TAG, "Write service not found")
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        bluetoothGatt?.let { gatt ->
            gatt.services.forEach { service ->
                service.characteristics.forEach { characteristic ->
                    if (characteristic.uuid == batteryLevelUuid || characteristic.uuid == vitalCapacityUuid) {
                        gatt.setCharacteristicNotification(characteristic, false)
                    }
                }
            }
            bluetoothGatt?.close()
        }
        bluetoothGatt = null
    }
}