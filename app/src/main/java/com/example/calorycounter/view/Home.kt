package com.example.calorycounter.view

import android.animation.ObjectAnimator
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.transition.ChangeBounds
import com.example.calorycounter.Keys
import com.example.calorycounter.R
import com.example.calorycounter.caloriesFile
import com.example.calorycounter.data.DataHandler
import com.example.calorycounter.data.HelperClass.Companion.getCurrentValue
import com.example.calorycounter.data.ProcessMeals
import com.example.calorycounter.data.SpeechSearch
import com.example.calorycounter.data.UpdateListener
import com.example.calorycounter.databinding.FragmentHomeBinding
import com.example.calorycounter.goalsFile
import com.example.calorycounter.historyFile
import com.example.calorycounter.iconFile
import com.example.calorycounter.languageFile
import com.example.calorycounter.mealsFile
import com.example.calorycounter.proteinFile
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import jp.wasabeef.blurry.Blurry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.jsoup.nodes.Document
import java.util.Calendar
import java.util.Locale
import kotlin.math.round

class Home : Fragment(), UpdateListener {
    private var _bnd: FragmentHomeBinding? = null
    private val bnd get() = _bnd!!
    private lateinit var goals: MutableMap<String, String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var bottomMealDialog: BottomSheetDialog
    private lateinit var bottomAddDialog: BottomSheetDialog
    private lateinit var bottomHistoryDialog: BottomSheetDialog
    private lateinit var mealsDialog: MealsDialog
    private lateinit var freeAddDialog: FreeAddDialog
    private lateinit var historyDialog: HistoryDialog
    private lateinit var speechSearch: SpeechSearch
    private lateinit var processMeals: ProcessMeals
    private val dataHandler = DataHandler()
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
        clearHistoryOnNextDay()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {_bnd = FragmentHomeBinding.inflate(inflater, container, false)
        val view = bnd.root
        val thisContext = requireContext()
        mealsDialog = MealsDialog(thisContext)
        freeAddDialog = FreeAddDialog(thisContext)
        speechSearch = SpeechSearch(thisContext)
        historyDialog = HistoryDialog(thisContext)
        processMeals = ProcessMeals(thisContext)
        freeAddDialog.addListener(this)
        historyDialog.addListener(this)

        createMainView()

        return view
    }

    private fun createMainView(){
        bnd.remainingCaloriesProgressBar.max = 1000
        bnd.remainingProteinProgressBar.max = 1000
        bnd.consumedCaloriesProgressBar.max = 1000
        bnd.consumedProteinProgressBar.max = 1000

        bottomMealDialog = createDialog(R.layout.meals_layout)
        bottomAddDialog = createDialog(R.layout.free_add_layout)
        bottomHistoryDialog = createDialog(R.layout.history_layout)

        val imageView = createImageView()
        imageView.visibility = View.GONE

        bnd.home.addView(imageView)

        updateGoals()
        updateMealsUI()

        bnd.infoToggle.setOnClickListener {

        }

        bnd.history.setOnClickListener {
            bnd.history.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
            historyDialog.showHistoryDialog(bottomHistoryDialog)
        }

        bnd.fb.setOnClickListener {
            if (!isAllFabVisible) {
                Blurry.with(requireContext()).capture(this.view).into(imageView)
                imageView.visibility = View.VISIBLE
                imageView.bringToFront()
                setFloatingButtonVisibilty(true)
            } else {
                imageView.visibility = View.GONE
                setFloatingButtonVisibilty(false)
            }
        }

        bnd.fbCustom.setOnClickListener {
            imageView.visibility = View.GONE
            setFloatingButtonVisibilty(false)
            freeAddDialog.show(bottomAddDialog)
        }

        bottomAddDialog.setOnDismissListener {
            updateUI()
        }

        bnd.fbMeals.setOnClickListener {
            imageView.visibility = View.GONE
            setFloatingButtonVisibilty(false)
            mealsDialog.show(0, bottomMealDialog)
        }

        bottomMealDialog.setOnDismissListener {
            updateMealsUI()
        }

        bnd.speechAdd.setOnClickListener {
            val selectedLanguage =
                dataHandler.loadData(requireContext(), languageFile)[Keys.Language.toString()]
            val language: String = when (selectedLanguage) {
                resources.getString(R.string.language_german) -> {
                    "de_DE"
                }
                resources.getString(R.string.language_english) -> {
                    "en_UK"
                }
                resources.getString(R.string.language_french) -> {
                    "fr_FR"
                }
                resources.getString(R.string.language_spanish) -> {
                    "es_ES"
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
                showSnackbar(2500, "Speech not supported!")
            }
        }

        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == RESULT_OK && result.data != null) {
                    processSpeechResult(result)
                }
            }
    }

    override fun onResume() {
        super.onResume()
        updateGoals()
        updateUI()
        updateMealsUI()
        clearHistoryOnNextDay()
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

    private fun updateUI(){
        val caloriesGoal = goals[Keys.Calories.toString()].toString()
        val proteinGoal = goals[Keys.Protein.toString()].toString()

        val currentCalories = getCurrentValue(caloriesFile, requireContext())
        val currentProtein = getCurrentValue(proteinFile, requireContext())

        val calCons = currentCalories.toInt().toString() + " kcal"
        val protCons = currentProtein.toInt().toString() + " g"

        animateProgressBar(caloriesGoal, currentCalories, false,1500, bnd.remainingCaloriesProgressBar)
        animateProgressBar(caloriesGoal, currentCalories, true, 2000, bnd.consumedCaloriesProgressBar)
        animateProgressBar(proteinGoal, currentProtein, false, 1500, bnd.remainingProteinProgressBar)
        animateProgressBar(proteinGoal, currentProtein, true,2000, bnd.consumedProteinProgressBar)

        bnd.consumedProt.text = protCons
        bnd.usedKcal.text = calCons

        updateReamainingUI(currentCalories, Keys.Calories)
        updateReamainingUI(currentProtein, Keys.Protein)

        if (currentCalories > caloriesGoal.toDouble()) {
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
    }

    private fun updateReamainingUI(currentValue: Double, key: Keys){
        var remaining: String
        remaining = prepareRemainingText(currentValue, key)
        if (Keys.Calories == key){
            remaining = "$remaining kcal"
            bnd.leftKcal.text = remaining
        } else {
            remaining = "$remaining g"
            bnd.leftProt.text = remaining
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

    private fun updateMealsUI() {
        val meals = dataHandler.loadData(requireContext(), mealsFile)
        val icons = dataHandler.loadData(requireContext(), iconFile)
        bnd.linearLayoutMeals.removeAllViews()
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

    private fun clearHistoryOnNextDay(){
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

    private fun setFloatingButtonVisibilty (visible: Boolean){
        if(visible){
            bnd.fbCustom.visibility = View.VISIBLE
            bnd.fbMeals.visibility = View.VISIBLE
            bnd.addFreeText.visibility = View.VISIBLE
            bnd.addMealText.visibility = View.VISIBLE
            bnd.addFreeText.bringToFront()
            bnd.addMealText.bringToFront()
            isAllFabVisible = true
        } else {
            bnd.fbCustom.visibility = View.GONE
            bnd.fbMeals.visibility = View.GONE
            bnd.addFreeText.visibility = View.GONE
            bnd.addMealText.visibility = View.GONE
            isAllFabVisible = false
        }
    }

    private fun processSpeechResult(result: ActivityResult){
        val resultArray = filterInput(result)
        if (resultArray.isNotEmpty()) {
            addSpeechResultInThread(resultArray)
        } else {
            println("wrong input")
        }
    }

    private fun filterInput(result: ActivityResult): Array<String> {
        val text = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

        var resultArray = emptyArray<String>()
        try {
            resultArray = speechSearch.filterInput(text.toString())
        } catch (exp: Exception) {
            println(exp)
            showSnackbar(3000, "Wrong input, amount or keyword not found")
        }
        return resultArray
    }

    private fun addSpeechResultInThread (resultArray: Array<String>){
        var document: Document
        //start coroutine (like threading in java stuff)
        try{
            lifecycleScope.launch(Dispatchers.IO) {
                var resultDocument = async {
                    speechSearch.searchRequest(resultArray, "calories")
                }
                document = resultDocument.await()
                var baseValue = speechSearch.extractCaloriesValues(document, "wDYxhc")
                if (baseValue == "") {
                    baseValue = speechSearch.extractCaloriesValues(document, "MjjYud")
                }
                speechSearch.addFromSpeech(baseValue, resultArray[1].trim(), resultArray[0].trim(), caloriesFile)

                resultDocument = async {
                    speechSearch.searchRequest(resultArray, "protein")
                }
                document = resultDocument.await()
                baseValue = speechSearch.extractProteinValues(document, "wDYxhc")
                if (baseValue == "") {
                    baseValue = speechSearch.extractProteinValues(document, "MjjYud")
                }
                speechSearch.addFromSpeech(baseValue, resultArray[1].trim(), resultArray[0].trim(), proteinFile)
            }.invokeOnCompletion {
                requireActivity().runOnUiThread {
                    updateUI()
                }
            }
        } catch (exp: Exception) {
            println(exp)
            showSnackbar(3000, "Search went wrong, please try again")
        }
    }

    private fun addMealLine (mealName: String, mealValue: String, mealProt: String, buttonID: Int, icon: String){
        val parentLayout = bnd.linearLayoutMeals
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
            processMeals.addMeal(mealValue, mealProt, mealName)
            updateUI()
        }

        mealsName.setOnClickListener{
            mealsName.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
            mealsDialog.show(mealsName.id, bottomMealDialog)
        }

        mealsName.setOnLongClickListener {
            Snackbar.make(bnd.home, "Delete Entry?", 4000)
                .setBackgroundTint(resources.getColor(R.color.black, null))
                .setAction("DELETE") {
                    processMeals.deleteMeal(mealsName.id)
                    updateMealsUI()
                    }
                .show()
            true
        }
        parentLayout.addView(relativeLayout)
    }

    private fun createDivider(mealsName: TextView): View {
        val divider = View(requireContext())
        divider.id = View.generateViewId()

        val dividerParam: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            1
        )

        dividerParam.addRule(RelativeLayout.BELOW, mealsName.id)
        divider.layoutParams = dividerParam
        divider.setBackgroundColor(ResourcesCompat.getColor(resources,
            R.color.white_low_transparency, null))

        return divider
    }

    private fun createRelativeLayout():RelativeLayout{
        val relativeLayout = RelativeLayout(requireContext())
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

    private fun createTextViewValue(mealValue: String):TextView{
        val textView = TextView(requireContext())
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
        textView.setTextColor(ResourcesCompat.getColor(resources, R.color.white, null))
        textView.layoutParams = mealsValueParam
        textView.gravity = Gravity.END
        textView.setCompoundDrawablesWithIntrinsicBounds(null,null,ResourcesCompat.getDrawable(resources,
            R.drawable.baseline_add_circle_24, null),null)
        textView.compoundDrawablePadding = 15
        return textView
    }

    private fun createTextViewName(buttonID: Int, mealName: String, icon: String):TextView{
        val textView = TextView(requireContext())
        textView.id = buttonID

        val mealsNameParam: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        mealsNameParam.addRule(RelativeLayout.ALIGN_PARENT_START)
        mealsNameParam.addRule(RelativeLayout.START_OF, textView.id)

        textView.text = mealName
        textView.textSize = 15f
        textView.setTextColor(ResourcesCompat.getColor(resources, R.color.white, null))
        if(icon != "") {
            textView.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(resources, icon.toInt(),null), null, null, null)
        }
        textView.compoundDrawablePadding = 15
        textView.layoutParams = mealsNameParam

        return textView
    }

    private fun createDialog(layout: Int): BottomSheetDialog {
        val bottomDialog = layoutInflater.inflate(layout, null)
        val bottomSheet = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        bottomSheet.setContentView(bottomDialog)

        return bottomSheet
    }

    private fun createImageView (): ImageView{
        val imageView = ImageView(requireContext())
        val layoutParam: ConstraintLayout.LayoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT)
        imageView.layoutParams = layoutParam
        imageView.setBackgroundColor(Color.argb(100,0,0,0))
        imageView.visibility = View.GONE
        return imageView
    }

    private fun showSnackbar(duration: Int, text: String){
        Snackbar.make(bnd.home, text, duration)
            .setBackgroundTint(resources.getColor(R.color.black, null))
            .show()
    }

    override fun onStuffUpdated() {
        updateUI()
    }
}