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
        }
            val currentMeals = dataHandler.loadData(context, mealsFile)
            val currentIcons = dataHandler.loadData(context, iconFile)

        //ToDo Das hier verbessern, funktioniert fast macht immer
        //ToDo {Meal1Meal2Name=Tee, Meal2Meal2Cal=20.0, Meal3Prot=1.0, Meal4Name=Spagetthi, Meal5Meal3Cal=780.0, Meal6Meal3Prot=23.0}

//            val updatedMeals = currentMeals.entries.toList()
//                .filter { !it.key.startsWith(keyPrefix) }
//                .mapIndexed { index, (key, value) ->
//                    val newKey = "Meal${index + 1}${key.substringAfter(index.toString())}"
//                    newKey to value
//                }
//                .toMap().toMutableMap()
//
//            val updatedIcons = currentIcons.entries.toList()
//                .filter { !it.key.startsWith(keyPrefix) }
//                .mapIndexed { index, (key, value) ->
//                    val newKey = "Meal${index + 1}${key.substringAfter(index.toString())}"
//                    newKey to value
//                }
//                .toMap().toMutableMap()
//
//            println(updatedMeals)
//
//          dataHandler.saveMapData(context, mealsFile, updatedMeals)
//          dataHandler.saveMapData(context, iconFile, updatedIcons)

        val mealsCount = ((currentMeals.count()/3)+1)
        val iconCount = currentIcons.count()
        val newMeals = mutableMapOf<String, String>()
        val newIcons = mutableMapOf<String, String>()

        saveMeals(currentMeals, newMeals, mealsCount)

        saveIcon(currentIcons, newIcons, iconCount)
    }

    private fun deleteMeal(keyName: String, keyCal: String, keyProt: String, keyIcons: String){
        dataHandler.deleteMapEntriesWithKeys(context, mealsFile, keyName)
        dataHandler.deleteMapEntriesWithKeys(context, mealsFile, keyCal)
        dataHandler.deleteMapEntriesWithKeys(context, mealsFile, keyProt)
        dataHandler.deleteMapEntriesWithKeys(context, iconFile, keyIcons)
    }

    private fun saveMeals(currentMeals: MutableMap<String,String>, newMeals: MutableMap<String,String>, mealsCount: Int){
        var i = 1
        var l = 1
        for(items in currentMeals) {
            var name = "Meal" + i.toString() + "Name"
            var value = "Meal" + i.toString() +"Cal"
            var protValue = "Meal" + i.toString() +"Prot"
            if (items.key.contains(name)){
                newMeals += mutableMapOf(name to currentMeals[name].toString())
                newMeals += mutableMapOf(value to currentMeals[value].toString())
                newMeals += mutableMapOf(protValue to currentMeals[protValue].toString())
            }
            else if (!items.key.contains(name) && !items.key.contains(value) && !items.key.contains(protValue)) {
                for (k in i..mealsCount) {
                    name = "Meal" + k.toString() + "Name"
                    value = "Meal" + k.toString() +"Cal"
                    protValue = "Meal" + k.toString() +"Prot"
                    if(currentMeals.containsKey("Meal" + (k + 1).toString() + "Name")) {
                        newMeals += mutableMapOf(name to currentMeals["Meal" + (k+1).toString() + "Name"].toString())
                        newMeals += mutableMapOf(value to currentMeals["Meal" + (k+1).toString() + "Cal"].toString())
                        newMeals += mutableMapOf(protValue to currentMeals["Meal" + (k+1).toString() + "Prot"].toString())
                    }
                    else {
                        break
                    }
                }
                break
            }
            if (l%3 == 0){
                i++
            }
            l++
        }
        dataHandler.saveMapData(context, mealsFile, newMeals)
    }

    private fun saveIcon(currentIcons: MutableMap<String,String>, newIcons: MutableMap<String,String>, iconCount: Int){
        var x = 1
        for(items in currentIcons) {
            val keyIcon = "Meal" + x.toString() +"Icon"
            if (!items.key.contains(keyIcon)) {
                newIcons += mutableMapOf(keyIcon to currentIcons["Meal" + (x+1).toString() + "Icon"].toString())
            }
            else if (items.key.contains(keyIcon)){
                newIcons += mutableMapOf(keyIcon to currentIcons[keyIcon].toString())
            }
            if(x >= iconCount)
                break
            x++
        }
        dataHandler.saveMapData(context, iconFile, newIcons)
    }
}