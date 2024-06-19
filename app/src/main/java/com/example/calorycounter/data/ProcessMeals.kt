package com.example.calorycounter.data

import android.content.Context
import android.icu.text.SimpleDateFormat
import com.example.calorycounter.view.dataHandler
import com.example.calorycounter.view.iconFile
import com.example.calorycounter.view.mealsFile
import java.util.Date
import java.util.Locale

class ProcessMeals (con: Context){
    private val context = con
    private val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

    fun addMeal(mealKcal: String, mealProt: String, mealName: String) {
        val historyMap = mutableMapOf<String, String>()
        val currentTime = HelperClass.getCurrentDateAndTime()
        if (mealKcal != "value" && mealKcal != "") {
            val currentKcalValue = HelperClass.getCurrentValue(
                com.example.calorycounter.view.caloriesFile,
                context
            ) + mealKcal.toDouble()
            dataHandler.saveData(context,
                com.example.calorycounter.view.caloriesFile, currentDate, currentKcalValue.toString())
            historyMap += mutableMapOf((currentTime + "_calo") to mealKcal)
        }
        if (mealProt != "value" && mealProt != "") {
            val currentProteinValue = HelperClass.getCurrentValue(
                com.example.calorycounter.view.proteinFile,
                context
            ) + mealProt.toDouble()
            dataHandler.saveData(context,
                com.example.calorycounter.view.proteinFile, currentDate, currentProteinValue.toString())
            historyMap += mutableMapOf((currentTime + "_prot") to mealProt)
        }
        historyMap += mutableMapOf((currentTime + "_name") to mealName)
        dataHandler.saveMapDataNO(context,
            com.example.calorycounter.view.historyFile, historyMap)
    }

    fun deleteMeal(mealNumber: Int){
        var keyName = ""
        var keyCal = ""
        var keyProt = ""
        var keyIcons = ""
        if(mealNumber != 0){
            keyName = "Meal" + mealNumber.toString() +"Name"
            keyCal = "Meal" + mealNumber.toString() +"Cal"
            keyProt = "Meal" + mealNumber.toString() +"Prot"
            keyIcons = "Meal" + mealNumber.toString() +"Icon"
        }
        dataHandler.deleteMapEntriesWithKeys(context, mealsFile, keyName)
        dataHandler.deleteMapEntriesWithKeys(context, mealsFile, keyCal)
        dataHandler.deleteMapEntriesWithKeys(context, mealsFile, keyProt)
        dataHandler.deleteMapEntriesWithKeys(context, iconFile, keyIcons)
        val currentMeals = dataHandler.loadData(context, mealsFile)
        val currentIcons = dataHandler.loadData(context, iconFile)
        val mealsCount = ((currentMeals.count()/3)+1)
        val iconCount = currentIcons.count()
        val newMeals = mutableMapOf<String, String>()
        val newIcons = mutableMapOf<String, String>()
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
        var x = 1
        dataHandler.saveMapData(context, mealsFile, newMeals)
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