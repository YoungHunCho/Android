package com.example.bluetooth_example

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import kotlin.text.Charsets.UTF_8


// http://jinyongjeong.github.io/2018/09/27/bluetoothpairing/

class MainActivity : AppCompatActivity() {
    private val DEVICE_NAME = "HC-06"
    private var status = 0
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private val stateFilter = IntentFilter()
    private var tv_status: TextView? = null
    private var pairedDevices: Set<BluetoothDevice>? = null
    private val REQUEST_ENABLE_BT = 1000

    private val mBluetoothStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action //입력된 action
            Log.d("#main_Bluetooth action", action!!)
            val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
            var name: String? = null
            if (device != null) {
                name = device.name //broadcast를 보낸 기기의 이름을 가져온다.
            }
            when (action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state =
                        intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    when (state) {
                        BluetoothAdapter.STATE_OFF -> {
                        }
                        BluetoothAdapter.STATE_TURNING_OFF -> {
                        }
                        BluetoothAdapter.STATE_ON -> {
                        }
                        BluetoothAdapter.STATE_TURNING_ON -> {
                        }
                    }
                }
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                }
                BluetoothDevice.ACTION_FOUND -> {
                    val device_name = device!!.name
                    val device_Address = device.address
                    Log.d("#main_device", "$device_name $device_Address")

                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d("#main_Bluetooth", "Call Discovery finished")
                }
                BluetoothDevice.ACTION_PAIRING_REQUEST -> {
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("#main", "on create")

        init()


        registerReceiver(mBluetoothStateReceiver, stateFilter)


//        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()   //블루투스 adapter 획득



    }

    private fun init(){
        // init filter
        stateFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED) //BluetoothAdapter.ACTION_STATE_CHANGED : 블루투스 상태변화 액션

        stateFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        stateFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED) //연결 확인

        stateFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED) //연결 끊김 확인

        stateFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        stateFilter.addAction(BluetoothDevice.ACTION_FOUND) //기기 검색됨

        stateFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED) //기기 검색 시작

        stateFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED) //기기 검색 종료

        stateFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST)

        // init bt
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        // init tv, btn
        tv_status = findViewById(R.id.tv_bt_status)

        if (mBluetoothAdapter == null) set_status(0)
        if (mBluetoothAdapter?.isEnabled == false) set_status(1)
        else set_status(2)

        pairedDevices = mBluetoothAdapter?.bondedDevices
        pairedDevices?.forEach { device ->
            val deviceName = device.name
            val deviceHardwareAddress = device.address // MAC address
            if (deviceName == DEVICE_NAME) set_status(4)
        }

        findViewById<Button>(R.id.btn_on_off).setOnClickListener{
            if (status == 1){
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
        }

        findViewById<Button>(R.id.btn_bt_scan).setOnClickListener{
            if (status == 2) {
                mBluetoothAdapter!!.startDiscovery() //블루투스 기기 검색 시작
            }
        }

        findViewById<Button>(R.id.btn_rcv).setOnClickListener{
//            mBluetoothAdapter!!.startDiscovery() //블루투스 기기 검색 시작
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mBluetoothStateReceiver);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            REQUEST_ENABLE_BT -> {
                if (resultCode == Activity.RESULT_OK) set_status(2)
            }
        }
    }

    private fun set_status(stat: Int){
        status = stat
        when(stat){
            0 -> tv_status!!.text = "이 기기는 블루투스를 지원하지 않습니다."
            1 -> tv_status!!.text = "블루투스 OFF"
            2 -> tv_status!!.text = "블루투스 ON"
            3 -> tv_status!!.text = "페어링 필요"
            4 -> tv_status!!.text = "페어링 완료"
        }
    }
}
