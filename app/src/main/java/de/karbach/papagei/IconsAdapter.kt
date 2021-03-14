package de.karbach.papagei

import android.util.ArrayMap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.karbach.papagei.model.Sound
import de.karbach.papagei.model.SoundList

class IconsAdapter(val icons: ArrayMap<String, String>, val clickCallback: ClickCallback?, val activeColor: Int, var iconsColor: Int): RecyclerView.Adapter<IconsAdapter.ViewHolder>() {

    var activeIcon: String? = null

    interface ClickCallback{
        fun clicked(icon: String);
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    inner class ViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        val textView = itemView.findViewById<TextView>(R.id.icon_display)
        var isActive = false

        fun setActiveState(active: Boolean){
            if(active){
                textView.setBackgroundColor(activeColor)
            }
            else{
                textView.setBackgroundColor(0)
            }
            isActive = active
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        // Inflate the custom layout
        val view = inflater.inflate(R.layout.icon_entry, parent, false)
        val iconFont = FontManager.getTypeface(context, FontManager.FONTAWESOME_SOLID)
        val textView = view.findViewById<TextView>(R.id.icon_display)
        FontManager.markAsIconContainer(textView, iconFont)
        // Return a new holder instance
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return icons.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val iconText = if(position < icons.size) icons.valueAt(position) else ""
        holder.textView.text = iconText ?: ""
        holder.textView.setOnClickListener{
            val activeState = !holder.isActive
            clickCallback?.clicked(iconText)
            holder.setActiveState(activeState)
            if(activeState){
                activeIcon = iconText
            }
            else{
                activeIcon = null
            }
        }
        holder.setActiveState(activeIcon == iconText)
        holder.textView.setTextColor(iconsColor)
    }

}