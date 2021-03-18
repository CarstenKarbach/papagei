package de.karbach.papagei

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.children
import androidx.fragment.app.Fragment
import de.karbach.papagei.model.Board
import de.karbach.papagei.model.SoundList
import de.karbach.papagei.utils.initTagAddButton
import de.karbach.papagei.utils.resetTagsContainer
import kotlinx.android.synthetic.main.board_entry.view.*

class BoardFragment: Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.title = resources.getString(R.string.board)
    }

    fun getBoard(): Board?{
        val boardID = arguments?.getInt(BoardActivity.EXTRA_BOARD_ID_PARAM, -1) ?: -1
        context?.let{
            return BoardsManager(it).getBoardByID(boardID)
        }
        return null
    }

    public fun getCurrentTags(onlyChecked: Boolean) : ArrayList<String>{
        val result = ArrayList<String>()
        view?.let{
            val tags_container = it.findViewById<LinearLayout>(R.id.tags_container)
            for(child in tags_container.children){
                val checkbox = child as CheckBox
                if(!onlyChecked || checkbox.isChecked){
                    val tagText = checkbox.text.toString()
                    if(! result.contains(tagText)){
                        result.add(tagText)
                    }
                }
            }
        }
        return result
    }

    fun loadData(){
        val board = getBoard()
        view?.let {
            resetTagsContainer(view, board)
        }
        if(board == null){
            return
        }
        view?.let{
            val bname = it.findViewById<EditText>(R.id.board_name)
            val active = it.findViewById<CheckBox>(R.id.board_active)
            bname.setText(board.name)
            active.isChecked = board.active
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val result = inflater.inflate(R.layout.board_details, container, false)

        val bname = result.findViewById<EditText>(R.id.board_name)
        val active = result.findViewById<CheckBox>(R.id.board_active)
        val save = result.findViewById<Button>(R.id.board_save)
        save.setOnClickListener{
            activity?.let {
                var board = getBoard()
                val bm = BoardsManager(it)
                var msg = ""
                if(board == null) {
                    val name = bname.text.toString()
                    val filename = bname.text.toString().toLowerCase().trim() + "_sounds.json"
                    if (name == "" || bm.getBoardByName(name) != null || bm.getBoardByFileName(filename) != null) {
                        Toast.makeText(it, "Der Name ist ungültig oder schon in Benutzung.", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    board = Board(bm.getNextBoardID(), bname.text.toString(), filename,
                            false, false, ArrayList<String>())
                    bm.addBoard(board)
                    msg = getString(R.string.new_board_created)
                }
                else{
                    val name = bname.text.toString()
                    val boardWithName = bm.getBoardByName(name)
                    if (name == "" || (boardWithName != null && boardWithName !== board)) {
                        Toast.makeText(it, "Der Name ist ungültig oder schon in Benutzung.", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    if(board.active && !active.isChecked){
                        Toast.makeText(it, getString(R.string.cannot_deactivate), Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    msg = getString(R.string.changes_saved)
                    board.name = name
                }
                board.visible_tags.clear()
                board.visible_tags.addAll(getCurrentTags(true))
                board.active = active.isChecked
                if(board.active){
                    bm.activateBoard(board)
                }
                Toast.makeText(it, msg, Toast.LENGTH_SHORT).show()
                it.finish()
            }
        }
        val cancel = result.findViewById<Button>(R.id.board_cancel)
        cancel.setOnClickListener {
            activity?.finish()
        }

        initTagAddButton(result, inflater)

        return result
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }
}