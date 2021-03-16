package de.karbach.papagei

import android.app.Activity
import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import de.karbach.papagei.model.Sound
import de.karbach.papagei.model.SoundList
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.util.Base64
import android.util.Log
import androidx.core.content.FileProvider
import java.io.*
import java.lang.RuntimeException
import android.content.res.AssetManager
import de.karbach.papagei.utils.StringFileUtils
import kotlin.random.Random


class SoundsManager(val context: Context) {

    val def_sound_folder = "hoemma"

    companion object{
        private var currentList:SoundList?=null

        fun getCurrentSoundListFilename(context: Context): String{
            return BoardsManager.getActiveBoard(context).filename
        }

        fun reloadCurrentList(context: Context){
            val man = SoundsManager(context)
            var res = man.loadList()
            if(res == null){
                res = man.getTestSounds()
                saveAsCurrentList(context, res)
            }
            SoundsManager.currentList = res
        }

        fun getCurrentList(context: Context): SoundList{
            if(SoundsManager.currentList != null){
                return SoundsManager.currentList as SoundList
            }
            reloadCurrentList(context)
            return SoundsManager.currentList as SoundList
        }

        fun saveAsCurrentList(context: Context, soundlist: SoundList? = SoundsManager.currentList){
            SoundsManager.currentList = soundlist
            if(SoundsManager.currentList != null){
                val man = SoundsManager(context)
                man.saveSoundList(SoundsManager.currentList as SoundList)
            }
        }
    }

    /*fun getTestSounds(): SoundList{
        val testSounds = SoundList()
        val testResource1 = "android.resource://"+context.packageName+"/"+R.raw.dice
        val testResource2 = "android.resource://"+context.packageName+"/"+R.raw.glas
        val testResource3 = "android.resource://"+context.packageName+"/"+R.raw.tuer
        val testResource4 = "android.resource://"+context.packageName+"/"+R.raw.hallo
        testSounds.addSound(Sound(actualResourceURI = testResource1, origResourceURI = testResource1, description = "Dices"))
        testSounds.addSound(Sound(actualResourceURI = testResource2, origResourceURI = testResource2, description = "Glass"))
        testSounds.addSound(Sound(actualResourceURI = testResource3, origResourceURI = testResource3, description = "Door"))
        testSounds.addSound(Sound(actualResourceURI = testResource4, origResourceURI = testResource4, description = "Hallo"))

        return testSounds
    }*/

    fun resetToTestSounds(subfolder: String = def_sound_folder){
        val soundlist = getTestSounds(subfolder)
        saveAsCurrentList(context, soundlist)
    }

    fun getTestSounds(subfolder: String = def_sound_folder):SoundList{
        val assetManager = context.assets
        val sounds = assetManager.list(subfolder )
        val soundList = SoundList()
        if(sounds == null){
            return soundList
        }
        val icons = context.resources.getStringArray(R.array.fa_icons)
        for(s:String in sounds){
            val description = s.replace(".wma", "").replace(".mp3", "")
            val uri = "content://de.karbach.papagei.assetprovider/"+subfolder+"/"+Uri.encode(s)
            val sound = Sound(origResourceURI = uri, actualResourceURI = uri, description = description, tags= arrayListOf("1"))
            val randomColor = java.util.Random().nextInt(7)
            val randomIcon = icons[java.util.Random().nextInt(icons.size)]
            val iconOnly = randomIcon.split("|")[1]
            sound.color = randomColor
            sound.icon = iconOnly
            soundList.addSound(sound)
        }
        return soundList
    }

    public fun copyFile(sourceuri: Uri, filename: String, useOutPutStream: FileOutputStream? = null):Boolean {
        var bis: BufferedInputStream? = null
        var bos: BufferedOutputStream? = null

        try {
            val inputStream = context.getContentResolver().openInputStream(sourceuri)
            val outputStream = if(useOutPutStream == null) context.openFileOutput(filename, MODE_PRIVATE) else useOutPutStream
            bis = BufferedInputStream(inputStream)
            bos = BufferedOutputStream(outputStream)
            val buf = ByteArray(1024)
            bis.read(buf)
            do {
                bos.write(buf)
            } while (bis.read(buf) !== -1)
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        } finally {
            try {
                bis?.close()
                bos?.close()
                Log.d("ey", "eyeyey")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    public fun copyFile2(sourceUri:Uri, destination:File):Boolean {
        try {
            val source = File(sourceUri.getPath());
            val src = FileInputStream(source).getChannel();
            val dst = FileOutputStream(destination).getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();
            return true
        } catch (ex: IOException) {
            ex.printStackTrace();
            return false
        }
    }

    fun loadList(filename: String? = null): SoundList?{
        val loadFile = filename ?: getCurrentSoundListFilename(context)
        val jsonString = StringFileUtils.readFromFile(loadFile, context)
        if(jsonString == ""){
            return null
        }
        val loadedJSON = Gson().fromJson(jsonString, SoundList::class.java)
        return loadedJSON
    }

    fun saveSoundList(soundlist:SoundList, filename: String? = null){
        val storeJSON = Gson().toJson(soundlist)
        Log.d("storejson", storeJSON)
        val storeFile = filename ?: getCurrentSoundListFilename(context)
        StringFileUtils.writeToFile(storeJSON, storeFile, context)
    }

    fun shareSound(sound:Sound){
        val loadFileUri = Uri.parse(sound.actualResourceURI)
        var filename = "exported"
        var ending = "mp3"
        if(loadFileUri != null){
            val uriAsString = loadFileUri.toString()
            val pointIndex = uriAsString.lastIndexOf(".")
            if(pointIndex >= 0){
                ending = uriAsString.substring(pointIndex+1)
            }
        }
        if(ending == "audio"){
            ending = "mp3"
        }
        filename = filename+"."+ending
        val file = File(context.getExternalFilesDir(null), filename)
        val stream = file.outputStream()
        val copied = copyFile(loadFileUri, filename, stream)
        if(! copied){
            throw RuntimeException("Could not copy file to "+filename)
        }
        val intent = Intent(Intent.ACTION_SEND)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        intent.type = "audio/*"
        val uri =
            FileProvider.getUriForFile(context, context.packageName+".fileprovider", file)
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        context.startActivity(intent)
    }
}