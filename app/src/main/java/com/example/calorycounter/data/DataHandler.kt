package com.example.calorycounter.data

import android.content.Context
import android.util.Log
import java.io.File


class DataHandler {
    fun saveData(context: Context?, fileName: String, key: String, value: String) {
        val directory = File(context?.filesDir, "LogFiles")
        val file = File(directory, fileName)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        if (file.exists()) {
            val out = loadData(context, fileName)
            if (out.containsKey(key)) {
                out[key] = value
            } else {
                out += mapOf(key to value)
            }
            file.writeText(out.toString())
        } else {
            file.writeText(mutableMapOf(key to value).toString())
        }
    }

    fun saveMapData(context: Context?, fileName: String, outputMap: MutableMap<String, String>) {
        val directory = File(context?.filesDir, "LogFiles")
        val file = File(directory, fileName)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        if(outputMap.isEmpty()) {
            file.delete()
        }
        else{
            file.writeText(outputMap.toString())
        }
    }

    fun saveMapDataNO(context: Context?, fileName: String, outputMap: MutableMap<String, String>) {
        val directory = File(context?.filesDir, "LogFiles")
        val file = File(directory, fileName)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        if (file.exists()) {
            val loadedMap = loadData(context, fileName)
            val out = loadedMap + outputMap
            file.writeText(out.toString())
        } else {
            file.writeText(outputMap.toString())
        }
    }

    fun loadData(context: Context?, fileName: String): MutableMap<String, String> {
        val directory = File(context?.filesDir, "LogFiles")
        val file = File(directory, fileName)
        var output = mutableMapOf<String, String>()
        if (directory.exists()) {
            if (file.exists()) {
                var contents = file.readText(Charsets.UTF_8)
//                println(contents)
                contents = contents.replace("{", "")
                contents = contents.replace("}", "")
                contents = contents.replace(" ", "")
                output = contents.split(",")
                    .map { it.split("=") }
                    .associate { it.first().toString() to it.last().toString() }.toMutableMap()
            }
        }
        return output
    }

    fun deleteFiles(context: Context?, fileName: String) {
        val directory = File(context?.filesDir, "LogFiles")
        if (directory.exists()) {
            val file = File(directory, fileName)
            if (file.exists()) {
                file.delete()
            }
            Log.d("Content", "File was deleted")
        }
    }

    fun deleteEntriesWithValue(context: Context?, fileName: String, value: String) {
        val directory = File(context?.filesDir, "LogFiles")
        if (directory.exists()) {
            val file = File(directory, fileName)
            if (file.exists()) {
                val loadedMap = loadData(context, fileName)
                if(loadedMap.containsValue(value)){
                    loadedMap.remove(loadedMap.filterValues { it == value }.keys.first())
                }
                saveMapData(context,fileName, loadedMap)
            }
        }
    }

    fun deleteMapEntriesWithKeys(context: Context?, fileName: String, key: String) {
        val directory = File(context?.filesDir, "LogFiles")
        if (directory.exists()) {
            val file = File(directory, fileName)
            if (file.exists()) {
                val loadedMap = loadData(context, fileName)
                if(loadedMap.containsKey(key)){
                    loadedMap.remove(loadedMap.filterKeys { it == key }.keys.first())
                }
                saveMapData(context,fileName, loadedMap)
            }
        }
    }
}






