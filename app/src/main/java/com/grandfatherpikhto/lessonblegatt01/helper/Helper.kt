package com.grandfatherpikhto.lessonblegatt01.helper

import android.view.View

typealias clickHandler<T> = (T, View) -> Unit
typealias longClickHandler<T> = (T, View) -> Unit