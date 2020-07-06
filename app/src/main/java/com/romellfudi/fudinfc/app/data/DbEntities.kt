package com.romellfudi.fudinfc.app.data

import com.romellfudi.fudinfc.app.Utils.formatTimestamp

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
) {
    val prettyPrint: String
        get() {
            val typePrint = when(entryType) {
                EntryType.IN -> "Entrada"
                EntryType.OUT -> "Salida"
            }
            return "$typePrint: $nfcId || ${formatTimestamp(timestamp)}"
        }
}

data class CompleteEntryLog(
        val log: NfcEntryLog,
        val dni: String
)

