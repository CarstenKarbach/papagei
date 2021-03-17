package de.karbach.papagei.model

import android.content.Context
import de.karbach.papagei.SoundsManager

class Board(var id: Int, var name: String, var filename: String, var active: Boolean = false, val isDefault: Boolean = false) {

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