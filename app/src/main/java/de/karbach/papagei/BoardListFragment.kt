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
                recycler.adapter = BoardsAdapter(BoardsManager.getCurrentBoards(it))
                recycler.layoutManager = LinearLayoutManager(it, LinearLayoutManager.VERTICAL, false)
            }
        }
    }

}