package com.example.hicure

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.hicure.databinding.ActivityNewMeasumeBinding
import com.example.hicure.databinding.CheckResultBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.util.ArrayList
import java.util.UUID

data class ResultData(
    var lableData: String = "",
    var lineData: Double = 0.0
)

class NewMeasume : AppCompatActivity() {


    lateinit var lineChart: LineChart
    private val resultData = ArrayList<ResultData>()

    private lateinit var binding: ActivityNewMeasumeBinding
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothGatt: BluetoothGatt? = null
    private var isDataProcessingEnabled = false
    private val vcValues = mutableListOf<String>()

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

        binding.button.setOnClickListener {
            writeToCharacteristic("START".toByteArray())
            binding.button.visibility = View.GONE
            binding.time.visibility = View.VISIBLE
            binding.progessBar.visibility = View.VISIBLE
            binding.help.text = "3초 후에 바람을 불어주세요."
            isDataProcessingEnabled = false
            vcValues.clear()

            // ready timer
            val firstTimer = object : CountDownTimer(3000, 10) {
                override fun onTick(millisUntilFinished: Long) {
                    val remainingTime = millisUntilFinished / 1000.0
                    binding.progessBar.max = 300
                    binding.time.text = String.format("%05.2f", remainingTime)
                    binding.progessBar.progress = ((3.0 - remainingTime) * 100).toInt()
                }

                override fun onFinish() {

                    isDataProcessingEnabled = true
                    binding.help.text = "강하게 한 번 불어주세요"

                    // recode timer
                    val secondTimer = object : CountDownTimer(5000, 10) {
                        override fun onTick(millisUntilFinished: Long) {
                            val remainingTime = millisUntilFinished / 1000.0
                            binding.progessBar.max = 500
                            binding.time.text = String.format("%05.2f", remainingTime)
                            binding.progessBar.progress = ((5.0 - remainingTime) * 100).toInt()
                        }

                        override fun onFinish() {
                            writeToCharacteristic("STOP".toByteArray())
                            binding.help.text = "버튼을 클릭해주세요"
                            binding.time.visibility = View.GONE
                            binding.progessBar.visibility = View.GONE
                            binding.button.visibility = View.VISIBLE
                            binding.VC.text = "--.--"
                            showResultDialog()
                        }
                    }
                    secondTimer.start()
                }
            }
            firstTimer.start()
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

    private fun showResultDialog() {
        val dialogBinding = CheckResultBinding.inflate(LayoutInflater.from(this))
        val dialogBuilder = AlertDialog.Builder(this).setView(dialogBinding.root)

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogBinding.Title.text = "측정 결과"

        lineChart = dialogBinding.root.findViewById(R.id.result_chart)

        setupLineChart()

        dialogBinding.exitButton.setOnClickListener {
            alertDialog.dismiss()
        }

        dialogBinding.reMeasure.setOnClickListener {
            alertDialog.dismiss()
        }

        dialogBinding.saveBtn.setOnClickListener {
            val result = vcValues.joinToString(", ")
            Toast.makeText(this, "측정 값: $result", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupLineChart() {
        resultData.clear()

        for (index in vcValues.indices) {
            val labelItem = String.format("%.1f", (index + 1) * 0.5) + "s"
            val dataItem = vcValues[index].toDoubleOrNull() ?: 0.0
            addResultItem(labelItem, dataItem)
        }

        val entries = mutableListOf<Entry>()

        for (item in resultData) {
            entries.add(
                Entry(
                    item.lableData.replace("[^\\d.]".toRegex(), "").toFloat(),
                    item.lineData.toFloat()
                )
            )
        }

        val lineDataSet = LineDataSet(entries, "")
        lineDataSet.color = Color.BLUE
        lineDataSet.setCircleColor(Color.DKGRAY)
        lineDataSet.setCircleHoleColor(Color.DKGRAY)

        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(lineDataSet)

        val data = LineData(dataSets)

        lineChart.data = data
        lineChart.description.isEnabled = false
        lineChart.invalidate()
    }

    private fun addResultItem(labelItem: String, dataItem: Double) {
        val item = ResultData(labelItem, dataItem)
        resultData.add(item)
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
                    binding.VC.text = "--.--"
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            runOnUiThread {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i(TAG, "Services discovered.")
                    startBatteryLevelUpdates()
                    val batteryCharacteristic =
                        gatt?.getService(batteryLevelUuid)?.getCharacteristic(batteryLevelUuid)
                    batteryCharacteristic?.let {
                        gatt.readCharacteristic(it)
                    }
                    startVitalCapacityUpdates()
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
            if (status == BluetoothGatt.GATT_SUCCESS && characteristic != null) {
                runOnUiThread {
                    when (characteristic.uuid) {
                        batteryLevelUuid -> {
                            val batteryLevel = characteristic.value?.get(0)?.toInt() ?: -1
                            Log.i(TAG, "Battery level read: $batteryLevel")
                            binding.batteryBar.progress = batteryLevel
                            binding.batteryLevel.text = "$batteryLevel%"
                        }
                    }
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
                        if (isDataProcessingEnabled) { // 데이터 처리 가능 시에만 VC 값을 업데이트합니다.
                            val vcValue =
                                characteristic?.value?.let { String(it).substringBefore("L/min") }
                            Log.i(TAG, "Vital Capacity changed: $vcValue")
                            binding.VC.text = "$vcValue"
                            vcValues.add(vcValue ?: "")
                        }
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
