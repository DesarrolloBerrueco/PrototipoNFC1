package com.romellfudi.fudinfc.app.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.romellfudi.fudinfc.app.BuildConfig
import com.romellfudi.fudinfc.util.async.Nfc
import java.util.*
import kotlin.Comparator

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
                listOf<NfcUser>()
            } else {
                //Try to deserialize user list json
                try {
                    val list: List<NfcUser> = Gson().fromJson(json, object : TypeToken<List<NfcUser>>() {}.type)
                    list
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                    listOf<NfcUser>()
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
        return userList.find { it.nfcId == idNfc }
    }
    //endregion

    //region Log table
    var entryLogList: List<NfcEntryLog>
        get() {
            val json = preferences!!.getString(KEY_ENTRY_LOG_TABLE, "") ?: ""
            return if(json.isBlank()) {
                listOf<NfcEntryLog>()
            } else {
                //Try to deserialize user list json
                try {
                    val list: List<NfcEntryLog> = Gson().fromJson(json, object : TypeToken<List<NfcEntryLog>>() {}.type)
                    list
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                    listOf<NfcEntryLog>()
                }
            }
        }
        set(value) {
            val json = Gson().toJson(value, object : TypeToken<List<NfcEntryLog>>() {}.type)
            val editPrefs = preferences!!.edit()
            editPrefs.putString(KEY_ENTRY_LOG_TABLE, json)
            editPrefs.commit()
        }

    val tablesJson: String
        get() {
            val userJson = preferences!!.getString(KEY_USER_TABLE, "") ?: ""
            val logJson = preferences!!.getString(KEY_ENTRY_LOG_TABLE, "") ?: ""
            return userJson + "\n\n" + logJson
        }

    fun insertLog(nfcId: String, type: EntryType) {
        //TODO careful timestamp is working in GMT, should work with UTC which is more consistent
        val timestamp = Calendar.getInstance().time.time
        insertLog(NfcEntryLog(nfcId, timestamp, type))
    }

    fun insertLog(item: NfcEntryLog) {
        val list = entryLogList.toMutableList()
        list.add(item)
        entryLogList = list
    }

    fun getAllLogsByUser(nfcId: String): List<NfcEntryLog> {
        TODO("implement")
    }

    fun getLastLog(): NfcEntryLog? {
        val list = entryLogList.toMutableList()
        return list.maxBy { it.timestamp }
    }

    fun getLogsSorted(): List<CompleteEntryLog> {
        //Key-Value map of idNfc -> dni
        val userMap = userList.map { it.nfcId to it.dni }.toMap()
        val sortedLogs = entryLogList.sortedBy { it.timestamp }
        return sortedLogs.map {
            CompleteEntryLog(it, userMap[it.nfcId] ?: "...")
        }
    }

    fun getAllLogs(): List<NfcEntryLog> {
        return entryLogList
    }
    //endregion
}