package com.example.kenwapwa

import android.content.Context
import android.content.SharedPreferences

object UserSessionManager {
    private const val PREF_NAME = "UserSessionPref"
    private const val KEY_USER_REG_ID = "user_reg_id"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveUserRegId(context: Context, regId: String) {
        val editor = getSharedPreferences(context).edit()
        editor.putString(KEY_USER_REG_ID, regId)
        editor.apply()
    }

    fun getUserRegId(context: Context): String? {
        return getSharedPreferences(context).getString(KEY_USER_REG_ID, null)
    }

    fun clearUserSession(context: Context) {
        val editor = getSharedPreferences(context).edit()
        editor.remove(KEY_USER_REG_ID)
        editor.apply()
    }
}