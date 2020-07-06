package com.romellfudi.fudinfc.app.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.romellfudi.fudinfc.app.BuildConfig

class UserRepository(context: Context) {

    companion object {
        val TAG = UserRepository::class.simpleName!!
        const val KEY_USER_TABLE = "key_user_table"
        const val KEY_ENTRY_LOG_TABLE = "key_entry_log_table"
    }

    private var preferences: SharedPreferences? = null
    private var aplicationName: String = BuildConfig.APPLICATION_ID

    init {
        this.preferences = context.getSharedPreferences(aplicationName, Context.MODE_PRIVATE)
    }

    //region User table
    var userList: List<NfcUser>
        get() {
            val json = preferences!!.getString(KEY_USER_TABLE, "") ?: ""
            return if(json.isBlank()) {
                listOf()
            } else {
                //Try to deserialize user list json
                try {
                    val list: List<NfcUser> = Gson().fromJson(json, object : TypeToken<List<NfcUser>>() {}.type)
                    list
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                    listOf()
                }
            }
        }
        set(value) {
            val json = Gson().toJson(value, object : TypeToken<List<NfcUser>>() {}.type)
            val editPrefs = preferences!!.edit()
            editPrefs.putString(KEY_USER_TABLE, json)
            editPrefs.commit()
        }


    fun replaceUser(user: NfcUser) {
        //Remove user from users if found
        val users = userList.toMutableList()
        val userIterator = users.iterator()
        while(userIterator.hasNext()) {
            val item = userIterator.next()
            if(item.nfcId == user.nfcId) {
                userIterator.remove()
            }
        }

        //Add user to the end of the list
        users.add(user)
        //Persist changes made to SharedPreferences
        userList = users
    }

    fun getUserById(idNfc: String): NfcUser? {
        TODO("implement")
        return null
    }
    //endregion

    //region Log table
    fun insertLog(log: NfcEntryLog) {
        TODO("implement")
    }

    fun getAllLogsByUser(nfcId: String): List<NfcEntryLog> {
        TODO("implement")
    }

    fun getAllLogs(): List<NfcEntryLog> {
        TODO("implement")
    }
    //endregion
}