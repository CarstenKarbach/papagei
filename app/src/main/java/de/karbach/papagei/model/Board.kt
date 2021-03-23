package de.karbach.papagei.model

import android.content.Context
import de.karbach.papagei.SoundsManager

class Board(var id: Int, var name: String, var filename: String, var active: Boolean = false, val isDefault: Boolean = false,
            val visible_tags: ArrayList<String> = ArrayList<String>()) : TagHolder {

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

    public override fun hasTag(tag: String):Boolean{
        return visible_tags.contains(tag)
    }
}