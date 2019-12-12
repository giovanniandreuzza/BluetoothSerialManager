package com.github.giovanniandreuzza.bluetoothserialmanagerlib

import android.bluetooth.BluetoothAdapter
import android.content.Context
import io.reactivex.disposables.Disposable

internal class RxBluetooth(private val context: Context) : RxBluetoothCallback {

    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private lateinit var scanDisposable: Disposable

    override fun scan() {
        bluetoothAdapter.bondedDevices.forEach { bluetoothDevice ->
            RxBluetoothDevice(bluetoothDevice).connect(
                onConnected = {
                    it.listen()
                    it.send()
                },
                onFailure = {

                }
            )
        }
    }

}
