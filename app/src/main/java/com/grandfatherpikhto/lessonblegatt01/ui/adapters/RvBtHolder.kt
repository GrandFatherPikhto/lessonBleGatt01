package com.grandfatherpikhto.lessonblegatt01.ui.adapters

import android.bluetooth.BluetoothDevice
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.grandfatherpikhto.lessonblegatt01.R
import com.grandfatherpikhto.lessonblegatt01.databinding.LayoutDeviceBinding

class RvBtHolder constructor(private val view: View) : RecyclerView.ViewHolder(view) {
    private val binding = LayoutDeviceBinding.bind(view)

    fun bind(bluetoothDevice: BluetoothDevice) {
        binding.apply {
            tvAddress.text = bluetoothDevice.address
            tvName.text = bluetoothDevice.name
                ?: view.context.getString(R.string.default_device_name)
            if (bluetoothDevice.bondState == BluetoothDevice.BOND_BONDED) {
                ivBond.setImageResource(R.drawable.ic_bluetooth_paired)
            } else {
                ivBond.setImageResource(R.drawable.ic_bluetooth_unpaired)
            }
        }
    }
}