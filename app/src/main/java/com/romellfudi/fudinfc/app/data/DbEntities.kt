package com.romellfudi.fudinfc.app.data

data class NfcUser(
        val nfcId: String,
        val dni: String
)

enum class EntryType {
    IN,
    OUT
}

data class NfcEntryLog(
        val nfcId: String,
        val timestamp: Long,
        val entryType: EntryType
)