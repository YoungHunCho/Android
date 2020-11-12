package com.example.beacon_example


import android.os.Bundle
import android.os.RemoteException
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pedro.library.AutoPermissions.Companion.loadAllPermissions
import com.pedro.library.AutoPermissionsListener
import org.altbeacon.beacon.*


// https://github.com/int128/android-ble-button/blob/master/app/src/main/kotlin/org/hidetake/blebutton/ScanDevicesActivity.kt
// UUID 74278BDA-B644-4520-8F0C-720EAF059935
class MainActivity : AppCompatActivity(), BeaconConsumer, AutoPermissionsListener {
    private var beaconManager: BeaconManager? = null

    var beaconUUID = "AAC54CD6-EAAD-48D2-B060-AAAAAAAAE" // beacon -uuid
    private val TAG = "####"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loadAllPermissions(this, 101) // AutoPermissions


        beaconManager = BeaconManager.getInstanceForApplication(this)
        beaconManager!!.beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"))
        beaconManager!!.bind(this)
    }

    override fun onBeaconServiceConnect() {
        beaconManager!!.removeAllMonitorNotifiers()
        beaconManager!!.addRangeNotifier(RangeNotifier { beacons, region ->
            if (beacons.isNotEmpty()) {
                Log.i(TAG, "The first beacon I see is about ${(beacons.iterator().next() as Beacon).bluetoothAddress} ${(beacons.iterator().next() as Beacon).bluetoothName}" +
                        " ${(beacons.iterator().next() as Beacon).id1} ${(beacons.iterator().next() as Beacon).id2}" +
                        " ${(beacons.iterator().next() as Beacon).id3}" +
                        " ${(beacons.iterator().next() as Beacon).distance}" )
            }
        })
//        beaconManager!!.addRangeNotifier(RangeNotifier{
//            fun didRangeBeaconsInRegion(beacons: Collection<*>, region: Region?) {
//
//            }
//        })
        beaconManager!!.addMonitorNotifier(object : MonitorNotifier {
            override fun didEnterRegion(region: Region?) {
                Log.i(TAG, "I just saw an beacon for the first time!")
                Toast.makeText(this@MainActivity, "didEnterRegion - 비콘 연결됨", Toast.LENGTH_SHORT).show()
            }

            override fun didExitRegion(region: Region?) {
                Log.i(TAG, "I no longer see an beacon")
                Toast.makeText(this@MainActivity, "didExitRegion - 비콘 연결 끊김", Toast.LENGTH_SHORT).show()
            }

            override fun didDetermineStateForRegion(state: Int, region: Region?) {
                Log.i(TAG, "I have just switched from seeing/not seeing beacons: $state")
            }
        })
        try {
            beaconManager!!.startMonitoringBeaconsInRegion(Region("beacon", null, null, null))
        } catch (e: RemoteException) {
        }
        try {
            beaconManager!!.startRangingBeaconsInRegion(Region("beacon", null, null, null))
        } catch (e: RemoteException) {
        }
    } // onBeaconServiceConnect()..


    override fun onDestroy() {
        super.onDestroy()
        beaconManager!!.unbind(this)
    }


    override fun onPointerCaptureChanged(hasCapture: Boolean) {}
    override fun onDenied(requestCode: Int, permissions: Array<String>) {}
    override fun onGranted(requestCode: Int, permissions: Array<String>) {}
}
