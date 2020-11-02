package com.example.bluetooth_example

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("#main", "on create")

        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }

        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 1000)
        }

//        when (PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
//            PackageManager.PERMISSION_GRANTED -> bluetoothLeScanner.startScan(bleScanner)
//            else -> requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1)
//        }

        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevices?.forEach { device ->
            val deviceName = device.name
            val deviceHardwareAddress = device.address // MAC address
            Log.e("#main_conneted", deviceName + deviceHardwareAddress)
        }

//        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
//        registerReceiver(receiver, filter)
//
//        val ret = bluetoothAdapter!!.startDiscovery()
//        Log.e("#main", ret.toString())

        scanLeDevice(true)
    }

    override fun onDestroy() {
        super.onDestroy()
//        unregisterReceiver(receiver)
    }

    private var mScanning: Boolean = false
    private var arrDevices = ArrayList<BluetoothDevice>()
    private val handler = Handler(Looper.getMainLooper())
    private val scanCallback = object: ScanCallback(){
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("#main", "scan failed " + errorCode)
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let{
                if(!arrDevices.contains(it.device)) {
                    arrDevices.add(it.device)
                    Log.d("#main", it.device.toString() + " " + it.device.name)
                    Log.d("#main", it.scanRecord!!.deviceName.toString())
                }
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            results?.let{
                for (result in it){
                    if(!arrDevices.contains(result.device)) arrDevices.add(result.device)
                }
            }
        }
    }
    private val SCAN_PERIOD: Long = 10000
    private fun scanLeDevice(enable: Boolean){
        when (enable){
            true -> {
                handler.postDelayed({
                    mScanning = false
                    bluetoothAdapter!!.bluetoothLeScanner.stopScan(scanCallback)
                }, SCAN_PERIOD)
                mScanning = true
                arrDevices.clear()
                bluetoothAdapter!!.bluetoothLeScanner.startScan(scanCallback)
            }
            else ->{
                mScanning = false
                bluetoothAdapter!!.bluetoothLeScanner.startScan(scanCallback)
            }
        }
    }
}
