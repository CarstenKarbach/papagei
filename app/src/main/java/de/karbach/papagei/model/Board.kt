package de.karbach.papagei.model

import android.content.Context
import de.karbach.papagei.SoundsManager

class Board(val id: Int, var name: String, val filename: String, var active: Boolean = false) {

    fun soundsCount(context: Context): Int{
        if(active){
            return SoundsManager.getCurrentList(context).sounds.size
        }
        val soundMan = SoundsManager(context)
        val soundList = soundMan.loadList(filename)
        if(soundList == null){
            return 0
        }
        return soundList.sounds.size
    }

}