package com.example.scannercardnumber.utils

import android.util.Log

object Logger {

    private const val STATUS: Boolean = true

    fun logD(tag: String, message: String) {
        if (STATUS) Log.d(tag, message)
    }

    fun logE(tag: String, message: String) {
        if (STATUS) Log.e(tag, message)
    }

    fun logI(tag: String, message: String) {
        if (STATUS) Log.i(tag, message)
    }

    fun logV(tag: String, message: String) {
        if (STATUS) Log.v(tag, message)
    }
}

