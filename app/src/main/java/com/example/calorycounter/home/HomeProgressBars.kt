package com.example.calorycounter.home

import android.animation.ObjectAnimator
import android.content.Context
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.example.calorycounter.R
import com.example.calorycounter.data.DataHandler
import com.example.calorycounter.helpers.HelperClass.Companion.getCurrentValue
import com.example.calorycounter.helpers.Keys
import com.example.calorycounter.helpers.caloriesFile
import com.example.calorycounter.helpers.goalsFile
import com.example.calorycounter.helpers.proteinFile
import com.google.android.material.snackbar.Snackbar
import kotlin.math.round

private var rnd = (0..12).random()
private var messages = arrayOf(
    "Disappointed but not surprised...",
    "Nope not today",
    "Losing everything but weight",
    "They said you could do anything so you became a disappointment",
    "I was expecting too much...",
    "Again?!?!",
    "Your are letting me down like gravity",
    "Going as a disappointment for halloween again?",
    "Gave up on that summer body?",
    "Hope you did enough of that workout!",
    "You should rethink your mindset",
    "That's NOT the spirit",
    "Maybe another day, huh?"
)

class HomeProgressBars (con: Context, remainingCalories: ProgressBar, consumedCalories: ProgressBar,
                        remainingProtein: ProgressBar, consumedProtein: ProgressBar,
                        consumedProtText: TextView, usedKcalText:TextView, relLayout: RelativeLayout,
                        leftCal: TextView, leftPro: TextView){
    private val context = con
    private val dataHandler = DataHandler()
    private val remainingCaloriesProgressBar = remainingCalories
    private val consumedCaloriesProgressBar = consumedCalories
    private val remainingProteinProgressBar = remainingProtein
    private val consumedProteinProgressBar = consumedProtein
    private val consumedProt = consumedProtText
    private val usedKcal = usedKcalText
    private val cardView = relLayout
    private val goals = dataHandler.loadData(context, goalsFile)
    private val leftKcal = leftCal
    private val leftProt = leftPro

    fun updateUI(){
        val caloriesGoal = goals[Keys.Calories.toString()].toString()
        val proteinGoal = goals[Keys.Protein.toString()].toString()

        val currentCalories = getCurrentValue(caloriesFile, context)
        val currentProtein = getCurrentValue(proteinFile, context)

        val calCons = currentCalories.toInt().toString() + " kcal"
        val protCons = currentProtein.toInt().toString() + " g"

        animateProgressBar(caloriesGoal, currentCalories, false,1500, remainingCaloriesProgressBar)
        animateProgressBar(caloriesGoal, currentCalories, true, 2000, consumedCaloriesProgressBar)
        animateProgressBar(proteinGoal, currentProtein, false, 1500, remainingProteinProgressBar)
        animateProgressBar(proteinGoal, currentProtein, true,2000, consumedProteinProgressBar)

        consumedProt.text = protCons
        usedKcal.text = calCons

        updateReamainingUI(currentCalories, Keys.Calories)
        updateReamainingUI(currentProtein, Keys.Protein)

        if (currentCalories > caloriesGoal.toDouble()) {
            rnd = (0..12).random()
            val snack: Snackbar = Snackbar.make(cardView, messages[rnd], 4000)
            val snackView = snack.view
            val params = snackView.layoutParams as FrameLayout.LayoutParams
            params.gravity = Gravity.TOP
            params.setMargins(20,30, 20,0)
            snackView.layoutParams = params
            snackView.setBackgroundColor(context.resources.getColor(R.color.black, null))
            snack.setBackgroundTint(context.resources.getColor(R.color.black, null))
            snack.show()
        }
    }

    private fun updateReamainingUI(currentValue: Double, key: Keys){
        var remaining: String
        remaining = prepareRemainingText(currentValue, key)
        if (Keys.Calories == key){
            remaining = "$remaining kcal"
            leftKcal.text = remaining
        } else {
            remaining = "$remaining g"
            leftProt.text = remaining
        }
    }

    private fun prepareRemainingText(currentValue: Double, key: Keys): String {
        val remaning: String
        val goal = goals[key.toString()]!!.toDouble()
        if (goal != 0.0) {
            var newRemainingValue = goal.toInt() - currentValue.toInt()
            if (newRemainingValue <= 0) {
                newRemainingValue = 0
            }
            remaning = newRemainingValue.toString()
        } else {
            remaning = 0.toString()
        }
        return remaning
    }

    private fun animateProgressBar(setAmount: String, consumed: Double, increment: Boolean, duration: Int, progressBar: ProgressBar) {
        if (setAmount != "") {
            val incementAmount = round(consumed / (setAmount.toInt() / 100))
            val decrementAmount = round(100 - (consumed / (setAmount.toInt() / 100)))
            if (increment) {
                val animator2 = ObjectAnimator.ofInt(progressBar, "progress", incementAmount.toInt()*10)
                animator2.setDuration(duration.toLong())
                animator2.interpolator = FastOutSlowInInterpolator()
                animator2.start()
            } else {
                val animator = ObjectAnimator.ofInt(progressBar, "progress", decrementAmount.toInt()*10)
                animator.setDuration(duration.toLong())
                animator.interpolator = FastOutSlowInInterpolator()
                animator.start()
            }
        }
    }
}