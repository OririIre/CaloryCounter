package com.example.calorycounter.settings

import android.animation.LayoutTransition
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.calorycounter.R
import com.example.calorycounter.data.DataHandler
import com.example.calorycounter.databinding.FragmentSettingsBinding
import com.example.calorycounter.helpers.appLanguageFile
import com.example.calorycounter.helpers.languageFile
import com.google.android.material.color.MaterialColors


class SettingsFragment : Fragment() {
    private val dataHandler = DataHandler()
    private var _bnd: FragmentSettingsBinding? = null
    private val bnd get() = _bnd!!
    private var toggleReset = true
    private var toggleGoals = true
    private var toggleLanguage = true
    private var toggleTheme = true
    private var selectedLanguage = "Default"
    private var selectedAppLanguage = "Default"
    private var selectedAppTheme = "Default"
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
        val themeList = arrayOf("Default", "Light", "Dark")
        settingsLogic = SettingsLogic(requireContext())

        bnd.saveGoals.setOnClickListener {
            settingsLogic.saveGoals(bnd.enterCaloriesGoal.text.toString(),bnd.enterProteinGoal.text.toString())
            bnd.enterCaloriesGoal.text?.clear()
            bnd.enterProteinGoal.text.clear()
        }

        bnd.iconAndTextGoal.setOnClickListener {
            toggleGoals = settingsLogic.expandPanel(bnd.goalVisibility, toggleGoals)
        }

        bnd.iconAndTextReset.setOnClickListener {
            toggleReset = settingsLogic.expandPanel(bnd.clearVisibility, toggleReset)
        }

        bnd.iconAndTextLanguage.setOnClickListener {
            toggleLanguage = settingsLogic.expandPanel(bnd.languageVisibility, toggleLanguage)
        }

        bnd.iconAndTextTheme.setOnClickListener {
            toggleTheme = settingsLogic.expandPanel(bnd.themeVisibility, toggleTheme)
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
                (bnd.languageDropdown.selectedView as? TextView)?.setTextColor(MaterialColors.getColor(requireContext(), R.attr.text_color, Color.BLACK))
                (bnd.languageDropdown.selectedView as? TextView)?.setBackgroundColor(Color.TRANSPARENT)
                selectedLanguage = parent?.getItemAtPosition(position).toString()
            }
        }

        currentAppLanguage = settingsLogic.setDropdownSelected(bnd.appLanguageDropdown, languageList, appLanguageFile, requireActivity())

        bnd.appLanguageDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected( parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                (bnd.appLanguageDropdown.selectedView as? TextView)?.setTextColor(MaterialColors.getColor(requireContext(), R.attr.text_color, Color.BLACK))
                (bnd.appLanguageDropdown.selectedView as? TextView)?.setBackgroundColor(Color.TRANSPARENT)
                selectedAppLanguage = parent?.getItemAtPosition(position).toString()
            }
        }

        bnd.saveLanguage.setOnClickListener {
            settingsLogic.saveLanguage(selectedLanguage, selectedAppLanguage, currentAppLanguage, requireActivity())
        }

        settingsLogic.setThemeDropdownSelected(bnd.themeDropdown, themeList, requireActivity())

        bnd.themeDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected( parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                (bnd.themeDropdown.selectedView as? TextView)?.setTextColor(MaterialColors.getColor(requireContext(), R.attr.text_color, Color.BLACK))
                (bnd.themeDropdown.selectedView as? TextView)?.setBackgroundColor(Color.TRANSPARENT)
                selectedAppTheme = parent?.getItemAtPosition(position).toString()
            }
        }

        bnd.saveTheme.setOnClickListener {
            settingsLogic.saveTheme(selectedAppTheme, requireActivity())
        }

        super.onViewCreated(view, savedInstanceState)
    }


}