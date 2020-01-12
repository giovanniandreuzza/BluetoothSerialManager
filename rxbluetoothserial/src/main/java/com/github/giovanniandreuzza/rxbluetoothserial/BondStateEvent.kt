package com.github.giovanniandreuzza.rxbluetoothserial

import android.bluetooth.BluetoothDevice

/**
 * Event container class. Contains bond state (whether the device is unbonded, bonding, or bonded),
 * previous bond state, and [RxBluetoothDevice].
 *
 * Possible state values are:
 * [BluetoothDevice.BOND_NONE],
 * [BluetoothDevice.BOND_BONDING],
 * [BluetoothDevice.BOND_BONDED]
 */
class BondStateEvent(
    val state: Int,
    val previousState: Int,
    val rxBluetoothDevice: RxBluetoothDevice?
) {

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other == null || javaClass != other.javaClass -> false
        other is BondStateEvent -> when {
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
        "BondStateEvent{state= $state, previousState= $previousState, rxBluetoothDevice= $rxBluetoothDevice}"

}