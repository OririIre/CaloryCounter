package com.example.calorycounter.data

import android.content.Context
import android.icu.text.SimpleDateFormat
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.calorycounter.R
import com.example.calorycounter.data.HelperClass.Companion.getCurrentValue
import com.example.calorycounter.view.Keys
import com.example.calorycounter.view.Value
import com.example.calorycounter.view.caloriesFile
import com.example.calorycounter.view.dataHandler
import com.example.calorycounter.view.historyFile
import org.jsoup.Jsoup
import org.jsoup.internal.StringUtil
import org.jsoup.nodes.Document
import java.io.IOException
import java.net.URL
import java.util.Date
import java.util.Locale

class SpeechSearch (con: Context) {
    private val context = con
    private val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

    fun filterInput(input: String): Array<String> {
        var resultArray = emptyArray<String>()
        var item = ""
        var value = ""
        var returnArray = emptyArray<String>()
        if (input != "") {
            var newText = input.replace("[", "")
            newText = newText.replace("]", "")
            resultArray = newText.split(" ").toTypedArray()

            value = filterNumeric(resultArray)
            item = filterItem(resultArray)

            if(value != "" && item != "")
            {
                returnArray = arrayOf(item, value)
            }
            else {
                println("could no find value or item in search request")
            }
        }
        return returnArray
    }

    private fun filterNumeric(resultArray: Array<String>): String {
        var value = ""
        for (results in resultArray) {
            if (results != "") {
                if (StringUtil.isNumeric(results)) {
                    value = results
                } else if (results.endsWith("g")) {
                    if (StringUtil.isNumeric(results.removeSuffix("g"))) {
                        value = results
                    }
                } else if (results.endsWith("gram")) {
                    if (StringUtil.isNumeric(results.removeSuffix("gram"))) {
                        value = results.removeSuffix("gram")
                    }
                } else if (results.endsWith("gramm")) {
                    if (StringUtil.isNumeric(results.removeSuffix("gramm"))) {
                        value = results.removeSuffix("gramm")
                    }
                } else if (results == context.getString(R.string.one)){
                    value = "1"
                } else if (results == context.getString(R.string.two)){
                    value = "2"
                } else if (results == context.getString(R.string.three)){
                    value = "3"
                } else if (results == context.getString(R.string.four)){
                    value = "4"
                } else if (results == context.getString(R.string.five)){
                    value = "5"
                } else if (results == context.getString(R.string.six)){
                    value = "6"
                } else if (results == context.getString(R.string.seven)){
                    value = "7"
                } else if (results == context.getString(R.string.eight)){
                    value = "8"
                } else if (results == context.getString(R.string.nine)){
                    value = "9"
                }
            }
        }
        return value
    }

    private fun filterItem(resultArray: Array<String>): String {
        var item = ""
        for (results in resultArray) {
            if (results != "") {
                if (results != "g" && results != "gram" && results != "gramm") {
                    item += "$results+"
                }
            }
        }
        return item
    }

    fun searchRequest(resultArray: Array<String>, query: String): Document {
        var result = Document("")
        val input = resultArray[0]
        val urlString =
            "https://www.google.com/search?q=" + input.trim() + query + "+per+100+g"
        val url = URL(urlString)
        println(urlString)
        try {
            val doc: Document = Jsoup.parse(url, 3 * 1000)
            result = doc
        } catch (e: IOException) {
            println("error")
        }
        return result
    }

    fun extractCaloriesValues(doc: Document, className: String): String {
        val element = doc.getElementsByClass(className)
        var value = ""
        if (element.isNotEmpty()) {
            for (i in element.indices) {
                var text = element[i].text()
                text = text.lowercase()
                if (text.contains("kcal")) {
                    val splitText = text.split("kcal")
                    var leftSide = splitText[0].trim()
                    leftSide = leftSide.takeLast(3)
                    leftSide = leftSide.trim()
                    var rightSide = splitText[1].trim()
                    rightSide = rightSide.take(3)
                    rightSide = rightSide.trim()
                    if (StringUtil.isNumeric(leftSide)) {
                        value = leftSide
                        break
                    } else if (StringUtil.isNumeric(rightSide)) {
                        value = rightSide
                        break
                    }
                } else if (text.contains("calories")) {
                    val splitText = text.split("calories")
                    var leftSide = splitText[0].trim()
                    leftSide = leftSide.takeLast(3)
                    leftSide = leftSide.trim()
                    var splitRightSide: List<String>
                    var rightSide = splitText[1].trim()
                    if (rightSide.contains(":")) {
                        rightSide = rightSide.replace(":", "")
                        rightSide = rightSide.trim()
                    }
                    splitRightSide = if (rightSide.take(3).contains(".")) {
                        rightSide.split(".")
                    } else if (rightSide.take(3).contains(",")) {
                        rightSide.split(",")
                    } else
                        listOf(rightSide.take(3))
                    if (StringUtil.isNumeric(leftSide)) {
                        value = leftSide
                        break
                    } else if (StringUtil.isNumeric(splitRightSide[0])) {
                        value = splitRightSide[0]
                        break
                    }
                }
            }
        }
        return value
    }

    fun extractProteinValues(doc: Document, className: String): String {
        val element = doc.getElementsByClass(className)
        var value = ""
        if (element.isNotEmpty()) {
            for (i in element.indices) {
                var text = element[i].text()
                text = text.lowercase()
                if (text.contains("protein")) {
                    val splitText = text.split("protein")
                    val leftSideSplitArray = splitText[0].split(" ")
                    val rightSideSplitArray = splitText[1].split(" ")
                    leftSideSplitArray[leftSideSplitArray.size-1]
                    value = checkNumeric(leftSideSplitArray[leftSideSplitArray.size-1])
                    if(value == ""){
                        value = checkNumeric(leftSideSplitArray[leftSideSplitArray.size-2])
                    }
                    if(value == ""){
                        value = checkNumeric(rightSideSplitArray[0])
                    }
                    if(value == ""){
                        value = checkNumeric(rightSideSplitArray[1])
                    }
                }
            }
        }
        return value
    }

    private fun checkNumeric (input: String): String{
        var returnValue = ""
        if (StringUtil.isNumeric(input)) {
            returnValue = input
        } else if (input.endsWith("g")) {
            if (StringUtil.isNumeric(input.removeSuffix("g"))) {
                returnValue = input
            }
        } else if (input.endsWith("gram")) {
            if (StringUtil.isNumeric(input.removeSuffix("gram"))) {
                returnValue = input.removeSuffix("gram")
            }
        } else if (input.endsWith("gramm")) {
            if (StringUtil.isNumeric(input.removeSuffix("gramm"))) {
                returnValue = input.removeSuffix("gramm")
            }
        }
        return returnValue
    }

    fun addFromSpeech(value: String, amount: String, name: String, fileType: String) {
        val historyMap = mutableMapOf<String, String>()
        val currentTime = HelperClass.getCurrentDateAndTime()
        if (StringUtil.isNumeric(amount.trim())) {
            var currentKcal = getCurrentValue(fileType, context)
            var consumed = 0.0
            if (value != "" && amount != "") {
                if (value.toDouble() > 0.0 && amount.trim().toDouble() > 0.0) {
                    consumed = (value.toDouble() * (amount.trim().toDouble() / 100))
                    currentKcal += consumed
                }
            }
            dataHandler.saveData(context, fileType, currentDate, currentKcal.toString())
            historyMap += if(fileType == caloriesFile){
                mutableMapOf((currentTime + "_calo") to consumed.toString())
            } else {
                mutableMapOf((currentTime + "_prot") to consumed.toString())
            }
            historyMap += mutableMapOf((currentTime + "_name") to name)
            dataHandler.saveMapDataNO(context, historyFile, historyMap)
        }
    }
}