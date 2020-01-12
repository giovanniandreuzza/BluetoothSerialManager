package com.github.giovanniandreuzza.rxbluetoothserial

import android.bluetooth.BluetoothAdapter


/**
 * Event container class. Contains connection state (whether the device is disconnected,
 * connecting, connected, or disconnecting), previous connection state, and [RxBluetoothDevice].
 *
 * Possible state values are:
 * [BluetoothAdapter.STATE_DISCONNECTED],
 * [BluetoothAdapter.STATE_CONNECTING],
 * [BluetoothAdapter.STATE_CONNECTED],
 * [BluetoothAdapter.STATE_DISCONNECTING]
 */
class ConnectionStateEvent(
    val state: Int,
    val previousState: Int,
    val rxBluetoothDevice: RxBluetoothDevice?
) {

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other == null || javaClass != other.javaClass -> false
        other is ConnectionStateEvent -> when {
            state != other.state -> false
            previousState != other.previousState -> false
            else -> !if (rxBluetoothDevice != null) rxBluetoothDevice != other.rxBluetoothDevice else other.rxBluetoothDevice != null
        }
        else -> false
    }

    override fun hashCode(): Int = state.also { it * 31 + previousState }.also {
        it * 31 + (rxBluetoothDevice?.hashCode() ?: 0)
    }

    override fun toString(): String =
        "ConnectionStateEvent{state= $state, previousState= $previousState, rxBluetoothDevice= $rxBluetoothDevice}"

}