package com.example.calorycounter.chart

import android.content.Context
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import com.example.calorycounter.R
import java.text.SimpleDateFormat
import java.util.Locale

class ChartCardCreation (con: Context){
    private val context = con

    fun prepareCards(chartDataCalories: MutableMap<String, String>, chartDataProtein: MutableMap<String, String>): List<CardView>{
        val allKeys = (chartDataCalories.keys + chartDataProtein.keys).distinct()
        return allKeys.sortedByDescending { it }
            .take(30)
            .map { key ->
                createCards(
                    formatString(key),
                    chartDataCalories[key] ?: "0",
                    chartDataProtein[key] ?: "0"
                )
            }
    }

    private fun createCards(dateString: String, caloriesString: String, proteinString: String): CardView {
        val cardLayout = createCardLayout()
        val relativeLayout = createRelativeLayout()
        val caloriesDescription = createTextView(
            RelativeLayout.CENTER_HORIZONTAL, 0, 0, context.getString(
                R.string.Calories), 15f)
        val proteinDescription = createTextView(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.BELOW, caloriesDescription.id, "Protein", 12f)
        val calories = createTextView(RelativeLayout.ALIGN_PARENT_END, 0, 0, caloriesString.toDouble().toInt().toString(), 15f)
        val protein = createTextView(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.BELOW, calories.id, proteinString.toDouble().toInt().toString(), 15f)
        val date = createTextView(RelativeLayout.CENTER_VERTICAL, 0, 0, dateString, 15f)

        caloriesDescription.setPadding(205, 0, 0, 0)
        proteinDescription.setPadding(165, 0, 0, 0)

        relativeLayout.addView(caloriesDescription)
        relativeLayout.addView(proteinDescription)
        relativeLayout.addView(calories)
        relativeLayout.addView(protein)
        relativeLayout.addView(date)

        cardLayout.addView(relativeLayout)

        return cardLayout
    }

    private fun createCardLayout(): CardView {
        val cardLayout = CardView(context)
        val cardPparam: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        cardPparam.setMargins(15, 10, 15, 10)
        cardLayout.layoutParams = cardPparam
        cardLayout.cardElevation = 10f
        cardLayout.radius = 15f
        cardLayout.preventCornerOverlap = true
        cardLayout.setCardBackgroundColor(ResourcesCompat.getColor(context.resources, R.color.almostDarkestGrey, null))

        return cardLayout
    }

    private fun createRelativeLayout(): RelativeLayout {
        val relativeLayout = RelativeLayout(context)
        val layoutParam: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        relativeLayout.layoutParams = layoutParam
        relativeLayout.setPadding(20)

        return relativeLayout
    }

    private fun createTextView(firstParam: Int, secondParam: Int, thirdParam:Int, valueText: String, textSize: Float): TextView {
        val textView = TextView(context)
        textView.id = View.generateViewId()
        val textParam: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        if(firstParam != 0) {
            textParam.addRule(firstParam)
        }
        if(secondParam != 0){
            textParam.addRule(secondParam, thirdParam)
        }
        textView.text = valueText
        textView.textSize = textSize
        textView.setTextColor(ResourcesCompat.getColor(context.resources, R.color.white, null))
        textView.layoutParams = textParam

        return textView
    }

    private fun formatString(date: String): String{
        var newDateString = "19900101"
        try {
            val formatString = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            println(date)
            val newDate = formatString.parse(date)
            newDateString = newDate?.toString()?.removeRange(11, 30).toString()
        }
        catch (e: Exception) {
            println("Formatting exception $e")
        }

        return newDateString
    }
}