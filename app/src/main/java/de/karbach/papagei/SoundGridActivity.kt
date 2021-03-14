package de.karbach.papagei

import android.os.Bundle
import androidx.fragment.app.Fragment

class SoundGridActivity: SingleFragmentActivity() {

    override fun createFragment(): Fragment {
        val res = SoundGridFragment()
        return res
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //supportActionBar?.setDisplayShowTitleEnabled(false)
    }

}