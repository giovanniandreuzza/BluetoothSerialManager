package com.github.giovanniandreuzza.bluetoothserialmanagerlib

import android.content.Context

object BluetoothSerialManager {

    private var rxBluetooth: RxBluetooth? = null

    fun instance(context: Context): RxBluetoothCallback {
        if (rxBluetooth == null) {
            rxBluetooth = RxBluetooth(context)
        }
        return rxBluetooth!!
    }

}