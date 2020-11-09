package com.example.beacon_example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.pedro.library.AutoPermissionsListener
import org.altbeacon.beacon.BeaconConsumer

class MainActivity : AppCompatActivity(), BeaconConsumer, AutoPermissionsListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onBeaconServiceConnect() {
        TODO("Not yet implemented")
    }

    override fun onDenied(requestCode: Int, permissions: Array<String>) {
        TODO("Not yet implemented")
    }

    override fun onGranted(requestCode: Int, permissions: Array<String>) {
        TODO("Not yet implemented")
    }
}