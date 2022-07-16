package com.grandfatherpikhto.blin

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanRecord
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.os.ParcelUuid
import org.junit.runner.manipulation.Ordering
import org.mockito.kotlin.mock
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import kotlin.random.Random

object MockBleGenerator {
    const val ADDRESS_01 = "00:01:02:03:04:05"
    const val ADDRESS_02 = "01:02:03:04:05:06"
    const val ADDRESS_03 = "02:03:04:05:06:07"

    const val NAME_01 = "BLE_01"
    const val NAME_02 = "BLE_02"
    const val NAME_03 = "BLE_03"

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        (RuntimeEnvironment.getApplication().applicationContext
            .getSystemService(Context.BLUETOOTH_SERVICE)
                as BluetoothManager).adapter
    }

    fun mockBluetoothDevice (address: String? = null,
                                     name: String? = null,
                                     uuids: List<ParcelUuid>? = null
    ) : BluetoothDevice = bluetoothAdapter.getRemoteDevice(address ?:
    Random.nextBytes(6).joinToString (":") { String.format("%02X", it) })
        .let { device ->
            if (name != null ) Shadows.shadowOf(device).setName(name)
            if (!uuids.isNullOrEmpty()) Shadows.shadowOf(device).setUuids(uuids.toTypedArray())
            device
        }

    fun mockIntentScanResult (devices: List<BluetoothDevice>
    ) : Intent = Intent(
        RuntimeEnvironment.getApplication().applicationContext,
        ScanResult::class.java).let { intent ->
        val scanResults = mutableListOf<ScanResult>()
        devices.forEach { device ->
            ScanResult(device,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                mock<ScanRecord>(),
                0).let { scanResult ->
                scanResults.add(scanResult)
            }
            intent.putParcelableArrayListExtra(
                BluetoothLeScanner.EXTRA_LIST_SCAN_RESULT,
                scanResults.toCollection(ArrayList<ScanResult>()))
        }
        intent
    }
}