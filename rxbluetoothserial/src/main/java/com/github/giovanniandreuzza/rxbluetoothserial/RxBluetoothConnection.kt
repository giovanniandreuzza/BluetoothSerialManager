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

    fun listen(): Byte = inputStream.read().toByte()

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