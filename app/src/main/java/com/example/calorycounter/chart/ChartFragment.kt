package com.example.calorycounter.chart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.calorycounter.R
import com.example.calorycounter.helpers.caloriesFile
import com.example.calorycounter.data.DataHandler
import com.example.calorycounter.databinding.FragmentChartBinding
import com.example.calorycounter.helpers.proteinFile
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChartFragment : Fragment() {
    private val dataHandler = DataHandler()
    private val chartLogic = ChartLogic()
    private val chartPrep = ChartDataPrep()
    private var _bnd: FragmentChartBinding? = null
    private val bnd get() = _bnd!!
    private lateinit var chartCardCreation: ChartCardCreation
    private lateinit var chartDataCalories: MutableMap<String, String>
    private lateinit var chartDataProtein: MutableMap<String, String>

    override fun onCreate(savedInstanceState: Bundle?) {
        loadData()
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _bnd = FragmentChartBinding.inflate(inflater, container, false)
        val view = bnd.root
        chartCardCreation = ChartCardCreation(requireContext())
        return view
    }

    override fun onResume() {
        super.onResume()
        loadData()
        buildUI()
    }

    private fun buildUI() {
//        chartDataCalories = mutableMapOf("20240518" to "1400", "20240519" to "1300", "20240520" to "1200", "20240521" to "1100", "20240522" to "1000", "20240523" to "900", "20240524" to "800", "20240525" to "800", "20240526" to "800", "20240527" to "800", "20240528" to "800")

        val dateArray: List<String> = chartLogic.reverseDateData(chartDataCalories)
        val caloriesArray: List<Float> = chartLogic.reverseChartData(chartDataCalories)

        bnd.layoutCalories.removeAllViews()
        val cardArrray = chartCardCreation.prepareCards(chartDataCalories, chartDataProtein, bnd.layoutCalories, bnd.chartScrollView)
        for (card in cardArrray) {
            bnd.layoutCalories.addView(card)
        }

        val lineData = chartLogic.prepareLineData(dateArray, caloriesArray).let { chartPrep.setUpLineData(it) }
        val xAxisValues: List<String> = chartLogic.prepareAxisData(dateArray)

        val minValue = chartLogic.calculateMinValue(caloriesArray)

        chartPrep.setUpXAxis(bnd.chart, xAxisValues)
        chartPrep.setUpYAxis(bnd.chart, minValue)
        chartPrep.setUpChart(bnd.chart)
        setUpChartMarker()

        lifecycleScope.launch(Dispatchers.Main) {
            bnd.chart.data = lineData
            bnd.chart.data.setDrawValues(false)
            bnd.chart.invalidate()
        }
    }

    private fun loadData(){
        chartDataCalories = dataHandler.loadData(requireContext(), caloriesFile)
        chartDataProtein = dataHandler.loadData(requireContext(), proteinFile)
    }

    private fun setUpChartMarker(){
        bnd.chart.marker = object : MarkerView(context, R.layout.chart_marker) {
            override fun refreshContent(e: Entry, highlight: Highlight) {
                val textViewContent = (e.y).toInt()
                (findViewById<View>(R.id.tvContent) as TextView).text = textViewContent.toString()
            }
        }
    }
}