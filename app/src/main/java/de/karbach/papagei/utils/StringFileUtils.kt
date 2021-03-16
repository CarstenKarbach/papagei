package de.karbach.papagei.utils

import android.content.Context
import android.net.Uri
import android.util.Base64
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

    fun getExternalFile(filename: String, context: Context): File{
        return File(context.getExternalFilesDir(null), filename)
    }

    fun writeToFile(data: String, filename: String, context: Context, external: Boolean = false) {
        try {
            val outputStreamWriter =
                    if(external)
                            OutputStreamWriter(getExternalFile(filename, context).outputStream())
                    else
                        OutputStreamWriter(context.openFileOutput(filename, Context.MODE_PRIVATE))
            outputStreamWriter.write(data)
            outputStreamWriter.close()
        } catch (e: IOException) {
            Log.e("Exception", "File write failed: " + e.toString())
        }
    }

    fun readAudioFileToBase64(uri: Uri, context: Context): String?{
        Log.d("Base64-prestart", uri.toString())
        val inputStream = context.getContentResolver().openInputStream(uri)
        if(inputStream == null){
            return null
        }
        val buffer = ByteArrayOutputStream()
        val data = ByteArray(16384)
        var nRead: Int = inputStream.read(data, 0, data.size)

        while (nRead != -1) {
            buffer.write(data, 0, nRead)
            nRead = inputStream.read(data, 0, data.size)
        }

        val ba= buffer.toByteArray()
        val res = Base64.encodeToString(ba, Base64.DEFAULT)

        return res
    }

    fun writeBase64ToFile(context: Context, filename: String, base64Content: String){
        val outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE)
        val bos = BufferedOutputStream(outputStream)
        val buf = ByteArray(1024)
        val bas = Base64.decode(base64Content, Base64.DEFAULT)
        bos.write(bas)
        bos.close()
    }
}