package de.karbach.papagei

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat

class ColorHelper {

    companion object{
        val defaultColor = 6
    }

    val nameToColorId = mapOf<Int, Int>(
        0 to R.color.red,
        1 to R.color.orange,
        2 to R.color.yellow,
        3 to R.color.green,
        4 to R.color.blue,
        5 to R.color.magenta,
        6 to R.color.black
    )

    fun nameToColor(name: Int?): Int{
        if(nameToColorId.containsKey(name)){
            return nameToColorId.get(name) ?: R.color.black
        }
        return R.color.black
    }

}