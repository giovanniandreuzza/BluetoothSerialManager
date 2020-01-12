package com.github.giovanniandreuzza.rxbluetoothserial

import android.Manifest.permission
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build.VERSION
import android.util.Log
import io.reactivex.Observable
import io.reactivex.android.MainThreadDisposable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class RxBluetooth(private val context: Context) : RxBluetoothCallback {

    companion object {
        fun build(context: Context): RxBluetoothCallback = RxBluetooth(context)
    }

    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private var scanDisposable = CompositeDisposable()

    /**
     * Return true if Bluetooth is available.
     *
     * @return true if bluetoothAdapter is not null, otherwise Bluetooth is
     * not supported on this hardware platform
     */
    override fun isBluetoothAvailable(): Boolean = bluetoothAdapter != null


    /**
     * Return true if Bluetooth is currently enabled and ready for use.
     *
     * Equivalent to:
     * `getBluetoothState() == STATE_ON`
     *
     * Requires [android.Manifest.permission.BLUETOOTH]
     *
     * @return true if the local adapter is turned on
     */
    override fun isBluetoothEnabled(): Boolean = bluetoothAdapter.isEnabled


    /**
     * Return true if Location permission is granted.
     *
     * @return true if the local permission is granted. Pre 23 it will always return true. Post 22
     * it will ask the Context whether the permission has been granted or not.
     */
    override fun isLocationPermissionGranted(): Boolean = if (VERSION.SDK_INT >= 23) {
        context.checkSelfPermission(permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    } else true


    /**
     * Return true if a location service is enabled.
     *
     * @return true if either the GPS or Network provider is enabled
     */
    override fun isLocationServiceEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }


    /**
     * This will issue a request to enable Bluetooth through the system settings (without stopping
     * your application) via ACTION_REQUEST_ENABLE action Intent.
     *
     * @param activity Activity
     * @param requestCode request code
     */
    override fun enableBluetooth(activity: Activity, requestCode: Int) {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity.startActivityForResult(enableBtIntent, requestCode)
        }
    }


    /**
     * Return the set of [RxBluetoothDevice] devices that are bonded
     * (paired) to the local adapter.
     *
     * If Bluetooth state is not [BluetoothAdapter.STATE_ON], this API
     * will return an empty set. After turning on Bluetooth,
     * wait for [BluetoothAdapter.ACTION_STATE_CHANGED] with [BluetoothAdapter.STATE_ON]
     * to get the updated value.
     *
     * Requires [android.Manifest.permission.BLUETOOTH].
     *
     * @return unmodifiable set of [RxBluetoothDevice], or null on error
     */
    override fun getBondedDevices(): Set<RxBluetoothDevice> =
        bluetoothAdapter.bondedDevices.map { RxBluetoothDevice(it) }.toSet()


    /**
     * Start the remote device discovery process.
     *
     * Requires [android.Manifest.permission.BLUETOOTH_ADMIN]
     *
     * @return true on success, false on error
     */
    private fun startDiscovery(): Boolean {
        return bluetoothAdapter.startDiscovery()
    }


    /**
     * Return true if the local Bluetooth adapter is currently in the device
     * discovery process.
     *
     * @return true if discovering
     */
    override fun isDiscovering(): Boolean = bluetoothAdapter.isDiscovering


    /**
     * Cancel the current device discovery process.
     *
     * Requires [android.Manifest.permission.BLUETOOTH_ADMIN]
     *
     * @return true on success, false on error
     */
    private fun cancelDiscovery(): Boolean {
        return bluetoothAdapter.cancelDiscovery()
    }

    /**
     * This will issue a request to make the local device discoverable to other devices. By default,
     * the device will become discoverable for 120 seconds. Maximum duration is capped at 300
     * seconds.
     *
     * @param activity Activity
     * @param requestCode request code
     * @param duration discoverability duration in seconds
     */
    override fun enableDiscoverability(activity: Activity, requestCode: Int, duration: Int) {
        Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            if (duration >= 0) {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration)
            }
            activity.startActivityForResult(this, requestCode)
        }
    }


    /**
     * Observes Bluetooth devices found while discovering.
     *
     * @return RxJava Observable with BluetoothDevice found
     */
    private fun observeDevices(): Observable<RxBluetoothDevice> {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        return Observable.create { emitter ->
            val receiver: BroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val action = intent.action
                    if (action == BluetoothDevice.ACTION_FOUND) {
                        val device =
                            intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

                        device?.let {
                            emitter.onNext(RxBluetoothDevice(device))
                        }
                    }
                }
            }
            context.registerReceiver(receiver, filter)
            emitter.setDisposable(object : MainThreadDisposable() {
                override fun onDispose() {
                    context.unregisterReceiver(receiver)
                }
            })
        }
    }

    /**
     * Observes BluetoothState. Possible values are:
     * [BluetoothAdapter.STATE_OFF],
     * [BluetoothAdapter.STATE_TURNING_ON],
     * [BluetoothAdapter.STATE_ON],
     * [BluetoothAdapter.STATE_TURNING_OFF],
     *
     * @return RxJava Observable with BluetoothState
     */
    override fun observeBluetoothState(): Observable<Int> {
        val filter = IntentFilter()
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        return Observable.create { emitter ->
            val receiver: BroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(
                    context: Context,
                    intent: Intent
                ) {
                    emitter.onNext(bluetoothAdapter.state)
                }
            }
            context.registerReceiver(receiver, filter)
            emitter.setDisposable(object : MainThreadDisposable() {
                override fun onDispose() {
                    context.unregisterReceiver(receiver)
                }
            })
        }
    }

    /**
     * Observes scan mode of device. Possible values are:
     * [BluetoothAdapter.SCAN_MODE_NONE],
     * [BluetoothAdapter.SCAN_MODE_CONNECTABLE],
     * [BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE]
     *
     * @return RxJava Observable with scan mode
     */
    override fun observeScanMode(): Observable<Int> {
        val filter = IntentFilter()
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
        return Observable.create { emitter ->
            val receiver: BroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(
                    context: Context,
                    intent: Intent
                ) {
                    emitter.onNext(bluetoothAdapter.scanMode)
                }
            }
            context.registerReceiver(receiver, filter)
            emitter.setDisposable(object : MainThreadDisposable() {
                override fun onDispose() {
                    context.unregisterReceiver(receiver)
                }
            })
        }
    }

    /**
     * Observes connection state of devices.
     *
     * @return RxJava Observable with [ConnectionStateEvent]
     */
    override fun observeConnectionState(): Observable<ConnectionStateEvent> {
        val filter = IntentFilter()
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        return Observable.create { emitter ->
            val receiver: BroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val status = intent.getIntExtra(
                        BluetoothAdapter.EXTRA_CONNECTION_STATE,
                        BluetoothAdapter.STATE_DISCONNECTED
                    )
                    val previousStatus = intent.getIntExtra(
                        BluetoothAdapter.EXTRA_PREVIOUS_CONNECTION_STATE,
                        BluetoothAdapter.STATE_DISCONNECTED
                    )
                    var device: RxBluetoothDevice? = null
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)?.let {
                        device = RxBluetoothDevice(it)
                    }

                    emitter.onNext(ConnectionStateEvent(status, previousStatus, device))
                }
            }

            context.registerReceiver(receiver, filter)
            emitter.setDisposable(object : MainThreadDisposable() {
                override fun onDispose() {
                    context.unregisterReceiver(receiver)
                }
            })
        }
    }

    /**
     * Observes bond state of devices.
     *
     * @return RxJava Observable with [BondStateEvent]
     */
    override fun observeBondState(): Observable<BondStateEvent> {
        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        return Observable.create { emitter ->
            val receiver: BroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val state = intent.getIntExtra(
                        BluetoothDevice.EXTRA_BOND_STATE,
                        BluetoothDevice.BOND_NONE
                    )
                    val previousState = intent.getIntExtra(
                        BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE,
                        BluetoothDevice.BOND_NONE
                    )

                    var device: RxBluetoothDevice? = null
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)?.let {
                        device = RxBluetoothDevice(it)
                    }

                    emitter.onNext(BondStateEvent(state, previousState, device))
                }
            }
            context.registerReceiver(receiver, filter)
            emitter.setDisposable(object : MainThreadDisposable() {
                override fun onDispose() {
                    context.unregisterReceiver(receiver)
                }
            })
        }
    }

    /**
     * Observes ACL broadcast actions from [BluetoothDevice]. Possible broadcast ACL action
     * values are:
     * [BluetoothDevice.ACTION_ACL_CONNECTED],
     * [BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED],
     * [BluetoothDevice.ACTION_ACL_DISCONNECTED]
     *
     * @return RxJava Observable with [AclEvent]
     */
    override fun observeAclEvent(): Observable<AclEvent> {
        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)
        return Observable.create { emitter ->
            val receiver: BroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val action = intent.action
                    var device: RxBluetoothDevice? = null

                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)?.let {
                        device = RxBluetoothDevice(it)
                    }

                    emitter.onNext(AclEvent(action, device))
                }
            }
            context.registerReceiver(receiver, filter)
            emitter.setDisposable(object : MainThreadDisposable() {
                override fun onDispose() {
                    context.unregisterReceiver(receiver)
                }
            })
        }
    }

    override fun scanAllDevices(
        aliasKey: String,
        duration: Long,
        deviceFound: (rxBluetoothDevice: RxBluetoothDevice) -> Unit,
        onScanCompleted: () -> Unit
    ) {
        scanDisposable.add(
            scan(aliasKey, duration, onScanCompleted)
                .subscribe({
                    deviceFound(it)
                }, {
                    Log.e("ERROR", "Scan error -> $it")
                })
        )
    }

    private fun scan(
        aliasKey: String,
        duration: Long,
        onCompleted: () -> Unit
    ): Observable<RxBluetoothDevice> {
        return observeDevices()
            .takeUntil(Observable.timer(duration, TimeUnit.SECONDS, Schedulers.io()))
            .doOnSubscribe {
                scanDisposable.add(it)
                startDiscovery()
            }
            .doOnDispose {
                cancelDiscovery()
            }
            .doOnComplete {
                scanDisposable.dispose()
                onCompleted()
            }
            .filter {
                it.name.contains(aliasKey, true)
            }
    }

    override fun stopScan() {
        cancelDiscovery()
        if (!scanDisposable.isDisposed) {
            scanDisposable.dispose()
            scanDisposable.clear()
        }
    }

}