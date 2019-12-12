package com.github.giovanniandreuzza.bluetoothserialmanagerlib

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import java.lang.reflect.InvocationTargetException

internal fun createRfcommSocket(device: BluetoothDevice, channel: Int): BluetoothSocket {
    try {
        val method =
            BluetoothDevice::class.java.getMethod("createInsecureRfcommSocket", Integer.TYPE)
        return method.invoke(device, channel) as BluetoothSocket
    } catch (e: NoSuchMethodException) {
        throw UnsupportedOperationException(e)
    } catch (e: InvocationTargetException) {
        throw UnsupportedOperationException(e)
    } catch (e: IllegalAccessException) {
        throw UnsupportedOperationException(e)
    }
}