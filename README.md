# BLE: Connecting and reading GATT profile
1. Scans devices. If the device is paired, remembers the selected address and opens the profile screen on startup
2. Connects to the device and reads the GATT profile. Displays it in RecyclerView.
3. Requests to pair with a device.
# Bluetooth Low Interface -- BLIN
## Connecting to a BLE device
### Functionality
1. Device pairing
2. Features of connectGatt
3. Errors 133 & 6. Rescan and connect
4. Read Services, Characterstics & Descriptors
### Library
BleManager
1. BleScannManager
2. BleGattManager
3. BleBondManager