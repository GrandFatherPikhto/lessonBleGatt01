package com.grandfatherpikhto.blin

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.content.Context
import com.grandfatherpikhto.blin.MockBleGenerator.ADDRESS_01
import com.grandfatherpikhto.blin.MockBleGenerator.ADDRESS_02
import com.grandfatherpikhto.blin.MockBleGenerator.ADDRESS_03
import com.grandfatherpikhto.blin.MockBleGenerator.NAME_01
import com.grandfatherpikhto.blin.MockBleGenerator.NAME_02
import com.grandfatherpikhto.blin.MockBleGenerator.NAME_03
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

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class BleScanManagerTest {
    private val bleManager:BleManager by lazy {
        BleManager(RuntimeEnvironment.getApplication().applicationContext, UnconfinedTestDispatcher())
    }

    private val bleScanManager:BleScanManager by lazy {
        BleScanManager(bleManager,
            UnconfinedTestDispatcher())
    }

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun testScan() = runTest(UnconfinedTestDispatcher()) {
        val bluetoothDevices = mutableListOf<BluetoothDevice>()
        val intent = mockIntentScanResult(List<BluetoothDevice>(7) {
            val bluetoothDevice = mockBluetoothDevice()
            bluetoothDevices.add(bluetoothDevice)
            bluetoothDevice
        })

        bleScanManager.startScan()
        assertEquals(BleScanManager.State.Scanning, bleScanManager.state)
        bleScanManager.onScanReceived(intent)
        assertNotNull(bleScanManager.device)
        assertEquals(bluetoothDevices.last().address, bleScanManager.device!!.address)
        bleScanManager.stopScan()
        assertEquals(BleScanManager.State.Stopped, bleScanManager.state)
    }

    @Test
    fun checkRepeatFilter() = runTest (UnconfinedTestDispatcher()) {
        val bluetoothDevices = listOf<BluetoothDevice>(
            mockBluetoothDevice(name = NAME_01, address = ADDRESS_01),
            mockBluetoothDevice(name = NAME_01, address = ADDRESS_01),
            mockBluetoothDevice(name = NAME_02, address = ADDRESS_02),
            mockBluetoothDevice(name = NAME_01, address = ADDRESS_01),
        )
        bleScanManager.startScan()
        val intent = mockIntentScanResult(bluetoothDevices)
        assertEquals(BleScanManager.State.Scanning, bleScanManager.state)
        bleScanManager.onScanReceived(intent)
        assertNotNull(bleScanManager.device)
        assertEquals(ADDRESS_02, bleScanManager.device!!.address)
        bleScanManager.stopScan()
    }

    @Test
    fun checkAddressFilter() = runTest {
        val bluetoothDevices = listOf<BluetoothDevice>(
            mockBluetoothDevice(name = NAME_01, address = ADDRESS_01),
            mockBluetoothDevice(name = NAME_02, address = ADDRESS_02),
            mockBluetoothDevice(name = NAME_03, address = ADDRESS_03),
        )
        bleScanManager.startScan(addresses = listOf(ADDRESS_02))
        val intent = mockIntentScanResult(bluetoothDevices)
        assertEquals(BleScanManager.State.Scanning, bleScanManager.state)
        bleScanManager.onScanReceived(intent)
        assertNotNull(bleScanManager.device)
        assertEquals(ADDRESS_02, bleScanManager.device!!.address)
        bleScanManager.stopScan()
        assertEquals(BleScanManager.State.Stopped, bleScanManager.state)
    }

    @Test
    fun checkAddressFilterWithStop() = runTest {
        val bluetoothDevices = listOf<BluetoothDevice>(
            mockBluetoothDevice(name = NAME_01, address = ADDRESS_01),
            mockBluetoothDevice(name = NAME_02, address = ADDRESS_02),
            mockBluetoothDevice(name = NAME_03, address = ADDRESS_03),
        )
        bleScanManager.startScan(addresses = listOf(ADDRESS_02),
            stopOnFind = true)
        val intent = mockIntentScanResult(bluetoothDevices)
        bleScanManager.onScanReceived(intent)
        assertEquals(BleScanManager.State.Stopped, bleScanManager.state)
        assertNotNull(bleScanManager.device)
        assertEquals(ADDRESS_02, bleScanManager.device!!.address)
    }

    @Test
    fun checkNamesFilterWithStop() = runTest (UnconfinedTestDispatcher()) {
        val bluetoothDevices = listOf<BluetoothDevice>(
            mockBluetoothDevice(name = NAME_01, address = ADDRESS_01),
            mockBluetoothDevice(name = NAME_02, address = ADDRESS_02),
            mockBluetoothDevice(name = NAME_03, address = ADDRESS_03),
        )
        bleScanManager.startScan(names = listOf(NAME_01),
            stopOnFind = true)
        val intent = mockIntentScanResult(bluetoothDevices)
        bleScanManager.onScanReceived(intent)
        assertEquals(BleScanManager.State.Stopped, bleScanManager.state)
        assertNotNull(bleScanManager.device)
        assertEquals(NAME_01, bleScanManager.device!!.name)
    }
}