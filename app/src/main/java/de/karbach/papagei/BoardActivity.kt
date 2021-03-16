package de.karbach.papagei

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class BoardActivity: SingleFragmentActivity() {

    override fun createFragment(): Fragment {
        return BoardFragment()
    }

}