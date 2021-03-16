package de.karbach.papagei

import android.app.Activity
import android.content.Context
import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import de.karbach.papagei.model.Board
import de.karbach.papagei.model.Sound

class BoardsAdapter(val boards: ArrayList<Board>): RecyclerView.Adapter<BoardsAdapter.ViewHolder>() {

    inner class ViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        val nameView = itemView.findViewById<TextView>(R.id.board_name)
        val soundsCountView = itemView.findViewById<TextView>(R.id.sounds_count)
        val activeView = itemView.findViewById<TextView>(R.id.active_icon)

        fun setActive(active: Boolean){
            activeView.visibility = if(active) View.VISIBLE else View.INVISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardsAdapter.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        // Inflate the custom layout
        val contactView = inflater.inflate(R.layout.board_entry, parent, false)
        // Return a new holder instance

        val activeIcon = contactView.findViewById<TextView>(R.id.active_icon)
        val iconFont = FontManager.getTypeface(context, FontManager.FONTAWESOME_SOLID)
        FontManager.markAsIconContainer(activeIcon, iconFont)

        return ViewHolder(contactView)
    }

    override fun getItemCount(): Int {
        return boards.size
    }

    override fun onBindViewHolder(holder: BoardsAdapter.ViewHolder, position: Int) {
        val board = boards[position]
        holder.nameView.text = board.name
        holder.soundsCountView.text = board.soundsCount(holder.soundsCountView.context).toString()
        holder.setActive(board.active)
    }

}