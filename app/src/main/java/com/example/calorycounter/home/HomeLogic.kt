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

    fun updateGoals(): MutableMap<String,String> {
        val goals = dataHandler.loadData(context, goalsFile)
        if (goals.isEmpty()) {
            goals[Keys.Calories.toString()] = "0"
            goals[Keys.Protein.toString()] = "0"
        }
        if (!goals.containsKey(Keys.Protein.toString())) {
            goals[Keys.Protein.toString()] = "0"
        }
        if (!goals.containsKey(Keys.Calories.toString())) {
            goals[Keys.Calories.toString()] = "0"
        }
        return goals
    }

    fun setFloatingButtonVisibilty (fbCustom: FloatingActionButton, fbMeals: FloatingActionButton, addFreeText: TextView, addMealText: TextView){
        if(!isAllFabVisible){
            fbCustom.visibility = View.VISIBLE
            fbMeals.visibility = View.VISIBLE
            addFreeText.visibility = View.VISIBLE
            addMealText.visibility = View.VISIBLE
            addFreeText.bringToFront()
            addMealText.bringToFront()
            isAllFabVisible = true
        } else {
            fbCustom.visibility = View.GONE
            fbMeals.visibility = View.GONE
            addFreeText.visibility = View.GONE
            addMealText.visibility = View.GONE
            isAllFabVisible = false
        }
    }
}