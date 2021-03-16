package de.karbach.papagei

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class BoardListActivity: SingleFragmentActivity() {

    override fun createFragment(): Fragment {
        return BoardListFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*if(intent?.getBooleanExtra(SEARCH_REQUEST, false) == true){
            this.onSearchRequested()
            //For importing a file
        }*/
    }

}