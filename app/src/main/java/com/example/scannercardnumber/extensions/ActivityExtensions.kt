package com.example.scannercardnumber.extensions

import android.app.Activity
import android.widget.Toast

object ActivityExtensions {
    fun Activity.toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}