package de.karbach.papagei

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class SoundListActivity: SingleFragmentActivity() {
    companion object {
        val SEARCH_REQUEST = "SoundListActivity.SEARCH_REQUEST"
        val DEFAULT_SOUND_SELECT = "SoundListActivity.DEFAULT_SOUND_SELECT"
        val SOUND_SELECT_RESULT = "SoundListActivity.SOUND_SELECT_RESULT"
    }

    override fun createFragment(): Fragment {
        val res = SoundListFragment()
        res.arguments = Bundle()
        res.arguments?.putBoolean(DEFAULT_SOUND_SELECT, intent.getBooleanExtra(DEFAULT_SOUND_SELECT, false))
        return res
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(intent?.getBooleanExtra(SEARCH_REQUEST, false) == true){
            this.onSearchRequested()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if(fragment == null){
            return
        }
        val soundlistfragment = fragment as SoundListFragment
        if(Intent.ACTION_SEARCH.equals(intent?.action)){
            val searchStr = if(intent?.getStringExtra(SearchManager.QUERY) != null) intent?.getStringExtra(SearchManager.QUERY) else ""
            soundlistfragment.search(searchStr)
        }
    }
}