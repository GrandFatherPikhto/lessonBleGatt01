package com.grandfatherpikhto.blin

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothProfile
import android.content.Intent
import com.grandfatherpikhto.blin.MockBleGenerator.mockBluetoothDevice
import com.grandfatherpikhto.blin.MockBleGenerator.mockIntentScanResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import com.grandfatherpikhto.blin.MockBleGenerator.ADDRESS_01
import com.grandfatherpikhto.blin.MockBleGenerator.ADDRESS_02
import com.grandfatherpikhto.blin.MockBleGenerator.ADDRESS_03
import com.grandfatherpikhto.blin.MockBleGenerator.NAME_01
import com.grandfatherpikhto.blin.MockBleGenerator.NAME_02
import com.grandfatherpikhto.blin.MockBleGenerator.NAME_03


@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class BleManagerTest {
    private val bleManager:BleManager by lazy {
        BleManager(RuntimeEnvironment.getApplication().applicationContext,
            UnconfinedTestDispatcher())
    }

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun testScan() = runTest (UnconfinedTestDispatcher()) {
        val bluetoothDevices = mutableListOf<BluetoothDevice>()
        val intent = mockIntentScanResult(List<BluetoothDevice>(7) {
            val bluetoothDevice = mockBluetoothDevice()
            bluetoothDevices.add(bluetoothDevice)
            bluetoothDevice
        })
        bleManager.startScan()
        assertEquals(BleScanManager.State.Scanning, bleManager.scanState)
        bleManager.scanner.onScanReceived(intent)
        assertNotNull(bleManager.scannedDevice)
        assertEquals(bluetoothDevices.last().address, bleManager.scannedDevice!!.address)
        bleManager.stopScan()
        assertEquals(BleScanManager.State.Stopped, bleManager.scanState)
    }

    @Test
    fun checkRepeatFilter() = runTest (UnconfinedTestDispatcher()) {
        val bluetoothDevices = listOf<BluetoothDevice>(
            mockBluetoothDevice(name = NAME_01, address = ADDRESS_01),
            mockBluetoothDevice(name = NAME_01, address = ADDRESS_01),
            mockBluetoothDevice(name = NAME_02, address = ADDRESS_02),
            mockBluetoothDevice(name = NAME_01, address = ADDRESS_01),
        )
        bleManager.startScan()
        val intent = mockIntentScanResult(bluetoothDevices)
        assertEquals(BleScanManager.State.Scanning, bleManager.scanState)
        bleManager.scanner.onScanReceived(intent)
        assertNotNull(bleManager.scannedDevice)
        assertEquals(ADDRESS_02, bleManager.scannedDevice!!.address)
        bleManager.stopScan()
    }

    @Test
    fun checkAddressFilter() = runTest {
        val bluetoothDevices = listOf<BluetoothDevice>(
            mockBluetoothDevice(name = NAME_01, address = ADDRESS_01),
            mockBluetoothDevice(name = NAME_02, address = ADDRESS_02),
            mockBluetoothDevice(name = NAME_03, address = ADDRESS_03),
        )
        bleManager.startScan(addresses = listOf(ADDRESS_02))
        val intent = mockIntentScanResult(bluetoothDevices)
        assertEquals(BleScanManager.State.Scanning, bleManager.scanState)
        bleManager.scanner.onScanReceived(intent)
        assertNotNull(bleManager.scannedDevice)
        assertEquals(ADDRESS_02, bleManager.scannedDevice!!.address)
        bleManager.stopScan()
        assertEquals(BleScanManager.State.Stopped, bleManager.scanState)
    }

    @Test
    fun checkAddressFilterWithStop() = runTest {
        val bluetoothDevices = listOf<BluetoothDevice>(
            mockBluetoothDevice(name = NAME_01, address = ADDRESS_01),
            mockBluetoothDevice(name = NAME_02, address = ADDRESS_02),
            mockBluetoothDevice(name = NAME_03, address = ADDRESS_03),
        )
        bleManager.startScan(addresses = listOf(ADDRESS_02),
            stopOnFind = true)
        val intent = mockIntentScanResult(bluetoothDevices)
        bleManager.scanner.onScanReceived(intent)
        assertEquals(BleScanManager.State.Stopped, bleManager.scanState)
        assertNotNull(bleManager.scannedDevice)
        assertEquals(ADDRESS_02, bleManager.scannedDevice!!.address)
    }

    @Test
    fun checkNamesFilterWithStop() = runTest (UnconfinedTestDispatcher()) {
        val bluetoothDevices = listOf<BluetoothDevice>(
            mockBluetoothDevice(name = NAME_01, address = ADDRESS_01),
            mockBluetoothDevice(name = NAME_02, address = ADDRESS_02),
            mockBluetoothDevice(name = NAME_03, address = ADDRESS_03),
        )
        bleManager.startScan(names = listOf(NAME_01),
            stopOnFind = true)
        val intent = mockIntentScanResult(bluetoothDevices)
        bleManager.scanner.onScanReceived(intent)
        assertEquals(BleScanManager.State.Stopped, bleManager.scanState)
        assertNotNull(bleManager.scannedDevice)
        assertEquals(NAME_01, bleManager.scannedDevice!!.name)
    }


    @Test
    fun testConnectFailed() = runTest (UnconfinedTestDispatcher()){
        val bluetoothGatt = bleManager.connect(ADDRESS_01)
        val bluetoothDevice = mockBluetoothDevice(address = ADDRESS_01, name = NAME_01)
        val intent = mockIntentScanResult(listOf(bluetoothDevice))
        assertEquals(BleGattManager.State.Connecting, bleManager.connectionState)
        assertNotNull(bluetoothGatt)
        for (iteration in 1..5) {
            bleManager.connector.onConnectionStateChange(bluetoothGatt, 133, 0)
            assertTrue(bleManager.connector.isReconnect)
            assertEquals(iteration, bleManager.connector.attempt)
            bleManager.scanner.onScanReceived(intent)
        }

        bleManager.connector.onConnectionStateChange(bluetoothGatt, 6, 0)
        assertFalse(bleManager.connector.isReconnect)
        assertEquals(0, bleManager.connector.attempt)
        assertEquals(BleGattManager.State.Error, bleManager.connectionState)
    }

    fun testConnectWithSuccess() = runTest(UnconfinedTestDispatcher()) {
        val bluetoothGatt = bleManager.connect(ADDRESS_01)
        val bluetoothDevice = mockBluetoothDevice(address = ADDRESS_01, name = NAME_01)
        val intent = mockIntentScanResult(listOf(bluetoothDevice))
        assertEquals(BleGattManager.State.Connecting, bleManager.connectionState)
        assertNotNull(bluetoothGatt)
        for (iteration in 1..5) {
            bleManager.connector.onConnectionStateChange(bluetoothGatt, 133, 0)
            assertTrue(bleManager.connector.isReconnect)
            assertEquals(iteration, bleManager.connector.attempt)
            bleManager.scanner.onScanReceived(intent)
        }

        bleManager.connector.onConnectionStateChange(bluetoothGatt,
            BluetoothGatt.GATT_SUCCESS,
            BluetoothProfile.STATE_CONNECTED)
        assertEquals(BleGattManager.State.Connecting, bleManager.connectionState)

        bleManager.connector.onGattDiscovered(bluetoothGatt, BluetoothGatt.GATT_SUCCESS)
        assertEquals(BleGattManager.State.Connected, bleManager.connectionState)

        bleManager.close()
        assertEquals(BleGattManager.State.Disconnecting, bleManager.connectionState)
        bleManager.connector.onConnectionStateChange(bluetoothGatt,
            BluetoothGatt.GATT_SUCCESS,
            BluetoothProfile.STATE_DISCONNECTED)
        assertEquals(BleGattManager.State.Disconnected, bleManager.connectionState)
    }
}