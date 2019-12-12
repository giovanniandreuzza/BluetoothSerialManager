package com.github.giovanniandreuzza.bluetoothserialmanagerlib

interface RxBluetoothDeviceCallback {

    fun getMacAddress(): String

    fun getName(): String

    fun isConnected(): Boolean

    fun connect(
        onConnected: (bluetoothConnectionCallback: BluetoothConnectionCallback) -> Unit,
        onFailure: (error: Throwable) -> Unit
    )

    fun disconnect()

    fun cancelOperation()

}