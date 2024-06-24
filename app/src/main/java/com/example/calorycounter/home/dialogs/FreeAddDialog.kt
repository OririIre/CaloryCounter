package com.example.calorycounter.home.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
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

        caloriesSwitch.background = ResourcesCompat.getDrawable(context.resources,
            R.drawable.custom_textview_border, null)
        proteinSwitch.background = null
        additionalSettings.visibility = View.GONE

        additionalInfoButton.setOnClickListener {
            if (toggleSettings) {
                additionalSettings.visibility = View.VISIBLE
                toggleSettings = false
            } else {
                additionalSettings.visibility = View.GONE
                toggleSettings = true
            }
        }

        custom.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == 4) {
                val amount = formatString(kcal.text.toString())
                val weight = formatString(gramm.text.toString())
                val customText = formatString(custom.text.toString())
                freeAddProcessing.addSub(calProtSwitch, amount, weight, customText)
                listener.get()?.onStuffUpdated()
                clearValues()
                true
            } else {
                false
            }
        }

        gramm.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == 4) {
                val amount = formatString(kcal.text.toString())
                val weight = formatString(gramm.text.toString())
                val customText = formatString(custom.text.toString())
                freeAddProcessing.addSub(calProtSwitch, amount, weight, customText)
                listener.get()?.onStuffUpdated()
                clearValues()
                true
            } else {
                false
            }
        }

        caloriesSwitch.setOnClickListener {
            calProtSwitch = true
            clearValues()
            caloriesSwitch.background = ResourcesCompat.getDrawable(context.resources,
                R.drawable.custom_textview_border, null)
            proteinSwitch.background = null
            typeText.text = context.getString(R.string.kcalDescription)
        }

        proteinSwitch.setOnClickListener {
            calProtSwitch = false
            clearValues()
            proteinSwitch.background = ResourcesCompat.getDrawable(context.resources,
                R.drawable.custom_textview_border, null)
            caloriesSwitch.background = null
            typeText.text = context.getString(R.string.proteinDescriptionAdd)
        }

        saveValues.setOnClickListener {
            val amount = formatString(kcal.text.toString())
            val weight = formatString(gramm.text.toString())
            val customText = formatString(custom.text.toString())
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
        var returnString = ""
        if(value != "") {
            returnString = String.format(Locale.getDefault(), "%.1f", value.toDouble())
        }
        return returnString
    }

    fun addListener(listener: UpdateListener){
        this.listener = WeakReference(listener)
    }
}

