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
        devices.add(device)
        notifyItemInserted(devices.size - 1)
    }

    fun addDevices(newDevices: List<ScanResult>) {
        val startPosition = devices.size
        devices.addAll(newDevices)
        notifyItemRangeInserted(startPosition, newDevices.size)
    }

    class DeviceViewHolder(private val binding: DeviceInfoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(device: ScanResult) {
            @SuppressLint("MissingPermission")
            binding.bleName.text = device.device.name ?: "Unknown Device"
            binding.bleAddress.text = device.device.address
        }
    }
}