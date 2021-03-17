package de.karbach.papagei

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.opengl.Visibility
import android.view.*
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import de.karbach.papagei.model.Board
import de.karbach.papagei.model.Sound

class BoardsAdapter(val boards: ArrayList<Board>, val callback: Callback? = null): RecyclerView.Adapter<BoardsAdapter.ViewHolder>() {

    interface Callback{
        fun onDataChanged()
    }

    inner class ViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        val nameView = itemView.findViewById<TextView>(R.id.board_name)
        val soundsCountView = itemView.findViewById<TextView>(R.id.sounds_count)
        val activeView = itemView.findViewById<TextView>(R.id.active_icon)
        val frame = itemView.findViewById<View>(R.id.board_entry)

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
        addContextMenu(holder, board)
    }

    fun addContextMenu(holder: BoardsAdapter.ViewHolder, board: Board){
        holder.frame.setOnCreateContextMenuListener(object: View.OnCreateContextMenuListener{
            override fun onCreateContextMenu(
                    menu: ContextMenu?,
                    v: View?,
                    menuInfo: ContextMenu.ContextMenuInfo?
            ) {
                MenuInflater(holder.frame.context).inflate(R.menu.boards_menu, menu)
                menu?.forEach {
                    it.setOnMenuItemClickListener {
                        when (it.itemId) {
                                R.id.menu_item_activate -> {
                                    BoardsManager(holder.frame.context).activateBoard(board)
                                    callback?.onDataChanged()
                                    return@setOnMenuItemClickListener true
                                }
                                R.id.menu_item_delete -> {
                                    if(board.active){
                                        Toast.makeText(holder.frame.context, holder.frame.context.getString(R.string.active_board_cannot_be_deleted), Toast.LENGTH_SHORT).show()
                                        return@setOnMenuItemClickListener true
                                    }
                                    BoardsManager(holder.frame.context).deleteBoard(board)
                                    callback?.onDataChanged()
                                    return@setOnMenuItemClickListener true
                                }
                                R.id.menu_item_export -> {
                                    BoardsManager(holder.frame.context).exportBoard(board)
                                    return@setOnMenuItemClickListener true
                                }
                                else -> {return@setOnMenuItemClickListener false}
                        }
                    }
                }
            }
        })
    }

}