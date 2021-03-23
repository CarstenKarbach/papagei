package de.karbach.papagei

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.*
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.children
import androidx.fragment.app.Fragment
import de.karbach.papagei.model.Sound
import java.io.File


class SoundFragment: Fragment() {
    val FILE_SELECT_CODE = 0
    val RECORD_AUDIO_CODE = 987
    val MY_PERMISSIONS_REQUEST = 165
    var origuri: Uri? = null
    var saveuri: Uri? = null
    var sound:Sound? = null
    val recorder = MediaRecorder()
    var recording = false
    var audioRecord:Boolean? = null

    var COLOR_RECORD_ACTIVE:Int? = null
    var COLOR_RECORD_INACTIVE:Int? = null

    val mediaPlayer = MediaPlayer()
    var playingUri:Uri?=null

    var iconColor: Int = ColorHelper.defaultColor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

        activity?.title = getString(R.string.sound_title)

        COLOR_RECORD_ACTIVE = ContextCompat.getColor(activity as Context, R.color.colorRecordActive)
        COLOR_RECORD_INACTIVE = ContextCompat.getColor(
            activity as Context,
            R.color.colorRecordInactive
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        if(recording) {
            recorder.stop()
            recording = false
        }
        recorder.release()

        mediaPlayer.stop()
        mediaPlayer.release()
    }

    private fun showFileChooser() {
        /**
        val intent = Intent(
            Intent.ACTION_PICK,
            android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        )
        */

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setType("audio/*")

        try {
            startActivityForResult(
                Intent.createChooser(intent, getString(R.string.select_audio)),
                FILE_SELECT_CODE
            )
        } catch (ex: android.content.ActivityNotFoundException) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(
                activity as Activity, getString(R.string.install_file_manager),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun storeAudioFileFromOrigUri(){
        loadDataIntoView(view)

        var msg = ""
        if (origuri != null) {
            val context = activity as Context
            val man = SoundsManager(context)

            val filename = getSoundsFilename(true)
            val internalFilesUri = Uri.fromFile(context.filesDir)
            val storeFileUri = Uri.withAppendedPath(internalFilesUri, filename)
            val success = if(origuri.toString() == storeFileUri.toString() ) true else man.copyFile(
                origuri as Uri,
                filename
            )

            if (!success) {
                msg = getString(R.string.cannot_read)
                origuri = null
            } else {
                msg = getString(R.string.success_selected)
                saveuri = storeFileUri

                configureMediaPlayer(view)
            }
        } else {
            msg = getString(R.string.file_not_found)
        }
        Toast.makeText(
            activity as Activity, msg,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == FILE_SELECT_CODE && resultCode == RESULT_OK) {
            origuri = data?.getData()
            audioRecord = false
        }
        if(requestCode == RECORD_AUDIO_CODE && resultCode == RESULT_OK) {
            origuri = data?.getData()
            audioRecord = true
        }
        if(requestCode == DEFSOUND_SELECT_REQUEST && resultCode == RESULT_OK){
            activity?.let {
                val selectedID = data?.getIntExtra(SoundListActivity.SOUND_SELECT_RESULT, -1)
                val selectedSound = SoundsManager(it).getTestSounds().getById(selectedID)
                selectedSound?.let {
                    origuri = Uri.parse(selectedSound.actualResourceURI)
                    audioRecord = false
                }
            }
        }
        if( arrayOf(FILE_SELECT_CODE, RECORD_AUDIO_CODE, DEFSOUND_SELECT_REQUEST).contains(requestCode) ) {
            storeAudioFileFromOrigUri()
        }
        if(requestCode == ICON_SELECT_REQUEST){
            if(resultCode == Activity.RESULT_OK){
                val selectedIcon = data?.getStringExtra(IconSelectionActivity.EXTRA_PRESELECTED)
                val color = data?.getIntExtra(IconSelectionActivity.EXTRA_COLOR, ColorHelper.defaultColor)
                val iconView = view?.findViewById<TextView>(R.id.icon_display)
                iconView?.let{
                    selectedIcon?.let{
                        iconView.text = selectedIcon
                    }
                    val resolvedColor = ContextCompat.getColor(activity as Context, ColorHelper().nameToColor(color))
                    iconView.setTextColor(resolvedColor)
                    iconColor = color ?: ColorHelper.defaultColor
                }
            }
        }
    }

    fun getPrivateUriForFilename(filename: String):Uri{
        val context = activity as Context
        val internalFilesUri = Uri.fromFile(context.filesDir)
        val fileUri = Uri.withAppendedPath(internalFilesUri, filename)
        return fileUri
    }

    fun getPrivateFileForFilename(filename: String):File{
        val fileUri = getPrivateUriForFilename(filename)
        val result = File(fileUri.getPath())
        return result
    }

    fun getSoundsFilename(newfile: Boolean):String{
        activity?.let {
            val board = BoardsManager.getActiveBoard(it)
            if (sound != null && !newfile) {
                return "soundrecord_" + board.id + "_" + sound?.id + ".audio"
            } else {
                return "soundrecord_" + board.id + "_" + (SoundsManager.getCurrentList(activity as Context).getMaxID() + 1) + ".audio"
            }
        }
        return "soundrecord_unknown.audio"
    }

    fun askForPermission(permission: String, msg: String, requestCode: Int): Boolean{
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        if (ContextCompat.checkSelfPermission(
                activity as Context,
                permission
            )
            != PackageManager.PERMISSION_GRANTED) {
            val fragment = this
            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(permission)) {
                val builder = AlertDialog.Builder(activity as Activity)
                builder.setMessage(msg)
                builder.setTitle(msg)
                builder.setPositiveButton(getString(R.string.ok),
                    DialogInterface.OnClickListener { dialogInterface, i ->
                        fragment.requestPermissions(
                            arrayOf(permission),
                            requestCode
                        )
                    })
                builder.setNeutralButton(getString(R.string.cancel), null)
                val dialog = builder.create()
                dialog.show()
            }
            else {
                requestPermissions(
                    arrayOf(permission),
                    requestCode
                );
            }

            return false
        }
        else{
            return true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sound = SoundsManager.getCurrentList(activity as Context).getById(
            arguments?.getInt(SoundActivity.EXTRA_SOUND_PARAM, -1)
        )

        val result = inflater.inflate(R.layout.sound_detail, container, false)
        val selectButton = result.findViewById<ImageButton>(R.id.detail_file)
        selectButton.setOnClickListener{
            showFileChooser()
        }

        val iconView = result.findViewById<TextView>(R.id.icon_display)
        iconView.text = Sound.defIcon
        activity?.let {
            val resolvedColor = ContextCompat.getColor(
                it,
                ColorHelper().nameToColor(ColorHelper.defaultColor)
            )
            iconView.setTextColor(resolvedColor)
        }

        loadDataIntoView(result)

        val saveButton = result.findViewById<Button>(R.id.detail_save)
        if(sound != null){
            saveButton.setText(getString(R.string.save))
        }
        saveButton.setOnClickListener{
            val descStr = view?.findViewById<TextView>(R.id.detail_description)?.text.toString()
            if(descStr == ""){
                Toast.makeText(
                    activity as Activity, getString(R.string.add_desc),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            var msg = getString(R.string.new_sound)+descStr+getString(R.string.saved_success)
            if(sound == null) {
                if(origuri == null || saveuri == null){
                    Toast.makeText(
                        activity as Activity, getString(R.string.select_file),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                val newSound = Sound(
                    description = descStr,
                    origResourceURI = (origuri as Uri).toString(),
                    actualResourceURI = (saveuri as Uri).toString(),
                    audioRecord = if (audioRecord == null || audioRecord == false) false else true
                )
                SoundsManager.getCurrentList(activity as Activity).addSound(newSound)
                sound = newSound // Allow to set icons, color and tags below
            }
            else{
                if(saveuri != null) {
                    if (saveuri.toString() != sound?.actualResourceURI) {
                        val context = activity as Context
                        val filename = getSoundsFilename(false)
                        val man = SoundsManager(context)
                        val storeFileUri = getPrivateUriForFilename(filename)
                        val success = man.copyFile(saveuri as Uri, filename)
                        val fdelete = File(saveuri?.getPath())
                        if (fdelete.exists()) {
                            fdelete.delete()
                        }
                        saveuri = storeFileUri
                    }
                    sound?.origResourceURI = (origuri as Uri).toString()
                    sound?.actualResourceURI = (saveuri as Uri).toString()
                    sound?.audioRecord = if(audioRecord==null || audioRecord == false) false else true
                }

                sound?.description = descStr

                msg = getString(R.string.sound_msg)+descStr+getString(R.string.saved_success)
            }
            val iconView = result.findViewById<TextView>(R.id.icon_display)
            var icon = iconView.text.toString()
            if(icon == "" || icon == null){
                icon = Sound.defIcon
            }
            sound?.icon = icon
            sound?.color = iconColor
            SoundsManager.saveAsCurrentList(activity as Activity)
            Toast.makeText(
                activity as Activity, msg,
                Toast.LENGTH_SHORT
            ).show()
            activity?.finish()
        }

        val cancelButton = result.findViewById<Button>(R.id.detail_cancel)
        cancelButton.setOnClickListener{
            activity?.finish()
        }

        val defaultSelectButton = result.findViewById<ImageButton>(R.id.default_select_button)
        defaultSelectButton.setOnClickListener{
            showDefSoundSelectList()
        }

        val recordButton = result.findViewById<ImageButton>(R.id.record_button)
        recordButton.setOnClickListener{
            /*
            val intent = Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION)
            try {
                startActivityForResult(intent, RECORD_AUDIO_CODE)
            }catch(e: ActivityNotFoundException){
            }
             */
            if( ! askForPermission(
                    Manifest.permission.RECORD_AUDIO,
                    getString(R.string.permission_micro),
                    MY_PERMISSIONS_REQUEST
                ) ){
                return@setOnClickListener
            }
            if(! recording) {
                startAudioRecord()
            }
            else{
                stopAudioRecord()
            }
        }

        configureMediaPlayer(result)

        activity?.let {
            val iconView = result.findViewById<TextView>(R.id.icon_display)
            val iconFont = FontManager.getTypeface(it, FontManager.FONTAWESOME_SOLID)
            FontManager.markAsIconContainer(iconView, iconFont)

            iconView.setOnClickListener{
                showIconSelectDialog(iconView)
            }
        }

        return result
    }



    fun getSelectedSoundUri():Uri?{
        if(saveuri != null){
            return saveuri
        }
        if(sound!=null){
            val actString = sound?.actualResourceURI
            if(actString != null){
                return Uri.parse(actString)
            }
        }
        return null
    }

    fun configureMediaPlayer(fragmentview: View?){
        val rootview = fragmentview
        if(rootview == null){
            return
        }
        val play = rootview.findViewById<ImageButton>(R.id.detail_play_pause)
        val stop = rootview.findViewById<ImageButton>(R.id.detail_stop)
        val playerRow = rootview.findViewById<View>(R.id.detail_mediaplayer)

        val selectedUri = getSelectedSoundUri()
        if(selectedUri == null){
            playerRow.visibility = View.INVISIBLE
            return
        }
        else{
            playerRow.visibility = View.VISIBLE
        }

        val playing = mediaPlayer.isPlaying
        val activeColor = ContextCompat.getColor(activity as Context, R.color.colorActive)
        if(playing){
            play.setImageResource(android.R.drawable.ic_media_pause)
            playerRow.setBackgroundColor(activeColor)
        }
        else{
            play.setImageResource(android.R.drawable.ic_media_play)
            if(playingUri == null) {
                playerRow.setBackgroundColor(0)
            }
            else{
                playerRow.setBackgroundColor(activeColor)
            }
        }

        play.setOnClickListener{
            if(mediaPlayer.isPlaying){
                mediaPlayer.pause()
                configureMediaPlayer(view)
                return@setOnClickListener
            }
            if(playingUri != null){
                mediaPlayer.start()
                configureMediaPlayer(view)
                return@setOnClickListener
            }
            val currentUri = getSelectedSoundUri()
            if(currentUri != null){
                mediaPlayer.stop()
                mediaPlayer.reset()
                mediaPlayer.setDataSource(activity as Context, currentUri)
                mediaPlayer.prepare()

                mediaPlayer.setOnCompletionListener {
                    playingUri = null
                    configureMediaPlayer(view)
                }
                playingUri = currentUri
                mediaPlayer.start()

                configureMediaPlayer(view)
            }
        }

        stop.setOnClickListener{
            mediaPlayer.stop()
            playingUri=null
            configureMediaPlayer(view)
        }
    }

    fun startAudioRecord(){
        val recordButton = view?.findViewById<ImageButton>(R.id.record_button)
        if(recordButton == null){
            return
        }

        recording = true
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        recorder.setOutputFile(getPrivateFileForFilename(getSoundsFilename(true)).toUri().path)
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        recorder.prepare()
        recorder.start()

        recordButton.setColorFilter(COLOR_RECORD_ACTIVE as Int)
    }

    fun stopAudioRecord(){
        val recordButton = view?.findViewById<ImageButton>(R.id.record_button)
        if(recordButton == null){
            return
        }

        recorder.stop()
        origuri = getPrivateFileForFilename(getSoundsFilename(true)).toUri()
        Log.d("stoprecord", origuri.toString())
        audioRecord = true
        storeAudioFileFromOrigUri()

        recording = false

        recordButton.setColorFilter(COLOR_RECORD_INACTIVE as Int)

        Toast.makeText(
            activity as Activity, getString(R.string.audio_store),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startAudioRecord()
                }
            }
        }
    }

    /**
     * Import sound
     */
    fun loadFromImportArgument(){
        val importURI = arguments?.getParcelable<Uri>(SoundActivity.SOUND_IMPORT_URI)
        importURI?.let {
            arguments?.remove(SoundActivity.SOUND_IMPORT_URI)
            origuri = it
            audioRecord = false
            storeAudioFileFromOrigUri()
        }
    }

    fun loadDataIntoView(rootView: View?){
        loadFromImportArgument()
        if(rootView != null){
            var showOrigUri = if(origuri!=null) origuri.toString() else sound?.origResourceURI
            val fileView = rootView.findViewById<TextView>(R.id.file_uri_display)
            if(audioRecord==true || (audioRecord == null && sound?.audioRecord == true) ){
                showOrigUri = getString(R.string.audio_record)
            }
            if(showOrigUri != null) {
                val uristr = showOrigUri
                val displaystr =
                    if (uristr.length <= 30) uristr else getString(R.string.dots) + uristr.substring(uristr.length - 27)
                fileView.text = displaystr
            }
            else{
                fileView.text = getString(R.string.please_select)
            }

            if(sound != null) {
                val descView = rootView.findViewById<EditText>(R.id.detail_description)
                descView.setText(sound?.description)
                val idView = rootView.findViewById<TextView>(R.id.detail_id)
                idView.text = sound?.id.toString()

                val iconView = rootView.findViewById<TextView>(R.id.icon_display)
                var icon = sound?.icon
                if (icon == null || icon == "") {
                    icon = Sound.defIcon
                }
                iconView.text = icon
                iconColor = sound?.color ?: ColorHelper.defaultColor
                val resolvedColor = ContextCompat.getColor(
                    activity as Context,
                    ColorHelper().nameToColor(iconColor)
                )
                iconView.setTextColor(resolvedColor)
            }
        }
    }

    val ICON_SELECT_REQUEST = 11553

    fun showIconSelectDialog(iconView: TextView){
        val preselected = iconView.text.toString()
        val intent = Intent(context, IconSelectionActivity::class.java)
        intent.putExtra(IconSelectionActivity.EXTRA_PRESELECTED, preselected)
        intent.putExtra(IconSelectionActivity.EXTRA_COLOR, iconColor)
        startActivityForResult(intent, ICON_SELECT_REQUEST)
    }

    val DEFSOUND_SELECT_REQUEST = 11554

    fun showDefSoundSelectList(){
        val intent = Intent(context, SoundListActivity::class.java)
        intent.putExtra(SoundListActivity.DEFAULT_SOUND_SELECT, true)
        startActivityForResult(intent, DEFSOUND_SELECT_REQUEST)
    }
}