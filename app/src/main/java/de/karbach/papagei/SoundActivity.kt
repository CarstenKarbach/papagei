package de.karbach.papagei

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment

class SoundActivity: SingleFragmentActivity() {
    companion object{
        val EXTRA_SOUND_PARAM = "EXTRA_SOUND_PARAM"
        val SOUND_IMPORT_URI = "SOUND_IMPORT_URI"
    }

    override fun createFragment(): Fragment {
        val res = SoundFragment()
        res.arguments = Bundle()
        res.arguments?.putInt(EXTRA_SOUND_PARAM, intent.getIntExtra(EXTRA_SOUND_PARAM, -1))

        if(intent?.action == Intent.ACTION_SEND) {
            (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
                res.arguments?.putParcelable(SOUND_IMPORT_URI, it)
            }
        }

        return res
    }

}