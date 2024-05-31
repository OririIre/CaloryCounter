package com.example.calorycounter

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.calorycounter.databinding.FragmentChartBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.Locale


class Chart : Fragment() {
    private var _bnd: FragmentChartBinding? = null
    private val bnd get() = _bnd!!
    private lateinit var usedCalories: TextView
    private lateinit var usedProtein: TextView
    private lateinit var dateView: TextView
    private var caloriesFile = "calLog.txt"
    private var proteinFile = "protLog.txt"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _bnd = FragmentChartBinding.inflate(inflater, container, false)
        val view = bnd.root
        buildUI()

        return view
    }

    override fun onResume() {
        super.onResume()
        buildUI()
    }

    private fun buildUI() {
        val chartDataCalories = dataHandler.loadData(requireContext(), caloriesFile)
        val chartDataProtein = dataHandler.loadData(requireContext(), proteinFile)
        val list: ArrayList<Entry> = ArrayList()
        val caloriesArray: ArrayList<String> = ArrayList()
        val dateArray: ArrayList<String> = ArrayList()
        val xAxisValues: ArrayList<String> = ArrayList()
        //ToDo Check this later
//        val dataFromMain = Intent.EXTRA_FROM_STORAGE
//        println(dataFromMain)

//        chartDataCalories = mutableMapOf("20240518" to "1400", "20240519" to "1300", "20240520" to "1200", "20240521" to "1100", "20240522" to "1000", "20240523" to "900", "20240524" to "800", "20240525" to "800", "20240526" to "800", "20240527" to "800", "20240528" to "800")

        var x = 0
        for (item in chartDataCalories.toSortedMap(reverseOrder())) {
            if (x < 30) {
                caloriesArray.add(x, item.value)
                dateArray.add(x, item.key)
                x++
            } else break
        }


        if (chartDataCalories.isNotEmpty() && chartDataProtein.isNotEmpty()) {
            for (i in 0..<caloriesArray.size) {
                val key = dateArray[i].takeLast(4)
                if (caloriesArray.reversed()[i] != "") {
                    list.add(Entry(i.toFloat(), caloriesArray.reversed()[i].toFloat()))
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
                        createCards(
                            bnd.layoutCalories,
                            item.key,
                            item.value,
                            chartDataProtein[item.key]!!
                        )
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
        lineDataSet.setColors(Color.argb(100, 86, 40, 166))
        lineDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        lineDataSet.setDrawFilled(true)
        lineDataSet.setFillColor(Color.argb(100, 86, 40, 166))
        lineDataSet.lineWidth = 3f
        lineDataSet.valueTextSize = 12f
        lineDataSet.valueTextColor = Color.WHITE

        val lineData = LineData(lineDataSet)

        bnd.chart.description.isEnabled = false

        bnd.chart.xAxis.valueFormatter = IndexAxisValueFormatter(xAxisValues.reversed())
        bnd.chart.data = lineData
        bnd.chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        bnd.chart.xAxis.setDrawGridLines(false)
        bnd.chart.xAxis.textSize = 15f
        bnd.chart.xAxis.textColor = Color.WHITE
        bnd.chart.xAxis.granularity = 1f
        bnd.chart.xAxis.labelCount = xAxisValues.size

        bnd.chart.axisLeft.textSize = 15f
        bnd.chart.axisLeft.textColor = Color.WHITE
        bnd.chart.axisLeft.axisMinimum = 0f
        bnd.chart.axisRight.isEnabled = false


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

        usedCalories.text = calories
        usedProtein.text = protein
        dateView.text = newDateString
    }

}