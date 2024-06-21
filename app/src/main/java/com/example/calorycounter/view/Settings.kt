package com.example.calorycounter.view

import android.animation.LayoutTransition
import android.app.LocaleManager
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import com.example.calorycounter.Keys
import com.example.calorycounter.R
import com.example.calorycounter.appLanguageFile
import com.example.calorycounter.data.DataHandler
import com.example.calorycounter.databinding.FragmentSettingsBinding
import com.example.calorycounter.goalsFile
import com.example.calorycounter.languageFile


class Settings : Fragment() {
    private val dataHandler = DataHandler()
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
        var selectedAppLanguage = "Phone Default"
        var languageList = listOf(requireContext().resources.getString(R.string.language_german),
            requireContext().resources.getString(R.string.language_english),
            requireContext().resources.getString(R.string.language_french),
            requireContext().resources.getString(R.string.language_spanish))

        bnd.layoutClearData.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        bnd.layoutCalories.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        bnd.layoutLanguage.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

        val items = arrayOf("Phone Default", resources.getString(R.string.language_german), resources.getString(R.string.language_english), resources.getString(R.string.language_french),resources.getString(R.string.language_spanish))
        val adapter: ArrayAdapter<String> =
            ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_dropdown_item, items)
        bnd.languageDropdown.adapter = adapter

        val language: Int =
            when (dataHandler.loadData(requireContext(), languageFile)[Keys.Language.toString()]) {
                resources.getString(R.string.language_german) -> {
                    1
                }
                resources.getString(R.string.language_english) -> {
                    2
                }
                resources.getString(R.string.language_french) -> {
                    3
                }
                resources.getString(R.string.language_spanish) -> {
                    4
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
            dataHandler.deleteFiles(requireContext(), "icon.txt")
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
            dataHandler.deleteFiles(requireContext(), "icon.txt")
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

        bnd.appLanguageDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                selectedAppLanguage = selectedItem
            }
        }

        bnd.saveLanguage.setOnClickListener {
            if (selectedLanguage != "") {
                dataHandler.saveData(requireContext(), languageFile, Keys.Language.toString(), selectedLanguage)
            }
            if (selectedAppLanguage != "") {
                dataHandler.saveData(requireContext(), appLanguageFile, Keys.Language.toString(), selectedAppLanguage)
                //Todo make it that it works and implement check so that it only recreates when the language actually got changed
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requireContext().getSystemService(LocaleManager::class.java)
                        .applicationLocales = LocaleList.forLanguageTags("de")
                } else {
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags("de")
                    )
                }
                requireActivity().recreate()
            }
        }

        return view
    }
}