package com.grandfatherpikhto.lessonblegatt01

import android.app.Application
import com.grandfatherpikhto.blin.BleManager

class GattApplication : Application() {
    var bleManager: BleManager? = null
}