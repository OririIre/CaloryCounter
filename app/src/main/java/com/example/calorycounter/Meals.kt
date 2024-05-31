package com.example.calorycounter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.calorycounter.databinding.FragmentMealsBinding


class Meals : Fragment() {
    private var _bnd: FragmentMealsBinding? = null
    private val bnd get() = _bnd!!
    private var meal1NameSave = ""
    private var meal1KcalSave = ""
    private var meal1ProtSave = ""
    private var meal2NameSave = ""
    private var meal2KcalSave = ""
    private var meal2ProtSave = ""
    private var meal3NameSave = ""
    private var meal3KcalSave = ""
    private var meal3ProtSave = ""
    private var meal4NameSave = ""
    private var meal4KcalSave = ""
    private var meal4ProtSave = ""
    private var meal5NameSave = ""
    private var meal5KcalSave = ""
    private var meal5ProtSave = ""
    private val mealsFile = "meals.txt"


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _bnd = FragmentMealsBinding.inflate(inflater, container, false)
        val view = bnd.root

        val savedMeals = dataHandler.loadData(requireContext(), mealsFile)
        writeMeals(savedMeals)

        bnd.buttonSave1.setOnClickListener {
            if (savedMeals.isNotEmpty()) {
                dataHandler.saveMapData(requireContext(), mealsFile, getMealValues(savedMeals))
            } else {
                val output = createMap()
                dataHandler.saveMapData(requireContext(), mealsFile, getMealValues(output))
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        writeMeals(dataHandler.loadData(requireContext(), mealsFile))
        resumeAfterPause()
    }

    override fun onPause() {
        super.onPause()
        saveDuringPause()
    }

    private fun writeMeals(currentMeals: MutableMap<String, String>) {
        if (currentMeals.keys.contains("Meal1Name") && currentMeals["Meal1Name"] != "value") {
            bnd.nameMeal1.text = currentMeals["Meal1Name"].toString().take(4)
            val conText = currentMeals["Meal1Cal"].toString() + " kcal"
            bnd.valueMeal1.text = conText
            bnd.editNameMeal1.setText(currentMeals["Meal1Name"].toString())
        } else {
            bnd.editNameMeal1.text.clear()
            bnd.nameMeal1.text = "Name"
            bnd.valueMeal1.text = "value"
        }
        if (currentMeals.keys.contains("Meal2Name") && currentMeals["Meal2Name"] != "value") {
            bnd.nameMeal2.text = currentMeals["Meal2Name"].toString().take(4)
            val conText = currentMeals["Meal2Cal"].toString() + " kcal"
            bnd.valueMeal2.text = conText
            bnd.editNameMeal2.setText(currentMeals["Meal2Name"].toString())
        } else {
            bnd.editNameMeal2.text.clear()
            bnd.nameMeal2.text = "Name"
            bnd.valueMeal2.text = "value"
        }
        if (currentMeals.keys.contains("Meal3Name") && currentMeals["Meal3Name"] != "value") {
            bnd.nameMeal3.text = currentMeals["Meal3Name"].toString().take(4)
            val conText = currentMeals["Meal3Cal"].toString() + " kcal"
            bnd.valueMeal3.text = conText
            bnd.editNameMeal3.setText(currentMeals["Meal3Name"].toString())
        } else {
            bnd.editNameMeal3.text.clear()
            bnd.nameMeal3.text = "Name"
            bnd.valueMeal3.text = "value"
        }
        if (currentMeals.keys.contains("Meal4Name") && currentMeals["Meal4Name"] != "value") {
            bnd.nameMeal4.text = currentMeals["Meal4Name"].toString().take(4)
            val conText = currentMeals["Meal4Cal"].toString() + " kcal"
            bnd.valueMeal4.text = conText
            bnd.editNameMeal4.setText(currentMeals["Meal4Name"].toString())
        } else {
            bnd.editNameMeal4.text.clear()
            bnd.nameMeal4.text = "Name"
            bnd.valueMeal4.text = "value"
        }
        if (currentMeals.keys.contains("Meal5Name") && currentMeals["Meal5Name"] != "value") {
            bnd.nameMeal5.text = currentMeals["Meal5Name"].toString().take(4)
            val conText = currentMeals["Meal5Cal"].toString() + " kcal"
            bnd.valueMeal5.text = conText
            bnd.editNameMeal5.setText(currentMeals["Meal5Name"].toString())
        } else {
            bnd.editNameMeal5.text.clear()
            bnd.nameMeal5.text = "Name"
            bnd.valueMeal5.text = "value"
        }
    }

    private fun getMealValues(currentMeals: MutableMap<String, String>): MutableMap<String, String> {
        if (bnd.editNameMeal1.text.isNotEmpty()) {
            currentMeals["Meal1Name"] = bnd.editNameMeal1.text.toString()
            if (bnd.editCaloriesMeal1.text.isNotEmpty()) {
                currentMeals["Meal1Cal"] = bnd.editCaloriesMeal1.text.toString()
                val conText = bnd.editCaloriesMeal1.text.toString() + " kcal"
                bnd.valueMeal1.text = conText
            }
            if (bnd.editProteinMeal1.text.isNotEmpty()) {
                currentMeals["Meal1Prot"] = bnd.editProteinMeal1.text.toString()
            }
            bnd.nameMeal1.text = bnd.editNameMeal1.text.toString().take(4)
            bnd.editCaloriesMeal1.text.clear()
            bnd.editProteinMeal1.text.clear()
        }
        if (bnd.editNameMeal2.text.isNotEmpty()) {
            currentMeals["Meal2Name"] = bnd.editNameMeal2.text.toString()
            if (bnd.editCaloriesMeal2.text.isNotEmpty()) {
                currentMeals["Meal2Cal"] = bnd.editCaloriesMeal2.text.toString()
                val conText = bnd.editCaloriesMeal2.text.toString() + " kcal"
                bnd.valueMeal2.text = conText
            }
            if (bnd.editProteinMeal2.text.isNotEmpty()) {
                currentMeals["Meal2Prot"] = bnd.editProteinMeal2.text.toString()
            }
            bnd.nameMeal2.text = bnd.editNameMeal2.text.toString().take(4)
            bnd.editCaloriesMeal2.text.clear()
            bnd.editProteinMeal2.text.clear()
        }
        if (bnd.editNameMeal3.text.isNotEmpty()) {
            currentMeals["Meal3Name"] = bnd.editNameMeal3.text.toString()
            if (bnd.editCaloriesMeal3.text.isNotEmpty()) {
                currentMeals["Meal3Cal"] = bnd.editCaloriesMeal3.text.toString()
                val conText = bnd.editCaloriesMeal3.text.toString() + " kcal"
                bnd.valueMeal3.text = conText
            }
            if (bnd.editProteinMeal3.text.isNotEmpty()) {
                currentMeals["Meal3Prot"] = bnd.editProteinMeal3.text.toString()
            }
            bnd.nameMeal3.text = bnd.editNameMeal3.text.toString().take(4)
            bnd.editCaloriesMeal3.text.clear()
            bnd.editProteinMeal3.text.clear()
        }
        if (bnd.editNameMeal4.text.isNotEmpty()) {
            currentMeals["Meal4Name"] = bnd.editNameMeal4.text.toString()
            if (bnd.editCaloriesMeal4.text.isNotEmpty()) {
                currentMeals["Meal4Cal"] = bnd.editCaloriesMeal4.text.toString()
                val conText = bnd.editCaloriesMeal5.text.toString() + " kcal"
                bnd.valueMeal4.text = conText
            }
            if (bnd.editProteinMeal4.text.isNotEmpty()) {
                currentMeals["Meal4Prot"] = bnd.editProteinMeal4.text.toString()
            }
            bnd.nameMeal4.text = bnd.editNameMeal4.text.toString().take(4)
            bnd.editCaloriesMeal5.text.clear()
            bnd.editProteinMeal4.text.clear()
        }
        if (bnd.editNameMeal5.text.isNotEmpty()) {
            currentMeals["Meal5Name"] = bnd.editNameMeal5.text.toString()
            if (bnd.editCaloriesMeal5.text.isNotEmpty()) {
                currentMeals["Meal5Cal"] = bnd.editCaloriesMeal5.text.toString()
                val conText = bnd.editCaloriesMeal5.text.toString() + " kcal"
                bnd.valueMeal5.text = conText
            }
            if (bnd.editProteinMeal5.text.isNotEmpty()) {
                currentMeals["Meal5Prot"] = bnd.editProteinMeal5.text.toString()
            }
            bnd.nameMeal5.text = bnd.editNameMeal5.text.toString().take(4)
            bnd.editCaloriesMeal5.text.clear()
            bnd.editProteinMeal5.text.clear()
        }
        return currentMeals
    }

    private fun createMap(): MutableMap<String, String> {

        val meals = mutableMapOf("Meal1Name" to "value")
        meals += mutableMapOf("Meal1Cal" to "value")
        meals += mutableMapOf("Meal1Prot" to "value")

        meals += mutableMapOf("Meal2Name" to "value")
        meals += mutableMapOf("Meal2Cal" to "value")
        meals += mutableMapOf("Meal2Prot" to "value")

        meals += mutableMapOf("Meal3Name" to "value")
        meals += mutableMapOf("Meal3Cal" to "value")
        meals += mutableMapOf("Meal3Prot" to "value")

        meals += mutableMapOf("Meal4Name" to "value")
        meals += mutableMapOf("Meal4Cal" to "value")
        meals += mutableMapOf("Meal4Prot" to "value")

        meals += mutableMapOf("Meal5Name" to "value")
        meals += mutableMapOf("Meal5Cal" to "value")
        meals += mutableMapOf("Meal5Prot" to "value")

        return meals
    }

    private fun saveDuringPause() {
        meal1NameSave = bnd.editNameMeal1.text.toString()
        meal1KcalSave = bnd.editCaloriesMeal1.text.toString()
        meal1ProtSave = bnd.editProteinMeal1.text.toString()

        meal2NameSave = bnd.editNameMeal2.text.toString()
        meal2KcalSave = bnd.editCaloriesMeal2.text.toString()
        meal2ProtSave = bnd.editProteinMeal2.text.toString()

        meal3NameSave = bnd.editNameMeal3.text.toString()
        meal3KcalSave = bnd.editCaloriesMeal3.text.toString()
        meal3ProtSave = bnd.editProteinMeal3.text.toString()

        meal4NameSave = bnd.editNameMeal4.text.toString()
        meal4KcalSave = bnd.editCaloriesMeal4.text.toString()
        meal4ProtSave = bnd.editProteinMeal4.text.toString()

        meal5NameSave = bnd.editNameMeal5.text.toString()
        meal5KcalSave = bnd.editCaloriesMeal5.text.toString()
        meal5ProtSave = bnd.editProteinMeal5.text.toString()
    }

    private fun resumeAfterPause() {
        if (meal1NameSave != "") {
            bnd.editNameMeal1.setText(meal1NameSave)
        }
        if (meal1KcalSave != "") {
            bnd.editCaloriesMeal1.setText(meal1KcalSave)
        }
        if (meal1ProtSave != "") {
            bnd.editProteinMeal1.setText(meal1ProtSave)
        }

        if (meal2NameSave != "") {
            bnd.editNameMeal2.setText(meal2NameSave)
        }
        if (meal2KcalSave != "") {
            bnd.editCaloriesMeal2.setText(meal2KcalSave)
        }
        if (meal2ProtSave != "") {
            bnd.editProteinMeal2.setText(meal2ProtSave)
        }

        if (meal3NameSave != "") {
            bnd.editNameMeal3.setText(meal3NameSave)
        }
        if (meal3KcalSave != "") {
            bnd.editCaloriesMeal3.setText(meal3KcalSave)
        }
        if (meal3ProtSave != "") {
            bnd.editProteinMeal3.setText(meal3ProtSave)
        }

        if (meal4NameSave != "") {
            bnd.editNameMeal4.setText(meal4NameSave)
        }
        if (meal4KcalSave != "") {
            bnd.editCaloriesMeal4.setText(meal4KcalSave)
        }
        if (meal4ProtSave != "") {
            bnd.editProteinMeal4.setText(meal4ProtSave)
        }

        if (meal5NameSave != "") {
            bnd.editNameMeal5.setText(meal5NameSave)
        }
        if (meal5KcalSave != "") {
            bnd.editCaloriesMeal5.setText(meal5KcalSave)
        }
        if (meal5ProtSave != "") {
            bnd.editProteinMeal5.setText(meal5ProtSave)
        }
    }
}