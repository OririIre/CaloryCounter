package com.example.calorycounter.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import com.example.calorycounter.adapter.IconAdapter
import com.example.calorycounter.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.Locale

class MealsDialog (con: Context) {
    
    private val context = con
    private val mealsFile = "meals.txt"
    private val iconFile = "icon.txt"

    @SuppressLint("InflateParams")
    fun show(mealNumber: Int, mealsDialog: BottomSheetDialog) {

        val save: Button = mealsDialog.findViewById(R.id.buttonSaveMeal)!!
        val caloriesField: EditText = mealsDialog.findViewById(R.id.enterMealCalories)!!
        val proteinField: EditText = mealsDialog.findViewById(R.id.enterMealProtein)!!
        val nameField: EditText = mealsDialog.findViewById(R.id.enterMealName)!!
        val iconDropdown: Spinner = mealsDialog.findViewById(R.id.iconSelection)!!

        var currentMeals = dataHandler.loadData(context, mealsFile)
        val currentIcons = dataHandler.loadData(context, iconFile)

        val items: ArrayList<Int> = arrayListOf(
            R.drawable.baseline_ramen_dining_24, R.drawable.baseline_coffee_24,
            R.drawable.baseline_dinner_dining_24, R.drawable.baseline_local_bar_24,
            R.drawable.baseline_lunch_dining_24, R.drawable.baseline_wine_bar_24,
            R.drawable.baseline_bakery_dining_24, R.drawable.baseline_local_pizza_24
        )
        val adapter = IconAdapter(context, R.layout.row, items)
        iconDropdown.adapter = adapter

        if (mealNumber != 0) {
            val keyName = "Meal" + mealNumber.toString() + "Name"
            val mealName = currentMeals[keyName]
            nameField.setText(mealName)
            val keyCal = "Meal" + mealNumber.toString() + "Cal"
            val mealCalories = currentMeals[keyCal]
            caloriesField.setText(mealCalories)
            val keyProt = "Meal" + mealNumber.toString() + "Prot"
            val mealProtein = currentMeals[keyProt]
            proteinField.setText(mealProtein)
            val keyIcon = "Meal" + mealNumber.toString() + "Icon"
            val mealIcon = currentIcons[keyIcon]
            if (mealIcon != null) {
                val dropDownPosition = adapter.getPosition(mealIcon.toInt())
                iconDropdown.setSelection(dropDownPosition)
            }
        } else {
            nameField.text.clear()
            caloriesField.text.clear()
            proteinField.text.clear()
        }

        var selectedIcon = ""

        iconDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedIcon = parent?.getItemAtPosition(position).toString()
            }
        }
        save.setOnClickListener{
            currentMeals = dataHandler.loadData(context, mealsFile)
            val mealsCount = ((currentMeals.count()/3) + 1)
            if(mealNumber == 0) {
                if (nameField.text.toString() != "") {
                    val keyName = "Meal" + mealsCount.toString() + "Name"
                    val keyIcon = "Meal" + mealsCount.toString() + "Icon"
                    val mealNameMap = mutableMapOf(keyName to nameField.text.toString())
                    val keyCal = "Meal" + mealsCount.toString() + "Cal"
                    dataHandler.saveMapDataNO(context, mealsFile, mealNameMap)
                    dataHandler.saveData(context, iconFile, keyIcon, selectedIcon)
                    if (caloriesField.text.toString() != "") {
                        val caloriesValue = formatString(caloriesField.text.toString())
                        val mealCaloriesMap = mutableMapOf(keyCal to caloriesValue)
                        dataHandler.saveMapDataNO(context, mealsFile, mealCaloriesMap)
                    } else {
                        val mealCaloriesMap = mutableMapOf(keyCal to "0")
                        dataHandler.saveMapDataNO(context, mealsFile, mealCaloriesMap)
                    }
                    val keyProt = "Meal" + mealsCount.toString() + "Prot"
                    if (proteinField.text.toString() != "") {
                        val proteinValue = formatString(proteinField.text.toString())
                        val mealProteinMap = mutableMapOf(keyProt to proteinValue)
                        dataHandler.saveMapDataNO(context, mealsFile, mealProteinMap)
                    } else {
                        val mealProteinMap = mutableMapOf(keyProt to "0")
                        dataHandler.saveMapDataNO(context, mealsFile, mealProteinMap)
                    }
                }
            }
            else{
                if (nameField.text.toString() != "") {
                    val keyName = "Meal" + mealNumber.toString() + "Name"
                    val keyIcon = "Meal" + mealNumber.toString() + "Icon"
                    val keyCal = "Meal" + mealNumber.toString() + "Cal"
                    val mealNameMap = mutableMapOf(keyName to nameField.text.toString())
                    dataHandler.saveMapDataNO(context, mealsFile, mealNameMap)
                    dataHandler.saveData(context, iconFile, keyIcon, selectedIcon)
                    if (caloriesField.text.toString() != "") {
                        val caloriesValue = formatString(caloriesField.text.toString())
                        val mealCaloriesMap = mutableMapOf(keyCal to caloriesValue)
                        dataHandler.saveMapDataNO(context, mealsFile, mealCaloriesMap)
                    } else {
                        val mealCaloriesMap = mutableMapOf(keyCal to "0")
                        dataHandler.saveMapDataNO(context, mealsFile, mealCaloriesMap)
                    }
                    val keyProt = "Meal" + mealNumber.toString() + "Prot"
                    if (proteinField.text.toString() != "") {
                        val proteinValue = formatString(proteinField.text.toString())
                        val mealProteinMap = mutableMapOf(keyProt to proteinValue)
                        dataHandler.saveMapDataNO(context, mealsFile, mealProteinMap)
                    } else {
                        val mealProteinMap = mutableMapOf(keyProt to "0")
                        dataHandler.saveMapDataNO(context, mealsFile, mealProteinMap)
                    }
                }
                nameField.text.clear()
                caloriesField.text.clear()
                proteinField.text.clear()
                mealsDialog.dismiss()
            }
            nameField.text.clear()
            caloriesField.text.clear()
            proteinField.text.clear()
        }
        mealsDialog.show()
    }

    private fun formatString (value: String): String {
        var returnString = ""
        if(value != "") {
            returnString = String.format(Locale.getDefault(), "%.1f", value.toDouble())
        }
        return returnString
    }
}