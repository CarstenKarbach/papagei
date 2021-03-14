package de.karbach.papagei

import android.Manifest
import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.ListFragment
import de.karbach.papagei.model.Sound
import de.karbach.papagei.model.SoundList
import androidx.core.app.ActivityCompat
import android.content.DialogInterface
import android.graphics.Color
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import android.widget.AdapterView.AdapterContextMenuInfo
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.get
import androidx.core.view.iterator
import androidx.core.view.size
import java.io.File
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

/**
 * List of all sounds independent of tags
 */
class SoundListFragment: ListFragment() {

    var displaySounds:List<Sound> = ArrayList<Sound>()
    val mediaPlayer:MediaPlayer = MediaPlayer()
    var permissionSoundCache:Sound? = null
    var searchStr = ""
    var playingSound:Sound? = null

    var COLOR_ACTIVE:Int? = null

    val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:Int = 12377

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        setHasOptionsMenu(true)

        COLOR_ACTIVE = ContextCompat.getColor(activity as Context, R.color.colorActive)
    }

    public fun getCurrentSoundList():SoundList{
        return SoundsManager.getCurrentList(activity as Context)
    }

    private fun notifyAdapterOnDataChanged(){
        listAdapter = SoundListAdapter(displaySounds, activity as FragmentActivity, this)
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        activity?.menuInflater?.inflate(R.menu.sound_context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        super.onContextItemSelected(item)
        val info = item?.menuInfo as AdapterContextMenuInfo
        val sound = displaySounds.get(info.position)
        when(item?.itemId){
            R.id.menu_item_sound_delete -> {
                if (sound != null) {
                    getCurrentSoundList().removeSound(sound)

                    Toast.makeText(
                        activity as Activity, getString(R.string.deleted_sound),
                        Toast.LENGTH_SHORT
                    ).show()

                    SoundsManager.saveAsCurrentList(activity as Context)
                    updateDisplayedItems()
                    return true
                }
            }
            R.id.menu_item_sound_share -> {
                val soundMan = SoundsManager(activity as Context)
                try {
                    soundMan.shareSound(sound)
                }
                catch(e:Exception){
                }
            }
        }

        return false
    }

    fun askForExternalPermission(sound: Sound): Boolean{
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            if (checkSelfPermission(activity as Context, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    val builder = AlertDialog.Builder(activity as Activity)
                    builder.setMessage(getString(R.string.need_external))
                    builder.setTitle(getString(R.string.please_grant))
                    builder.setPositiveButton(getString(R.string.ok),
                        DialogInterface.OnClickListener { dialogInterface, i ->
                            this@SoundListFragment.requestPermissions(
                                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
                            )
                        })
                    builder.setNeutralButton(getString(R.string.cancel), null)
                    val dialog = builder.create()
                    dialog.show()
                }
                else {
                    requestPermissions(
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
                    );
                }

                return checkSelfPermission(activity as Context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            }
            else{
                return true
            }
        }
        else{
            return true
        }
    }

    override fun onResume() {
        super.onResume()

        updateDisplayedItems()
        registerForContextMenu(listView)
    }

    override fun onPause() {
        super.onPause()

        mediaPlayer.stop()
        playingSound = null
    }

    override fun onDestroy() {
        super.onDestroy()

        mediaPlayer.stop()
        mediaPlayer.release()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(permissionSoundCache != null) {
                        playSound(permissionSoundCache as Sound)
                    }
                }
            }
        }
    }

    fun resetIcons(){
        for(v:View in listView){
            val icon = v.findViewById<ImageView>(R.id.play_icon)
            icon.setImageResource(android.R.drawable.ic_media_play)
            val item = v.findViewById<ConstraintLayout>(R.id.sound_item)
            item.setBackgroundColor(0)
        }
    }

    fun setIconForSound(sound:Sound, resId:Int, color:Int?){
        val childPos = displaySounds.indexOf(sound)
        if(color != null && color != 0 && childPos >= 0 && childPos< displaySounds.size) {
            listView.smoothScrollToPosition(childPos)
        }
        val view= listView.findViewWithTag<View>(sound.id)
        if(view == null){
            return
        }
        val icon = view.findViewById<ImageView>(R.id.play_icon)
        icon.setImageResource(resId)
        val item = view.findViewById<ConstraintLayout>(R.id.sound_item)
        if(color != null) {
            item.setBackgroundColor(color)
        }
    }

    fun playSound(sound:Sound){
        resetIcons()

        mediaPlayer.stop()
        mediaPlayer.reset()
        mediaPlayer.setDataSource(activity as Context, Uri.parse(sound.actualResourceURI) )
        mediaPlayer.prepare()

        mediaPlayer.setOnCompletionListener {
            setIconForSound(sound, android.R.drawable.ic_media_play, 0)
            playingSound = null
        }

        mediaPlayer.start()
        playingSound = sound
        setIconForSound(sound, android.R.drawable.ic_media_pause, COLOR_ACTIVE)
    }

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        super.onListItemClick(l, v, position, id)
        val item = displaySounds.get(position)

        if(playingSound?.id == item.id){
            if(mediaPlayer.isPlaying){
                mediaPlayer.pause()
                setIconForSound(item, android.R.drawable.ic_media_play, COLOR_ACTIVE)
            }
            else{
                mediaPlayer.start()
                setIconForSound(item, android.R.drawable.ic_media_pause, COLOR_ACTIVE)
            }
        }
        else {
            playSound(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.soundlistmenu, menu)

        val searchItem = menu?.findItem(R.id.menu_item_search)
        val searchView: SearchView? = searchItem?.actionView as SearchView
        val sm = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager

        val searchInfo = sm.getSearchableInfo(activity?.componentName)
        searchView?.setSearchableInfo(searchInfo)

        searchView?.setOnCloseListener {
            search()
            return@setOnCloseListener false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_item_grid -> {
                val intent = Intent(context, SoundGridActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.menu_item_search -> {
                activity?.onSearchRequested()
                return true
            }
            R.id.menu_item_sound_random -> {
                if(displaySounds.isEmpty()){
                    return false
                }
                val randomIndex = Random().nextInt(displaySounds.size)
                playSound(displaySounds[randomIndex])
                return true
            }
            R.id.menu_item_sound_add -> {
                val i = Intent(activity,SoundActivity::class.java)
                startActivity(i)
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

    public fun updateDisplayedItems(){
        search(searchStr)
    }

    public fun search(searchStr:String=""){
        this.searchStr = searchStr
        displaySounds = getCurrentSoundList().search(searchStr)
        notifyAdapterOnDataChanged()
    }
}