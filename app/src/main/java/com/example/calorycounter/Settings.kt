package com.example.calorycounter

import android.animation.LayoutTransition
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.calorycounter.databinding.FragmentSettingsBinding

val dataHandler = DataHandler()

internal enum class Keys {
    Protein, Calories, Language
}

class Settings : Fragment() {

    private var _bnd: FragmentSettingsBinding? = null
    private val bnd get() = _bnd!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _bnd = FragmentSettingsBinding.inflate(inflater, container, false)
        val view = bnd.root
        var toggleReset = true
        var toggleGoals = true
        var toggleLanguage = true
        var selectedLanguage = "Phone Default"
        val languageFile = "language.txt"
        val goalsFile = "goals.txt"

        bnd.layoutClearData.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        bnd.layoutCalories.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        bnd.layoutLanguage.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

        val items = arrayOf("Phone Default", "German", "English", "French")
        val adapter: ArrayAdapter<String> =
            ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_dropdown_item, items)
        bnd.languageDropdown.adapter = adapter

        val language: Int =
            when (dataHandler.loadData(requireContext(), languageFile)[Keys.Language.toString()]) {
                "German" -> {
                    1
                }

                "English" -> {
                    2
                }

                "French" -> {
                    3
                }

                else -> {
                    0
                }
            }
        bnd.languageDropdown.setSelection(language)

        bnd.saveGoals.setOnClickListener {
            val calories = bnd.enterCaloriesGoal.text.toString()
            val protein = bnd.enterProteinGoal.text.toString()
            if (calories.isNotEmpty()) {
                dataHandler.saveData(
                    requireContext(), goalsFile,
                    Keys.Calories.toString(), calories
                )
            }
            if (protein.isNotEmpty()) {
                dataHandler.saveData(
                    requireContext(), goalsFile,
                    Keys.Protein.toString(), protein
                )
            }
            bnd.enterCaloriesGoal.text?.clear()
            bnd.enterProteinGoal.text.clear()
        }

        bnd.iconAndTextGoal.setOnClickListener {
            if (toggleGoals) {
                bnd.goalVisibility.visibility = View.VISIBLE
                toggleGoals = false
            } else {
                bnd.goalVisibility.visibility = View.GONE
                toggleGoals = true
            }
        }

        bnd.iconAndTextReset.setOnClickListener {
            if (toggleReset) {
                bnd.clearVisibility.visibility = View.VISIBLE
                toggleReset = false
            } else {
                bnd.clearVisibility.visibility = View.GONE
                toggleReset = true
            }
        }

        bnd.iconAndTextLanguage.setOnClickListener {
            if (toggleLanguage) {
                bnd.languageVisibility.visibility = View.VISIBLE
                toggleLanguage = false
            } else {
                bnd.languageVisibility.visibility = View.GONE
                toggleLanguage = true
            }
        }

        bnd.clearCalories.setOnClickListener {
            dataHandler.deleteFiles(requireContext(), "calLog.txt")
        }

        bnd.clearProtein.setOnClickListener {
            dataHandler.deleteFiles(requireContext(), "protLog.txt")
        }

        bnd.clearMeals.setOnClickListener {
            dataHandler.deleteFiles(requireContext(), "meals.txt")
        }

        bnd.clearGoals.setOnClickListener {
            dataHandler.deleteFiles(requireContext(), "goals.txt")
        }

        bnd.clearAll.setOnClickListener {
            dataHandler.deleteFiles(requireContext(), "calLog.txt")
            dataHandler.deleteFiles(requireContext(), "protLog.txt")
            dataHandler.deleteFiles(requireContext(), "meals.txt")
            dataHandler.deleteFiles(requireContext(), "goals.txt")
            dataHandler.deleteFiles(requireContext(), "language.txt")
            dataHandler.deleteFiles(requireContext(), "history.txt")
        }

        bnd.languageDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                selectedLanguage = selectedItem
            }
        }

        bnd.saveLanguage.setOnClickListener {
            if (selectedLanguage != "") {
                dataHandler.saveData(
                    requireContext(),
                    languageFile,
                    Keys.Language.toString(),
                    selectedLanguage
                )
            }
        }

        return view
    }
}