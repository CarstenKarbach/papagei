package de.karbach.papagei

import android.os.Bundle
import androidx.fragment.app.Fragment

class SettingsActivity : SingleFragmentActivity() {
    override fun createFragment(): Fragment {
        return Settings()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}