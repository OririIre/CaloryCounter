package com.example.calorycounter.home

import android.content.Context
import android.view.View
import android.widget.TextView
import com.example.calorycounter.data.DataHandler
import com.example.calorycounter.helpers.Keys
import com.example.calorycounter.helpers.goalsFile
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeLogic (con: Context){
    private val context = con
    private val dataHandler = DataHandler()
    private var isAllFabVisible: Boolean = false

    fun updateGoals(): MutableMap<String, String> {
        val goals = dataHandler.loadData(context, goalsFile).toMutableMap()
        goals.getOrPut(Keys.Calories.toString()) { "0" }
        goals.getOrPut(Keys.Protein.toString()) { "0" }
        return goals
    }

    fun setFloatingButtonVisibilty (fbCustom: FloatingActionButton, fbMeals: FloatingActionButton, addFreeText: TextView, addMealText: TextView) {
        val visibility = if (isAllFabVisible) View.GONE else View.VISIBLE
        fbCustom.visibility = visibility
        fbMeals.visibility = visibility
        addFreeText.visibility = visibility
        addMealText.visibility = visibility
        if (visibility == View.VISIBLE) {
            addFreeText.bringToFront()
            addMealText.bringToFront()
        }

        isAllFabVisible = !isAllFabVisible
    }
}