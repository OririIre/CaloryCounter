package com.example.calorycounter.data

import android.content.Context
import android.icu.text.SimpleDateFormat
import com.example.calorycounter.view.dataHandler
import java.util.Date
import java.util.Locale

class ProcessFreeAdd (con: Context) {
    private val context = con
    private val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
    private val caloriesFile = "calLog.txt"
    private val proteinFile = "protLog.txt"
    private val historyFile = "history.txt"
    fun addSub(calProtSwitch: Boolean, perGrammValue: String, weightValue: String, customValue: String) {
        val historyMap = mutableMapOf<String, String>()
        val currentTime = HelperClass.getCurrentDateAndTime()
        if (calProtSwitch) {
            val currentKcalValue = calcValue(caloriesFile, perGrammValue, weightValue, customValue)
            dataHandler.saveData(context, caloriesFile, currentDate, currentKcalValue.toString())
            if (customValue != "") {
                historyMap += mutableMapOf((currentTime + "_calo") to customValue)
            }
            else if (perGrammValue != "" && weightValue != "") {
                val caloriesDouble = perGrammValue.toDouble()
                val grammDouble = weightValue.toDouble()
                historyMap += mutableMapOf((currentTime + "_calo") to (caloriesDouble * (grammDouble / 100)).toString())
            }
        } else {
            val currentProteinValue = calcValue(proteinFile, perGrammValue, weightValue, customValue)
            println(currentProteinValue)
            dataHandler.saveData(context, proteinFile, currentDate, currentProteinValue.toString())
            if (customValue != "") {
                historyMap += mutableMapOf((currentTime + "_prot") to customValue)
            }
            else if (perGrammValue != "" && weightValue != "") {
                val proteinDouble = perGrammValue.toDouble()
                val grammDouble = weightValue.toDouble()
                historyMap += mutableMapOf((currentTime + "_prot") to (formatString((proteinDouble * (grammDouble / 100)).toString())))
            }
        }
        dataHandler.saveMapDataNO(context, historyFile, historyMap)
    }

    private fun calcValue(fileName: String, value: String, gramm: String, custom: String): Double {
        var currentKcal = formatString(HelperClass.getCurrentValue(fileName, context).toString()).toDouble()
        if (custom != "") {
            currentKcal += custom.toDouble()
        }
        else if (value != "" && gramm != "") {
            if (value.toDouble() > 0.0 && gramm.toDouble() > 0.0) {
                currentKcal += (value.toDouble() * (gramm.toDouble() / 100))
            }
        }
        currentKcal = formatString(currentKcal.toString()).toDouble()
        return currentKcal
    }

    private fun formatString (value: String): String {
        var returnString = ""
        if(value != "") {
            returnString = String.format(Locale.getDefault(), "%.1f", value.toDouble())
        }
        return returnString
    }
}