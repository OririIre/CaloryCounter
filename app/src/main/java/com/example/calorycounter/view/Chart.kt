package com.example.calorycounter.view

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.calorycounter.R
import com.example.calorycounter.caloriesFile
import com.example.calorycounter.data.DataHandler
import com.example.calorycounter.databinding.FragmentChartBinding
import com.example.calorycounter.proteinFile
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import java.text.SimpleDateFormat
import java.util.Collections.min
import java.util.Locale

class Chart : Fragment() {
    private val dataHandler = DataHandler()
    private var _bnd: FragmentChartBinding? = null
    private val bnd get() = _bnd!!
    private lateinit var usedCalories: TextView
    private lateinit var usedProtein: TextView
    private lateinit var dateView: TextView
    private lateinit var chartDataCalories: MutableMap<String, String>
    private lateinit var chartDataProtein: MutableMap<String, String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chartDataCalories = dataHandler.loadData(requireContext(), caloriesFile)
        chartDataProtein = dataHandler.loadData(requireContext(), proteinFile)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _bnd = FragmentChartBinding.inflate(inflater, container, false)
        val view = bnd.root
        //ToDo Check if this is working asynchronous
//        lifecycleScope.launch(Dispatchers.IO) {
//
//        }
        requireActivity().runOnUiThread{
            buildUI()
        }
//        buildUI()
//            .invokeOnCompletion {
//            requireActivity().runOnUiThread{
//                updateUI()
//            }
//        }
        return view
    }

    override fun onResume() {
        super.onResume()
        requireActivity().runOnUiThread{
            buildUI()
        }
    }

    private fun buildUI() {

        val list: ArrayList<Entry> = ArrayList()
        val caloriesArray: ArrayList<Float> = ArrayList()
        val dateArray: ArrayList<String> = ArrayList()
        val xAxisValues: ArrayList<String> = ArrayList()
        //ToDo Check this later
//        val dataFromMain = Intent.EXTRA_FROM_STORAGE
//        println(dataFromMain)
        chartDataCalories = dataHandler.loadData(requireContext(), caloriesFile)
        chartDataProtein = dataHandler.loadData(requireContext(), proteinFile)

//        chartDataCalories = mutableMapOf("20240518" to "1400", "20240519" to "1300", "20240520" to "1200", "20240521" to "1100", "20240522" to "1000", "20240523" to "900", "20240524" to "800", "20240525" to "800", "20240526" to "800", "20240527" to "800", "20240528" to "800")

        var x = 0
        for (item in chartDataCalories.toSortedMap(reverseOrder())) {
            if (x < 30) {
                caloriesArray.add(x, item.value.toFloat())
                dateArray.add(x, item.key)
                x++
            } else break
        }


        if (chartDataCalories.isNotEmpty() && chartDataProtein.isNotEmpty()) {
            for (i in 0..<caloriesArray.size) {
                val key = dateArray[i].takeLast(4)
                if (!caloriesArray.reversed()[i].isNaN()) {
                    list.add(Entry(i.toFloat(), caloriesArray.reversed()[i]))
                } else {
                    list.add(Entry(i.toFloat(), 0f))
                }
                if (i == 0 || i == (caloriesArray.size - 1) || i == (caloriesArray.size / 3) || i == (caloriesArray.size * 2 / 3)) {
                    val sb = StringBuilder(key)
                    sb.insert(2, ".")
                    xAxisValues.add(sb.toString().trim())
                } else {
                    xAxisValues.add("")
                }
            }
        }

        bnd.layoutCalories.removeAllViews()
        if (chartDataCalories.isNotEmpty() && chartDataProtein.isNotEmpty()) {
            var i = 0
            for (item in chartDataCalories.toSortedMap(reverseOrder())) {
                if (i < 30) {
                    i++
                    if (chartDataProtein.containsKey(item.key)) {
                        createCards(bnd.layoutCalories, item.key, item.value, chartDataProtein[item.key]!!)
                    } else {
                        createCards(bnd.layoutCalories, item.key, item.value, "0")
                    }
                }
            }
            for (item in chartDataProtein.toSortedMap(reverseOrder())) {
                if (i < 30) {
                    i++
                    if (!chartDataCalories.containsKey(item.key)) {
                        createCards(bnd.layoutCalories, item.key, "0", item.value)
                    }
                }
            }
        }


        val lineDataSet = LineDataSet(list, "kcal")
        lineDataSet.setColors(Color.argb(100, 93, 139, 212))
        lineDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        lineDataSet.setDrawFilled(true)
        lineDataSet.setFillColor(Color.argb(100, 93, 139, 212))
        lineDataSet.lineWidth = 3f
        lineDataSet.valueTextSize = 12f
        lineDataSet.valueTextColor = Color.WHITE
        lineDataSet.setCircleColor(Color.argb(100, 93, 139, 212))
        lineDataSet.setDrawCircleHole(false)
        lineDataSet.setDrawHighlightIndicators(false)
        lineDataSet.color

        val lineData = LineData(lineDataSet)
        var minValue = 0f
        if(caloriesArray.isNotEmpty()){
            minValue = min(caloriesArray)
        }

        if ((minValue - 200) > 0) {
            minValue -= 200
        }
        else{
            minValue = 0f
        }

        bnd.chart.xAxis.valueFormatter = IndexAxisValueFormatter(xAxisValues.reversed())
        bnd.chart.data = lineData
        bnd.chart.data.setDrawValues(false)
        bnd.chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        bnd.chart.xAxis.setDrawGridLines(false)
        bnd.chart.xAxis.textSize = 15f
        bnd.chart.xAxis.textColor = Color.WHITE
        bnd.chart.xAxis.granularity = 1f
        bnd.chart.xAxis.labelCount = xAxisValues.size

        bnd.chart.axisLeft.textSize = 15f
        bnd.chart.axisLeft.textColor = Color.WHITE
        bnd.chart.axisLeft.axisMinimum = minValue
        bnd.chart.axisRight.isEnabled = false
        bnd.chart.axisLeft.labelCount = 4
        bnd.chart.axisLeft.setDrawGridLines(false)
        bnd.chart.legend.textColor = Color.WHITE
        bnd.chart.extraRightOffset = 30f
        bnd.chart.description.isEnabled = false

        bnd.chart.marker = object : MarkerView(context, R.layout.chart_marker) {
            override fun refreshContent(e: Entry, highlight: Highlight) {
                (findViewById<View>(R.id.tvContent) as TextView).text = "${(e.y).toInt()}"
            }
        }

        bnd.chart.setBorderColor(Color.WHITE)
    }

    private fun createCards(parent: LinearLayout, date: String, calories: String, protein: String) {
        val inflater = layoutInflater
        val myLayout: View = inflater.inflate(R.layout.card_layout, parent, true)

        val formatString = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val newDate = formatString.parse(date)
        val newDateString = newDate?.toString()?.removeRange(11, 30)

        usedCalories = myLayout.findViewById(R.id.usedCalories)
        usedProtein = myLayout.findViewById(R.id.usedProtein)
        dateView = myLayout.findViewById(R.id.date)

        usedCalories.id = View.generateViewId()
        usedProtein.id = View.generateViewId()
        dateView.id = View.generateViewId()

        val param: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        param.setMargins(500, 12, 0, 0)
        param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        param.addRule(RelativeLayout.BELOW, usedCalories.id)

        usedProtein.layoutParams = param

        usedCalories.text = calories.toDouble().toInt().toString()
        usedProtein.text = protein.toDouble().toInt().toString()
        dateView.text = newDateString
    }

}