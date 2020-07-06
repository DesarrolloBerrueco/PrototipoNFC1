package com.romellfudi.fudinfc.app.data

import java.text.SimpleDateFormat
import java.util.*

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
            //TODO finish
            val typePrint = when(entryType) {
                EntryType.IN -> "Entrada"
                EntryType.OUT -> "Salida"
            }

            val pattern = "dd/MM/yyyy HH:mm:ss"
            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            val timestampFormat = sdf.format(Date(timestamp))
            return "$typePrint: $nfcId || $timestampFormat"
        }
}