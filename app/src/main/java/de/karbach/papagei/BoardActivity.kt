package de.karbach.papagei

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class BoardActivity: SingleFragmentActivity() {

    companion object{
        val EXTRA_BOARD_ID_PARAM = "EXTRA_BOARD_ID_PARAM"
    }

    override fun createFragment(): Fragment {
        val res = BoardFragment()
        res.arguments = Bundle()
        res.arguments?.putInt(EXTRA_BOARD_ID_PARAM, intent.getIntExtra(EXTRA_BOARD_ID_PARAM, -1))
        return res
    }

}