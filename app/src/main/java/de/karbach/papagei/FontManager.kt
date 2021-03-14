package de.karbach.papagei

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.TextView


object FontManager {
    const val ROOT = "fafonts/webfonts/"
    const val FONTAWESOME_REGULAR = ROOT + "fa-regular-400.ttf"
    const val FONTAWESOME_SOLID = ROOT + "fa-solid-900.ttf"
    fun getTypeface(context: Context, font: String?): Typeface {
        return Typeface.createFromAsset(context.getAssets(), font)
    }

    fun markAsIconContainer(v: View, typeface: Typeface?) {
        if (v is ViewGroup) {
            val vg = v as ViewGroup
            for (i in 0 until vg.childCount) {
                val child: View = vg.getChildAt(i)
                markAsIconContainer(child, typeface)
            }
        } else if (v is TextView) {
            (v as TextView).setTypeface(typeface)
        } else if (v is AutoCompleteTextView) {
            (v as AutoCompleteTextView).setTypeface(typeface)
        }
    }
}