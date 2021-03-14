package de.karbach.papagei

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import de.karbach.papagei.model.Sound
import de.karbach.papagei.model.SoundList

class SoundListAdapter(soundlist: List<Sound>, val activity: Activity, val fragment:SoundListFragment): ArrayAdapter<Sound>(activity, 0, soundlist) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = super.getItem(position)
        var itemView = convertView;
        if (itemView == null) {
            itemView = activity.layoutInflater.inflate(R.layout.sound_item, parent, false)
        }
        var realView = itemView as View
        if(item == null){
            return realView
        }
        realView.findViewById<ImageView>(R.id.edit_button).apply{
            setOnClickListener{
                val i = Intent(activity,SoundActivity::class.java)
                i.putExtra(SoundActivity.EXTRA_SOUND_PARAM, item.id)
                activity.startActivity(i)
            }
        }
        realView.findViewById<TextView>(R.id.description).apply{ text=item.description }

        val frameitem = realView.findViewById<ConstraintLayout>(R.id.sound_item)
        val icon = realView.findViewById<ImageView>(R.id.play_icon)

        if(fragment.playingSound?.id == item.id){
            val color = ContextCompat.getColor(activity as Context, R.color.colorActive)
            frameitem.setBackgroundColor(color)
            val icon = realView.findViewById<ImageView>(R.id.play_icon)
            if(fragment.mediaPlayer.isPlaying) {
                icon.setImageResource(android.R.drawable.ic_media_pause)
            }
            else{
                icon.setImageResource(android.R.drawable.ic_media_play)
            }
        }
        else{
            frameitem.setBackgroundColor(0)
            icon.setImageResource(android.R.drawable.ic_media_play)
        }

        val soundIconDisplay = realView.findViewById<TextView>(R.id.icon)
        val iconFont = FontManager.getTypeface(context, FontManager.FONTAWESOME_SOLID)
        FontManager.markAsIconContainer(soundIconDisplay, iconFont)
        soundIconDisplay.text = item?.icon ?: Sound.defIcon

        val resolvedColor = ContextCompat.getColor(context, ColorHelper().nameToColor(item?.color))
        soundIconDisplay.setTextColor(resolvedColor)

        realView.setTag(item.id)
        return realView
    }
}