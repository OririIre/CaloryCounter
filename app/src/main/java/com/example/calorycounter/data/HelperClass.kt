package com.example.calorycounter.data

import java.util.Calendar
import java.util.Locale

class HelperClass {
    companion object {
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
    }
}