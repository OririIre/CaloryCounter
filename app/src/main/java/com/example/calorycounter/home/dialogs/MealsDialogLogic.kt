package com.example.calorycounter.home.dialogs

import android.content.Context
import android.icu.text.SimpleDateFormat
import com.example.calorycounter.data.DataHandler
import com.example.calorycounter.helpers.HelperClass
import java.util.Date
import java.util.Locale
import com.example.calorycounter.helpers.mealsFile
import com.example.calorycounter.helpers.caloriesFile
import com.example.calorycounter.helpers.proteinFile
import com.example.calorycounter.helpers.historyFile
import com.example.calorycounter.helpers.iconFile

class MealsDialogLogic (con: Context){
    private val dataHandler = DataHandler()
    private val context = con
    private val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

    fun addMeal(mealKcal: String?, mealProt: String?, mealName: String) {
        val historyMap = mutableMapOf<String, String>()
        val currentTime = HelperClass.getCurrentDateAndTime()

        mealKcal?.toDoubleOrNull()?.let { kcal ->
            val currentKcalValue = HelperClass.getCurrentValue(caloriesFile, context) + kcal
            dataHandler.saveData(context, caloriesFile, currentDate, currentKcalValue.toString())
            historyMap[currentTime + "_calo"] = kcal.toString()
        }

        mealProt?.toDoubleOrNull()?.let { protein ->
            val currentProteinValue = HelperClass.getCurrentValue(proteinFile, context) + protein
            dataHandler.saveData(context, proteinFile, currentDate, currentProteinValue.toString())
            historyMap[currentTime + "_prot"] = protein.toString()
        }

        historyMap[currentTime + "_name"] = mealName
        dataHandler.saveMapDataNO(context, historyFile, historyMap)
    }

    fun deleteMeal(mealNumber: Int){
        if(mealNumber != 0) {
            val keyPrefix = "Meal$mealNumber"
            deleteMeal(
                keyPrefix + "Name",
                keyPrefix + "Cal",
                keyPrefix + "Prot",
                keyPrefix + "Icon"
            )

            val currentMeals = dataHandler.loadData(context, mealsFile)
            val currentIcons = dataHandler.loadData(context, iconFile)

            val updatedMeals = currentMeals.entries.toList()
                .filter { !it.key.startsWith(keyPrefix) }
                .mapIndexed { index, (key, value) ->
                    val newIndex = index / 3
                    val newKey = "Meal${(newIndex + 1)}${key.substringAfter(key.take(5))}"
                    newKey to value
                }
                .toMap().toMutableMap()

            val updatedIcons = currentIcons.entries.toList()
                .filter { !it.key.startsWith(keyPrefix) }
                .mapIndexed { index, (key, value) ->
                    val newKey = "Meal${index + 1}${key.substringAfter(key.take(5))}"
                    newKey to value
                }
                .toMap().toMutableMap()

            dataHandler.saveMapData(context, mealsFile, updatedMeals)
            dataHandler.saveMapData(context, iconFile, updatedIcons)
        }
    }

    private fun deleteMeal(keyName: String, keyCal: String, keyProt: String, keyIcons: String){
        dataHandler.deleteMapEntriesWithKeys(context, mealsFile, keyName)
        dataHandler.deleteMapEntriesWithKeys(context, mealsFile, keyCal)
        dataHandler.deleteMapEntriesWithKeys(context, mealsFile, keyProt)
        dataHandler.deleteMapEntriesWithKeys(context, iconFile, keyIcons)
    }
}