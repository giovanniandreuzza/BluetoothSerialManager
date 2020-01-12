package com.github.giovanniandreuzza.rxbluetoothserial

import android.bluetooth.BluetoothDevice

/**
 * Event container class. Contains broadcast ACL action and [BluetoothDevice].
 *
 * Possible broadcast ACL action values are:
 * [BluetoothDevice.ACTION_ACL_CONNECTED],
 * [BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED],
 * [BluetoothDevice.ACTION_ACL_DISCONNECTED]
 */
class AclEvent(val action: String?, val rxBluetoothDevice: RxBluetoothDevice?) {

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other == null || javaClass != other.javaClass -> false
        other is AclEvent -> when {
            action != null && action != other.action -> false
            else -> !if (rxBluetoothDevice != null) rxBluetoothDevice != other.rxBluetoothDevice else other.rxBluetoothDevice != null
        }
        else -> false
    }

    override fun hashCode(): Int = (action?.hashCode() ?: 0).also {
        it * 31 + (rxBluetoothDevice?.hashCode() ?: 0)
    }

    override fun toString(): String =
        "AclEvent{action= $action, bluetoothDevice= $rxBluetoothDevice}"

}