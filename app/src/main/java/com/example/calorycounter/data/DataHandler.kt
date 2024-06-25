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

        val data = loadData(context, fileName).toMutableMap()
        data[key] = value
        file.writeText(data.toString())
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
        val directory= File(context?.filesDir, "LogFiles")
        val file = File(directory, fileName)
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val existingData = loadData(context, fileName).toMutableMap()
        existingData.putAll(outputMap)
        file.writeText(existingData.toString())
    }

    fun loadData(context: Context?, fileName: String): MutableMap<String, String> {
        val directory = File(context?.filesDir, "LogFiles")
        val file = File(directory, fileName)
        return if (directory.exists() && file.exists()) {
            file.readText(Charsets.UTF_8)
                .substringAfter("{").substringBeforeLast("}")
                .split(",")
                .map { it.split("=") }
                .associate { it.first().trim() to it.last().trim() }.toMutableMap()
        } else {
            mutableMapOf()
        }
    }

    fun deleteFiles(context: Context?, fileName: String) {
        val directory = File(context?.filesDir, "LogFiles")
        val file = File(directory, fileName)
        if (file.exists()) {
            if (file.delete()) {
                Log.d("Content", "File $fileName was deleted")
            } else {
                Log.e("Content", "Failed to delete file $fileName")
            }
        }
    }

    fun deleteEntriesWithValue(context: Context?, fileName: String, value: String) {
        val directory = File(context?.filesDir, "LogFiles")
        val file = File(directory, fileName)
        if (file.exists()) {
            val loadedMap = loadData(context, fileName).toMutableMap()
            loadedMap.values.removeAll(listOf(value).toSet())
            saveMapData(context, fileName,loadedMap)
        }
    }

    fun deleteMapEntriesWithKeys(context: Context?, fileName: String, key: String) {
        val directory = File(context?.filesDir, "LogFiles")
        val file = File(directory, fileName)
        if (file.exists()) {
            val loadedMap = loadData(context, fileName).toMutableMap()
            loadedMap.remove(key)
            saveMapData(context, fileName, loadedMap)
        }
    }
}






