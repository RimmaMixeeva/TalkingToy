package mr.talkingtoy.engine

import android.content.Context
import android.content.SharedPreferences

class SettingsManager () {
    companion object {
    val SONG = "SONG"
    val TALE = "TALE"

    fun getString(key: String, defaultValue: String, context: Context): String? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        return sharedPreferences.getString(key, defaultValue)
    }

    fun putString(key: String, value: String, context: Context) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun remove(key: String, context: Context) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)

        sharedPreferences.edit().remove(key).apply()
    }

    fun clear(context: Context) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
    }
    }
}