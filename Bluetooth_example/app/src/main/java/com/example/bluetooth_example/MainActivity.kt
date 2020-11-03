package com.example.bluetooth_example

//import jdk.nashorn.internal.objects.ArrayBufferView.buffer
//import sun.security.krb5.Confounder.bytes
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


// http://jinyongjeong.github.io/2018/09/27/bluetoothpairing/

class MainActivity : AppCompatActivity() {
    private val DEVICE_NAME = "HC-06"
    private var DEVICE: BluetoothDevice? = null
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
                    if (device_name == DEVICE_NAME) {
                        DEVICE = device
                    }
                    Log.d("#main_device", "$device_name $device_Address")

                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    if (DEVICE != null) {
                        DEVICE!!.createBond()
                        set_status(4)
                    }
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
            if (deviceName == DEVICE_NAME) {
                set_status(4)
                DEVICE = device
            }
        }
//        val BluetoothDevice
//        pairedDevices.contains()

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
//            start_rcv()
            val intent: Intent = Intent(this, ReceiveActivity::class.java)
        intent.putExtra("device", DEVICE)
            startActivity(intent)
        }

        createNotificationChannel()
    }

    // https://developer.android.com/guide/topics/connectivity/bluetooth?hl=ko#ConnectDevices
    private var mBtSoket: BluetoothSocket? = null
    private var mInput: InputStream? = null
    private var mOutput: OutputStream? = null
    private var mmBuffer: ByteArray = ByteArray(555)





    private fun start_rcv(){
        Log.d("#main_rcv", "start")
        val uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

        try{
            mBtSoket = DEVICE!!.createRfcommSocketToServiceRecord(uuid)
            mBtSoket!!.connect()
            Log.d("#main_rcv", "socket connect")

            mInput = mBtSoket!!.inputStream
            mOutput = mBtSoket!!.outputStream
        }catch (e: Exception){
            e.printStackTrace()
        }

        Thread(Runnable {
            var numBytes: Int
            var str: String = ""
            while (true) {
                try {
                    numBytes = mInput!!.read(mmBuffer, 0, 512)
                    val readMessage: String = String(mmBuffer, 0, numBytes)
                    str += readMessage
                    if (str[str.lastIndex] == '^') {
                        str = str.substring(0, str.lastIndex)
                        Log.d("#main_rcv", str)
                        if (str.subSequence(0, 6).toString().trim().toInt() % 10 == 0) {
                            NotificationSomethings(str.substring(7,str.lastIndex))
                        }
                        str = ""
                    }
//                    Log.d("#main_rcv", "$numBytes $readMessage")
                } catch (e: IOException) {
                    Log.d("#main_rcv", "Input stream was disconnected", e)
                    break
                }
            }
        }).start()
    }

    // https://developer.android.com/training/notify-user/build-notification?hl=ko
    private val CHANNEL_ID = "cho"
    private fun NotificationSomethings(str: String) {
        var builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_search)
            .setContentTitle("test")
            .setContentText(str)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)


        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(10, builder.build())
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mBluetoothStateReceiver);
        try{
            mBtSoket!!.close()
        }catch (e: Exception){
            e.printStackTrace()
        }
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
