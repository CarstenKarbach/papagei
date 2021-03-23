package de.karbach.papagei

import android.content.Intent
import android.view.*
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.recyclerview.widget.RecyclerView
import de.karbach.papagei.model.Sound
import de.karbach.papagei.model.SoundList

class SoundGridAdapter(val soundList: SoundList, val soundClickCallback: SoundClickCallback?, val activeColor: Int): RecyclerView.Adapter<SoundGridAdapter.ViewHolder>() {

    var playingSound: Sound? = null

    interface SoundClickCallback{
        fun soundClicked(sound: Sound)
        fun deleteSound(sound: Sound)
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    inner class ViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        val textView = itemView.findViewById<TextView>(R.id.short_text)
        val frame = itemView.findViewById<View>(R.id.sound_item)
        val icon = itemView.findViewById<TextView>(R.id.icon)
        var sound: Sound? = null

        fun setPlayingState(active: Boolean){
            if(active){
                frame.setBackgroundColor(activeColor)
                icon.text = Sound.pauseIcon
            }
            else{
                frame.setBackgroundColor(0)
                icon.text = sound?.icon ?: Sound.defIcon
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        // Inflate the custom layout
        val contactView = inflater.inflate(R.layout.small_sound_item, parent, false)
        // Return a new holder instance

        val soundIconDisplay = contactView.findViewById<TextView>(R.id.icon)
        val iconFont = FontManager.getTypeface(context, FontManager.FONTAWESOME_SOLID)
        FontManager.markAsIconContainer(soundIconDisplay, iconFont)

        return ViewHolder(contactView)
    }

    override fun getItemCount(): Int {
        return soundList.sounds.size
    }

    private val mBoundViewHolders: HashSet<ViewHolder> = HashSet()

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        mBoundViewHolders.remove(holder)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        mBoundViewHolders.add(holder)
        val sound = if(position < soundList.sounds.size) soundList.sounds.get(position) else null
        holder.textView.text = sound?.description ?: ""
        holder.frame.setOnClickListener{
            sound?.let{ soundClickCallback?.soundClicked(sound) }
        }
        holder.sound = sound
        val playingState = sound != null && sound.id == playingSound?.id
        holder.setPlayingState(playingState)

        val color = sound?.color ?: ColorHelper.defaultColor
        val resolvedColor = ContextCompat.getColor(holder.icon.context, ColorHelper().nameToColor(color))
        holder.icon.setTextColor(resolvedColor)

        holder.frame.setOnCreateContextMenuListener(object : View.OnCreateContextMenuListener {
            override fun onCreateContextMenu(
                    menu: ContextMenu?,
                    v: View?,
                    menuInfo: ContextMenu.ContextMenuInfo?
            ) {
                MenuInflater(holder.frame.context).inflate(R.menu.sound_grid_context, menu)
                menu?.forEach {
                    it.setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.menu_item_sound_edit -> {
                                sound?.let {
                                    val i = Intent(holder.frame.context, SoundActivity::class.java)
                                    i.putExtra(SoundActivity.EXTRA_SOUND_PARAM, sound.id)
                                    holder.frame.context.startActivity(i)
                                    return@setOnMenuItemClickListener true
                                }
                                return@setOnMenuItemClickListener false
                            }
                            R.id.menu_item_sound_delete -> {
                                sound?.let {
                                    soundClickCallback?.deleteSound(it)
                                    return@setOnMenuItemClickListener true
                                }
                                return@setOnMenuItemClickListener false
                            }
                            R.id.menu_item_sound_share -> {
                                sound?.let {
                                    val soundMan = SoundsManager(holder.frame.context)
                                    try {
                                        soundMan.shareSound(it)
                                    } catch (e: Exception) {
                                    }
                                    return@setOnMenuItemClickListener true
                                }
                                return@setOnMenuItemClickListener false
                            }
                            else -> {
                                return@setOnMenuItemClickListener false
                            }
                        }
                    }
                }
            }
        })
    }

    fun clearViewStates(recyclerView: RecyclerView, activateForSound: Sound? = null){
        for (viewHolder in mBoundViewHolders) {
            val currentSound = viewHolder.sound
            val playingState = currentSound != null && currentSound.id == activateForSound?.id
            viewHolder.setPlayingState(playingState)
        }
    }
}