package com.example.calorycounter.settings

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
import android.widget.Spinner
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import com.example.calorycounter.helpers.Keys
import com.example.calorycounter.R
import com.example.calorycounter.helpers.appLanguageFile
import com.example.calorycounter.data.DataHandler
import com.example.calorycounter.databinding.FragmentSettingsBinding
import com.example.calorycounter.helpers.goalsFile
import com.example.calorycounter.helpers.languageFile


class SettingsFragment : Fragment() {
    private val dataHandler = DataHandler()
    private var _bnd: FragmentSettingsBinding? = null
    private val bnd get() = _bnd!!

    private var selectedLanguage = "Default"
    private var selectedAppLanguage = "Default"
    private var currentAppLanguage = "en"
    private lateinit var settingsLogic: SettingsLogic

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _bnd = FragmentSettingsBinding.inflate(inflater, container, false)
        val view = bnd.root

        bnd.layoutClearData.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        bnd.layoutCalories.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        bnd.layoutLanguage.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val languageList = arrayOf("Default", requireContext().resources.getString(R.string.language_german), requireContext().resources.getString(R.string.language_english), requireContext().resources.getString(R.string.language_french), requireContext().resources.getString(R.string.language_spanish))
        settingsLogic = SettingsLogic(requireContext())

        bnd.saveGoals.setOnClickListener {
            settingsLogic.saveGoals(bnd.enterCaloriesGoal.text.toString(),bnd.enterProteinGoal.text.toString())
            bnd.enterCaloriesGoal.text?.clear()
            bnd.enterProteinGoal.text.clear()
        }

        bnd.iconAndTextGoal.setOnClickListener {
            settingsLogic.expandGoals(bnd.goalVisibility)
        }

        bnd.iconAndTextReset.setOnClickListener {
            settingsLogic.expandReset(bnd.clearVisibility)
        }

        bnd.iconAndTextLanguage.setOnClickListener {
            settingsLogic.expandLanguage(bnd.languageVisibility)
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

        settingsLogic.setDropdownSelected(bnd.languageDropdown, languageList, languageFile, requireActivity())

        bnd.languageDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedLanguage = parent?.getItemAtPosition(position).toString()
            }
        }

        currentAppLanguage = settingsLogic.setDropdownSelected(bnd.appLanguageDropdown, languageList, appLanguageFile, requireActivity())

        bnd.appLanguageDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected( parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedAppLanguage = parent?.getItemAtPosition(position).toString()
            }
        }

        bnd.saveLanguage.setOnClickListener {
            settingsLogic.saveLanguage(selectedLanguage, selectedAppLanguage, currentAppLanguage, requireActivity())
        }
        super.onViewCreated(view, savedInstanceState)
    }


}