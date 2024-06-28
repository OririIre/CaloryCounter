package com.example.calorycounter.home

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.icu.text.SimpleDateFormat
import com.example.calorycounter.R
import com.example.calorycounter.data.DataHandler
import com.example.calorycounter.helpers.HelperClass
import com.example.calorycounter.helpers.HelperClass.Companion.getCurrentValue
import com.example.calorycounter.helpers.Keys
import com.example.calorycounter.helpers.caloriesFile
import com.example.calorycounter.helpers.historyFile
import com.example.calorycounter.helpers.languageFile
import org.apache.commons.lang3.LocaleUtils
import org.apache.commons.lang3.math.NumberUtils
import org.jsoup.Jsoup
import org.jsoup.internal.StringUtil
import org.jsoup.nodes.Document
import java.io.IOException
import java.net.URL
import java.util.Date
import java.util.Locale
import kotlin.math.min


class SpeechSearch (con: Context) {
    private val dataHandler = DataHandler()
    private val context = con
    private val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

    fun filterInput(input: String): Array<String> {
        var item = ""
        var returnArray = emptyArray<String>()

        if (input.isNotBlank()) {
            val sanitizedInput = input.replace(Regex("[\\[\\]]"), "")
            val resultArray = sanitizedInput.split(" ").map { it.lowercase().trim() }.toTypedArray()

            val localizedResource = getLocalizedResources(context)
            val value = filterNumeric(resultArray)
            for (stuff in resultArray) {
                if (!stuff.contains(value) && stuff != "g" && stuff != "gram" && stuff != "gramm"
                    && stuff != localizedResource.getString(R.string.one) && stuff != localizedResource.getString(R.string.two)
                    && stuff != localizedResource.getString(R.string.three) && stuff != localizedResource.getString(R.string.four)
                    && stuff != localizedResource.getString(R.string.five) && stuff != localizedResource.getString(R.string.six)
                    && stuff != localizedResource.getString(R.string.seven) && stuff != localizedResource.getString(R.string.eight)
                    && stuff != localizedResource.getString(R.string.nine)) {
                    item += stuff
                }
            }
            if(value.isNotBlank() && item.isNotBlank())
            {
                returnArray = arrayOf(item.replace(",","."), value.replace(",","."))
            }
            else {
                println("could no find value or item in search request")
            }
        }
        return returnArray
    }

    private fun filterNumeric(resultArray: Array<String>): String {
        var value = ""
        val localizedResource = getLocalizedResources(context)
        for (item in resultArray) {
            val results = item.lowercase()
            if (results.isNotBlank()) {
                if (StringUtil.isNumeric(results)) {
                    value = results
                } else if (results.removeSuffix("g").toDoubleOrNull() != null) {
                        value = results.removeSuffix("g")
                } else if (results.removeSuffix("gram").toDoubleOrNull() != null) {
                        value = results.removeSuffix("gram")
                } else if (results.removeSuffix("gramm").toDoubleOrNull() != null) {
                        value = results.removeSuffix("gramm")
                } else if (results == localizedResource.getString(R.string.one)){
                    value = "1"
                } else if (results == localizedResource.getString(R.string.two)){
                    value = "2"
                } else if (results == localizedResource.getString(R.string.three)){
                    value = "3"
                } else if (results == localizedResource.getString(R.string.four)){
                    value = "4"
                } else if (results == localizedResource.getString(R.string.five)){
                    value = "5"
                } else if (results == localizedResource.getString(R.string.six)){
                    value = "6"
                } else if (results == localizedResource.getString(R.string.seven)){
                    value = "7"
                } else if (results == localizedResource.getString(R.string.eight)){
                    value = "8"
                } else if (results == localizedResource.getString(R.string.nine)){
                    value = "9"
                }
            }
        }
        return value
    }

    private fun getLocalizedResources(context: Context): Resources {
        val locale = getVoiceLanguage()
        var conf: Configuration = context.resources.configuration
        conf = Configuration(conf)
        conf.setLocale(LocaleUtils.toLocale(locale))
        val localizedContext = context.createConfigurationContext(conf)
        return localizedContext.resources
    }

    fun searchRequest(resultArray: Array<String>, query: String): Document {
        var result = Document("")
        val input = resultArray[0].trim() + "+"
        val urlString =
            "https://www.google.com/search?q=" + input.trim() + query + "+per+100+g"
        val url = URL(urlString)
        try {
            val doc: Document = Jsoup.parse(url, 3 * 1000)
            result = doc
        } catch (e: IOException) {
            println("Parsing error $e")
        }
        return result
    }

    fun extractCaloriesValues(doc: Document, className: String): String {
        val elements = doc.getElementsByClass(className)
        for (element in elements) {
            val text = element.text().lowercase()
            val value = extractCaloriesValueFromText(text)
            if (value != null) return formatString(value)
        }

        return ""
    }

    private fun extractCaloriesValueFromText(text: String): String? {
        if (text.contains("kcal")) {
            return extractValue(text.split("kcal"))
        } else if (text.contains("calories")) {
            return extractValue(text.split("calories").map { it.replace(":", "").trim() })
        }
        return null
    }

    private fun extractValue(splitText: List<String>): String? {
        val leftSide = splitText.getOrNull(0)?.trim()?.takeLast(3)?.trim()
        val rightSide = splitText.getOrNull(1)?.trim()?.let {
            if (it.take(3).contains(".") || it.take(3).contains(",")) {
                it.split(Regex("[.,]")).getOrNull(0)?.trim()
            } else {
                it.take(3).trim()
            }
        }

        return when {
            leftSide?.isNumeric() == true -> leftSide
            rightSide?.isNumeric() == true -> rightSide
            else -> null
        }
    }

    private fun String.isNumeric(): Boolean = toDoubleOrNull() != null

    fun extractProteinValues(doc: Document, className: String): String {
        val element = doc.getElementsByClass(className)
        var value = ""
        if (element.isNotEmpty()) {
            for (i in element.indices) {
                val text = element.text().lowercase()
                if (text.contains("protein")) {
                    value = extractProteinValueFromText(text)
                }
            }
        }
        value = formatString(value)
        return value
    }

    private fun extractProteinValueFromText(text: String): String {
        var value = ""
        val splitText = text.split("protein")
        val leftSideSplitArray = splitText[0].split(" ")
        val rightSideSplitArray = splitText[1].split(" ")
        val arraySizeOne = leftSideSplitArray.size
        val arraySizeTwo = rightSideSplitArray.size
        val maxValue = min(arraySizeOne, arraySizeTwo)
        for (x in 0..<maxValue){
            if(value.isBlank()){
                value = checkNumeric(leftSideSplitArray[leftSideSplitArray.size-(x+1)])
            }
            if(value.isBlank()){
                value = checkNumeric(rightSideSplitArray[x])
            }
            if(value.isNotBlank()){
                break
            }
        }
        return value
    }

    private fun checkNumeric (inputString: String): String{
        var returnValue = ""
        var input = inputString
        if(inputString.contains(",")){
            input = inputString.replace(",",".")
        }
        input.lowercase()
        if (NumberUtils.isCreatable(input)) {
            if(input.toDouble() < 45){
                returnValue = input
            }
        } else if (input.endsWith("g")) {
            if (NumberUtils.isCreatable(input.removeSuffix("g"))) {
                if(input.removeSuffix("g").toDouble() < 45){
                    returnValue = input.removeSuffix("g")
                }
            }
        } else if (input.endsWith("gram")) {
            if (NumberUtils.isCreatable(input.removeSuffix("gram"))) {
                if(input.removeSuffix("g").toDouble() < 45){
                    returnValue = input.removeSuffix("gram")
                }
            }
        } else if (input.endsWith("gramm")) {
            if (NumberUtils.isCreatable(input.removeSuffix("gramm"))) {
                if(input.removeSuffix("g").toDouble() < 45){
                    returnValue = input.removeSuffix("gramm")
                }
            }
        }
        return returnValue
    }

    fun addFromSpeech(value: String, amount: String, name: String, fileType: String) {
        val historyMap = mutableMapOf<String, String>()
        val currentTime = HelperClass.getCurrentDateAndTime()
        val amountValue = amount.trim().replace(",",".").toDoubleOrNull() ?: return
        val min = 1
        val max = 9
        val isCountable = amount.toIntOrNull()?.let { it in min..max} ?: false

        val currentValue = getCurrentValue(fileType, context)
        val consumed = when { value.isBlank() -> 0.0
            isCountable -> value.replace(",",".").toDoubleOrNull()?.let { it * amountValue * 5 } ?: 0.0
            else -> value.replace(",",".").toDoubleOrNull()?.let{ it * (amountValue / 100) } ?: 0.0
        }

        val newTotal = currentValue + consumed
        dataHandler.saveData(context, fileType, currentDate, formatString(newTotal.toString()))

        val formattedConsumed = formatString(consumed.toString()).replace(",",".")
        println(formattedConsumed)
        val suffix = if (fileType == caloriesFile) "_calo" else "_prot"
        historyMap += mutableMapOf((currentTime + suffix) to formattedConsumed)

        historyMap += mutableMapOf((currentTime + "_name") to name)
        dataHandler.saveMapDataNO(context, historyFile, historyMap)
    }

    fun getVoiceLanguage():String{
        val selectedLanguage = dataHandler.loadData(context, languageFile)[Keys.Language.toString()].toString()
        return when (selectedLanguage) {
            "de" -> "de_DE"
            "en" -> "en_UK"
            "fr" -> "fr_FR"
            "es" -> "es_ES"
            else -> Locale.getDefault().toString()
        }
    }

    private fun formatString (value: String): String {
        return if (value.isNotBlank()) {
            String.format(Locale.getDefault(), "%.1f", value.replace(",",".").toDoubleOrNull() ?: 0.0)
        } else {
            ""
        }
    }
}