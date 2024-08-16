package com.example.calorycounter.home.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.example.calorycounter.helpers.MIN_SWIPE_DISTANCE
import com.example.calorycounter.R
import com.example.calorycounter.helpers.caloriesFile
import com.example.calorycounter.data.DataHandler
import com.example.calorycounter.helpers.UpdateListener
import com.example.calorycounter.helpers.HelperClass
import com.example.calorycounter.helpers.historyFile
import com.example.calorycounter.helpers.proteinFile
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.lang.ref.WeakReference
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class HistoryDialog (con: Context) {
    private val dataHandler = DataHandler()
    private val context = con
    private val inflater = LayoutInflater.from(con)
    private val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

    private var listener = WeakReference<UpdateListener>(null)

    @SuppressLint("InflateParams")
    fun showHistoryDialog(historyDialog : BottomSheetDialog) {
        
        val layoutHistoryCards: LinearLayout = historyDialog.findViewById(R.id.layoutHistoryCards)!!
        val historyScrollView: NestedScrollView = historyDialog.findViewById(R.id.historyScrollView)!!

        val historyValues = dataHandler.loadData(context, historyFile)
        if(historyValues.isNotEmpty()){
            layoutHistoryCards.removeAllViews()
            val groupedEntries = historyValues.entries.groupBy { ita ->
                ita.key.substringBefore("_").takeIf { it.isNotEmpty() }
            }

            groupedEntries.forEach { (timeKey, entries) ->
                if (timeKey != null) {
                    val name = entries.find { it.key.endsWith("_name") }?.value ?: ""
                    entries.forEach { (key, value) ->
                        if (key.endsWith("_calo") || key.endsWith("_prot")) {
                            createCards(layoutHistoryCards, key, value, name, historyScrollView)
                        }
                    }
                }
            }
        }
        historyDialog.show()
    }

    private fun createCards(parent: LinearLayout, time: String, value: String, name: String, scrollView: NestedScrollView) {
        val card: View = inflater.inflate(R.layout.card_layout2, parent, false)
        val relLayout: RelativeLayout = card.findViewById(R.id.tempID)
        val descriptionText: TextView = card.findViewById(R.id.description)
        val descriptionName: TextView = card.findViewById(R.id.descriptionName)
        val historyValue: TextView = card.findViewById(R.id.value)
        val dateView: TextView = card.findViewById(R.id.date)

        card.id = View.generateViewId()
        dateView.id = View.generateViewId()

        val newTime = formatTime(time)
        val calOrProt = determinCalOrProt(time)

        val newName = name.takeLast(13).replace("_name", "")

        if(calOrProt){
            setText(historyValue, descriptionText, descriptionName, newName, value, relLayout, context.getString(R.string.Calories))
        }
        else{
            setText(historyValue, descriptionText, descriptionName, newName, value, relLayout, context.getString(R.string.Protein))
        }

        dateView.text = newTime

        val transition = ChangeBounds().apply { setDuration(200) }

        var startX = 0f
        card.setOnTouchListener(
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
                            card.animate()
                                .x(abs(newX) - abs(startX))
                                .setDuration(0)
                                .start()
                        }
                        if (maxWidth - newX < 25) {
                            TransitionManager.beginDelayedTransition(parent, transition)
                            parent.removeView(card)
                            removeHistoryItem(calOrProt, value, name)
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        scrollView.requestDisallowInterceptTouchEvent(false)
                        if (card.x > MIN_SWIPE_DISTANCE) {
                            TransitionManager.beginDelayedTransition(parent, transition)
                            parent.removeView(card)
                            removeHistoryItem(calOrProt, value, name)
                        }
                        else {
                            card.translationX = 0f
                        }
                    }
                }
                // required to by-pass lint warning
                view.performClick()
                return@OnTouchListener true
            }
        )
        parent.addView(card)
    }

    private fun setText (historyValue: TextView, descriptionText: TextView, descriptionName: TextView, newName: String, value: String, relLayout: RelativeLayout, description: String){
        historyValue.id = View.generateViewId()
        historyValue.text = if (value.replace(",",".").toDoubleOrNull() != 0.0) {
            String.format(Locale.getDefault(), "%.1f", value.replace(",",".").toDouble())
        } else {
            "0"
        }
        descriptionText.text = description

        if (newName.isNotBlank()) {
            descriptionName.text = newName
        } else {
            relLayout.removeView(descriptionName)
        }
    }

    private fun determinCalOrProt(time: String): Boolean {
        return time.contains("_calo")
    }

    private fun formatTime(time: String): String {
        return time.takeLast(13).replace("_calo", "").replace("_prot", "")
    }

    private fun removeHistoryItem(valueType: Boolean, value: String, name: String){
        val (fileName, currentValue) =if (valueType) {
            caloriesFile to calcNewValue(caloriesFile, value.replace(",","."))
        } else {
            proteinFile to calcNewValue(proteinFile, value.replace(",","."))
        }

        dataHandler.saveData(context, fileName, currentDate, currentValue.toString())
        dataHandler.deleteEntriesWithValue(context, historyFile, value.replace(",","."))
        dataHandler.deleteMapEntriesWithKeys(context, historyFile, name)

        listener.get()?.onStuffUpdated()
    }

    private fun calcNewValue (fileType: String, value: String): Double {
        var currentValue = HelperClass.getCurrentValue(fileType, context)
        value.replace(",",".").toDoubleOrNull()?.let {
            if (it > 0.0) {
                currentValue -= it
            }
        }
        return currentValue
    }

    fun addListener(listener: UpdateListener){
        this.listener = WeakReference(listener)
    }
}