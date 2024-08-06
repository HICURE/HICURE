package com.example.hicure

import android.Manifest
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.hicure.databinding.DeviceInfoBinding

class DeviceAdapter : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    private val devices = mutableListOf<ScanResult>()
    private val deviceAddresses = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = DeviceInfoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeviceViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(devices[position])
    }

    fun addDevice(device: ScanResult) {
        val deviceAddress = device.device.address
        if (!deviceAddresses.contains(deviceAddress)) {
            devices.add(device)
            deviceAddresses.add(deviceAddress)
            notifyItemInserted(devices.size - 1)
        }
    }

    fun clearDevices() {
        devices.clear()
        notifyDataSetChanged()
    }

    fun addDevices(newDevices: List<ScanResult>) {
        val uniqueNewDevices = newDevices.filter { !deviceAddresses.contains(it.device.address) }
        devices.addAll(uniqueNewDevices)
        uniqueNewDevices.forEach { deviceAddresses.add(it.device.address) }
        notifyItemRangeInserted(devices.size - uniqueNewDevices.size, uniqueNewDevices.size)
    }

    class DeviceViewHolder(private val binding: DeviceInfoBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("MissingPermission")
        fun bind(device: ScanResult) {
            binding.bleName.text = device.device.name ?: "Unknown Device"
            binding.bleAddress.text = device.device.address
        }
    }
}