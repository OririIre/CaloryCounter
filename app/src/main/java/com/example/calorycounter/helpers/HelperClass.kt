package com.example.calorycounter.helpers

import android.content.Context
import android.icu.text.SimpleDateFormat
import com.example.calorycounter.data.DataHandler
import java.util.Date
import java.util.Locale

class HelperClass {
    companion object {
        private val dataHandler = DataHandler()
        fun getCurrentDateAndTime(): String {
            val currentDateTime = SimpleDateFormat("dd.MMHH:mm:ss", Locale.getDefault()).format(Date())
            return currentDateTime
        }

        fun getCurrentValue(fileName: String, context: Context): Double {
            val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            val data = dataHandler.loadData(context, fileName)
            return data[currentDate]?.toDoubleOrNull() ?: 0.0
        }
    }
}