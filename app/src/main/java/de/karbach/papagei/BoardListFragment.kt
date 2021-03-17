package de.karbach.papagei

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class BoardListFragment: Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.title = resources.getString(R.string.manage_boards)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val result = inflater.inflate(R.layout.boards, container, false)
        return result
    }

    override fun onResume() {
        super.onResume()

        view?.let{
            val recycler = it.findViewById<RecyclerView>(R.id.boards)
            registerForContextMenu(recycler)
            activity?.let {
                val dataChangeCallback = object: BoardsAdapter.Callback{
                    override fun onDataChanged() {
                        recycler.adapter?.notifyDataSetChanged()
                    }
                }
                recycler.adapter = BoardsAdapter(BoardsManager.getCurrentBoards(it), dataChangeCallback)
                recycler.layoutManager = LinearLayoutManager(it, LinearLayoutManager.VERTICAL, false)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.boardslist_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_item_add_board -> {
                val intent = Intent(context, BoardActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

}