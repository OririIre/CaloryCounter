package com.example.calorycounter.view

import android.animation.ObjectAnimator
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.transition.ChangeBounds
import com.example.calorycounter.R
import com.example.calorycounter.data.HelperClass.Companion.getCurrentValue
import com.example.calorycounter.data.ProcessMeals
import com.example.calorycounter.data.SpeechSearch
import com.example.calorycounter.data.UpdateListener
import com.example.calorycounter.databinding.FragmentHomeBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import jp.wasabeef.blurry.Blurry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.jsoup.internal.StringUtil
import org.jsoup.nodes.Document
import java.util.Calendar
import java.util.Locale
import kotlin.math.round


internal enum class Value {
    Protein, Calories
}
const val MIN_SWIPE_DISTANCE = 250

//ToDo Check out how this works? Do I pass Icon.Ramen(R.drawable.baseline_ramen_dining_24)?
//ToDo Problem is that the resource IDs are probably not consistent, can be obfuscated
enum class Icon(@DrawableRes val resourceId: Int) {
    RAMEN(R.drawable.baseline_ramen_dining_24),
    COFFEE(R.drawable.baseline_coffee_24),
    DINNER(R.drawable.baseline_dinner_dining_24),
    COCKTAIL(R.drawable.baseline_local_bar_24),
    LUNCH(R.drawable.baseline_lunch_dining_24),
    WINE(R.drawable.baseline_wine_bar_24),
    BAKED(R.drawable.baseline_bakery_dining_24),
    BREAKFAST(R.drawable.baseline_breakfast_dining_24)
}

internal const val caloriesFile = "calLog.txt"
internal const val proteinFile = "protLog.txt"
internal const val languageFile = "language.txt"
internal const val mealsFile = "meals.txt"
internal const val goalsFile = "goals.txt"
internal const val historyFile = "history.txt"
internal const val iconFile = "icon.txt"

class Home : Fragment(), UpdateListener {
    private var _bnd: FragmentHomeBinding? = null
    private val bnd get() = _bnd!!
    private lateinit var goals: MutableMap<String, String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var amount: Array<String>
    private lateinit var document: Document
    private lateinit var bottomMealDialog: BottomSheetDialog
    private lateinit var bottomAddDialog: BottomSheetDialog
    private lateinit var bottomHistoryDialog: BottomSheetDialog
    private lateinit var mealsDialog: MealsDialog
    private lateinit var freeAddDialog: FreeAddDialog
    private lateinit var historyDialog: HistoryDialog
    private lateinit var speechSearch: SpeechSearch
    private lateinit var processMeals: ProcessMeals
    private var isAllFabVisible: Boolean = false
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
    private var rnd = (0..12).random()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        clearHistoryForNextDay()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _bnd = FragmentHomeBinding.inflate(inflater, container, false)
        val view = bnd.root
        var speechBubble = 1
        val thisContext = requireContext()
        mealsDialog = MealsDialog(thisContext)
        freeAddDialog = FreeAddDialog(thisContext)
        speechSearch = SpeechSearch(thisContext)
        historyDialog = HistoryDialog(thisContext)
        processMeals = ProcessMeals(thisContext)

        bnd.progressBar.max = 1000
        bnd.ProteinProgressBar.max = 1000
        bnd.progressBar2.max = 1000
        bnd.progressBar3.max = 1000

        bottomMealDialog = createDialog(R.layout.meals_layout)
        bottomAddDialog = createDialog(R.layout.free_add_layout)
        bottomHistoryDialog = createDialog(R.layout.history_layout)

        updateGoals()
        updateDaily()
        updateMeals()

        bnd.infoToggle.setOnClickListener {
            bnd.shading.visibility = View.VISIBLE
        }

        historyDialog.addListener(this)

        bnd.histroy.setOnClickListener {
            historyDialog.showHistoryDialog(bottomHistoryDialog)
        }

        bnd.shading.setOnClickListener {
            when (speechBubble) {
//                1 -> bnd.infoGroup.visibility = View.VISIBLE
                2 -> {
                    bnd.infoGroup2.visibility = View.VISIBLE
//                    bnd.infoGroup.visibility = View.GONE
                }
                3 -> {
                    bnd.infoGroup3.visibility = View.VISIBLE
                    bnd.infoGroup2.visibility = View.GONE
                }
                4 -> {
                    bnd.infoGroup4.visibility = View.VISIBLE
                    bnd.infoGroup3.visibility = View.GONE
                }
                5 -> {
                    bnd.cardView.visibility = View.INVISIBLE
                    bnd.infoGroup5.visibility = View.VISIBLE
                    bnd.infoGroup4.visibility = View.GONE
                }
                6 -> {
                    bnd.cardView.visibility = View.VISIBLE
                    bnd.infoGroup6.visibility = View.VISIBLE
                    bnd.infoGroup5.visibility = View.GONE
                }
                7 -> {
                    bnd.infoGroup6.visibility = View.GONE
                }
            }
            speechBubble++
            if (speechBubble == 8) {
                bnd.shading.visibility = View.GONE
                speechBubble = 1
            }
        }

        bnd.speechAdd.setOnClickListener {
            val selectedLanguage =
                dataHandler.loadData(requireContext(), languageFile)[Keys.Language.toString()]
            val language: String = when (selectedLanguage) {
                "German" -> {
                    "de_DE"
                }

                "English" -> {
                    "en_UK"
                }

                "French" -> {
                    "fr_FR"
                }

                else -> {
                    Locale.getDefault().toString()
                }
            }
            val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            speechIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)

            try {
                activityResultLauncher.launch(speechIntent)
            } catch (exp: ActivityNotFoundException) {
                Toast.makeText(activity, "Speech not supported", Toast.LENGTH_SHORT).show()
            }
        }

        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == RESULT_OK && result.data != null) {
                    val text = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    amount = speechSearch.filterInput(text.toString())
                    //start coroutine (like threading in java stuff)
                    if (amount.isNotEmpty()) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            val resultDocument = async {
                                speechSearch.searchRequest(amount)
                            }
                            document = resultDocument.await()
                            var value = speechSearch.extractValues(document, "wDYxhc")
                            if (value == "") {
                                value = speechSearch.extractValues(document, "MjjYud")
                            }
                            var input = ""
                            if (StringUtil.isNumeric(amount[0])) {
                                input = amount[0]
                            } else if (StringUtil.isNumeric(amount[1])) {
                                input = amount[1]
                            }
                            speechSearch.addFromSpeech(value, input)
                        }.invokeOnCompletion {
                            requireActivity().runOnUiThread{
                                updateUI()
                            }
                        }
                    } else {
                        println("wrong input")
                    }
                }

            }

        val imageView = ImageView(requireContext())
        val layoutParam: ConstraintLayout.LayoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT)
        imageView.layoutParams = layoutParam
        imageView.setBackgroundColor(Color.argb(100,0,0,0))
        imageView.visibility = View.GONE

        bnd.home.addView(imageView)
        freeAddDialog.addListener(this)

        bnd.fb.setOnClickListener {
            if (!isAllFabVisible) {
                Blurry.with(requireContext()).capture(this.view).into(imageView)
                imageView.visibility = View.VISIBLE
                imageView.bringToFront()
                bnd.fbCustom.visibility = View.VISIBLE
                bnd.fbMeals.visibility = View.VISIBLE
                bnd.addFreeText.visibility = View.VISIBLE
                bnd.addFreeText.bringToFront()
                bnd.addMealText.visibility = View.VISIBLE
                bnd.addMealText.bringToFront()
                isAllFabVisible = true
            } else {
                imageView.visibility = View.GONE
                bnd.fbCustom.visibility = View.GONE
                bnd.fbMeals.visibility = View.GONE
                bnd.addFreeText.visibility = View.GONE
                bnd.addMealText.visibility = View.GONE
                isAllFabVisible = false
            }
        }

        bnd.fbCustom.setOnClickListener {
            imageView.visibility = View.GONE
            bnd.fbCustom.visibility = View.GONE
            bnd.fbMeals.visibility = View.GONE
            bnd.addFreeText.visibility = View.GONE
            bnd.addMealText.visibility = View.GONE
            isAllFabVisible = false
            freeAddDialog.show(bottomAddDialog)
        }

        bottomAddDialog.setOnDismissListener {
            updateUI()
        }

        bnd.fbMeals.setOnClickListener {
            imageView.visibility = View.GONE
            bnd.fbCustom.visibility = View.GONE
            bnd.fbMeals.visibility = View.GONE
            bnd.addFreeText.visibility = View.GONE
            bnd.addMealText.visibility = View.GONE
            isAllFabVisible = false
            mealsDialog.show(0, bottomMealDialog)
        }

        bottomMealDialog.setOnDismissListener {
            updateMeals()
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        updateGoals()
        updateDaily()
        updateMeals()
        updateUI()
        clearHistoryForNextDay()
    }

    private fun updateGoals() {
        goals = dataHandler.loadData(requireContext(), goalsFile)
        if (goals.isEmpty()) {
            goals[Keys.Calories.toString()] = "0"
            goals[Keys.Protein.toString()] = "0"
        }
        if (!goals.containsKey(Keys.Protein.toString())) {
            goals[Keys.Protein.toString()] = "0"
        }
        if (!goals.containsKey(Keys.Calories.toString())) {
            goals[Keys.Calories.toString()] = "0"
        }
    }

    private fun updateDaily() {
        val calsCons = getCurrentValue(caloriesFile, requireContext()).toInt()
        val stringCal = "$calsCons kcal"
        bnd.usedKcal.text = stringCal
        val protCons = getCurrentValue(proteinFile, requireContext()).toInt()
        val stringProt = "$protCons g"
        bnd.consumedProt.text = stringProt
        updateRemaining(calsCons.toDouble(), Value.Calories)
        updateRemaining(protCons.toDouble(), Value.Protein)
        changeProgressBar(goals[Keys.Protein.toString()]!!, protCons.toDouble(), false)
        changeProgressBar(goals[Keys.Calories.toString()]!!, calsCons.toDouble(), true)
    }

    private fun updateMeals() {
        val meals = dataHandler.loadData(requireContext(), mealsFile)
        val icons = dataHandler.loadData(requireContext(), iconFile)
        bnd.linearLayoutMeals.removeAllViews()
        var i = 1
        for(items in meals){
            val name = i.toString() + "Name"
            val value = i.toString() + "Cal"
            val protValue = i.toString() + "Prot"
            val icon = i.toString() + "Icon"
            if(items.key.contains(name) && items.value != "value"){
                val currentMealName = items.value
                var currentMealValue = ""
                var currentMealProt = ""
                var currentIcon = ""
                var calFound = false
                var protFound = false
                for(item in meals) {
                    if(item.key.contains(value)) {
                        currentMealValue = item.value
                        calFound = true
                    }
                    if(item.key.contains(protValue)) {
                        currentMealProt = item.value
                        protFound = true
                    }
                    for(ic in icons) {
                        if (ic.key.contains(icon)) {
                            currentIcon = ic.value
                        }
                    }
                    if(calFound && protFound){
                        createMealUI (currentMealName, currentMealValue, currentMealProt, i, currentIcon)
                        i++
                        calFound = false
                        protFound = false
                    }
                }
            }
        }
    }

    private fun updateRemaining(currentValue: Double, goal: Value) {
        if (goal == Value.Calories) {
            if (goals[Keys.Calories.toString()]!!.toDouble() != 0.0) {
                var remaining = goals[Keys.Calories.toString()]!!.toInt() - currentValue.toInt()
                if (remaining <= 0) {
                    remaining = 0
                }
                val left = "$remaining kcal"
                bnd.leftKcal.text = left
            } else if (goals[Keys.Calories.toString()]!!.toDouble() == 0.0) {
                bnd.leftKcal.text = 0.toString()
            }
        } else {
            if (goals[Keys.Protein.toString()]!!.toDouble() != 0.0) {
                var remaining = goals[Keys.Protein.toString()]!!.toInt() - currentValue.toInt()
                if (remaining <= 0) {
                    remaining = 0
                }
                val left = "$remaining g"
                bnd.leftProt.text = left
            } else if (goals[Keys.Protein.toString()]!!.toDouble() == 0.0) {
                bnd.leftProt.text = 0.toString()
            }
        }
    }

    private fun createMealUI (mealName: String, mealValue: String, mealProt: String, buttonID: Int, icon: String){
        val parentLayout = bnd.linearLayoutMeals
        val relativeLayout = RelativeLayout(requireContext())
        val mealsName = TextView(requireContext())
        val mealsValue = TextView(requireContext())
        val divider = View(requireContext())
        mealsName.id = buttonID
        mealsValue.id = View.generateViewId()
        relativeLayout.id = View.generateViewId()

        val layoutParam: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        relativeLayout.layoutParams = layoutParam
        relativeLayout.setPadding(20,20,20,20)
        relativeLayout.gravity = Gravity.CENTER
        relativeLayout.focusable = View.FOCUSABLE
        relativeLayout.isFocusableInTouchMode = true

        val mealsValueParam: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
            280,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        mealsValueParam.addRule(RelativeLayout.ALIGN_PARENT_END)

        val valueText = "$mealValue kcal"
        mealsValue.text = valueText
        mealsValue.textSize = 15f
        mealsValue.isSingleLine = true
        mealsValue.setTextColor(ResourcesCompat.getColor(resources, R.color.white, null))
        mealsValue.layoutParams = mealsValueParam
        mealsValue.gravity = Gravity.END
        mealsValue.setCompoundDrawablesWithIntrinsicBounds(null,null,ResourcesCompat.getDrawable(resources,
            R.drawable.baseline_add_circle_24, null),null)
        mealsValue.compoundDrawablePadding = 15

        val mealsNameParam: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        mealsNameParam.addRule(RelativeLayout.ALIGN_PARENT_START)
        mealsNameParam.addRule(RelativeLayout.START_OF, mealsName.id)

        mealsName.text = mealName
        mealsName.textSize = 15f
        mealsName.setTextColor(ResourcesCompat.getColor(resources, R.color.white, null))
        if(icon != "") {
            mealsName.setCompoundDrawablesWithIntrinsicBounds(
                ResourcesCompat.getDrawable(
                    resources,
                    icon.toInt(),
                    null
                ), null, null, null
            )
        }
        mealsName.compoundDrawablePadding = 15
        mealsName.layoutParams = mealsNameParam

        val dividerParam: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            1
        )
        dividerParam.addRule(RelativeLayout.BELOW, mealsName.id)
        divider.layoutParams = dividerParam
        divider.setBackgroundColor(ResourcesCompat.getColor(resources,
            R.color.white_low_transparency, null))
        divider.id = View.generateViewId()

        relativeLayout.addView(mealsValue)
        relativeLayout.addView(mealsName)
        relativeLayout.addView(divider)

        val transition = ChangeBounds()
        transition.setDuration(200)

        mealsValue.setOnClickListener{
            processMeals.addMeal(mealValue, mealProt)
            updateUI()
        }

        mealsName.setOnClickListener{
            mealsDialog.show(mealsName.id, bottomMealDialog)
        }

        mealsName.setOnLongClickListener {
            Snackbar.make(bnd.home, "Delete Entry?", 4000)
                .setBackgroundTint(resources.getColor(R.color.black, null))
                .setAction("DELETE") {
                    processMeals.deleteMeal(mealsName.id)
                    updateMeals()
                    }
                .show()
            true
        }
        parentLayout.addView(relativeLayout)
    }

    private fun changeProgressBar(setAmount: String, consumed: Double, cal: Boolean) {
        if (setAmount != "") {
            val goal = setAmount.toInt()
            val progress = round(consumed / (goal / 100))
            val remaining = round(100 - (consumed / (goal / 100)))
            if (cal) {
                val animator = ObjectAnimator.ofInt(bnd.progressBar, "progress", remaining.toInt()*10)
                animator.setDuration(1500)
                animator.interpolator = FastOutSlowInInterpolator()
                animator.start()
                val animator2 = ObjectAnimator.ofInt(bnd.progressBar2, "progress", progress.toInt()*10)
                animator2.setDuration(2000)
                animator2.interpolator = FastOutSlowInInterpolator()
                animator2.start()
            } else {
                val animator = ObjectAnimator.ofInt(bnd.ProteinProgressBar, "progress", remaining.toInt()*10)
                animator.setDuration(1500)
                animator.interpolator = FastOutSlowInInterpolator()
                animator.start()
                val animator2 = ObjectAnimator.ofInt(bnd.progressBar3, "progress", progress.toInt()*10)
                animator2.setDuration(2000)
                animator2.interpolator = FastOutSlowInInterpolator()
                animator2.start()
            }
        }
    }

    private fun updateUI(){
        val currentCalories = getCurrentValue(caloriesFile, requireContext())
        val currentProtein = getCurrentValue(proteinFile, requireContext())

        changeProgressBar(goals[Keys.Calories.toString()]!!, currentCalories, true)
        val calCons = currentCalories.toInt().toString() + " kcal"
        bnd.usedKcal.text = calCons
        updateRemaining(currentCalories, Value.Calories)
        if (currentCalories > goals[Keys.Calories.toString()]!!.toDouble()) {
            rnd = (0..12).random()
            val snack: Snackbar = Snackbar.make(bnd.cardView, messages[rnd], 4000)
            val snackView = snack.view
            val params = snackView.layoutParams as FrameLayout.LayoutParams
            params.gravity = Gravity.TOP
            params.setMargins(20,30, 20,0)
            snackView.layoutParams = params
            snackView.setBackgroundColor(resources.getColor(R.color.black, null))
            snack.show()
        }

        changeProgressBar(goals[Keys.Protein.toString()]!!, currentProtein, false)
        val protCons = currentProtein.toInt().toString() + " g"
        bnd.consumedProt.text = protCons
        updateRemaining(currentProtein, Value.Protein)
    }

    override fun onStuffUpdated() {
        updateUI()
    }

    private fun createDialog(layout: Int): BottomSheetDialog {
        val bottomDialog = layoutInflater.inflate(layout, null)
        val bottomSheet = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        bottomSheet.setContentView(bottomDialog)

        return bottomSheet
    }

    private fun clearHistoryForNextDay(){
        val historyValues = dataHandler.loadData(requireContext(), historyFile)
        val calendar  = Calendar.getInstance()
        val currentTime = String.format(Locale.getDefault(),"%02d.%02d",
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.MONTH)+1)
        for(items in historyValues){
            if(!items.key.contains(currentTime)){
                dataHandler.deleteFiles(requireContext(), historyFile)
            }
        }
    }
}