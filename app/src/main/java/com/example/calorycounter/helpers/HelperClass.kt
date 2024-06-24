package com.example.calorycounter.helpers

import android.content.Context
import android.icu.text.SimpleDateFormat
import com.example.calorycounter.data.DataHandler
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HelperClass {
    companion object {
        private val dataHandler = DataHandler()
        fun getCurrentDateAndTime(): String {
            val calendar  = Calendar.getInstance()
            val currentTime = String.format(
                Locale.getDefault(), "%02d.%02d%02d:%02d:%02d",
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH)+1,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND))
            return currentTime
        }

        fun getCurrentValue(fileName: String, context: Context): Double {
            val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            val out = dataHandler.loadData(context, fileName)
            val currentValue = if (out.containsKey(currentDate)) {
                out[currentDate]?.toDouble()!!
            } else {
                0.0
            }
            return currentValue
        }
    }
}