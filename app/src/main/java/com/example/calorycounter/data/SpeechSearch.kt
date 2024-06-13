package com.example.calorycounter.data

import android.content.Context
import android.icu.text.SimpleDateFormat
import androidx.core.content.ContentProviderCompat.requireContext
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
    fun searchRequest(amount: Array<String>): Document {
        var result = Document("")
        val urlString =
            "https://www.google.com/search?q=" + amount[1].trim() + "+calories+per+100+g"
        val url = URL(urlString)
        try {
            val doc: Document = Jsoup.parse(url, 3 * 1000)
            result = doc
        } catch (e: IOException) {
            println("error")
        }

        return result
    }

    fun extractValues(doc: Document, className: String): String {
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

    fun addFromSpeech(value: String, amount: String) {
        val historyMap = mutableMapOf<String, String>()
        val currentTime = HelperClass.getCurrentDateAndTime()
        if (StringUtil.isNumeric(amount.trim())) {
            var currentKcal = getCurrentValue(caloriesFile, context)
            var consumed = 0.0
            if (value != "" && amount != "") {
                if (value.toDouble() > 0.0 && amount.trim().toDouble() > 0.0) {
                    consumed = (value.toDouble() * (amount.trim().toDouble() / 100))
                    currentKcal += consumed
                }
            }
            dataHandler.saveData(context, caloriesFile, currentDate, currentKcal.toString())
            historyMap += mutableMapOf((currentTime + "_calo") to consumed.toString())
            dataHandler.saveMapDataNO(context, historyFile, historyMap)
        }
    }
}