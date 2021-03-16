package de.karbach.papagei.utils

import android.content.Context
import android.util.Log
import java.io.*

object StringFileUtils {
    fun readFromFile(filename: String, context: Context): String {
        var ret = ""
        try {
            val inputStream = context.openFileInput(filename)

            if (inputStream != null) {
                val inputStreamReader = InputStreamReader(inputStream)
                val bufferedReader = BufferedReader(inputStreamReader)
                val allText = bufferedReader.use(BufferedReader::readText)
                ret = allText
            }
        } catch (e: FileNotFoundException) {
            Log.e("readFromFile", "File not found: " + e.toString())
        } catch (e: IOException) {
            Log.e("readFromFile", "Can not read file: $e")
        }
        return ret
    }

    fun writeToFile(data: String, filename: String, context: Context) {
        try {
            val outputStreamWriter =
                    OutputStreamWriter(context.openFileOutput(filename, Context.MODE_PRIVATE))
            outputStreamWriter.write(data)
            outputStreamWriter.close()
        } catch (e: IOException) {
            Log.e("Exception", "File write failed: " + e.toString())
        }
    }
}