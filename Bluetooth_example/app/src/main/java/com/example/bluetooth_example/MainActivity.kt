package com.example.bluetooth_example

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
import androidx.appcompat.app.AppCompatActivity
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import kotlin.text.Charsets.UTF_8


// http://jinyongjeong.github.io/2018/09/27/bluetoothpairing/

class MainActivity : AppCompatActivity() {
    private val REQUEST_ENABLE_BT = 3

    var hc_06: BluetoothDevice? = null

    var mPairedDeviceCount = 0
    var mRemoteDevice: BluetoothDevice? = null
    var mSocket: BluetoothSocket? = null
    var mInputStream: InputStream? = null
    var mOutputStream: OutputStream? = null
    var mWorkerThread: Thread? = null
    var readBufferPositon = 0
    var mDelimiter: Byte = 10
    var mBluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

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


//        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()   //블루투스 adapter 획득
        findViewById<Button>(R.id.btn_bt_scan).setOnClickListener{
            mBluetoothAdapter!!.startDiscovery() //블루투스 기기 검색 시작
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mBluetoothStateReceiver);
    }

    var mBluetoothStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
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
                    if (device_name == "HC-06") {
                        hc_06 = device
                        device.createBond()
                        val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
                        try {
                            // 소켓 생성
                            mSocket = hc_06!!.createRfcommSocketToServiceRecord(uuid)
                            // RFCOMM 채널을 통한 연결, socket에 connect하는데 시간이 걸린다. 따라서 ui에 영향을 주지 않기 위해서는
                            // Thread로 연결 과정을 수행해야 한다.
                            mSocket!!.connect()
                            mHandler.sendEmptyMessage(1)
                        } catch (e: java.lang.Exception) {
                            // 블루투스 연결 중 오류 발생
                            mHandler.sendEmptyMessage(-1)
                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d("#main_Bluetooth", "Call Discovery finished")
                }
                BluetoothDevice.ACTION_PAIRING_REQUEST -> {
                }
            }
        }
    }

    val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (msg.what === 1) // 연결 완료
            {
                try {
                    //연결이 완료되면 소켓에서 outstream과 inputstream을 얻는다. 블루투스를 통해
                    //데이터를 주고 받는 통로가 된다.
                    mOutputStream = mSocket!!.outputStream
                    mInputStream = mSocket!!.inputStream
                    // 데이터 수신 준비
                    beginListenForData()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            } else {    //연결 실패
                Log.e("#main", "no")
                try {
                    mSocket!!.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    //블루투스 데이터 수신 Listener
    protected fun beginListenForData() {
        val handler = Handler(Looper.getMainLooper())
        val readBuffer = ByteArray(1024) //  수신 버퍼
        readBufferPositon = 0 //   버퍼 내 수신 문자 저장 위치

        // 문자열 수신 쓰레드
        mWorkerThread = Thread {
            while (!Thread.currentThread().isInterrupted) {
                try {
                    val bytesAvailable = mInputStream!!.available()
                    if (bytesAvailable > 0) { //데이터가 수신된 경우
                        val packetBytes = ByteArray(bytesAvailable)
                        mInputStream!!.read(packetBytes)
                        for (i in 0 until bytesAvailable) {
                            val b = packetBytes[i]
                            if (b == mDelimiter) {
                                val encodedBytes = ByteArray(readBufferPositon)
                                System.arraycopy(
                                    readBuffer,
                                    0,
                                    encodedBytes,
                                    0,
                                    encodedBytes.size
                                )
                                val data = String(encodedBytes, UTF_8)
                                readBufferPositon = 0
                                handler.post(Runnable {
                                    //수신된 데이터는 data 변수에 string으로 저장!! 이 데이터를 이용하면 된다.
                                    val c_arr = data.toCharArray() // char 배열로 변환
                                    Log.d("#main", data)
                                })
                            } else {
                                readBuffer[readBufferPositon] = b
                                readBufferPositon += 1
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        //데이터 수신 thread 시작
        mWorkerThread!!.start()
    }
}
