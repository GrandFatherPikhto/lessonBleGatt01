package com.grandfatherpikhto.lessonblegatt01.ui.adapters

import android.bluetooth.BluetoothGattCharacteristic
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.grandfatherpikhto.lessonblegatt01.databinding.LayoutCharacteristicBinding

class RvCharacteristicHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val logTag = this.javaClass.simpleName
    private val binding = LayoutCharacteristicBinding.bind(view)

    fun bind(bluetoothGattCharacteristic: BluetoothGattCharacteristic) {
        binding.apply {
            tvCharacteristic.text = bluetoothGattCharacteristic.uuid.toString()
            val value = bluetoothGattCharacteristic.value?.let { value ->
                value.toUByteArray()
                    .joinToString(" ") { String.format("%02X") }
            } ?: ""
            // Log.d(logTag, "Value: $value")
            tvValue.text = value
        }
    }
}