package de.karbach.papagei.utils

import android.content.Intent
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import de.karbach.papagei.BoardListActivity
import de.karbach.papagei.R
import de.karbach.papagei.SoundGridActivity
import de.karbach.papagei.SoundListActivity

fun Fragment.initNavigationView(selected: Int){
    view?.let {
        val bottomNavigation = it.findViewById<BottomNavigationView>(R.id.navbar)
        bottomNavigation.setOnNavigationItemSelectedListener(null)
        bottomNavigation.setSelectedItemId(selected)
        bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_boards -> {
                    val intent = Intent(context, BoardListActivity::class.java)
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_sound_grid -> {
                    val intent = Intent(context, SoundGridActivity::class.java)
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_sound_list -> {
                    val intent = Intent(context, SoundListActivity::class.java)
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false
        }
    }
}