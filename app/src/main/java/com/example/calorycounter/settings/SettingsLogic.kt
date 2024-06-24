package com.example.calorycounter.settings

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.calorycounter.R
import com.example.calorycounter.data.DataHandler
import com.example.calorycounter.helpers.Keys
import com.example.calorycounter.helpers.appLanguageFile
import com.example.calorycounter.helpers.goalsFile
import com.example.calorycounter.helpers.languageFile

class SettingsLogic (con: Context){
    private val context = con
    private var toggleReset = true
    private var toggleGoals = true
    private var toggleLanguage = true
    private val dataHandler = DataHandler()

     fun expandLanguage(languageVisibility: LinearLayout){
         if (toggleLanguage) {
            languageVisibility.visibility = View.VISIBLE
            toggleLanguage = false
        } else {
            languageVisibility.visibility = View.GONE
            toggleLanguage = true
        }
    }

     fun expandReset(clearVisibility: LinearLayout){
        if (toggleReset) {
            clearVisibility.visibility = View.VISIBLE
            toggleReset = false
        } else {
            clearVisibility.visibility = View.GONE
            toggleReset = true
        }
    }

     fun expandGoals(goalVisibility: LinearLayout){
        if (toggleGoals) {
            goalVisibility.visibility = View.VISIBLE
            toggleGoals = false
        } else {
            goalVisibility.visibility = View.GONE
            toggleGoals = true
        }
    }

     fun saveGoals(caloriesGoal:String, proteinGoal: String){
         if (caloriesGoal.isNotEmpty()) {
             dataHandler.saveData(
                 context, goalsFile,
                 Keys.Calories.toString(), caloriesGoal
             )
         }
         if (proteinGoal.isNotEmpty()) {
             dataHandler.saveData(
                 context, goalsFile,
                 Keys.Protein.toString(), proteinGoal
             )
         }
    }

     fun saveLanguage(selectedLanguage: String, selectedAppLanguage: String, currentAppLanguage: String, actv: Activity){
        if (selectedLanguage != "") {
            val voiceCountryCode = convertToCountryCode(selectedLanguage)
            dataHandler.saveData(context, languageFile, Keys.Language.toString(), voiceCountryCode)
        }
        if (selectedAppLanguage != "") {
            val countryCode = convertToCountryCode(selectedAppLanguage)
            if(currentAppLanguage != countryCode) {
                dataHandler.saveData(
                    context,
                    appLanguageFile,
                    Keys.Language.toString(),
                    countryCode
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.getSystemService(LocaleManager::class.java)
                        .applicationLocales = LocaleList.forLanguageTags(countryCode)
                } else {
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(countryCode)
                    )
                }
                actv.recreate()
            }
        }
    }

     fun setDropdownSelected(dropdown: Spinner, languageList: Array<String>, file: String, actv: Activity): String{
        val adapterLanguage: ArrayAdapter<String> =
            ArrayAdapter(actv, android.R.layout.simple_spinner_dropdown_item, languageList)
        dropdown.adapter = adapterLanguage

        val currentAppLanguage = dataHandler.loadData(context, file)[Keys.Language.toString()].toString()
        val appLanguage: Int = convertToInt(currentAppLanguage)
        dropdown.setSelection(appLanguage)

         return currentAppLanguage
    }

     private fun convertToCountryCode(language: String): String{
        val returnString: String
        when (language) {
            context.resources.getString(R.string.language_german) -> {
                returnString = "de"
            }
            context.resources.getString(R.string.language_english) -> {
                returnString = "en"
            }
            context.resources.getString(R.string.language_french) -> {
                returnString = "fr"
            }
            context.resources.getString(R.string.language_spanish) -> {
                returnString = "es"
            }
            else -> {
                returnString = "en"
            }
        }
        return returnString
    }

     private fun convertToInt(language: String): Int {
        val returnInt: Int
        when (language) {
            "de" -> {
                returnInt = 1
            }
            "en" -> {
                returnInt = 2
            }
            "fr" -> {
                returnInt = 3
            }
            "es" -> {
                returnInt = 4
            }
            else -> {
                returnInt = 0
            }
        }
        return returnInt
    }
}