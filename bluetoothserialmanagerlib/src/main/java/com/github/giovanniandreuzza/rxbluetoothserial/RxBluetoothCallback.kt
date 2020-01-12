package com.github.giovanniandreuzza.rxbluetoothserial

import android.app.Activity
import io.reactivex.Observable

interface RxBluetoothCallback {

    fun isBluetoothAvailable(): Boolean

    fun isBluetoothEnabled(): Boolean

    fun isLocationPermissionGranted(): Boolean

    fun isLocationServiceEnabled(): Boolean

    fun enableBluetooth(activity: Activity, requestCode: Int)

    fun getBondedDevices(): Set<RxBluetoothDevice>

    fun isDiscovering(): Boolean

    fun enableDiscoverability(activity: Activity, requestCode: Int, duration: Int = -1)

    fun observeBluetoothState(): Observable<Int>

    fun observeScanMode(): Observable<Int>

    fun observeConnectionState(): Observable<ConnectionStateEvent>

    fun observeBondState(): Observable<BondStateEvent>

    fun observeAclEvent(): Observable<AclEvent>

    fun scanAllDevices(
        aliasKey: String = "",
        duration: Long = 5,
        deviceFound: (rxBluetoothDevice: RxBluetoothDevice) -> Unit,
        onScanCompleted: () -> Unit
    )

    fun stopScan()

}