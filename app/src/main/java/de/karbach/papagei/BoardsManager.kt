package de.karbach.papagei

import android.R
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import de.karbach.papagei.model.Board
import de.karbach.papagei.model.BoardExport
import de.karbach.papagei.model.SoundList
import de.karbach.papagei.utils.StringFileUtils
import java.io.*


class BoardsManager(val context: Context) {

    companion object{
        private var currentList: ArrayList<Board>?=null

        fun clearAllBoards(context: Context){
            val bm = BoardsManager(context)
            currentList?.let{
                while(! it.isEmpty()){
                    val board = it.last()
                    bm.deleteBoard(board)
                }
            }
            currentList = bm.getDefaultBoards()
            currentList?.let {
                val defboard = it.last()

                val tags = SoundList().getDefaultTags().toSet()
                defboard.visible_tags.clear()
                defboard.visible_tags.addAll(tags)

                bm.activateBoard(defboard)
            }
            saveAsCurrentBoards(context)
        }

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

        fun getActiveBoard(context: Context): Board{
            return getCurrentBoards(context).filter{ b -> b.active}.first()
        }

    }

    fun resetToDefaultBoard(){
        val boards = getDefaultBoards()
        saveAsCurrentBoards(context, boards)
    }

    fun getDefaultBoards(): ArrayList<Board> {
        return arrayListOf(Board(1, "Hömma", "soundlist.json", true, true, ArrayList<String>()))
    }

    val deffilename = "boardlist.json"

    fun loadList(): ArrayList<Board>?{
        val jsonString = StringFileUtils.readFromFile(deffilename, context)
        if(jsonString == ""){
            return null
        }
        val loadedJSON = Gson().fromJson(jsonString, Array<Board>::class.java)
        val result = ArrayList<Board>()
        result.addAll(loadedJSON)
        return result
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

        SoundsManager.reloadCurrentList(context)
    }

    fun getNextBoardID(): Int{
        return (getCurrentBoards(context).map{ b -> b.id}.max() ?: 0)+1
    }

    fun addBoard(board: Board){
        getCurrentBoards(context).add(board)
        saveAsCurrentBoards(context)
    }

    fun deleteBoard(board: Board){
        val soundsList = SoundsManager(context).loadList(board.filename)
        soundsList?.let {
            for (s in it.sounds) {
                if(s.actualResourceURI.startsWith("file:/")){
                    val uri = Uri.parse(s.actualResourceURI)
                    val file = uri.toFile()
                    val res = file.delete()
                }
            }
        }
        getCurrentBoards(context).remove(board)
        context.deleteFile(board.filename)
        saveAsCurrentBoards(context)
    }

    fun getExportedBoard(board: Board): BoardExport{
        val soundsList = SoundsManager(context).loadList(board.filename) ?: SoundList()
        val soundToBase64 = HashMap<Int, String>()
        for(sound in soundsList.sounds){
            val uri = Uri.parse(sound.actualResourceURI)
            val base64Encoded = StringFileUtils.readAudioFileToBase64(uri, context)
            base64Encoded?.let{
                soundToBase64[sound.id] = base64Encoded
            }
        }
        return BoardExport(board, soundsList, soundToBase64)
    }

    fun getExportName(board: Board): String{
        return board.name + ".hoemma"
    }

    fun storeExportToFile(boardExport: BoardExport){
        val exportName = getExportName(boardExport.board)
        val file = StringFileUtils.getExternalFile(exportName, context)
        file.delete()
        val storeJSON = Gson().toJson(boardExport)
        StringFileUtils.writeToFile(storeJSON, exportName, context, true)
    }

    fun startIntentForFileShare(board: Board){
        val exportName = getExportName(board)
        val file = StringFileUtils.getExternalFile(exportName, context)
        val intent = Intent(Intent.ACTION_SEND)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        intent.type = "application/json"
        val uri =
                FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        context.startActivity(intent)
    }

    fun exportBoard(board: Board){
        val boardExport = getExportedBoard(board)
        storeExportToFile(boardExport)
        startIntentForFileShare(board)
    }


    fun getBoardByName(name: String): Board?{
        val checkName = name.toLowerCase().trim()
        return getCurrentBoards(context).filter{ b -> b.name == checkName}.firstOrNull()
    }

    fun getBoardByFileName(filename: String): Board?{
        return getCurrentBoards(context).filter{ b -> b.filename == filename}.firstOrNull()
    }

    fun getBoardByID(id: Int): Board?{
        return getCurrentBoards(context).filter{ b -> b.id == id}.firstOrNull()
    }

    fun importBoard(uri: Uri, boardName: String){
        val inputStream = InputStreamReader(context.getContentResolver().openInputStream(uri))
        val boardExport = Gson().fromJson(inputStream, BoardExport::class.java)
        if(boardExport != null){
            val board = boardExport.board
            board.name = boardName
            val soundslist = boardExport.soundlist
            board.id = getNextBoardID()
            board.filename = "imported_board_"+board.id+".json"
            addBoard(board)
            activateBoard(board)
            for((id, fileBase64Str) in boardExport.soundIdToBase64File){
                val sound = soundslist.getById(id)
                sound?.let {
                    val soundFileName = "sound_" + board.id + "_" + id + ".audio"
                    val uri = StringFileUtils.writeBase64ToFile(context, soundFileName, fileBase64Str)
                    sound.actualResourceURI = uri.toString()
                    sound.origResourceURI = uri.toString()
                }
            }
            SoundsManager.saveAsCurrentList(context, soundslist)
        }
    }
}