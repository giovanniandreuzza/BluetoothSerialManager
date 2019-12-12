package com.github.giovanniandreuzza.bluetoothserialmanager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.giovanniandreuzza.bluetoothserialmanagerlib.BluetoothSerialManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val a = BluetoothSerialManager.instance(this)

        a.scan()
    }
}
