package com.github.giovanniandreuzza.rxbluetoothserial

import android.bluetooth.BluetoothDevice
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import java.io.IOException

class RxBluetoothDevice(private val bluetoothDevice: BluetoothDevice) {

    private var compositeDisposable = CompositeDisposable()

    val macAddress: String = bluetoothDevice.address

    val name: String = bluetoothDevice.name ?: bluetoothDevice.address

    fun connect(
        channel: Int = 1,
        onConnected: (rxBluetoothConnection: RxBluetoothConnection) -> Unit,
        onFailure: (error: Throwable) -> Unit
    ) {
        compositeDisposable.add(
            Observable.create<RxBluetoothConnection> { emitter ->
                try {
                    emitter.onNext(
                        RxBluetoothConnection(
                            createRfcommSocket(bluetoothDevice, channel).apply {
                                connect()
                            }
                        )
                    )
                    emitter.onComplete()
                } catch (e: IOException) {
                    emitter.onError(e)
                }
            }.doOnDispose {
                // Nothing
            }.subscribe({
                onConnected(it)
            }, {
                onFailure(it)
            })
        )
    }

    fun stopConnection() {
        with(compositeDisposable) {
            dispose()
            clear()
        }
    }

}