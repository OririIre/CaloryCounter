package com.example.calorycounter

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Icon
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
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
    private lateinit var mealsDialog: BottomSheetDialog
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
    private var isAllFabVisible: Boolean = false
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

        bnd.progressBar.max = 1000
        bnd.ProteinProgressBar.max = 1000
        bnd.progressBar2.max = 1000
        bnd.progressBar3.max = 1000

        updateGoals()
        updateDaily()

        bnd.infoToggle.setOnClickListener {
            bnd.shading.visibility = View.VISIBLE
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
            showBottomDialog()
        }

        bnd.fbMeals.setOnClickListener {
            imageView.visibility = View.GONE
            bnd.fbCustom.visibility = View.GONE
            bnd.fbMeals.visibility = View.GONE
            bnd.addFreeText.visibility = View.GONE
            bnd.addMealText.visibility = View.GONE
            isAllFabVisible = false
            showMealsDialog(0)
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

    @SuppressLint("DefaultLocale")
    private fun addFromSpeech(value: String, amount: String) {
        val historyMap = mutableMapOf<String, String>()
        val currentTime = getCurrentDateTime()
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
        println(meals)
        bnd.linearLayoutMeals.removeAllViews()
        var i = 1
        for(items in meals){
            val name = i.toString() + "Name"
            val value = i.toString() + "Cal"
            val protValue = i.toString() + "Prot"
            if(items.key.contains(name) && items.value != "value"){
                val currentMealName = items.value
                var currentMealValue = ""
                var currentMealProt = ""
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
                    if(calFound && protFound){
                        createMealUI (currentMealName, currentMealValue, currentMealProt, i)
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


    private fun createMealUI (mealName: String, mealValue: String, mealProt: String, buttonID: Int){
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
        mealsValue.setCompoundDrawablesWithIntrinsicBounds(null,null,ResourcesCompat.getDrawable(resources, R.drawable.baseline_add_circle_outline_24, null),null)
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
        mealsName.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(resources, R.drawable.baseline_ramen_dining_24, null),null,null,null)
        mealsName.compoundDrawablePadding = 15
        mealsName.layoutParams = mealsNameParam

        val dividerParam: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            1
        )
        dividerParam.addRule(RelativeLayout.BELOW, mealsName.id)
        divider.layoutParams = dividerParam
        divider.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.white_low_transparency, null))
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
            showMealsDialog(mealsName.id)
        }

        mealsName.setOnLongClickListener {
            val deleteButton = TextView(requireContext())
            val buttonLayoutParam: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            buttonLayoutParam.marginStart = 20
            buttonLayoutParam.addRule(RelativeLayout.END_OF, mealsName.id)
            deleteButton.layoutParams = buttonLayoutParam
            deleteButton.setPadding(20,5,20,5)
            deleteButton.text = "Delete!"
            deleteButton.background = ResourcesCompat.getDrawable(resources, R.drawable.button_states_3, null)
            relativeLayout.addView(deleteButton)
            deleteButton.setOnClickListener{
                deleteMeal(mealsName.id)
                relativeLayout.removeView(deleteButton)
            }
            true
        }
        parentLayout.addView(relativeLayout)
    }

    @SuppressLint("DefaultLocale")
    private fun addMeal(mealKcal: String, mealProt: String) {
        val historyMap = mutableMapOf<String, String>()
        val currentTime = getCurrentDateTime()
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
                badMessages = Toast.makeText(activity, messages[rnd], Toast.LENGTH_LONG)
                badMessages.show()
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
        if(mealNumber != 0){
            keyName = "Meal" + mealNumber.toString() +"Name"
            keyCal = "Meal" + mealNumber.toString() +"Cal"
            keyProt = "Meal" + mealNumber.toString() +"Prot"
        }
        dataHandler.deleteMapEntriesWithKeys(requireContext(), mealsFile, keyName)
        dataHandler.deleteMapEntriesWithKeys(requireContext(), mealsFile, keyCal)
        dataHandler.deleteMapEntriesWithKeys(requireContext(), mealsFile, keyProt)
        val currentMeals = dataHandler.loadData(requireContext(), mealsFile)
        val mealsCount = ((currentMeals.count()/3))
        val newMeals = mutableMapOf<String, String>()
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
        dataHandler.saveMapData(requireContext(), mealsFile, newMeals)
        updateMeals()
    }

    @SuppressLint("InflateParams")
    private fun showMealsDialog(mealNumber: Int) {
        val bottomMealsDialog = layoutInflater.inflate(R.layout.meals_layout, null)
        mealsDialog = BottomSheetDialog(requireActivity(), R.style.BottomSheetDialogTheme)
        mealsDialog.setContentView(bottomMealsDialog)
        mealsDialog.show()

        val save: Button = mealsDialog.findViewById(R.id.buttonSaveMeal)!!
        val caloriesField: EditText = mealsDialog.findViewById(R.id.enterMealCalories)!!
        val proteinField: EditText = mealsDialog.findViewById(R.id.enterMealProtein)!!
        val nameField: EditText = mealsDialog.findViewById(R.id.enterMealName)!!
        val iconDropdown: Spinner = mealsDialog.findViewById(R.id.iconSelection)!!

        val items = arrayOf(R.drawable.baseline_ramen_dining_24, R.drawable.baseline_home_24)
        val adapter: ArrayAdapter<Int> =
            ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_dropdown_item, items)
        iconDropdown.adapter = adapter

        var currentMeals = dataHandler.loadData(requireContext(), mealsFile)

        if(mealNumber != 0){
            val keyName = "Meal" + mealNumber.toString() +"Name"
            val mealName = currentMeals[keyName]
            nameField.setText(mealName)
            val keyCal = "Meal" + mealNumber.toString() +"Cal"
            val mealCalories = currentMeals[keyCal]
            caloriesField.setText(mealCalories)
            val keyProt = "Meal" + mealNumber.toString() +"Prot"
            val mealProtein = currentMeals[keyProt]
            proteinField.setText(mealProtein)
        }

        var selectedIcon: Int

        iconDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                selectedIcon = selectedItem.toInt()
                println(selectedIcon)
            }
        }

        save.setOnClickListener{
            currentMeals = dataHandler.loadData(requireContext(), mealsFile)
            val mealsCount = ((currentMeals.count()/3) + 1)
            if(mealNumber == 0) {
                if (nameField.text.toString() != "") {
                    val keyName = "Meal" + mealsCount.toString() + "Name"
                    val mealNameMap = mutableMapOf(keyName to nameField.text.toString())
                    dataHandler.saveMapDataNO(requireContext(), mealsFile, mealNameMap)
                    val keyCal = "Meal" + mealsCount.toString() + "Cal"
                    if (caloriesField.text.toString() != "") {
                        val mealCaloriesMap = mutableMapOf(keyCal to caloriesField.text.toString())
                        dataHandler.saveMapDataNO(requireContext(), mealsFile, mealCaloriesMap)
                    } else {
                        val mealCaloriesMap = mutableMapOf(keyCal to "0")
                        dataHandler.saveMapDataNO(requireContext(), mealsFile, mealCaloriesMap)
                    }
                    val keyProt = "Meal" + mealsCount.toString() + "Prot"
                    if (proteinField.text.toString() != "") {
                        val mealProteinMap = mutableMapOf(keyProt to proteinField.text.toString())
                        dataHandler.saveMapDataNO(requireContext(), mealsFile, mealProteinMap)
                    } else {
                        val mealProteinMap = mutableMapOf(keyProt to "0")
                        dataHandler.saveMapDataNO(requireContext(), mealsFile, mealProteinMap)
                    }
                }
            }
            else{
                if (nameField.text.toString() != "") {
                    val keyName = "Meal" + mealNumber.toString() + "Name"
                    val mealNameMap = mutableMapOf(keyName to nameField.text.toString())
                    dataHandler.saveMapDataNO(requireContext(), mealsFile, mealNameMap)
                    val keyCal = "Meal" + mealNumber.toString() + "Cal"
                    if (caloriesField.text.toString() != "") {
                        val mealCaloriesMap = mutableMapOf(keyCal to caloriesField.text.toString())
                        dataHandler.saveMapDataNO(requireContext(), mealsFile, mealCaloriesMap)
                    } else {
                        val mealCaloriesMap = mutableMapOf(keyCal to "0")
                        dataHandler.saveMapDataNO(requireContext(), mealsFile, mealCaloriesMap)
                    }
                    val keyProt = "Meal" + mealNumber.toString() + "Prot"
                    if (proteinField.text.toString() != "") {
                        val mealProteinMap = mutableMapOf(keyProt to proteinField.text.toString())
                        dataHandler.saveMapDataNO(requireContext(), mealsFile, mealProteinMap)
                    } else {
                        val mealProteinMap = mutableMapOf(keyProt to "0")
                        dataHandler.saveMapDataNO(requireContext(), mealsFile, mealProteinMap)
                    }
                }
                updateMeals()
                nameField.text.clear()
                caloriesField.text.clear()
                proteinField.text.clear()
                mealsDialog.dismiss()
            }
            updateMeals()
            nameField.text.clear()
            caloriesField.text.clear()
            proteinField.text.clear()
        }
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
            historyValue.text = value.toInt().toString()
            descriptionText.text = getString(R.string.Calories)
        }
        else{
            historyValue.id = View.generateViewId()
            historyValue.text = value.toInt().toString()
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


    @SuppressLint("InflateParams")
    private fun showBottomDialog() {
        val bottomDialog = layoutInflater.inflate(R.layout.bottomsheetlayout, null)
        var calProtSwitch = true
        var toggleSettings = true

        dialog = BottomSheetDialog(requireActivity(), R.style.BottomSheetDialogTheme)
        dialog.setContentView(bottomDialog)
        dialog.show()

        add2 = dialog.findViewById(R.id.button_add2)!!
        kcal = dialog.findViewById(R.id.kcal)!!
        gramm = dialog.findViewById(R.id.gramm)!!
        custom = dialog.findViewById(R.id.enter_calorie_amount)!!
        caloriesSwitch = dialog.findViewById(R.id.caloriesSwitcher)!!
        proteinSwitch = dialog.findViewById(R.id.proteinSwitcher)!!

        additionalInfoButton = dialog.findViewById(R.id.button_additional_settings)!!
        additionalSettings = dialog.findViewById(R.id.layoutAdditionalSettings)!!

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
             clearFields ()
            caloriesSwitch.background = ResourcesCompat.getDrawable(resources, R.drawable.custom_textview_border, null)
            proteinSwitch.background = null
         }

        proteinSwitch.setOnClickListener {
            calProtSwitch = false
            clearFields ()
            proteinSwitch.background = ResourcesCompat.getDrawable(resources, R.drawable.custom_textview_border, null)
            caloriesSwitch.background = null
        }

        add2.setOnClickListener {
            addSub(calProtSwitch)
        }
    }

    private fun addSub(calProtSwitch: Boolean) {
        val historyMap = mutableMapOf<String, String>()
        val currentTime = getCurrentDateTime()
        val valueString = kcal.text.toString()
        val grammString = gramm.text.toString()
        val customString = custom.text.toString()
        if (calProtSwitch) {
            val currentKcalValue = calcValue(caloriesFile, valueString, grammString, customString)
            dataHandler.saveData(requireContext(), caloriesFile, currentDate, currentKcalValue.toString())
            if (customString != "") {
                historyMap += mutableMapOf((currentTime + "_calo") to customString)
            }
            else if (valueString != "" && grammString != "") {
                val caloriesDouble = valueString.toDouble()
                val grammDouble = grammString.toDouble()
                historyMap += mutableMapOf((currentTime + "_calo") to (caloriesDouble * (grammDouble / 100)).toString())
            }
            changeProgressBar(goals[Keys.Calories.toString()]!!, currentKcalValue, true)
            val calCons = getCurrentValue(caloriesFile).toInt().toString() + " kcal"
            bnd.usedKcal.text = calCons
            updateRemaining(currentKcalValue, Value.Calories)
            if (currentKcalValue > goals[Keys.Calories.toString()]!!.toDouble()) {
                rnd = (0..12).random()
                Snackbar.make(this.requireView(), messages[rnd], Snackbar.LENGTH_LONG)
                    .setAnchorView(bnd.progressBar2)
                    .show()
            }
        } else {
            val currentProteinValue = calcValue(proteinFile, valueString, grammString, customString)
            dataHandler.saveData(requireContext(), proteinFile, currentDate, currentProteinValue.toString())
            if (customString != "") {
                historyMap += mutableMapOf((currentTime + "_prot") to customString)
            }
            else if (valueString != "" && grammString != "") {
                val caloriesDouble = valueString.toDouble()
                val grammDouble = grammString.toDouble()
                historyMap += mutableMapOf((currentTime + "_prot") to (caloriesDouble * (grammDouble / 100)).toString())
            }
            changeProgressBar(goals[Keys.Protein.toString()]!!, currentProteinValue, false)
            val protCons = getCurrentValue(proteinFile).toInt().toString() + " g"
            bnd.consumedProt.text = protCons
            updateRemaining(currentProteinValue, Value.Protein)
        }
        dataHandler.saveMapDataNO(requireContext(), historyFile, historyMap)
        clearFields ()
    }

    private fun clearFields (){
        kcal.text.clear()
        gramm.text.clear()
        custom.text.clear()
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

    @SuppressLint("DefaultLocale")
    private fun getCurrentDateTime(): String {
        val calendar  = Calendar.getInstance()
        val currentTime = String.format("%02d.%02d%02d:%02d:%02d",
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.MONTH)+1,
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            calendar.get(Calendar.SECOND))
        return currentTime
    }

    private fun calcValue(fileName: String, value: String, gramm: String, custom: String): Double {
        val toast = Toast.makeText(activity, "Alcohol mode is on! Nothing is added!", Toast.LENGTH_LONG)
        var currentKcal = getCurrentValue(fileName)
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

    private fun getCurrentValue(fileName: String): Double {
        val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val out = dataHandler.loadData(requireContext(), fileName)
        val currentValue = if (out.containsKey(currentDate)) {
            out[currentDate]?.toDouble()!!
        } else {
            0.0
        }
        return currentValue
    }
}