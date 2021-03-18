package de.karbach.papagei.model

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.widget.Toast
import de.karbach.papagei.BoardsManager
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class SoundList {
    companion object{
        val defaultTags = arrayListOf("1", "2")
    }

    public val sounds = ArrayList<Sound>()

    public fun getMaxID():Int{
        var currentMax = sounds.maxBy{ it.id }?.id
        if(currentMax == null){
            currentMax = 0
        }
        return currentMax
    }

    public fun addSound(sound: Sound){
        var currentMax = getMaxID()
        sound.id = currentMax+1
        if(sounds.contains(sound)){
            return
        }
        sounds.add(sound)
    }
    public fun removeSound(sound: Sound){
        if(sounds.contains(sound)){
            sounds.remove(sound)

            val fdelete = File(Uri.parse(sound.actualResourceURI).getPath())
            if (fdelete.exists()) {
                fdelete.delete()
            }
        }
    }

    public fun search(searchStr:String):List<Sound>{
        return sounds.filter{ it.matches(searchStr) }.toList()
    }

    public fun getById(id: Int?):Sound?{
        return sounds.filter{ it.id==id }.firstOrNull()
    }

    public fun getDefaultTags():List<String>{
        return defaultTags
    }

    public fun getAllTags(context: Context, board: Board? = null):List<String>{
        val result = ArrayList<String>()
        for(s in sounds){
            for(t in s.getValidTags()){
                if(result.contains(t)){
                    continue
                }
                result.add(t)
            }
        }
        val default_tags = getDefaultTags()
        for(dt in default_tags){
            if(result.contains(dt)){
                continue
            }
            result.add(dt)
        }
        val actualBoard = board ?: BoardsManager.getActiveBoard(context)
        val boardTags = actualBoard.visible_tags
        for(bt in boardTags){
            if(result.contains(bt)){
                continue
            }
            result.add(bt)
        }
        result.sort()
        return result
    }

    public fun filterByTags(tags: Set<String>): SoundList{
        val res = SoundList()
        res.sounds.addAll( sounds.filter{ sound -> tags.any{tag -> sound.hasTag(tag)} } )
        return res
    }
}