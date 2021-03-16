package de.karbach.papagei

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.core.content.FileProvider
import com.google.gson.Gson
import de.karbach.papagei.model.Board
import de.karbach.papagei.model.Sound
import de.karbach.papagei.model.SoundList
import de.karbach.papagei.utils.StringFileUtils
import java.io.*
import java.lang.RuntimeException

class BoardsManager (val context: Context) {

    companion object{
        private var currentList: ArrayList<Board>?=null

        fun getCurrentBoards(context: Context): ArrayList<Board> {
            if(currentList != null){
                return currentList as ArrayList<Board>
            }
            val man = BoardsManager(context)
            var res = man.loadList()
            if(res == null){
                res = man.getDefaultBoards()
            }
            currentList = res
            return res as ArrayList<Board>
        }

        fun saveAsCurrentBoards(context: Context, boards: ArrayList<Board>? = currentList){
            currentList = boards
            val toSave = currentList
            toSave?.let{
                val man = BoardsManager(context)
                man.save(it)
            }
        }
    }

    fun resetToDefaultBoard(){
        val boards = getDefaultBoards()
        saveAsCurrentBoards(context, boards)
    }

    fun getDefaultBoards(): ArrayList<Board> {
        return arrayListOf(Board(1, "Hömma", SoundsManager.deffilename, true))
    }

    val deffilename = "boardlist.json"

    fun loadList(): ArrayList<Board>?{
        val jsonString = StringFileUtils.readFromFile(deffilename, context)
        if(jsonString == ""){
            return null
        }
        val loadedJSON = Gson().fromJson(jsonString, ArrayList::class.java)
        return loadedJSON as ArrayList<Board>
    }

    fun save(boardlist: ArrayList<Board>){
        val storeJSON = Gson().toJson(boardlist)
        StringFileUtils.writeToFile(storeJSON, deffilename, context)
    }

    fun activateBoard(board: Board){
        val boards = getCurrentBoards(context)
        for (b in boards) {
            b.active = false
        }
        board.active = true
        saveAsCurrentBoards(context)
    }

    fun getNextBoardID(): Int{
        return (getCurrentBoards(context).map{b -> b.id}.max() ?: 0)+1
    }

    fun addBoard(board: Board){
        getCurrentBoards(context).add(board)
        saveAsCurrentBoards(context)
    }

    fun deleteBoard(board: Board){
        getCurrentBoards(context).remove(board)
        saveAsCurrentBoards(context)
    }

}