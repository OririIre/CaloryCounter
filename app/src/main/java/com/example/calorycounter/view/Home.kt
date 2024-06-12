package com.example.calorycounter.view

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.example.calorycounter.R
import com.example.calorycounter.data.HelperClass
import com.example.calorycounter.databinding.FragmentHomeBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import jp.wasabeef.blurry.Blurry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.internal.StringUtil.isNumeric
import org.jsoup.nodes.Document
import java.io.IOException
import java.net.URL
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
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

class Home : Fragment() {
    private var _bnd: FragmentHomeBinding? = null
    private val bnd get() = _bnd!!
    private lateinit var dateView: TextView
    private lateinit var add2: Button
    private lateinit var kcal: EditText
    private lateinit var gramm: EditText
    private lateinit var custom: EditText
    private lateinit var dialog: BottomSheetDialog
    private lateinit var historyDialog: BottomSheetDialog
    private lateinit var layoutHistoryCards: LinearLayout
    private lateinit var historyScrollView: ScrollView
    private lateinit var caloriesSwitch: TextView
    private lateinit var proteinSwitch: TextView
    private lateinit var goals: MutableMap<String, String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var amount: Array<String>
    private lateinit var additionalInfoButton: TextView
    private lateinit var additionalSettings: RelativeLayout
    private lateinit var document: Document
    private lateinit var mLayout: CoordinatorLayout
    private lateinit var relativeLayout: RelativeLayout
    private lateinit var bottomMealDialog: BottomSheetDialog
    private lateinit var bottomAddDialog: BottomSheetDialog
    private var isAllFabVisible: Boolean = false
    private var alcoholToggle = false
    private lateinit var mealsDialog: MealsDialog
    private lateinit var freeAddDialog: FreeAddDialog
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
    private val caloriesFile = "calLog.txt"
    private val proteinFile = "protLog.txt"
    private val languageFile = "language.txt"
    private val mealsFile = "meals.txt"
    private val goalsFile = "goals.txt"
    private val historyFile = "history.txt"
    private val iconFile = "icon.txt"
    private val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _bnd = FragmentHomeBinding.inflate(inflater, container, false)
        val view = bnd.root
        var speechBubble = 1
        mealsDialog = MealsDialog(requireActivity())
        freeAddDialog = FreeAddDialog(requireActivity())

        bnd.progressBar.max = 1000
        bnd.ProteinProgressBar.max = 1000
        bnd.progressBar2.max = 1000
        bnd.progressBar3.max = 1000

        bottomMealDialog = createDialog(R.layout.meals_layout)
        bottomAddDialog = createDialog(R.layout.free_add_layout)

        updateGoals()
        updateDaily()
        updateMeals()

        bnd.infoToggle.setOnClickListener {
            bnd.shading.visibility = View.VISIBLE
        }

        bnd.histroy.setOnClickListener {
            showHistoryDialog()
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
                    var wrongInput = false
                    val text = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if (text != null) {
                        var newText = text.toString().replace("[", "")
                        newText = newText.replace("]", "")
                        if (" gramm " in newText) {
                            amount = newText.split(" gramm ").toTypedArray()
                        } else if (" g " in newText) {
                            amount = newText.split(" g ").toTypedArray()
                        } else if (" gram " in newText) {
                            amount = newText.split(" gram ").toTypedArray()
                        } else {
                            wrongInput = true
                        }
                        if (!wrongInput) {
                            lifecycleScope.launch(Dispatchers.IO) {
                                val resultDocument = async {
                                    searchRequest(amount)
                                }
                                document = resultDocument.await()
                                var value = extractValues(document, "wDYxhc")
                                if (value == "") {
                                    value = extractValues(document, "MjjYud")
                                }
                                addFromSpeech(value, amount[0])
                            }
                        } else {
                            println("wrong input")
                        }

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
            val currentCaloriesMap = dataHandler.loadData(context, caloriesFile)
            val currentProteinMap = dataHandler.loadData(context, proteinFile)              
            if(currentCaloriesMap.containsKey(currentDate.toString())) {
                val currentCalories = currentCaloriesMap[currentDate.toString()].toString()
                changeProgressBar(goals[Keys.Calories.toString()]!!, currentCalories.toDouble(), true)
                val calCons = currentCalories.toDouble().toInt().toString() + " kcal"
                bnd.usedKcal.text = calCons
                updateRemaining(currentCalories.toDouble(), Value.Calories)
                if (currentCalories.toDouble() > goals[Keys.Calories.toString()]!!.toDouble()) {
                    rnd = (0..12).random()
                    val snack: Snackbar = Snackbar.make(bnd.cardView, messages[rnd], 4000)
                    val snackView = snack.view
                    val params = snackView.layoutParams as FrameLayout.LayoutParams
                    params.gravity = Gravity.TOP
                    params.setMargins(20,30, 20,0)
                    snackView.layoutParams = params
                    snack.show()
                }
            }
            println(currentProteinMap)
            if(currentProteinMap.containsKey(currentDate.toString())) {
                val currentProtein = currentProteinMap[currentDate.toString()].toString()
                changeProgressBar(goals[Keys.Protein.toString()]!!, currentProtein.toDouble(), false)
                val protCons = currentProtein.toDouble().toInt().toString() + " g"
                bnd.consumedProt.text = protCons
                updateRemaining(currentProtein.toDouble(), Value.Protein)
            }
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

    private fun createDialog(layout: Int): BottomSheetDialog {
        val bottomDialog = layoutInflater.inflate(layout, null)
        val bottomSheet = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        bottomSheet.setContentView(bottomDialog)

        return bottomSheet
    }

    private fun searchRequest(amount: Array<String>): Document {
        var result = Document("")
        val urlString =
            "https://www.google.com/search?q=" + amount[1].trim() + "+calories+per+100+g"
        val url = URL(urlString)
        try {
            val doc: Document = Jsoup.parse(url, 3 * 1000)
            result = doc
        } catch (e: IOException) {
            println("error")
        }

        return result
    }

    private fun extractValues(doc: Document, className: String): String {
        val element = doc.getElementsByClass(className)
        var value = ""
        if (element.isNotEmpty()) {
            for (i in element.indices) {
                var text = element[i].text()
                text = text.lowercase()
                if (text.contains("kcal")) {
                    val splitText = text.split("kcal")
                    var leftSide = splitText[0].trim()
                    leftSide = leftSide.takeLast(3)
                    leftSide = leftSide.trim()
                    var rightSide = splitText[1].trim()
                    rightSide = rightSide.take(3)
                    rightSide = rightSide.trim()
                    if (isNumeric(leftSide)) {
                        value = leftSide
                        break
                    } else if (isNumeric(rightSide)) {
                        value = rightSide
                        break
                    }
                } else if (text.contains("calories")) {
                    val splitText = text.split("calories")
                    var leftSide = splitText[0].trim()
                    leftSide = leftSide.takeLast(3)
                    leftSide = leftSide.trim()
                    var splitRightSide: List<String>
                    var rightSide = splitText[1].trim()
                    if (rightSide.contains(":")) {
                        rightSide = rightSide.replace(":", "")
                        rightSide = rightSide.trim()
                    }
                    splitRightSide = if (rightSide.take(3).contains(".")) {
                        rightSide.split(".")
                    } else if (rightSide.take(3).contains(",")) {
                        rightSide.split(",")
                    } else
                        listOf(rightSide.take(3))
                    if (isNumeric(leftSide)) {
                        value = leftSide
                        break
                    } else if (isNumeric(splitRightSide[0])) {
                        value = splitRightSide[0]
                        break
                    }
                }
            }
        }
        return value
    }

    override fun onResume() {
        super.onResume()
        updateGoals()
        updateDaily()
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
        val calsCons = getCurrentValue(caloriesFile).toInt()
        val stringCal = "$calsCons kcal"
        bnd.usedKcal.text = stringCal
        val protCons = getCurrentValue(proteinFile).toInt()
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

    private fun addFromSpeech(value: String, amount: String) {
        val historyMap = mutableMapOf<String, String>()
        val currentTime = HelperClass.getCurrentDateAndTime()
        if (isNumeric(amount.trim())) {
            var currentKcal = getCurrentValue(caloriesFile)
            var consumed = 0.0
            if (value != "" && amount != "") {
                if (value.toDouble() > 0.0 && amount.trim().toDouble() > 0.0) {
                    consumed = (value.toDouble() * (amount.trim().toDouble() / 100))
                    currentKcal += consumed
                }
            }
            dataHandler.saveData(requireContext(), caloriesFile, currentDate, currentKcal.toString())
            historyMap += mutableMapOf((currentTime + "_calo") to consumed.toString())
            changeProgressBar(goals[Keys.Calories.toString()]!!, currentKcal, true)
            val calCons = currentKcal.toInt().toString() + " kcal"
            bnd.usedKcal.text = calCons
            updateRemaining(currentKcal, Value.Calories)
            dataHandler.saveMapDataNO(requireContext(), historyFile, historyMap)
        }
    }

    private fun createMealUI (mealName: String, mealValue: String, mealProt: String, buttonID: Int, icon: String){
        val parentLayout = bnd.linearLayoutMeals
        relativeLayout = RelativeLayout(requireContext())
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
            addMeal(mealValue, mealProt)
        }

        mealsName.setOnClickListener{
            mealsDialog.show(mealsName.id, bottomMealDialog)
        }

        mealsName.setOnLongClickListener {
            Snackbar.make(bnd.home, "Delete Entry?", 4000)
            .setAction("DELETE") {
                    deleteMeal(mealsName.id)
                }
            .show()
            true
        }
        parentLayout.addView(relativeLayout)
    }

    private fun addMeal(mealKcal: String, mealProt: String) {
        val historyMap = mutableMapOf<String, String>()
        val currentTime = HelperClass.getCurrentDateAndTime()
        if (mealKcal != "value" && mealKcal != "") {
            val currentKcalValue = getCurrentValue(caloriesFile) + mealKcal.toDouble()
            dataHandler.saveData(requireContext(), caloriesFile, currentDate, currentKcalValue.toString())
            historyMap += mutableMapOf((currentTime + "_calo") to mealKcal)
            changeProgressBar(goals[Keys.Calories.toString()]!!, currentKcalValue, true)
            val calCons = "${currentKcalValue.toInt()} kcal"
            bnd.usedKcal.text = calCons
            updateRemaining(currentKcalValue, Value.Calories)
            if (currentKcalValue > goals[Keys.Calories.toString()]!!.toDouble()) {
                rnd = (0..12).random()
                val snack: Snackbar = Snackbar.make(bnd.cardView, messages[rnd], 4000)
                val view = snack.view
                val params = view.layoutParams as FrameLayout.LayoutParams
                params.setMargins(20,30, 20,0)
                params.gravity = Gravity.TOP
                view.layoutParams = params
                snack.show()
            }
        }
        if (mealProt != "value" && mealProt != "") {
            val currentProteinValue = getCurrentValue(proteinFile) + mealProt.toDouble()
            dataHandler.saveData(requireContext(), proteinFile, currentDate, currentProteinValue.toString())
            historyMap += mutableMapOf((currentTime + "_prot") to mealProt)
            changeProgressBar(goals[Keys.Protein.toString()]!!, currentProteinValue, false)
            val protCons = "${currentProteinValue.toInt()} g"
            bnd.consumedProt.text = protCons
            updateRemaining(currentProteinValue, Value.Protein)
        }
        dataHandler.saveMapDataNO(requireContext(), historyFile, historyMap)
    }

    private fun deleteMeal(mealNumber: Int){
        var keyName = ""
        var keyCal = ""
        var keyProt = ""
        var keyIcons = ""
        if(mealNumber != 0){
            keyName = "Meal" + mealNumber.toString() +"Name"
            keyCal = "Meal" + mealNumber.toString() +"Cal"
            keyProt = "Meal" + mealNumber.toString() +"Prot"
            keyIcons = "Meal" + mealNumber.toString() +"Icon"
        }
        dataHandler.deleteMapEntriesWithKeys(requireContext(), mealsFile, keyName)
        dataHandler.deleteMapEntriesWithKeys(requireContext(), mealsFile, keyCal)
        dataHandler.deleteMapEntriesWithKeys(requireContext(), mealsFile, keyProt)
        dataHandler.deleteMapEntriesWithKeys(requireContext(), iconFile, keyIcons)
        val currentMeals = dataHandler.loadData(requireContext(), mealsFile)
        val currentIcons = dataHandler.loadData(requireContext(), iconFile)
        val mealsCount = (currentMeals.count()/3)
        val iconCount = currentIcons.count()
        val newMeals = mutableMapOf<String, String>()
        val newIcons = mutableMapOf<String, String>()
        var i = 1
        for(items in currentMeals) {
            val name = "Meal" + i.toString() + "Name"
            val value = "Meal" + i.toString() +"Cal"
            val protValue = "Meal" + i.toString() +"Prot"
            if (!items.key.contains(name) && !items.key.contains(value) && !items.key.contains(protValue)) {
                newMeals += mutableMapOf(name to currentMeals["Meal" + (i+1).toString() + "Name"].toString())
                newMeals += mutableMapOf(value to currentMeals["Meal" + (i+1).toString() + "Cal"].toString())
                newMeals += mutableMapOf(protValue to currentMeals["Meal" + (i+1).toString() + "Prot"].toString())
            }
            else if (items.key.contains(name)){
                newMeals += mutableMapOf(name to currentMeals[name].toString())
                newMeals += mutableMapOf(value to currentMeals[value].toString())
                newMeals += mutableMapOf(protValue to currentMeals[protValue].toString())
            }
            if(i >= mealsCount)
                break
            i++
        }
        var x = 1
        dataHandler.saveMapData(requireContext(), mealsFile, newMeals)
        for(items in currentIcons) {
            val keyIcon = "Meal" + x.toString() +"Icon"
            if (!items.key.contains(keyIcon)) {
                newIcons += mutableMapOf(keyIcon to currentIcons["Meal" + (x+1).toString() + "Icon"].toString())
            }
            else if (items.key.contains(keyIcon)){
                newIcons += mutableMapOf(keyIcon to currentIcons[keyIcon].toString())
            }
            if(x >= iconCount)
                break
            x++
        }
        dataHandler.saveMapData(requireContext(), iconFile, newIcons)
        updateMeals()
    }

    @SuppressLint("InflateParams")
    private fun showHistoryDialog() {
        val bottomHistoryDialog = layoutInflater.inflate(R.layout.history_layout, null)
        historyDialog = BottomSheetDialog(requireActivity(), R.style.BottomSheetDialogTheme)
        historyDialog.setContentView(bottomHistoryDialog)
        layoutHistoryCards = historyDialog.findViewById(R.id.layoutHistoryCards)!!
        historyScrollView = historyDialog.findViewById(R.id.historyScrollView)!!

        val historyValues = dataHandler.loadData(requireContext(), historyFile)
        if(historyValues.isNotEmpty()){
            for (item in historyValues) {
                createCards(layoutHistoryCards, item.key, item.value, historyScrollView)
            }
        }
        historyDialog.show()
    }

    private fun createCards(parent: LinearLayout, time: String, value: String, scrollView: ScrollView) {
        val inflater = layoutInflater
        val card: View = inflater.inflate(R.layout.card_layout2, parent, false)
        val descriptionText:TextView = card.findViewById(R.id.description)
        val historyValue:TextView = card.findViewById(R.id.value)
        var startX = 0f
        card.id = View.generateViewId()

        var newTime = ""
        var calOrProt = true
        if(time.contains("_calo")){
            newTime = time.takeLast(13).replace("_calo", "")
            calOrProt = true
        } else if (time.contains("_prot")){
            newTime = time.takeLast(13).replace("_prot", "")
            calOrProt = false
        }

        if(calOrProt){
            historyValue.id = View.generateViewId()
            historyValue.text = String.format(Locale.getDefault(),"%.1f", value.toDouble())
            descriptionText.text = getString(R.string.Calories)
        }
        else{
            historyValue.id = View.generateViewId()
            historyValue.text = String.format(Locale.getDefault(),"%.1f", value.toDouble())
            descriptionText.text = getString(R.string.Protein)
        }

        dateView = card.findViewById(R.id.date)
        dateView.id = View.generateViewId()
        dateView.text = newTime

        val transition = ChangeBounds()
        transition.setDuration(200)

        card.setOnTouchListener(
            View.OnTouchListener { view, event ->
                val displayMetrics = resources.displayMetrics
                val maxWidth = displayMetrics.widthPixels.toFloat()
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = event.rawX
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val newX = event.rawX
                        // carry out swipe if motion is bigger than 25 dp and to the right
                        if (newX - startX > 25) {
                            scrollView.requestDisallowInterceptTouchEvent(true)
                            card.animate()
                                .x(abs(newX) - abs(startX))
                                .setDuration(0)
                                .start()
                        }
                        if (maxWidth - newX < 25) {
                            TransitionManager.beginDelayedTransition(parent, transition)
                            parent.removeView(card)
                            removeHistoryItem(descriptionText.text.toString(), value)
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        scrollView.requestDisallowInterceptTouchEvent(false)
                        if (card.x > MIN_SWIPE_DISTANCE) {
                            TransitionManager.beginDelayedTransition(parent, transition)
                            parent.removeView(card)
                            removeHistoryItem(descriptionText.text.toString(), value)
                        }
                        else {
                            card.translationX = 0f
                        }
                    }
                }
                // required to by-pass lint warning
                view.performClick()
                return@OnTouchListener true
            }
        )
        parent.addView(card)
    }

    private fun removeHistoryItem(valueType: String, value: String){

        if(valueType == "Calories"){
            var currentKcal = getCurrentValue(caloriesFile)
            if (value != "") {
                if (value.toDouble() > 0.0) {
                    currentKcal -= value.toDouble()
                }
            }
            dataHandler.saveData(requireContext(), caloriesFile, currentDate, currentKcal.toString())
            changeProgressBar(goals[Keys.Calories.toString()]!!, currentKcal, true)
            val calCons = currentKcal.toInt().toString() + " kcal"
            bnd.usedKcal.text = calCons
            updateRemaining(currentKcal, Value.Calories)
            dataHandler.deleteEntriesWithValue(requireContext(), historyFile, value)
        }
        else{
            var currentProt = getCurrentValue(proteinFile)
            if (value != "") {
                if (value.toDouble() > 0.0) {
                    currentProt -= value.toDouble()
                }
            }
            dataHandler.saveData(requireContext(), proteinFile, currentDate, currentProt.toString())
            changeProgressBar(goals[Keys.Protein.toString()]!!, currentProt, false)
            val protCons = currentProt.toInt().toString() + " g"
            bnd.consumedProt.text = protCons
            updateRemaining(currentProt, Value.Protein)
            dataHandler.deleteEntriesWithValue(requireContext(), historyFile, value)
        }
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

    private fun getCurrentValue(fileName: String): Double {
        val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val out = dataHandler.loadData(context, fileName)
        val currentValue = if (out.containsKey(currentDate)) {
            out[currentDate]?.toDouble()!!
        } else {
            0.0
        }
        return currentValue
    }
}