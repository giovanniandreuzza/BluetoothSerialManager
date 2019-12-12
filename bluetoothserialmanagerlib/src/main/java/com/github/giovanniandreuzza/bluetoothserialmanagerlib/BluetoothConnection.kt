package com.github.giovanniandreuzza.bluetoothserialmanagerlib

import android.bluetooth.BluetoothSocket

internal class BluetoothConnection(private val bluetoothSocket: BluetoothSocket) :
    BluetoothConnectionCallback, BluetoothConnectionInternalCallback {

    private val inputStream = bluetoothSocket.inputStream

    private val outputStream = bluetoothSocket.outputStream

    override fun listen() {

    }

    override fun send() {

    }

    override fun getSelf(): BluetoothConnection = this

    override fun isConnected(): Boolean = bluetoothSocket.isConnected

    override fun close() {
        inputStream.close()
        outputStream.close()
        bluetoothSocket.close()
    }

}