package com.example.calorycounter.chart

import com.github.mikephil.charting.data.Entry
import java.util.Collections.min

class ChartLogic {

    fun reverseChartData(chartDataCalories: MutableMap<String, String>): ArrayList<Float>{
        val caloriesArray: ArrayList<Float> = ArrayList()
        var x = 0
        for (item in chartDataCalories.toSortedMap(reverseOrder())) {
            if (x < 30) {
                caloriesArray.add(x, item.value.toFloat())
                x++
            } else break
        }
        return caloriesArray
    }

    fun reverseDateData(chartDataCalories: MutableMap<String, String>): ArrayList<String>{
        val dateArray: ArrayList<String> = ArrayList()
        var x = 0
        for (item in chartDataCalories.toSortedMap(reverseOrder())) {
            if (x < 30) {
                dateArray.add(x, item.key)
                x++
            } else break
        }
        return dateArray
    }

    fun prepareLineData(dateArray: ArrayList<String>, caloriesArray: ArrayList<Float>): ArrayList<Entry>{
        val list: ArrayList<Entry> = ArrayList()
        if (caloriesArray.isNotEmpty()) {
            for (i in 0..<dateArray.size) {
                if (!caloriesArray.reversed()[i].isNaN()) {
                    list.add(Entry(i.toFloat(), caloriesArray.reversed()[i]))
                } else {
                    list.add(Entry(i.toFloat(), 0f))
                }
            }
        }
        return list
    }

    fun prepareAxisData(dateArray: ArrayList<String>, caloriesArray: ArrayList<Float>): ArrayList<String>
    {
        val xAxisValues: ArrayList<String> = ArrayList()
        if (caloriesArray.isNotEmpty()) {
            for (i in 0..<dateArray.size) {
                val key = dateArray[i].takeLast(4)
                if (i == 0 || i == (caloriesArray.size - 1) || i == (caloriesArray.size / 3) || i == (caloriesArray.size * 2 / 3)) {
                    val sb = StringBuilder(key)
                    sb.insert(2, ".")
                    xAxisValues.add(sb.toString().trim())
                } else {
                    xAxisValues.add("")
                }
            }
        }
        return xAxisValues
    }

    fun calculateMinValue(caloriesArray: ArrayList<Float>): Float {
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
        return minValue
    }
}