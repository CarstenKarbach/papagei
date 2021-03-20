package de.karbach.papagei.utils

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.view.children
import androidx.fragment.app.Fragment
import de.karbach.papagei.R
import de.karbach.papagei.SoundsManager
import de.karbach.papagei.model.Board
import de.karbach.papagei.model.TagHolder

public fun Fragment.getCurrentTags(onlyChecked: Boolean) : ArrayList<String>{
    val result = ArrayList<String>()
    view?.let{
        val tags_container = it.findViewById<LinearLayout>(R.id.tags_container)
        for(child in tags_container.children){
            val checkbox = child as CheckBox
            if(!onlyChecked || checkbox.isChecked){
                val tagText = checkbox.text.toString()
                if(! result.contains(tagText)){
                    result.add(tagText)
                }
            }
        }
    }
    return result
}

fun Fragment.initTagAddButton(view: View, inflater: LayoutInflater){
    val tagAddButton = view.findViewById<Button>(R.id.add_tag)
    val tagEditBox = view.findViewById<EditText>(R.id.new_tag_name)
    tagAddButton.setOnClickListener{
        val newTag = tagEditBox.text.trim().toString().toLowerCase()
        if(newTag == ""){
            Toast.makeText(
                    activity as Activity, getString(R.string.tag_not_empty),
                    Toast.LENGTH_SHORT
            ).show()
            return@setOnClickListener
        }
        if(getCurrentTags(false).contains(newTag)){
            Toast.makeText(
                    activity as Activity, getString(R.string.tag_available),
                    Toast.LENGTH_SHORT
            ).show()
            return@setOnClickListener
        }
        val tags_container = view.findViewById<LinearLayout>(R.id.tags_container)
        activity?.let {
            val checkbox = inflater.inflate(R.layout.tag_checkbox, null) as CheckBox
            checkbox.text = newTag
            checkbox.isChecked = true
            tags_container.addView(checkbox)
        }
    }
}

fun Fragment.resetTagsContainer(rootView: View?, tagHolder: TagHolder?, board: Board? = null){
    if(rootView != null){
        val tags_container = rootView.findViewById<LinearLayout>(R.id.tags_container)
        tags_container.removeAllViews()
        activity?.let {

            val tags = SoundsManager.getCurrentList(it).getAllTags(it, board)
            val inflater = LayoutInflater.from(it)
            for(t in tags){
                val checkbox = inflater.inflate(R.layout.tag_checkbox, null) as CheckBox
                checkbox.text = t
                if(tagHolder == null){
                    checkbox.isChecked = true
                }
                else {
                    if (tagHolder?.hasTag(t) == true) {
                        checkbox.isChecked = true
                    }
                }
                tags_container.addView(checkbox)
            }
        }
    }
}