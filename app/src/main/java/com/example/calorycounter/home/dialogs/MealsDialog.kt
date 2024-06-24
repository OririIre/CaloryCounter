package com.example.calorycounter.home.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import com.example.calorycounter.helpers.IconAdapter
import com.example.calorycounter.R
import com.example.calorycounter.data.DataHandler
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.Locale

class MealsDialog (con: Context) {
    private val dataHandler = DataHandler()
    private val context = con
    private val mealsFile = "meals.txt"
    private val iconFile = "icon.txt"
    private lateinit var caloriesField: EditText
    private lateinit var proteinField: EditText
    private lateinit var nameField: EditText
    private lateinit var iconDropdown: Spinner

    @SuppressLint("InflateParams")
    fun show(mealNumber: Int, mealsDialog: BottomSheetDialog) {

        val save: Button = mealsDialog.findViewById(R.id.buttonSaveMeal)!!
        caloriesField = mealsDialog.findViewById(R.id.enterMealCalories)!!
        proteinField = mealsDialog.findViewById(R.id.enterMealProtein)!!
        nameField = mealsDialog.findViewById(R.id.enterMealName)!!
        iconDropdown = mealsDialog.findViewById(R.id.iconSelection)!!

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

        prepareDialog(mealNumber, currentMeals, currentIcons, adapter)

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
            saveAtKey(mealNumber, selectedIcon, mealsDialog)
        }
        mealsDialog.show()
    }

    private fun saveAtKey(mealNumber: Int, selectedIcon: String, mealsDialog: BottomSheetDialog){
        val mealsCount = ((dataHandler.loadData(context, mealsFile).count() / 3) +1)
        val keyPrefix = if (mealNumber == 0) {
            "Meal$mealsCount"
        } else {
            "Meal$mealNumber"
        }

        if (nameField.text.toString() != "") {
            dataHandler.saveMapDataNO(context, mealsFile, mutableMapOf(keyPrefix + "Name" to nameField.text.toString()))
            dataHandler.saveData(context, iconFile, keyPrefix + "Icon", selectedIcon)

            val caloriesValue = formatString(caloriesField.text.toString()).ifEmpty { "0" }
            dataHandler.saveMapDataNO(context, mealsFile, mutableMapOf(keyPrefix + "Cal" to caloriesValue))

            val proteinValue = formatString(proteinField.text.toString()).ifEmpty { "0" }
            dataHandler.saveMapDataNO(context, mealsFile, mutableMapOf(keyPrefix + "Prot" to proteinValue))
        }

        nameField.text.clear()
        caloriesField.text.clear()
        proteinField.text.clear()
        if (mealNumber != 0) mealsDialog.dismiss()
    }

    private fun prepareDialog(mealNumber: Int, currentMeals: MutableMap<String,String>, currentIcons: MutableMap<String,String>, adapter: IconAdapter){
        if (mealNumber != 0) {
            val keyPrefix = "Meal$mealNumber"
            nameField.setText(currentMeals[keyPrefix + "Name"])
            caloriesField.setText(currentMeals[keyPrefix + "Cal"])
            proteinField.setText(currentMeals[keyPrefix + "Prot"])

            val mealIcon = currentIcons[keyPrefix + "Icon"]
            mealIcon?.toIntOrNull()?.let { iconValue ->
                iconDropdown.setSelection(adapter.getPosition(iconValue))
            }
        } else {
            nameField.text.clear()
            caloriesField.text.clear()
            proteinField.text.clear()
        }
    }

    private fun formatString (value: String): String {
        var returnString = ""
        if(value != "") {
            returnString = String.format(Locale.getDefault(), "%.1f", value.toDouble())
        }
        return returnString
    }
}