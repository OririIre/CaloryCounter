package com.example.calorycounter

import android.content.Context
import android.media.AudioMetadata.createMap
import android.util.Log
import java.io.File



class DataHandler (){
    fun saveData (context: Context?, fileName: String, key: String, value: String){
        val directory = File(context?.filesDir, "LogFiles")
        val file = File(directory, fileName)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        if (file.exists()) {
            val out = loadData(context, fileName)
            print("What is loaded in save:${out}")
            if(out.containsKey(key)){
                out[key] = value
            }
            else{
                out += mapOf(key to value)
            }
            file.writeText(out.toString())
        }
        else{
            file.writeText(mutableMapOf(key to value).toString())
        }
    }

    fun saveMapData(context: Context?, fileName: String, outputMap:MutableMap<String, String>) {
        val directory = File(context?.filesDir, "LogFiles")
        val file = File(directory, fileName)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        if (file.exists()) {
            file.writeText(outputMap.toString())
        }
        else{
            file.writeText(outputMap.toString())
        }
    }

    fun loadData (context: Context?, fileName: String): MutableMap<String, String> {
        val directory = File(context?.filesDir, "LogFiles")
        val file = File(directory, fileName)
        var output = mutableMapOf<String,String>()
        if (directory.exists()) {
            if (file.exists()) {
                var contents = file.readText(Charsets.UTF_8)
                contents = contents.replace("{", "")
                contents = contents.replace("}", "")
                contents = contents.replace(" ", "")
                output = contents.split(",")
                    .map { it.split("=") }.associate { it.first().toString() to it.last().toString() }.toMutableMap()
            }
        }
        return output
    }

    fun deleteFiles(context: Context?, fileName: String) {
        val directory = File(context?.filesDir, "LogFiles")
        if (directory.exists()) {
            val file = File(directory, fileName)
            if(file.exists()){file.delete()}
            Log.d("Content", "File was deleted")
        }
    }
}






