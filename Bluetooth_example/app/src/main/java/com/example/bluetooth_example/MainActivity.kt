package com.example.bluetooth_example

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.InputStream
import java.io.OutputStream


class MainActivity : AppCompatActivity() {
    private val REQUEST_ENABLE_BT = 3

    var mDevices: Set<BluetoothDevice>? = null
    var mPairedDeviceCount = 0
    var mRemoteDevice: BluetoothDevice? = null
    var mSocket: BluetoothSocket? = null
    var mInputStream: InputStream? = null
    var mOutputStream: OutputStream? = null
    var mWorkerThread: Thread? = null
    var readBufferPositon = 0
//    var readBuffer: ByteArray =
    var mDelimiter: Byte = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("#main", "on create")

        val stateFilter = IntentFilter()
        stateFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED) //BluetoothAdapter.ACTION_STATE_CHANGED : 블루투스 상태변화 액션

        stateFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        stateFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED) //연결 확인

        stateFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED) //연결 끊김 확인

        stateFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        stateFilter.addAction(BluetoothDevice.ACTION_FOUND) //기기 검색됨

        stateFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED) //기기 검색 시작

        stateFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED) //기기 검색 종료

        stateFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST)
        registerReceiver(mBluetoothStateReceiver, stateFilter)

        var mBluetoothAdapter: BluetoothAdapter? = null
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()   //블루투스 adapter 획득
        mBluetoothAdapter.startDiscovery() //블루투스 기기 검색 시작
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mBluetoothStateReceiver);
    }

    var mBluetoothStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action //입력된 action
            Toast.makeText(context, "받은 액션 : $action", Toast.LENGTH_SHORT).show()
            Log.d("Bluetooth action", action!!)
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
                    Log.d("#main_device", device_name + " " + device_Address)
                    //본 함수는 블루투스 기기 이름의 앞글자가 "GSM"으로 시작하는 기기만을 검색하는 코드이다
//                    if (device_name != null && device_name.length > 4) {
//                        Log.d("Bluetooth Name: ", device_name)
//                        Log.d("Bluetooth Mac Address: ", device_Address)
//                        if (device_name.substring(0, 3) == "GSM") {
//                            bluetooth_device.add(device)
//                        }
//                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d("Bluetooth", "Call Discovery finished")
//                    StartBluetoothDeviceConnection() //원하는 기기에 연결
                }
                BluetoothDevice.ACTION_PAIRING_REQUEST -> {
                }
            }
        }
    }
}
