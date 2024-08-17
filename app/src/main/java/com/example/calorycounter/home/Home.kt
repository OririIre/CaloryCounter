package com.example.calorycounter.home

import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.calorycounter.R
import com.example.calorycounter.data.DataHandler
import com.example.calorycounter.databinding.FragmentHomeBinding
import com.example.calorycounter.helpers.UpdateListener
import com.example.calorycounter.helpers.caloriesFile
import com.example.calorycounter.helpers.historyFile
import com.example.calorycounter.helpers.proteinFile
import com.example.calorycounter.home.dialogs.FreeAddDialog
import com.example.calorycounter.home.dialogs.HistoryDialog
import com.example.calorycounter.home.dialogs.MealsDialog
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    private lateinit var homeLogic: HomeLogic
    private lateinit var homeMealsCreation: HomeMealsCreation
    private lateinit var homeProgressBars: HomeProgressBars
    private lateinit var blurView: ImageView
    private val dataHandler = DataHandler()
    private var isAllFabVisible: Boolean = false

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
        homeLogic = HomeLogic(requireContext())
        freeAddDialog.addListener(this)
        historyDialog.addListener(this)
        bottomMealDialog = createDialog(R.layout.meals_layout)
        bottomAddDialog = createDialog(R.layout.free_add_layout)
        bottomHistoryDialog = createDialog(R.layout.history_layout)
        blurView = createImageView()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        goals = homeLogic.updateGoals()
        homeProgressBars = HomeProgressBars(requireContext(), bnd.remainingCaloriesProgressBar,
            bnd.consumedCaloriesProgressBar, bnd.remainingProteinProgressBar,
            bnd.consumedProteinProgressBar, bnd.consumedProt, bnd.usedKcal, bnd.cardView,
            bnd.leftKcal, bnd.leftProt)
        homeMealsCreation = HomeMealsCreation(requireContext(), bnd.linearLayoutMeals, bnd.home, bottomMealDialog, homeProgressBars)
        homeMealsCreation.updateMealsUI()
        createMainView()

        super.onViewCreated(view, savedInstanceState)
    }

    private fun createMainView(){
        bnd.remainingCaloriesProgressBar.max = 1000
        bnd.remainingProteinProgressBar.max = 1000
        bnd.consumedCaloriesProgressBar.max = 1000
        bnd.consumedProteinProgressBar.max = 1000

        blurView.visibility = View.GONE

        bnd.home.addView(blurView)

        bnd.infoToggle.setOnClickListener {

        }

        bnd.history.setOnClickListener {
            bnd.history.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
            historyDialog.showHistoryDialog(bottomHistoryDialog)
        }

        bnd.fb.setOnClickListener {
            if (!isAllFabVisible) {
//                Blurry.with(requireContext()).capture(this.view).into(blurView)
                blurView.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.image_background, null))
                blurView.visibility = View.VISIBLE
                blurView.bringToFront()
            } else {
                blurView.visibility = View.GONE
            }

            homeLogic.setFloatingButtonVisibilty(bnd.fbCustom, bnd.fbMeals, bnd.addFreeText, bnd.addMealText, isAllFabVisible)
            isAllFabVisible = !isAllFabVisible
        }

        bnd.fbCustom.setOnClickListener {
            toggelFABVisibility ()
            freeAddDialog.show(bottomAddDialog)
        }

        bottomAddDialog.setOnDismissListener {
            homeProgressBars.updateUI(true)
        }

        bnd.fbMeals.setOnClickListener {
            toggelFABVisibility ()
            mealsDialog.show(0, bottomMealDialog)
        }

        bottomMealDialog.setOnDismissListener {
            homeMealsCreation.updateMealsUI()
        }

        blurView.setOnClickListener{
            toggelFABVisibility ()
        }

        bnd.speechAdd.setOnClickListener {
            val language = speechSearch.getVoiceLanguage()
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
        goals = homeLogic.updateGoals()
        println(goals)
        homeProgressBars.updateUI(false)
        homeMealsCreation.updateMealsUI()
        clearHistoryOnNextDay()
    }

    override fun onPause() {
        toggelFABVisibility()
        super.onPause()
    }

    private fun toggelFABVisibility() {
        blurView.visibility = View.GONE
        isAllFabVisible = true
        homeLogic.setFloatingButtonVisibilty(bnd.fbCustom, bnd.fbMeals, bnd.addFreeText, bnd.addMealText, isAllFabVisible)
        isAllFabVisible = false
    }

    private fun clearHistoryOnNextDay(){
        val historyValues = dataHandler.loadData(requireContext(), historyFile)
        val currentDate = SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date())

        if (historyValues.keys.any { !it.contains(currentDate) }) {
            dataHandler.deleteFiles(requireContext(), historyFile)
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
        //start coroutine (like threading in java stuff)
        try{
            lifecycleScope.launch(Dispatchers.IO) {
                val caloriesDocument = async {
                    speechSearch.searchRequest(resultArray, "calories")
                }.await()
                var caloriesValue = speechSearch.extractCaloriesValues(caloriesDocument, "wDYxhc")
                if (caloriesValue == "") {
                    caloriesValue = speechSearch.extractCaloriesValues(caloriesDocument, "MjjYud")
                }
                speechSearch.addFromSpeech(caloriesValue, resultArray[1].trim(), resultArray[0].trim(), caloriesFile)

                val proteinDocument = async {
                    speechSearch.searchRequest(resultArray, "protein")
                }.await()
                var proteinValue = speechSearch.extractProteinValues(proteinDocument, "wDYxhc")
                if (proteinValue == "") {
                    proteinValue = speechSearch.extractProteinValues(proteinDocument, "MjjYud")
                }
                speechSearch.addFromSpeech(proteinValue, resultArray[1].trim(), resultArray[0].trim(), proteinFile)
            }.invokeOnCompletion {
                requireActivity().runOnUiThread {
                    homeProgressBars.updateUI(true)
                }
            }
        } catch (exp: Exception) {
            println(exp)
            requireActivity().runOnUiThread {
                showSnackbar(3000, "Search went wrong, please try again")
            }
        }
    }

    private fun createDialog(layout: Int): BottomSheetDialog {
        val bottomDialog = layoutInflater.inflate(layout, null)
        val bottomSheet = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        bottomSheet.setContentView(bottomDialog)

        return bottomSheet
    }

    private fun createImageView (): ImageView{
        return ImageView(requireContext()).apply {
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
            )
            visibility = View.GONE
        }
    }

    private fun showSnackbar(duration: Int, text: String){
        Snackbar.make(bnd.home, text, duration)
            .setBackgroundTint(resources.getColor(R.color.black, null))
            .show()
    }

    override fun onStuffUpdated() {
        homeProgressBars.updateUI(true)
    }
}