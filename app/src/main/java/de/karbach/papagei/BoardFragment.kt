package de.karbach.papagei

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import de.karbach.papagei.model.Board

class BoardFragment: Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.title = resources.getString(R.string.board)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val result = inflater.inflate(R.layout.board_details, container, false)

        val bname = result.findViewById<EditText>(R.id.board_name)
        val save = result.findViewById<Button>(R.id.board_save)
        save.setOnClickListener{
            activity?.let {
                val bm = BoardsManager(it)
                val name = bname.text.toString()
                val filename = bname.text.toString().toLowerCase().trim()+"_sounds.json"
                if(name == "" || bm.getBoardByName(name) != null || bm.getBoardByFileName(filename) != null){
                    Toast.makeText(it, "Der Name ist ungültig oder schon in Benutzung.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val board = Board(bm.getNextBoardID(), bname.text.toString(), filename, false)
                bm.addBoard(board)
                Toast.makeText(it, "Neues Board wurde erfolgreich erstellt.", Toast.LENGTH_SHORT).show()
                it.finish()
            }
        }
        val cancel = result.findViewById<Button>(R.id.board_cancel)
        cancel.setOnClickListener {
            activity?.finish()
        }

        return result
    }

}