package com.example.hicure

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView

class BluetoothRepository(private val context: Context) : RecyclerView.Adapter<BluetoothRepository.BleViewHolder>() {

    private var items: ArrayList<BluetoothDevice>? = ArrayList()
    private lateinit var itemClickListener: ItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BleViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.rv_ble_item, parent, false)
        return BleViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: BleViewHolder, position: Int) {
        val currentDevice = items?.get(position)
        holder.bind(currentDevice)
        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, currentDevice)
        }
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    fun setItems(item: ArrayList<BluetoothDevice>?) {
        if (item == null) return
        items = item
        notifyDataSetChanged()
    }

    inner class BleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val bleName: TextView = itemView.findViewById(R.id.ble_name)
        private val bleAddress: TextView = itemView.findViewById(R.id.ble_address)

        fun bind(currentDevice: BluetoothDevice?) {
            if (currentDevice != null) {
                bleName.text = getDeviceName(currentDevice)
                bleAddress.text = currentDevice.address ?: "Unknown Address"
            } else {
                bleName.text = "Unknown Device"
                bleAddress.text = "Unknown Address"
            }
        }

        private fun getDeviceName(device: BluetoothDevice): String {
            return if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                device.name ?: "Unknown Device"
            } else {
                "Permission Denied"
            }
        }
    }

    interface ItemClickListener {
        fun onClick(view: View, device: BluetoothDevice?)
    }

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }
}
