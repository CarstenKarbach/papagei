package de.karbach.papagei

import android.os.Bundle
import androidx.fragment.app.Fragment

class SoundActivity: SingleFragmentActivity() {
    companion object{
        val EXTRA_SOUND_PARAM = "EXTRA_SOUND_PARAM"
    }

    override fun createFragment(): Fragment {
        val res = SoundFragment()
        res.arguments = Bundle()
        res.arguments?.putInt(EXTRA_SOUND_PARAM, intent.getIntExtra(EXTRA_SOUND_PARAM, -1))
        return res
    }
}