package de.karbach.papagei

import android.os.Bundle
import androidx.fragment.app.Fragment

class IconSelectionActivity: SingleFragmentActivity() {
    companion object{
        val EXTRA_PRESELECTED = "PRESELECTED"
        val EXTRA_COLOR = "ICON_COLOR"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(getString(R.string.select_icon))
    }

    override fun createFragment(): Fragment {
        val res = IconSelectionFragment()
        val preselected = intent.getStringExtra(IconSelectionActivity.EXTRA_PRESELECTED)
        if(preselected != null){
            res.preselected = preselected
        }
        val color = intent.getIntExtra(IconSelectionActivity.EXTRA_COLOR, ColorHelper.defaultColor)
        res.color = color
        return res
    }
}