package com.example.calorycounter.home.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.example.calorycounter.R
import com.example.calorycounter.helpers.UpdateListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.lang.ref.WeakReference
import java.util.Locale

class FreeAddDialog (con: Context) {
    private val context = con
    private val freeAddProcessing = FreeAddDialogLogic(con)
    private lateinit var custom: EditText
    private lateinit var kcal: EditText
    private lateinit var gramm: EditText

    private var listener = WeakReference<UpdateListener>(null)
    @SuppressLint("InflateParams")
    fun show(freeAddDialog: BottomSheetDialog) {
        var calProtSwitch = true
        var toggleSettings = true

        kcal = freeAddDialog.findViewById(R.id.kcal)!!
        gramm = freeAddDialog.findViewById(R.id.gramm)!!
        custom = freeAddDialog.findViewById(R.id.enter_calorie_amount)!!
        val caloriesSwitch: TextView = freeAddDialog.findViewById(R.id.caloriesSwitcher)!!
        val proteinSwitch: TextView = freeAddDialog.findViewById(R.id.proteinSwitcher)!!
        val typeText: TextView = freeAddDialog.findViewById(R.id.typeText)!!
        val saveValues: Button = freeAddDialog.findViewById(R.id.button_add2)!!

        val additionalInfoButton: TextView = freeAddDialog.findViewById(R.id.button_additional_settings)!!
        val additionalSettings: RelativeLayout = freeAddDialog.findViewById(R.id.layoutAdditionalSettings)!!

        val borderDrawable = ResourcesCompat.getDrawable(context.resources, R.drawable.custom_textview_border, null)

        fun updateUI(isCalories: Boolean) {
            calProtSwitch = isCalories
            clearValues()
            caloriesSwitch.background = if (isCalories) borderDrawable else null
            proteinSwitch.background = if (!isCalories) borderDrawable else null
            typeText.text = if (isCalories) context.getString(R.string.kcalDescription) else context.getString(R.string.proteinDescriptionAdd)
        }

        updateUI(true) // Initialize with calories selected
        additionalSettings.visibility = View.GONE

        additionalInfoButton.setOnClickListener {
            toggleSettings = !toggleSettings
            additionalSettings.visibility = if (toggleSettings) View.GONE else View.VISIBLE
        }

        val handleEditorAction: (TextView, Int, KeyEvent?) -> Boolean = { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                val amount = formatString(kcal.text.toString()).replace(",",".")
                val weight = formatString(gramm.text.toString()).replace(",",".")
                val customText = formatString(custom.text.toString()).replace(",",".")
                freeAddProcessing.addSub(calProtSwitch, amount, weight, customText)
                listener.get()?.onStuffUpdated()
                clearValues()
                true
            } else {
                false
            }
        }

        custom.setOnEditorActionListener(handleEditorAction)
        gramm.setOnEditorActionListener(handleEditorAction)

        caloriesSwitch.setOnClickListener { updateUI(true) }
        proteinSwitch.setOnClickListener { updateUI(false) }

        saveValues.setOnClickListener {
            val amount = formatString(kcal.text.toString()).replace(",",".")
            val weight = formatString(gramm.text.toString()).replace(",",".")
            val customText = formatString(custom.text.toString()).replace(",",".")
            println(customText)
            freeAddProcessing.addSub(calProtSwitch, amount, weight, customText)
            listener.get()?.onStuffUpdated()
            clearValues()
        }
        freeAddDialog.show()
    }

    private fun clearValues(){
        kcal.text.clear()
        gramm.text.clear()
        custom.text.clear()
    }

    private fun formatString (value: String): String {
        return if (value.isNotBlank()) {
            String.format(Locale.getDefault(), "%.1f", value.replace(",",".").toDoubleOrNull() ?: 0.0)
        } else {
            ""
        }
    }

    fun addListener(listener: UpdateListener){
        this.listener = WeakReference(listener)
    }
}

