package mr.talkingtoy.engine

import android.content.Context
import android.content.SharedPreferences

class SettingsManager() {
    companion object {
        val SONG = "SONG"
        val TALE = "TALE"
        val FIRST_ENTERING = "FIRST_ENTERING"
        val NAME = "NAME"

        fun getString(key: String, defaultValue: String, context: Context): String? {
            val sharedPreferences: SharedPreferences =
                context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
            return sharedPreferences.getString(key, defaultValue)
        }

        fun putString(key: String, value: String, context: Context) {
            val sharedPreferences: SharedPreferences =
                context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
            sharedPreferences.edit().putString(key, value).apply()
        }

        fun getBoolean(key: String, defaultValue: Boolean, context: Context): Boolean {
            val sharedPreferences: SharedPreferences =
                context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
            return sharedPreferences.getBoolean(key, defaultValue)
        }

        fun putBoolean(key: String, value: Boolean, context: Context) {
            val sharedPreferences: SharedPreferences =
                context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
            sharedPreferences.edit().putBoolean(key, value).apply()
        }

        fun remove(key: String, context: Context) {
            val sharedPreferences: SharedPreferences =
                context.getSharedPreferences("Settings", Context.MODE_PRIVATE)

            sharedPreferences.edit().remove(key).apply()
        }

        fun clear(context: Context) {
            val sharedPreferences: SharedPreferences =
                context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
            sharedPreferences.edit().clear().apply()
        }
    }
}