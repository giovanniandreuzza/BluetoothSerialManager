package com.github.giovanniandreuzza.bluetoothserialmanagerlib

import android.bluetooth.BluetoothDevice
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import java.io.IOException

internal class RxBluetoothDevice(private val bluetoothDevice: BluetoothDevice) :
    RxBluetoothDeviceCallback {

    private var compositeDisposable = CompositeDisposable()

    private var bluetoothConnectionInternalCallback: BluetoothConnectionInternalCallback? = null

    override fun getMacAddress(): String = bluetoothDevice.address

    override fun getName(): String = bluetoothDevice.name ?: bluetoothDevice.address

    override fun isConnected(): Boolean =
        bluetoothConnectionInternalCallback?.isConnected() ?: false

    override fun connect(
        onConnected: (bluetoothConnectionCallback: BluetoothConnectionCallback) -> Unit,
        onFailure: (error: Throwable) -> Unit
    ) {
        compositeDisposable.add(
            Completable.create { emitter ->
                try {
                    bluetoothConnectionInternalCallback = BluetoothConnection(
                        createRfcommSocket(bluetoothDevice, 1).apply {
                            connect()
                        }
                    )
                    emitter.onComplete()
                } catch (e: IOException) {
                    bluetoothConnectionInternalCallback?.let {
                        try {
                            it.close()
                        } catch (suppressed: IOException) {
                            emitter.onError(suppressed)
                        }
                    }
                }
            }.doOnDispose {
                // Nothing
            }.subscribe({
                bluetoothConnectionInternalCallback?.let {
                    onConnected(it.getSelf())
                }
            }, {
                onFailure(it)
            })
        )
    }

    override fun disconnect() {
        bluetoothConnectionInternalCallback?.close()
        bluetoothConnectionInternalCallback = null
    }

    override fun cancelOperation() {
        with(compositeDisposable) {
            dispose()
            clear()
        }
    }

}