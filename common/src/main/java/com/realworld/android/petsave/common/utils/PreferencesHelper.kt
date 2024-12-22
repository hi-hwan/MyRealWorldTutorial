package com.realworld.android.petsave.common.utils

import android.content.Context
import android.util.Base64
import java.text.DateFormat
import java.util.Date

class PreferencesHelper {
    companion object {
        fun lastLoggedIn(context: Context): String? {
            val preferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            return preferences.getString("lastLogin", null)
        }

        fun saveLastLoggedInTime(context: Context) {
            val currentDateTimeString = DateFormat.getDateTimeInstance().format(Date())
            val editor = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit()
            editor.putString("lastLogin", currentDateTimeString)
            editor.apply()
        }

        fun iv(context: Context): ByteArray {
            val preferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val base64Iv = preferences.getString("iv", "")
            return Base64.decode(base64Iv, Base64.NO_WRAP)
        }

        fun saveIV(context: Context, iv: ByteArray) {
            val editor = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit()
            val ivString = Base64.encodeToString(iv, Base64.NO_WRAP)
            editor.putString("iv", ivString)
            editor.apply()
        }
    }
}