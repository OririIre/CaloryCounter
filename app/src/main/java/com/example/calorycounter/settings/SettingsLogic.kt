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
    private val dataHandler = DataHandler()

     fun expandPanel(languageVisibility: LinearLayout, toggle: Boolean): Boolean{
         languageVisibility.visibility = if (toggle) View.VISIBLE else View.GONE
         return !toggle
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
         selectedLanguage.takeIf { it.isNotEmpty() }?.let {
             val voiceCountryCode = convertToCountryCode(it)
             dataHandler.saveData(context, languageFile, Keys.Language.toString(), voiceCountryCode)
         }

         selectedAppLanguage.takeIf { it.isNotEmpty() && convertToCountryCode(it) != currentAppLanguage }?.let {
             val countryCode = convertToCountryCode(it)
             dataHandler.saveData(context, appLanguageFile, Keys.Language.toString(), countryCode)
             updateAppLocale(countryCode, actv)
         }
    }

    private fun updateAppLocale(countryCode: String, actv: Activity) {
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

     fun setDropdownSelected(dropdown: Spinner, languageList: Array<String>, file: String, actv: Activity): String{
         val currentAppLanguage = dataHandler.loadData(context, file)[Keys.Language.toString()].toString()
         val appLanguageIndex: Int = convertToInt(currentAppLanguage)

         val adapterLanguage: ArrayAdapter<String> =
            ArrayAdapter(actv, android.R.layout.simple_spinner_dropdown_item, languageList)
        dropdown.adapter = adapterLanguage
        dropdown.setSelection(appLanguageIndex)

         return currentAppLanguage
    }

     private fun convertToCountryCode(language: String): String{
         return when (language) {
             context.resources.getString(R.string.language_german) -> "de"
             context.resources.getString(R.string.language_english) -> "en"
             context.resources.getString(R.string.language_french) -> "fr"
             context.resources.getString(R.string.language_spanish) -> "es"
             else -> "en"
         }
    }

    private fun convertToInt(language: String): Int {
        return when (language) {
            "de" -> 1
            "en" -> 2
            "fr" -> 3
            "es" -> 4
            else -> 0
        }
    }
}