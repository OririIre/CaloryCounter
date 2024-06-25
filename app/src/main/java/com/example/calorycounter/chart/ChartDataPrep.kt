package com.example.calorycounter.chart

import android.graphics.Color
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class ChartDataPrep {

    fun setUpLineData(list: List<Entry>): LineData {
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

        return lineData
    }

    fun setUpXAxis(chart: LineChart, xAxisValues: List<String>){
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(xAxisValues.reversed())
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.setDrawGridLines(false)
        chart.xAxis.textSize = 15f
        chart.xAxis.textColor = Color.WHITE
        chart.xAxis.granularity = 1f
        chart.xAxis.labelCount = xAxisValues.size
    }

    fun setUpYAxis(chart: LineChart, minValue: Float){
        chart.axisLeft.textSize = 15f
        chart.axisLeft.textColor = Color.WHITE
        chart.axisLeft.axisMinimum = minValue
        chart.axisLeft.labelCount = 4
        chart.axisLeft.setDrawGridLines(false)
        chart.axisRight.isEnabled = false

    }

    fun setUpChart(chart: LineChart){
        chart.legend.textColor = Color.WHITE
        chart.extraRightOffset = 30f
        chart.description.isEnabled = false
        chart.setBorderColor(Color.WHITE)

    }
}