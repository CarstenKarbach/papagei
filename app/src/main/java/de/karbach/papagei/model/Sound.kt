package de.karbach.papagei.model

import android.net.Uri
import de.karbach.papagei.ColorHelper
import java.net.URL

class Sound(var id: Int = 0, var origResourceURI: String, var actualResourceURI: String,
            var description: String, var audioRecord:Boolean=false, var tags: ArrayList<String>? = ArrayList<String>(),
            var icon:String=Sound.defIcon, var color:Int= ColorHelper.defaultColor) : TagHolder {

    companion object {
        const val defIcon = "\uf144"
        const val pauseIcon = "\uf28b"
    }

    public fun matches(searchString:String):Boolean{
        return searchString == null || searchString == "" || description.toLowerCase().indexOf(searchString.toLowerCase()) != -1
    }

    public fun getValidTags(): ArrayList<String>{
        if(tags == null){
            tags = ArrayList<String>()
        }
        return tags as ArrayList<String>
    }

    public override fun hasTag(tag: String):Boolean{
        return getValidTags().contains(tag)
    }

    public fun addTag(tag: String){
        if(this.hasTag(tag)){
            return
        }
        getValidTags().add(tag)
    }

    public fun removeTag(tag: String){
        if(! this.hasTag(tag)){
            return
        }
        getValidTags().remove(tag)
    }
}