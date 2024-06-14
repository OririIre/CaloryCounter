package com.example.calorycounter.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.example.calorycounter.R
import com.example.calorycounter.data.UpdateListener
import com.example.calorycounter.data.ProcessFreeAdd
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.lang.ref.WeakReference

class FreeAddDialog (con: Context) {
    private val context = con
    private val freeAddProcessing = ProcessFreeAdd(con)

    private var listener = WeakReference<UpdateListener>(null)
    @SuppressLint("InflateParams")
    fun show(freeAddDialog: BottomSheetDialog) {
        var calProtSwitch = true
        var toggleSettings = true

        val saveValues: Button = freeAddDialog.findViewById(R.id.button_add2)!!
        val kcal: EditText = freeAddDialog.findViewById(R.id.kcal)!!
        val gramm: EditText = freeAddDialog.findViewById(R.id.gramm)!!
        val custom: EditText = freeAddDialog.findViewById(R.id.enter_calorie_amount)!!
        val caloriesSwitch: TextView = freeAddDialog.findViewById(R.id.caloriesSwitcher)!!
        val proteinSwitch: TextView = freeAddDialog.findViewById(R.id.proteinSwitcher)!!
        val typeText: TextView = freeAddDialog.findViewById(R.id.typeText)!!

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
                freeAddProcessing.addSub(calProtSwitch, kcal.text.toString(), gramm.text.toString(), custom.text.toString())
                listener.get()?.onStuffUpdated()
                kcal.text.clear()
                gramm.text.clear()
                custom.text.clear()
                true
            } else {
                false
            }
        }

        gramm.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == 4) {
                freeAddProcessing.addSub(calProtSwitch, kcal.text.toString(), gramm.text.toString(), custom.text.toString())
                listener.get()?.onStuffUpdated()
                kcal.text.clear()
                gramm.text.clear()
                custom.text.clear()
                true
            } else {
                false
            }
        }

        caloriesSwitch.setOnClickListener {
            calProtSwitch = true
            kcal.text.clear()
            gramm.text.clear()
            custom.text.clear()
            caloriesSwitch.background = ResourcesCompat.getDrawable(context.resources,
                R.drawable.custom_textview_border, null)
            proteinSwitch.background = null
            typeText.text = context.getString(R.string.kcalDescription)
        }

        proteinSwitch.setOnClickListener {
            calProtSwitch = false
            kcal.text.clear()
            gramm.text.clear()
            custom.text.clear()
            proteinSwitch.background = ResourcesCompat.getDrawable(context.resources,
                R.drawable.custom_textview_border, null)
            caloriesSwitch.background = null
            typeText.text = context.getString(R.string.proteinDescriptionAdd)
        }

        saveValues.setOnClickListener {
            freeAddProcessing.addSub(calProtSwitch, kcal.text.toString(), gramm.text.toString(), custom.text.toString())
            listener.get()?.onStuffUpdated()
            kcal.text.clear()
            gramm.text.clear()
            custom.text.clear()
        }
        freeAddDialog.show()
    }

    fun addListener(listener: UpdateListener){
        this.listener = WeakReference(listener)
    }
}

