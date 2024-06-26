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
        val (fileName, currentValue) = if (calProtSwitch) {
            caloriesFile to calcValue(caloriesFile, perGrammValue, weightValue, customValue)
        } else {
            proteinFile to calcValue(proteinFile, perGrammValue, weightValue, customValue)}

        dataHandler.saveData(context, fileName, currentDate, currentValue.toString())

        val historyMap = addToHistory(mutableMapOf(), customValue, perGrammValue, weightValue, if (calProtSwitch) "_calo" else "_prot")
        dataHandler.saveMapDataNO(context, historyFile, historyMap)
    }

    private fun addToHistory(historyMap: MutableMap<String, String>, customValue: String, perGrammValue: String, weightValue: String, keyType: String): MutableMap<String, String>{
        val currentTime = HelperClass.getCurrentDateAndTime()

        when {
            customValue.isNotEmpty() -> {
                historyMap[currentTime + keyType] = customValue.replace(",",".")
            }
            perGrammValue.isNotEmpty() && weightValue.isNotEmpty() -> {
                val valueDouble = perGrammValue.replace(",",".").toDoubleOrNull() ?: 0.0
                val grammDouble = weightValue.replace(",",".").toDoubleOrNull() ?: 0.0
                historyMap[currentTime + keyType] = (valueDouble * (grammDouble / 100)).toString()
            }
        }

        return historyMap
    }

    private fun calcValue(fileName: String, value: String, gramm: String, custom: String): Double {
        var currentValue = HelperClass.getCurrentValue(fileName, context)
        when {
            custom.isNotEmpty() -> {
                currentValue += custom.replace(",",".").toDoubleOrNull() ?: 0.0
            }
            value.isNotEmpty() && gramm.isNotEmpty() -> {
                val valueDouble = value.replace(",",".").toDoubleOrNull() ?:0.0
                val grammDouble = gramm.replace(",",".").toDoubleOrNull() ?: 0.0
                if (valueDouble > 0.0 && grammDouble > 0.0) {
                    currentValue += (valueDouble * (grammDouble / 100))
                }
            }
        }
        return currentValue
    }
}