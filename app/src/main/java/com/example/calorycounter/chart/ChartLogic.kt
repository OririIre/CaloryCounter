package com.example.calorycounter.chart

import com.github.mikephil.charting.data.Entry

class ChartLogic {

    fun reverseChartData(chartDataCalories: MutableMap<String, String>): List<Float> {
        return chartDataCalories.toSortedMap(reverseOrder())
            .values
            .mapNotNull { it.toFloatOrNull() }
            .take(30)
    }

    fun reverseDateData(chartDataCalories: MutableMap<String, String>): List<String> {
        return chartDataCalories.toSortedMap(reverseOrder())
            .keys
            .take(30)
            .toList()
    }

    fun prepareLineData(dateArray: List<String>, caloriesArray: List<Float>): List<Entry> {
        return dateArray.indices.map { i ->
            Entry(
                i.toFloat(),
                caloriesArray.reversed().getOrNull(i)?.takeUnless { it.isNaN() } ?: 0f
            )
        }
    }

    fun prepareAxisData(dateArray: List<String>): List<String> {
        return dateArray.indices.map { i ->if (i == 0 || i == dateArray.lastIndex || i == dateArray.size / 3 || i == dateArray.size * 2 / 3) {
            dateArray[i].takeLast(4).let {
                StringBuilder(it).insert(2, ".").toString()
            }
        } else {
            ""
        }
        }
    }

    fun calculateMinValue(caloriesArray: List<Float>): Float {
        val minValue = caloriesArray.minOrNull() ?: return 0f
        return if (minValue - 200 > 0) minValue - 200 else 0f
    }
}