package com.example.bluetooth_example

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_receive_list.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class ReceiveActivity : AppCompatActivity() {
    private var DEVICE: BluetoothDevice? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private var ItemList = arrayListOf<Item>(
//        Item(0.0, 1.0, 2.0, 3.0, 4.0)
    )

    private var mAdapter: RvAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receive_list)

        mAdapter = RvAdapter(this, ItemList)
        rcv_list.adapter = mAdapter
        val lm = LinearLayoutManager(this)
        rcv_list.layoutManager = lm
        rcv_list.setHasFixedSize(true)

        DEVICE = intent.extras!!.getParcelable<BluetoothDevice>("device")

        start_rcv()

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
            Log.d("#main_rcv", "123")
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
                        Log.d("#main_rcv", str)
                        str = str.substring(0, str.lastIndex)
                        val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        val items = str.split("$")[1].split(",")

                        ItemList.add(Item(items[1].toDouble(), items[2].toDouble(), items[3].toDouble(), items[4].toDouble(), time))
                        runOnUiThread{
                            mAdapter!!.notifyDataSetChanged()
                            rcv_list.scrollToPosition(ItemList.size - 1)
                        }
                        str = ""
                    }

                } catch (e: IOException) {
                    Log.d("#main_rcv", "Input stream was disconnected", e)
                    break
                }
            }
        }).start()
    }
}



class Item(flow: Double, meter: Double, lat: Double, lon: Double, time: String){
    var flow: Double = 0.0
    var meter: Double = 0.0
    var lat: Double = 0.0
    var lon: Double = 0.0
    var time: String = ""
    init {
        this.flow = flow
        this.meter = meter
        this.time = time
        this.lat = lat
        this.lon = lon
    }
}


class RvAdapter(val context: Context, val itemList: ArrayList<Item>): RecyclerView.Adapter<RvAdapter.Holder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(context).inflate(R.layout.rv_item, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder?.bind(itemList[position], context)
    }

    inner class Holder(itemView: View?): RecyclerView.ViewHolder(itemView!!){
        val time = itemView!!.findViewById<TextView>(R.id.time)
        val flow = itemView!!.findViewById<TextView>(R.id.flow)
        val meter = itemView!!.findViewById<TextView>(R.id.meter)
        val lat = itemView!!.findViewById<TextView>(R.id.lat)
        val lon = itemView!!.findViewById<TextView>(R.id.lon)

        fun bind(item: Item, context: Context){
            time.text = item.time
            flow.text = "flow: ${item.flow.toString()}"
            meter.text = "meter: ${item.meter.toString()}"
            lat.text = "lat: ${item.lat.toString()}"
            lon.text = "lon: ${item.lon.toString()}"
        }
    }
}