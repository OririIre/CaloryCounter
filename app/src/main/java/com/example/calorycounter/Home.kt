package com.example.calorycounter

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.example.calorycounter.databinding.FragmentHomeBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
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


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

//val dataHandler = DataHandler()

internal enum class Value {
    Protein, Calories
}
const val MIN_SWIPE_DISTANCE = 250

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
    private var alcoholToggle = false
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
    private lateinit var badMessages: Toast
    private val caloriesFile = "calLog.txt"
    private val proteinFile = "protLog.txt"
    private val languageFile = "language.txt"
    private val mealsFile = "meals.txt"
    private val goalsFile = "goals.txt"
    private val historyFile = "history.txt"
    private val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())



    @SuppressLint("DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val historyValues = dataHandler.loadData(requireContext(), historyFile)
        val calendar  = Calendar.getInstance()
        val currentTime = String.format("%02d.%02d",
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

        bnd.progressBar.max = 100
        bnd.ProteinProgressBar.max = 100
        bnd.progressBar2.max = 100
        bnd.progressBar3.max = 100

        updateGoals()
        updateDaily()
        val meals = dataHandler.loadData(requireContext(), mealsFile)

        bnd.buttonMealOne.setOnClickListener {
            if (meals.isNotEmpty()) {
                addMeal(meals["Meal1Cal"]!!, meals["Meal1Prot"]!!)
            }
        }
        bnd.buttonMealTwo.setOnClickListener {
            if (meals.isNotEmpty()) {
                addMeal(meals["Meal2Cal"]!!, meals["Meal2Prot"]!!)
            }
        }
        bnd.buttonMealThree.setOnClickListener {
            if (meals.isNotEmpty()) {
                addMeal(meals["Meal3Cal"]!!, meals["Meal3Prot"]!!)
            }
        }
        bnd.buttonMealFour.setOnClickListener {
            if (meals.isNotEmpty()) {
                addMeal(meals["Meal4Cal"]!!, meals["Meal4Prot"]!!)
            }
        }
        bnd.buttonMealFive.setOnClickListener {
            if (meals.isNotEmpty()) {
                addMeal(meals["Meal5Cal"]!!, meals["Meal5Prot"]!!)
            }
        }

        bnd.infoToggle.setOnClickListener {
            bnd.shading.visibility = View.VISIBLE
        }

        bnd.buttonAddAmount.setOnClickListener {
            showBottomDialog()
        }

        bnd.histroy.setOnClickListener {
            showHistoryDialog()
        }

//        bnd.alcoholMode.setOnClickListener {
//            if (!alcoholToggle) {
//                alcoholToggle = true
//                bnd.alcoholMode.background.setTint(Color.parseColor("#7C0D34"))
//            } else {
//                alcoholToggle = false
//                bnd.alcoholMode.background.setTint(Color.parseColor("#280861"))
//            }
//        }

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
        return view
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
                    if (rightSide.take(3).contains(".")) {
                        splitRightSide = rightSide.split(".")
                    } else if (rightSide.take(3).contains(",")) {
                        splitRightSide = rightSide.split(",")
                    } else
                        splitRightSide = listOf(rightSide.take(3))
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

    @SuppressLint("DefaultLocale")
    private fun addFromSpeech(value: String, amount: String) {
        val historyMap = mutableMapOf<String, String>()
        val calendar  = Calendar.getInstance()
        val currentTime = String.format("%02d.%02d%02d:%02d:%02d",
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.MONTH)+1,
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            calendar.get(Calendar.SECOND))
        if (isNumeric(amount.trim())) {
            var currentKcal = getCurrentValue(true)
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
            val calCons = getCurrentValue(true).toInt().toString() + " kcal"
            bnd.usedKcal.text = calCons
            updateRemaining(currentKcal, Value.Calories)
            dataHandler.saveMapDataNO(requireContext(), historyFile, historyMap)
        }
    }

    override fun onResume() {
        super.onResume()
        updateGoals()
        updateMeals()
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
        val calsCons = getCurrentValue(true).toInt()
        val stringCal = "$calsCons kcal"
        bnd.usedKcal.text = stringCal
        val protCons = getCurrentValue(false).toInt()
        val stringProt = "$protCons g"
        bnd.consumedProt.text = stringProt
        updateRemaining(calsCons.toDouble(), Value.Calories)
        updateRemaining(protCons.toDouble(), Value.Protein)
        changeProgressBar(goals[Keys.Protein.toString()]!!, protCons.toDouble(), false)
        changeProgressBar(goals[Keys.Calories.toString()]!!, calsCons.toDouble(), true)
    }

    private fun updateMeals() {
        val meals = dataHandler.loadData(requireContext(), mealsFile)
        if (meals.isNotEmpty()) {
            bnd.valueMeal1.text = meals["Meal1Cal"]
            bnd.valueMeal2.text = meals["Meal2Cal"]
            bnd.valueMeal3.text = meals["Meal3Cal"]
            bnd.valueMeal4.text = meals["Meal4Cal"]
            bnd.valueMeal5.text = meals["Meal5Cal"]
            bnd.mealname1.text = meals["Meal1Name"].toString().take(4)
            bnd.mealname2.text = meals["Meal2Name"].toString().take(4)
            bnd.mealname3.text = meals["Meal3Name"].toString().take(4)
            bnd.mealname4.text = meals["Meal4Name"].toString().take(4)
            bnd.mealname5.text = meals["Meal5Name"].toString().take(4)
        }
    }

    private fun updateRemaining(currentValue: Double, goal: Value) {
        if (goal == Value.Calories) {
            if (goals[Keys.Calories.toString()]!!.toDouble() != 0.0) {
                var remaining = goals[Keys.Calories.toString()]!!.toDouble() - currentValue
                if (remaining <= 0) {
                    remaining = 0.0
                }
                val left = "$remaining kcal"
                bnd.leftKcal.text = left
            } else if (goals[Keys.Calories.toString()]!!.toDouble() == 0.0) {
                bnd.leftKcal.text = 0.0.toString()
            }
        } else {
            if (goals[Keys.Protein.toString()]!!.toDouble() != 0.0) {
                var remaining = goals[Keys.Protein.toString()]!!.toDouble() - currentValue
                if (remaining <= 0) {
                    remaining = 0.0
                }
                val left = "$remaining g"
                bnd.leftProt.text = left
            } else if (goals[Keys.Protein.toString()]!!.toDouble() == 0.0) {
                bnd.leftProt.text = 0.0.toString()
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun addMeal(mealKcal: String, mealProt: String) {
        val historyMap = mutableMapOf<String, String>()
        val calendar  = Calendar.getInstance()
        val currentTime = String.format("%02d.%02d%02d:%02d:%02d",
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.MONTH+1),
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            calendar.get(Calendar.SECOND))
        if (mealKcal != "value" && mealKcal != "") {
            val currentKcalValue = getCurrentValue(true) + mealKcal.toDouble()
            dataHandler.saveData(requireContext(), caloriesFile, currentDate, currentKcalValue.toString())
            historyMap += mutableMapOf((currentTime + "_calo") to mealKcal)
            changeProgressBar(goals[Keys.Calories.toString()]!!, currentKcalValue, true)
            val calCons = "$currentKcalValue kcal"
            bnd.usedKcal.text = calCons
            updateRemaining(currentKcalValue, Value.Calories)
            if (currentKcalValue > goals[Keys.Calories.toString()]!!.toDouble()) {
                rnd = (0..12).random()
                badMessages = Toast.makeText(activity, messages[rnd], Toast.LENGTH_LONG)
                badMessages.show()
            }
        }
        if (mealProt != "value" && mealProt != "") {
            val currentProteinValue = getCurrentValue(false) + mealProt.toDouble()
            dataHandler.saveData(requireContext(), proteinFile, currentDate, currentProteinValue.toString())
            historyMap += mutableMapOf((currentTime + "_prot") to mealProt)
            changeProgressBar(goals[Keys.Protein.toString()]!!, currentProteinValue, false)
            val protCons = "$currentProteinValue g"
            bnd.consumedProt.text = protCons
            updateRemaining(currentProteinValue, Value.Protein)
        }
        dataHandler.saveMapDataNO(requireContext(), historyFile, historyMap)
    }

    private fun changeProgressBar(setAmount: String, consumed: Double, cal: Boolean) {
        if (setAmount != "") {
            val goal = setAmount.toInt()
            val progress = round(consumed / (goal / 100))
            val remaining = round(100 - (consumed / (goal / 100)))
            if (cal) {
                bnd.progressBar.progress = remaining.toInt()
                bnd.progressBar2.progress = progress.toInt()
            } else {
                bnd.ProteinProgressBar.progress = remaining.toInt()
                bnd.progressBar3.progress = progress.toInt()
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun showHistoryDialog() {
        val bottomHistoryDialog = layoutInflater.inflate(R.layout.history_layout, null)
        historyDialog = BottomSheetDialog(requireActivity(), R.style.BottomSheetDialogTheme)
        historyDialog.setContentView(bottomHistoryDialog)
        historyDialog.show()
        layoutHistoryCards = historyDialog.findViewById(R.id.layoutHistoryCards)!!
        historyScrollView = historyDialog.findViewById(R.id.historyScrollView)!!

        val historyValues = dataHandler.loadData(requireContext(), historyFile)
        if(historyValues.isNotEmpty()){
            for (item in historyValues) {
                createCards(layoutHistoryCards, item.key, item.value, historyScrollView)
            }
        }
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
            historyValue.text = value
            descriptionText.text = getString(R.string.Calories)
        }
        else{
            historyValue.id = View.generateViewId()
            historyValue.text = value
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
                            removeHistoryItem(descriptionText.text.toString(), historyValue.text.toString())
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        scrollView.requestDisallowInterceptTouchEvent(false)
                        if (card.x > MIN_SWIPE_DISTANCE) {
                            TransitionManager.beginDelayedTransition(parent, transition)
                            parent.removeView(card)
                            removeHistoryItem(descriptionText.text.toString(), historyValue.text.toString())
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
            var currentKcal = getCurrentValue(true)
            if (value != "") {
                if (value.toDouble() > 0.0) {
                    currentKcal -= value.toDouble()
                }
            }
            dataHandler.saveData(requireContext(), caloriesFile, currentDate, currentKcal.toString())
            changeProgressBar(goals[Keys.Calories.toString()]!!, currentKcal, true)
            val calCons = getCurrentValue(true).toInt().toString() + " kcal"
            bnd.usedKcal.text = calCons
            updateRemaining(currentKcal, Value.Calories)
            dataHandler.deleteEntriesWithValue(requireContext(), historyFile, value)
        }
        else{
            var currentProt = getCurrentValue(false)
            if (value != "") {
                if (value.toDouble() > 0.0) {
                    currentProt -= value.toDouble()
                }
            }
            dataHandler.saveData(requireContext(), proteinFile, currentDate, currentProt.toString())
            changeProgressBar(goals[Keys.Protein.toString()]!!, currentProt, false)
            val protCons = getCurrentValue(false).toInt().toString() + " g"
            bnd.consumedProt.text = protCons
            updateRemaining(currentProt, Value.Protein)
            dataHandler.deleteEntriesWithValue(requireContext(), historyFile, value)
        }
    }

    @SuppressLint("InflateParams")
    private fun showBottomDialog() {
        val bottomDialog = layoutInflater.inflate(R.layout.bottomsheetlayout, null)
        var calProtSwitch = true
        var toggleSettings = true
//        val transition = ChangeBounds()
//        transition.setDuration(200)
        dialog = BottomSheetDialog(requireActivity(), R.style.BottomSheetDialogTheme)
        dialog.setContentView(bottomDialog)
        dialog.show()

        add2 = dialog.findViewById(R.id.button_add2)!!
        kcal = dialog.findViewById(R.id.kcal)!!
        gramm = dialog.findViewById(R.id.gramm)!!
        custom = dialog.findViewById(R.id.enter_calorie_amount)!!
        caloriesSwitch = dialog.findViewById(R.id.caloriesSwitcher)!!
        proteinSwitch = dialog.findViewById(R.id.proteinSwitcher)!!

        additionalInfoButton = dialog.findViewById<TextView>(R.id.button_additional_settings)!!
        additionalSettings = dialog.findViewById(R.id.layoutAdditionalSettings)!!
//        mainLayout = dialog.findViewById(R.id.mainLayout)!!
//        mainLayout.layoutTransition.enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING)

//        val transition = LayoutTransition()
//        transition.setAnimateParentHierarchy(false)
//        transition.setDuration(100)
//        mainLayout.layoutTransition = transition

        additionalInfoButton.setOnClickListener {
            if (toggleSettings) {
//                TransitionManager.beginDelayedTransition(mainLayout)
                additionalSettings.visibility = View.VISIBLE
                toggleSettings = false
            } else {

                additionalSettings.visibility = View.GONE
                toggleSettings = true
            }
        }

        custom.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == 4) {
                addSub(calProtSwitch)
                true
            } else {
                false
            }
        }

        gramm.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == 4) {
                addSub(calProtSwitch)
                true
            } else {
                false
            }
        }

         caloriesSwitch.setOnClickListener {
            calProtSwitch = true
            gramm.text.clear()
            kcal.text.clear()
            custom.text.clear()
            caloriesSwitch.background = ResourcesCompat.getDrawable(resources, R.drawable.custom_textview_border, null)
            proteinSwitch.background = null
         }

        proteinSwitch.setOnClickListener {
            calProtSwitch = false
            gramm.text.clear()
            kcal.text.clear()
            custom.text.clear()
            proteinSwitch.background = ResourcesCompat.getDrawable(resources, R.drawable.custom_textview_border, null)
            caloriesSwitch.background = null
        }

        add2.setOnClickListener {
            addSub(calProtSwitch)
        }
    }

    @SuppressLint("DefaultLocale")
    private fun addSub(calProtSwitch: Boolean) {
        val historyMap = mutableMapOf<String, String>()
        val calendar  = Calendar.getInstance()
        val currentTime = String.format("%02d.%02d%02d:%02d:%02d",
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.MONTH)+1,
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            calendar.get(Calendar.SECOND))
        val caloriesString = kcal.text.toString()
        val grammString = gramm.text.toString()
        if (calProtSwitch) {
            val currentKcalValue = calcValue(true, kcal.text.toString(), gramm.text.toString(), custom.text.toString())
            dataHandler.saveData(requireContext(), caloriesFile, currentDate, currentKcalValue.toString())
            if (custom.text.toString() != "") {
                historyMap += mutableMapOf((currentTime + "_calo") to custom.text.toString())
            }
            else if (kcal.text.toString() != "" && gramm.text.toString() != "") {
                val caloriesDouble = caloriesString.toDouble()
                val grammDouble = grammString.toDouble()
                historyMap += mutableMapOf((currentTime + "_calo") to (caloriesDouble * (grammDouble / 100)).toString())
            }
            changeProgressBar(goals[Keys.Calories.toString()]!!, currentKcalValue, true)
            val calCons = getCurrentValue(true).toInt().toString() + " kcal"
            bnd.usedKcal.text = calCons
            updateRemaining(currentKcalValue, Value.Calories)
            if (currentKcalValue > goals[Keys.Calories.toString()]!!.toDouble()) {
                rnd = (0..12).random()
                badMessages = Toast.makeText(activity, messages[rnd], Toast.LENGTH_LONG)
                badMessages.show()
            }
        } else {
            val currentProteinValue = calcValue(false, kcal.text.toString(), gramm.text.toString(), custom.text.toString())
            dataHandler.saveData(requireContext(), proteinFile, currentDate, currentProteinValue.toString())
            if (custom.text.toString() != "") {
                historyMap += mutableMapOf((currentTime + "_prot") to custom.text.toString())
            }
            else if (kcal.text.toString() != "" && gramm.text.toString() != "") {
                val caloriesDouble = caloriesString.toDouble()
                val grammDouble = grammString.toDouble()
                historyMap += mutableMapOf((currentTime + "_prot") to (caloriesDouble * (grammDouble / 100)).toString())
            }
            changeProgressBar(goals[Keys.Protein.toString()]!!, currentProteinValue, false)
            val protCons = getCurrentValue(false).toInt().toString() + " g"
            bnd.consumedProt.text = protCons
            updateRemaining(currentProteinValue, Value.Protein)
        }
        dataHandler.saveMapDataNO(requireContext(), historyFile, historyMap)
        kcal.text.clear()
        gramm.text.clear()
        custom.text.clear()
    }

    private fun calcValue(cal: Boolean, value: String, gramm: String, custom: String): Double {
        val toast = Toast.makeText(activity, "Alcohol mode is on! Nothing is added!", Toast.LENGTH_LONG)
        var currentKcal = getCurrentValue(cal)
        if (!alcoholToggle) {
            if (custom != "") {
               currentKcal += custom.toDouble()
            }
            else if (value != "" && gramm != "") {
                if (value.toDouble() > 0.0 && gramm.toDouble() > 0.0) {
                    currentKcal += (value.toDouble() * (gramm.toDouble() / 100))
                }
            }
        } else {
            toast.show()
        }
        return currentKcal
    }

    private fun getCurrentValue(cal: Boolean): Double {
        val filePath = if (cal) {
            "calLog.txt"
        } else {
            "protLog.txt"
        }
        val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val out = dataHandler.loadData(requireContext(), filePath)
        val currentValue = if (out.containsKey(currentDate)) {
            out[currentDate]?.toDouble()!!
        } else {
            0.0
        }
        return currentValue
    }
}