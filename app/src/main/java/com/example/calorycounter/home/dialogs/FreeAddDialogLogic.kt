package com.example.calorycounter.home.dialogs

import android.content.Context
import android.icu.text.SimpleDateFormat
import com.example.calorycounter.data.DataHandler
import com.example.calorycounter.helpers.HelperClass
import java.util.Date
import java.util.Locale

class FreeAddDialogLogic (con: Context) {
    private val dataHandler = DataHandler()
    private val context = con
    private val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
    private val caloriesFile = "calLog.txt"
    private val proteinFile = "protLog.txt"
    private val historyFile = "history.txt"

    fun addSub(calProtSwitch: Boolean, perGrammValue: String, weightValue: String, customValue: String) {
        var historyMap = mutableMapOf<String, String>()

        if (calProtSwitch) {
            val currentKcalValue = calcValue(caloriesFile, perGrammValue, weightValue, customValue)
            dataHandler.saveData(context, caloriesFile, currentDate, currentKcalValue.toString())
            historyMap = addToHistory(historyMap, customValue, perGrammValue, weightValue, "_calo")
        } else {
            val currentProteinValue = calcValue(proteinFile, perGrammValue, weightValue, customValue)
            dataHandler.saveData(context, proteinFile, currentDate, currentProteinValue.toString())
            historyMap = addToHistory(historyMap, customValue, perGrammValue, weightValue, "_prot")
        }
        dataHandler.saveMapDataNO(context, historyFile, historyMap)
    }

    private fun addToHistory(historyMap: MutableMap<String, String>, customValue: String, perGrammValue: String, weightValue: String, keyType: String): MutableMap<String, String>{
        val currentTime = HelperClass.getCurrentDateAndTime()
        if (customValue != "") {
            historyMap += mutableMapOf((currentTime + keyType) to customValue)
        }
        else if (perGrammValue != "" && weightValue != "") {
            val valueDouble = perGrammValue.toDouble()
            val grammDouble = weightValue.toDouble()
            historyMap += mutableMapOf((currentTime + keyType) to (valueDouble * (grammDouble / 100)).toString())
        }
        return historyMap
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