package com.github.giovanniandreuzza.bluetoothserialmanagerlib

internal interface BluetoothConnectionInternalCallback {

    fun getSelf(): BluetoothConnection

    fun isConnected(): Boolean

    fun close()

}