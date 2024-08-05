package com.example.hicure

import android.R
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.SystemClock

import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.UnsupportedEncodingException
import java.util.UUID

class NewMeasume : AppCompatActivity() {
    var mTvBluetoothStatus: TextView? = null
    var mTvReceiveData: TextView? = null
    var mTvSendData: TextView? = null
    var mBtnBluetoothOn: Button? = null
    var mBtnBluetoothOff: Button? = null
    var mBtnConnect: Button? = null
    var mBtnSendData: Button? = null
}