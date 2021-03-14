package de.karbach.papagei

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.ArrayMap
import android.util.ArraySet
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import de.karbach.papagei.model.Sound
import de.karbach.papagei.model.SoundList
import java.util.*
import kotlin.collections.HashSet

class SoundGridFragment: Fragment() {

    val mediaPlayer: MediaPlayer = MediaPlayer()
    var playingSound:Sound? = null
    var soundGridAdapter: SoundGridAdapter? = null
    var displayedSounds: SoundList? = null
    var itemTouchHelper:ItemTouchHelper? = null

    fun getRecyclerView():RecyclerView?{
        view?.let {
            return it.findViewById(R.id.sounds)
        }
        return null
    }

    fun getVisibleTags() : Set<String>{
        val defTags = SoundList.defaultTags.toSet()
        activity?.let {
            val soundlist = SoundsManager.getCurrentList(it)
            val sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(activity)
            val res = sharedPreferences.getStringSet("visible_tags", defTags)
            return res ?: defTags
        }
        return defTags
    }

    override fun onResume() {
        super.onResume()

        updateDisplaySounds()
        connectSoundListAdapter()
        soundGridAdapter?.notifyDataSetChanged()
    }

    fun deleteSoundFromList(sound: Sound){
        activity?.let {
            SoundsManager.getCurrentList(it).removeSound(sound)

            Toast.makeText(
                activity as Activity, getString(R.string.deleted_sound),
                Toast.LENGTH_SHORT
            ).show()

            SoundsManager.saveAsCurrentList(it)
        }
        itemTouchHelper?.attachToRecyclerView(null)
        updateDisplaySounds()
        connectSoundListAdapter()
        soundGridAdapter?.notifyDataSetChanged()
    }

    fun storePlayingSound(sound: Sound?){
        playingSound = sound
        soundGridAdapter?.playingSound = sound
    }

    override fun onPause() {
        super.onPause()

        mediaPlayer.stop()
        storePlayingSound(null)
        itemTouchHelper?.attachToRecyclerView(null)
    }

    override fun onDestroy() {
        super.onDestroy()

        mediaPlayer.stop()
        mediaPlayer.release()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        setHasOptionsMenu(true)
    }

    fun updateDisplaySounds() : SoundList{
        activity?.let {
            val soundList = SoundsManager.getCurrentList(it)
            val res = soundList.filterByTags(getVisibleTags())
            displayedSounds = res
            return res
        }
        val res = SoundList()
        displayedSounds = res
        return res
    }

    fun getDisplaySounds(): SoundList{
        return displayedSounds ?: updateDisplaySounds()
    }

    fun connectSoundListAdapter(){
        activity?.let {
            val soundList = getDisplaySounds()
            soundGridAdapter = SoundGridAdapter(soundList, object: SoundGridAdapter.SoundClickCallback {
                override fun soundClicked(sound: Sound) {
                    playSound(sound)
                }

                override fun deleteSound(sound: Sound) {
                    deleteSoundFromList(sound)
                }
            }, R.color.colorActive
            )
            val recyclerView = getRecyclerView()
            recyclerView?.adapter = soundGridAdapter
            recyclerView?.layoutManager = GridLayoutManager(it, 3)
        }
        attachDragDrop()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val result = inflater.inflate(R.layout.play_grid, container, false)
        return result
    }


    fun attachDragDrop(){
        val simpleCallback = object: ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or
                    ItemTouchHelper.START or ItemTouchHelper.END, 0){

            private var dragFromPosition = -1
            private var dragToPosition = -1

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                dragToPosition = target.adapterPosition
                if(target is SoundGridAdapter.ViewHolder) {
                    soundGridAdapter?.clearViewStates(recyclerView)
                    activity?.let {
                        val colorAccent = ContextCompat.getColor(it, R.color.colorAccent)
                        (target as SoundGridAdapter.ViewHolder).frame.setBackgroundColor(colorAccent)
                        it.closeContextMenu()
                    }
                }
                return false
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)

                when (actionState) {
                    ItemTouchHelper.ACTION_STATE_DRAG -> {
                        viewHolder?.also { dragFromPosition = it.adapterPosition }
                    }
                    ItemTouchHelper.ACTION_STATE_IDLE -> {
                        if (dragFromPosition != -1 && dragToPosition != -1 && dragFromPosition != dragToPosition) {
                            // Item successfully dragged
                            activity?.let {
                                val soundList = getDisplaySounds()
                                val soundFrom = soundList.sounds.get(dragFromPosition)
                                val soundTo = soundList.sounds.get(dragToPosition)
                                Collections.swap(soundList.sounds, dragFromPosition, dragToPosition)
                                soundGridAdapter?.notifyDataSetChanged()
                                //Swap also in full list
                                val fullList = SoundsManager.getCurrentList(it)
                                val fullFrom = fullList.sounds.indexOf(soundFrom)
                                val fullTo = fullList.sounds.indexOf(soundTo)
                                if(fullFrom != -1 && fullTo != -1) {
                                    Collections.swap(fullList.sounds, fullFrom, fullTo)
                                    SoundsManager.saveAsCurrentList(it)
                                }
                            }
                            // Reset drag positions
                            dragFromPosition = -1
                            dragToPosition = -1
                        }
                    }
                }
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }
        }
        itemTouchHelper = ItemTouchHelper(simpleCallback)
        val recyclerView = getRecyclerView()
        recyclerView?.let {
            itemTouchHelper?.attachToRecyclerView(recyclerView)
            registerForContextMenu(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.soundgridmenu, menu)
    }

    fun scrollToSound(sound: Sound){
        val sounds = getDisplaySounds().sounds
        val childPos = sounds.indexOf(sound)
        if(childPos >= 0 && childPos< sounds.size) {
            getRecyclerView()?.smoothScrollToPosition(childPos)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_item_search -> {
                val intent = Intent(context, SoundListActivity::class.java)
                intent.putExtra(SoundListActivity.SEARCH_REQUEST, true)
                startActivity(intent)
                return true
            }
            R.id.menu_item_list -> {
                val intent = Intent(context, SoundListActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.menu_item_sound_random -> {
                val displaySounds = getDisplaySounds()
                val displaySoundsArray = displaySounds.sounds
                if(displaySoundsArray.isEmpty()){
                    return false
                }
                val randomIndex = Random().nextInt(displaySoundsArray.size)
                val sound = displaySoundsArray[randomIndex]
                storePlayingSound(sound)
                scrollToSound(sound)
                playSound(sound)
                return true
            }
            R.id.menu_item_sound_add -> {
                val intent = Intent(context, SoundActivity::class.java)
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

    fun displaySound(itemView: View, sound: Sound?){
        if(sound == null){
            itemView.visibility = View.INVISIBLE
            return
        }
        val textView = itemView.findViewById<TextView>(R.id.short_text)
        textView.text = sound.description
        val listener = {
            playSound(sound)
        }
        val frame = itemView.findViewById<View>(R.id.sound_item)
        frame.setOnTouchListener(ViewTouchButton(R.color.colorActive, 0))
        frame.setOnClickListener{ listener() }

    }

    var currentThread = 0;

    fun playSound(sound:Sound) {
        mediaPlayer.stop()
        mediaPlayer.reset()
        mediaPlayer.setDataSource(activity as Context, Uri.parse(sound.actualResourceURI))
        mediaPlayer.prepare()

        mediaPlayer.setOnCompletionListener {
            storePlayingSound(null)
            getRecyclerView()?.let {
                soundGridAdapter?.clearViewStates(it)
            }
        }

        val threadId = currentThread+1
        currentThread = threadId

        val stateDisplayer = Thread{
            while(currentThread == threadId && playingSound != null) {
                activity?.runOnUiThread {
                    getRecyclerView()?.let {
                        soundGridAdapter?.clearViewStates(it, playingSound)
                    }
                }
                Thread.sleep(10)
            }
        }

        mediaPlayer.start()
        storePlayingSound(sound)
        stateDisplayer.start()
        getRecyclerView()?.let {
            soundGridAdapter?.clearViewStates(it, sound)
        }
    }
}