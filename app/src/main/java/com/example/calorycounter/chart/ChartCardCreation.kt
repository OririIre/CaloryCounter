package com.example.calorycounter.chart

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.example.calorycounter.R
import com.example.calorycounter.data.DataHandler
import com.example.calorycounter.helpers.Keys
import com.example.calorycounter.helpers.MIN_SWIPE_DISTANCE
import com.example.calorycounter.helpers.caloriesFile
import com.example.calorycounter.helpers.goalsFile
import com.example.calorycounter.helpers.proteinFile
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

class ChartCardCreation (con: Context){
    private val context = con
    private val dataHandler = DataHandler()

    fun prepareCards(chartDataCalories: MutableMap<String, String>, chartDataProtein: MutableMap<String, String>, parentView: LinearLayout, scrollView: ScrollView): List<CardView>{
        val allKeys = (chartDataCalories.keys + chartDataProtein.keys).distinct()
        return allKeys.sortedByDescending { it }
            .take(30)
            .map { key ->
                createCards(
                    formatString(key),
                    chartDataCalories[key] ?: "0",
                    chartDataProtein[key] ?: "0",
                    parentView, scrollView
                )
            }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createCards(dateString: String, caloriesString: String, proteinString: String, parentView: LinearLayout, scrollView: ScrollView): CardView {
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

        val transition = ChangeBounds().apply { setDuration(200) }

        var startX = 0f
        cardLayout.setOnTouchListener(
            View.OnTouchListener { view, event ->
                val displayMetrics = context.resources.displayMetrics
                val maxWidth = displayMetrics.widthPixels.toFloat()
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = event.rawX
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val newX = event.rawX
                        // carry out swipe if motion is bigger than 25 dp and to the right
                        if (newX - startX > 25) {
                            scrollView.requestDisallowInterceptTouchEvent(true)
                            cardLayout.animate()
                                .x(abs(newX) - abs(startX))
                                .setDuration(0)
                                .start()
                        }
                        if (maxWidth - newX < 25) {
                            TransitionManager.beginDelayedTransition(parentView, transition)
                            val removable = removeItems(caloriesString, proteinString)
                            if (removable){
                                parentView.removeView(cardLayout)
                            }
                            else{
                                cardLayout.translationX = 0f
                            }
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        scrollView.requestDisallowInterceptTouchEvent(false)
                        if (cardLayout.x > MIN_SWIPE_DISTANCE) {
                            TransitionManager.beginDelayedTransition(parentView, transition)
                            val removable = removeItems(caloriesString, proteinString)
                            if (removable){
                                parentView.removeView(cardLayout)
                            }
                            else{
                                cardLayout.translationX = 0f
                            }
                        }
                        else {
                            cardLayout.translationX = 0f
                        }
                    }
                }
                // required to by-pass lint warning
                view.performClick()
                return@OnTouchListener true
            }
        )

        return cardLayout
    }

    private fun removeItems(caloriesString: String, proteinString: String): Boolean{
        val goals = getGoals()
        val caloriesGoal = goals[Keys.Calories.toString()]
        var removable = false

        if (caloriesGoal != null) {
            println(caloriesString)
            println(caloriesGoal)
            if (caloriesString.toDouble() < caloriesGoal.toDouble()) {
                removable = true
                dataHandler.deleteEntriesWithValue(
                    context,
                    caloriesFile,
                    caloriesString.replace(",", ".")
                )
                dataHandler.deleteEntriesWithValue(
                    context,
                    proteinFile,
                    proteinString.replace(",", ".")
                )
            }
        }
        return removable
    }

    private fun getGoals(): MutableMap<String, String> {
        val goals = dataHandler.loadData(context, goalsFile).toMutableMap()
        goals.getOrPut(Keys.Calories.toString()) { "0" }
        goals.getOrPut(Keys.Protein.toString()) { "0" }
        return goals
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
        textView.layoutParams = textParam

        return textView
    }

    private fun formatString(date: String): String{
        var newDateString = "19900101"
        try {
            val formatString = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            val newDate = formatString.parse(date)
            newDateString = newDate?.toString()?.removeRange(11, 30).toString()
        }
        catch (e: Exception) {
            println("Formatting exception $e")
        }

        return newDateString
    }
}