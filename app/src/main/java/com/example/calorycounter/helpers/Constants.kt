package com.example.calorycounter.helpers

import androidx.annotation.DrawableRes
import com.example.calorycounter.R

enum class Keys {
    Protein, Calories, Language
}

//ToDo Use as Icon.Ramen.resourceId()
//ToDo Problem is that the resource IDs are probably not consistent, can be obfuscated (only happens wehen adding/removing drawables so far)
//enum class Icon(@DrawableRes val resourceId: Int) {
//    RAMEN(R.drawable.baseline_ramen_dining_24),
//    COFFEE(R.drawable.baseline_coffee_24),
//    DINNER(R.drawable.baseline_dinner_dining_24),
//    COCKTAIL(R.drawable.baseline_local_bar_24),
//    LUNCH(R.drawable.baseline_lunch_dining_24),
//    WINE(R.drawable.baseline_wine_bar_24),
//    BAKED(R.drawable.baseline_bakery_dining_24),
//    WATER(R.drawable.baseline_local_drink_24)
//}

const val MIN_SWIPE_DISTANCE = 250

const val caloriesFile = "calLog.txt"
const val proteinFile = "protLog.txt"
const val languageFile = "language.txt"
const val appLanguageFile = "appLanguage.txt"
const val mealsFile = "meals.txt"
const val goalsFile = "goals.txt"
const val historyFile = "history.txt"
const val iconFile = "icon.txt"
const val themesFile = "theme.txt"

