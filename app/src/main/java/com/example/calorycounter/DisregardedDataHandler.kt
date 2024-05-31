package com.example.calorycounter
// Simply too much at the moment for such a small project.
// Keep it for other projects as example of how to use DataStore.


//private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "Language")
//private object PreferencesKeys {
//    val LANGUAGE = stringPreferencesKey("language")
//    val COUNTRY = stringPreferencesKey("country")
//}
//private lateinit  var appContext: Context

class DisregardedDataHandler {

//    suspend fun saveLanguage(context: Context, selectedLanguage: String) {
//        println("Here$selectedLanguage")
//        context.dataStore.edit { language ->
//            language[PreferencesKeys.COUNTRY] = selectedLanguage
//        }
//    }
//
//    fun loadLanguage(context: Context): Flow<String> {
//        val stringFlow: Flow<String> = context.dataStore.data.map { language ->
//            language[PreferencesKeys.COUNTRY] ?: ""
//        }
//
//        return stringFlow
//    }
}

//
//lifecycleScope.launch(Dispatchers.IO) {
//    dataHandler.saveLanguage(requireContext(), selectedLanguage)
//}
//
//val lang = dataHandler.loadLanguage(requireContext())
//lifecycleScope.launch(Dispatchers.IO) {
//    try {
//        lang.collect { value ->
//            println("Received $value")
//        }
//    } catch (e: Exception) {
//        println("The flow has thrown an exception: $e")
//    }
//}




