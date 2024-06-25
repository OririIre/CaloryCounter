package com.example.calorycounter.helpers

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import com.example.calorycounter.R

class IconAdapter(con: Context, viewID: Int, objects: ArrayList<Int>): ArrayAdapter<Int> (con, viewID, objects) {
    private val inflater = LayoutInflater.from(con)

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        // TODO Auto-generated method stub
        return getCustomView(position, parent)
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // TODO Auto-generated method stub
        return getCustomView(position, parent)
    }

    private fun getCustomView(position: Int, parent: ViewGroup): View {

        val row: View = inflater.inflate(R.layout.row, parent, false)
        val icon: ImageView = row.findViewById(R.id.icon)

        when (position) {
            0 -> {
                icon.setImageResource(R.drawable.baseline_ramen_dining_24)
            }
            1 -> {
                icon.setImageResource(R.drawable.baseline_coffee_24)
            }
            2 -> {
                icon.setImageResource(R.drawable.baseline_dinner_dining_24)
            }
            3 -> {
                icon.setImageResource(R.drawable.baseline_local_bar_24)
            }
            4 -> {
                icon.setImageResource(R.drawable.baseline_lunch_dining_24)
            }
            5 -> {
                icon.setImageResource(R.drawable.baseline_wine_bar_24)
            }
            6 -> {
                icon.setImageResource(R.drawable.baseline_bakery_dining_24)
            }
            7 -> {
                icon.setImageResource(R.drawable.baseline_local_pizza_24)
            }
        }
        return row
    }
}







