package com.romellfudi.fudinfc.app.data

class UserRepository {

    //region User table
    fun replaceUser(user: NfcUser) {
        TODO("implement")
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