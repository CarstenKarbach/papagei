package de.karbach.papagei

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.karbach.papagei.utils.initNavigationView
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

    fun notifyDataChanged(){
        view?.let {
            val recycler = it.findViewById<RecyclerView>(R.id.boards)
            recycler?.adapter?.let{
                it.notifyDataSetChanged()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        initNavigationView(R.id.navigation_boards)

        view?.let{
            val recycler = it.findViewById<RecyclerView>(R.id.boards)

            recycler.addItemDecoration(DividerItemDecoration(recycler.getContext(), DividerItemDecoration.VERTICAL))

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.boardslist_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            R.id.menu_item_add_board -> {
                val intent = Intent(context, BoardActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.menu_item_settings -> {
                val intent = Intent(context, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

}