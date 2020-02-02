package com.github.giovanniandreuzza.rxbluetoothserial

import android.bluetooth.BluetoothSocket
import android.util.Log
import io.reactivex.Observable
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class RxBluetoothConnection(private val bluetoothSocket: BluetoothSocket) {

    companion object {
        private val TAG: String = RxBluetoothConnection::class.java.name
    }

    private val inputStream: InputStream = bluetoothSocket.inputStream

    private val outputStream: OutputStream = bluetoothSocket.outputStream

    private var observeInputStream: Observable<Byte>? = null

    fun isConnected(): Boolean = bluetoothSocket.isConnected

    fun listen(): Observable<Byte> {
        if (observeInputStream == null) {
            observeInputStream = Observable.create<Byte> { subscriber ->
                while (!subscriber.isDisposed) {
                    try {
                        subscriber.onNext(inputStream.read().toByte())
                    } catch (e: IOException) {
                        subscriber.onError(IOException("Can't read stream", e))
                    } finally {
                        if (!isConnected()) {
                            close()
                        }
                    }
                }
            }
        }

        return observeInputStream!!
    }

    fun send(byteArray: ByteArray): Boolean {
        return if (!isConnected()) false else try {
            outputStream.write(byteArray)
            outputStream.flush()
            true
        } catch (e: IOException) { // Error occurred. Better to close terminate the connection
            Log.e(TAG, "Fail to send data")
            false
        } finally {
            if (!isConnected()) {
                close()
            }
        }
    }

    fun close() {
        inputStream.close()
        outputStream.close()
        bluetoothSocket.close()
    }

}