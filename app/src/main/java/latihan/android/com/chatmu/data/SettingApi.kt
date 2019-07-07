package  latihan.android.com.chatmu.data

import android.content.Context
import android.content.SharedPreferences
import  latihan.android.com.chatmu.R
import java.util.ArrayList



class SettingApi {
    internal var mContext: Context
    private var sharedSettings: SharedPreferences

    constructor(context: Context){
        mContext = context
        sharedSettings =
            mContext.getSharedPreferences(mContext.getString(R.string.settings_file_name), Context.MODE_PRIVATE)
    }

    fun readSetting(key: String): String {
        return sharedSettings.getString(key, "na")
    }

    fun addUpdateSettings(key: String, value: String) {
        val editor = sharedSettings.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun deleteAllSettings() {
        sharedSettings.edit().clear().apply()
    }

    fun readAll(): List<String> {
        val allUser = ArrayList<String>()
        val allEntries = sharedSettings.all
        for ((key, value) in allEntries) {
            if (key.contains("@"))
                allUser.add("$key ($value)")
        }
        return allUser
    }
}