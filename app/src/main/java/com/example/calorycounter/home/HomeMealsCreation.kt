package com.example.calorycounter.home

import android.content.Context
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.example.calorycounter.R
import com.example.calorycounter.data.DataHandler
import com.example.calorycounter.helpers.iconFile
import com.example.calorycounter.helpers.mealsFile
import com.example.calorycounter.home.dialogs.MealsDialog
import com.example.calorycounter.home.dialogs.MealsDialogLogic
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar

class HomeMealsCreation (con: Context, linearLMeals: LinearLayout, homeLayout: ConstraintLayout, dialog: BottomSheetDialog, homeProgressBars: HomeProgressBars){
    private val context = con
    private val dataHandler = DataHandler()
    private val linearLayoutMeals = linearLMeals
    private var mealsDialogLogic = MealsDialogLogic(context)
    private val home = homeLayout
    private val mealsDialog = MealsDialog(context)
    private val bottomMealDialog = dialog
    private val homeProgress = homeProgressBars
    
    fun updateMealsUI() {
        val meals = dataHandler.loadData(context, mealsFile)
        val icons = dataHandler.loadData(context, iconFile)
        linearLayoutMeals.removeAllViews()
        var i = 1
        for(items in meals){
            val nameKey = "Meal${i}Name"
            val valueKey = "Meal${i}Cal"
            val protValueKey = "Meal${i}Prot"
            val iconKey = "Meal${i}Icon"
            if(items.key.contains(nameKey) && items.value != "value" && meals.containsKey(valueKey) && meals.containsKey(protValueKey) && icons.containsKey(iconKey)){
                addMealLine (items.value, meals[valueKey].toString(), meals[protValueKey].toString(), i, icons[iconKey].toString())
                i++
            }
        }
    }

    private fun addMealLine (mealName: String, mealValue: String, mealProt: String, buttonID: Int, icon: String){
        val parentLayout = linearLayoutMeals
        val relativeLayout = createRelativeLayout()
        val mealsName = createTextViewName(buttonID, mealName, icon)
        val mealsValue = createTextViewValue(mealValue)
        val divider = createDivider(mealsName)

        relativeLayout.addView(mealsValue)
        relativeLayout.addView(mealsName)
        relativeLayout.addView(divider)

        mealsValue.setOnClickListener{
            mealsValue.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
            mealsDialogLogic.addMeal(mealValue, mealProt, mealName)
            homeProgress.updateUI()
        }

        mealsName.setOnClickListener{
            mealsName.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
            mealsDialog.show(mealsName.id, bottomMealDialog)
        }

        mealsName.setOnLongClickListener {
            Snackbar.make(home, "Delete Entry?", 4000)
                .setBackgroundTint(context.resources.getColor(R.color.black, null))
                .setAction("DELETE") {
                    mealsDialogLogic.deleteMeal(mealsName.id)
                    updateMealsUI()
                }
                .show()
            true
        }
        parentLayout.addView(relativeLayout)
    }

    private fun createDivider(mealsName: TextView): View {
        return View(context).apply {
            id = View.generateViewId()
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                1
            ).apply {
                addRule(RelativeLayout.BELOW, mealsName.id)
            }
            setBackgroundColor(
                ResourcesCompat.getColor(context.resources, R.color.white_low_transparency, null)
            )
        }
    }

    private fun createRelativeLayout(): RelativeLayout {
        return RelativeLayout(context).apply {
            id = View.generateViewId()
            layoutParams= RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(20, 20, 20, 20)
            gravity = Gravity.CENTER
        }
    }

    private fun createTextViewValue(mealValue: String): TextView {
        val valueConversion = mealValue.toDouble().toInt().toString()
        val valueText = "$valueConversion kcal"

        return TextView(context).apply {
            id = View.generateViewId()
            layoutParams = RelativeLayout.LayoutParams(
                280,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.ALIGN_PARENT_END)
            }
            text = valueText
            textSize = 15f
            isSingleLine = true
            gravity = Gravity.END
            setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                ResourcesCompat.getDrawable(context.resources, R.drawable.baseline_add_circle_24, null),
                null
            )
            compoundDrawablePadding = 15
        }
    }

    private fun createTextViewName(buttonID: Int, mealName: String, icon: String): TextView {
        return TextView(context).apply {
            id = buttonID
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.ALIGN_PARENT_START)
                addRule(RelativeLayout.START_OF,id)
            }
            text = mealName
            textSize = 15f
            if (icon.isNotEmpty()) {
                val iconDrawable = try {
                    ResourcesCompat.getDrawable(context.resources, icon.toInt(), null)
                } catch (e: NumberFormatException) {
                    null
                }
                setCompoundDrawablesWithIntrinsicBounds(iconDrawable, null, null, null)
            }
            compoundDrawablePadding = 15}
    }
}