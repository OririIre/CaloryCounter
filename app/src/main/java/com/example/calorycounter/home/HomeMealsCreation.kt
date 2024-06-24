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
import androidx.transition.ChangeBounds
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
            val name = "Meal" + i.toString() + "Name"
            val value = "Meal" + i.toString() + "Cal"
            val protValue = "Meal" + i.toString() + "Prot"
            val icon = "Meal" + i.toString() + "Icon"
            if(items.key.contains(name) && items.value != "value" && meals.containsKey(value) && meals.containsKey(protValue) && icons.containsKey(icon)){
                addMealLine (items.value, meals[value].toString(), meals[protValue].toString(), i, icons[icon].toString())
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

        val transition = ChangeBounds()
        transition.setDuration(200)

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
        val divider = View(context)
        divider.id = View.generateViewId()

        val dividerParam: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            1
        )

        dividerParam.addRule(RelativeLayout.BELOW, mealsName.id)
        divider.layoutParams = dividerParam
        divider.setBackgroundColor(
            ResourcesCompat.getColor(context.resources,
            R.color.white_low_transparency, null))

        return divider
    }

    private fun createRelativeLayout(): RelativeLayout {
        val relativeLayout = RelativeLayout(context)
        relativeLayout.id = View.generateViewId()

        val layoutParam: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )

        relativeLayout.layoutParams = layoutParam
        relativeLayout.setPadding(20,20,20,20)
        relativeLayout.gravity = Gravity.CENTER

        return relativeLayout
    }

    private fun createTextViewValue(mealValue: String): TextView {
        val textView = TextView(context)
        textView.id = View.generateViewId()

        val mealsValueParam: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
            280,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        mealsValueParam.addRule(RelativeLayout.ALIGN_PARENT_END)

        val valueConversion = mealValue.toDouble().toInt().toString()
        val valueText = "$valueConversion kcal"
        textView.text = valueText
        textView.textSize = 15f
        textView.isSingleLine = true
        textView.setTextColor(ResourcesCompat.getColor(context.resources, R.color.white, null))
        textView.layoutParams = mealsValueParam
        textView.gravity = Gravity.END
        textView.setCompoundDrawablesWithIntrinsicBounds(null,null, ResourcesCompat.getDrawable(context.resources,
            R.drawable.baseline_add_circle_24, null),null)
        textView.compoundDrawablePadding = 15
        return textView
    }

    private fun createTextViewName(buttonID: Int, mealName: String, icon: String): TextView {
        val textView = TextView(context)
        textView.id = buttonID

        val mealsNameParam: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        mealsNameParam.addRule(RelativeLayout.ALIGN_PARENT_START)
        mealsNameParam.addRule(RelativeLayout.START_OF, textView.id)

        textView.text = mealName
        textView.textSize = 15f
        textView.setTextColor(ResourcesCompat.getColor(context.resources, R.color.white, null))
        if(icon != "") {
            textView.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(context.resources, icon.toInt(),null), null, null, null)
        }
        textView.compoundDrawablePadding = 15
        textView.layoutParams = mealsNameParam

        return textView
    }
}