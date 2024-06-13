package com.example.calorycounter.view

import android.annotation.SuppressLint
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.example.calorycounter.R
import com.example.calorycounter.data.UpdateListener
import com.example.calorycounter.data.HelperClass
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.lang.ref.WeakReference
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class HistoryDialog (con: Context) {
    private val context = con
    private val inflater = LayoutInflater.from(con)
    private val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

    private var listener = WeakReference<UpdateListener>(null)

    @SuppressLint("InflateParams")
    fun showHistoryDialog(historyDialog : BottomSheetDialog) {
        
        val layoutHistoryCards: LinearLayout = historyDialog.findViewById(R.id.layoutHistoryCards)!!
        val historyScrollView: ScrollView = historyDialog.findViewById(R.id.historyScrollView)!!

        val historyValues = dataHandler.loadData(context, historyFile)
        if(historyValues.isNotEmpty()){
            layoutHistoryCards.removeAllViews()
            for (item in historyValues) {
                createCards(layoutHistoryCards, item.key, item.value, historyScrollView)
            }
        }
        historyDialog.show()
    }

    private fun createCards(parent: LinearLayout, time: String, value: String, scrollView: ScrollView) {
        val card: View = inflater.inflate(R.layout.card_layout2, parent, false)
        val descriptionText: TextView = card.findViewById(R.id.description)
        val historyValue: TextView = card.findViewById(R.id.value)
        var startX = 0f
        card.id = View.generateViewId()

        var newTime = ""
        var calOrProt = true
        if(time.contains("_calo")){
            newTime = time.takeLast(13).replace("_calo", "")
            calOrProt = true
        } else if (time.contains("_prot")){
            newTime = time.takeLast(13).replace("_prot", "")
            calOrProt = false
        }

        if(calOrProt){
            historyValue.id = View.generateViewId()
            historyValue.text = String.format(Locale.getDefault(),"%.1f", value.toDouble())
            descriptionText.text = context.getString(R.string.Calories)
        }
        else{
            historyValue.id = View.generateViewId()
            historyValue.text = String.format(Locale.getDefault(),"%.1f", value.toDouble())
            descriptionText.text = context.getString(R.string.Protein)
        }

        val dateView: TextView = card.findViewById(R.id.date)
        dateView.id = View.generateViewId()
        dateView.text = newTime

        val transition = ChangeBounds()
        transition.setDuration(200)

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
                            removeHistoryItem(descriptionText.text.toString(), value)
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        scrollView.requestDisallowInterceptTouchEvent(false)
                        if (card.x > MIN_SWIPE_DISTANCE) {
                            TransitionManager.beginDelayedTransition(parent, transition)
                            parent.removeView(card)
                            removeHistoryItem(descriptionText.text.toString(), value)
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

    private fun removeHistoryItem(valueType: String, value: String){

        if(valueType == "Calories"){
            var currentKcal = HelperClass.getCurrentValue(caloriesFile, context)
            if (value != "") {
                if (value.toDouble() > 0.0) {
                    currentKcal -= value.toDouble()
                }
            }
            dataHandler.saveData(context, caloriesFile, currentDate, currentKcal.toString())
            dataHandler.deleteEntriesWithValue(context, historyFile, value)
            listener.get()?.onStuffUpdated()
        }
        else{
            var currentProt = HelperClass.getCurrentValue(proteinFile, context)
            if (value != "") {
                if (value.toDouble() > 0.0) {
                    currentProt -= value.toDouble()
                }
            }
            dataHandler.saveData(context, proteinFile, currentDate, currentProt.toString())
            dataHandler.deleteEntriesWithValue(context, historyFile, value)
            listener.get()?.onStuffUpdated()
        }
    }

    fun addListener(listener: UpdateListener){
        this.listener = WeakReference(listener)
    }
}