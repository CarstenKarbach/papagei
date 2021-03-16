package de.karbach.papagei

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BoardListFragment: Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val result = inflater.inflate(R.layout.boards, container, false)
        return result
    }

    override fun onResume() {
        super.onResume()

        view?.let{
            val recycler = it.findViewById<RecyclerView>(R.id.boards)
            activity?.let {
                recycler.adapter = BoardsAdapter(BoardsManager.getCurrentBoards(it))
                recycler.layoutManager = LinearLayoutManager(it, LinearLayoutManager.VERTICAL, false)
            }
        }
    }
}