package de.karbach.papagei

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.preference.MultiSelectListPreference
import androidx.preference.PreferenceFragmentCompat


class Settings : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        context?.let {
            val soundlist = SoundsManager.getCurrentList(it)
            val tags = soundlist.getAllTags(it).toTypedArray()
            val multiSelectListPreference = MultiSelectListPreference(context).apply {
                key = "visible_tags"
                title = getString(R.string.visible_tags)
                summary = getString(R.string.choose_vis_text)
                entries = tags
                entryValues = tags
            }
            multiSelectListPreference.setDefaultValue(soundlist.getDefaultTags().toSet())
            multiSelectListPreference.isIconSpaceReserved = true
            multiSelectListPreference.icon = it.getDrawable(R.drawable.ic_eye_slash_solid)
            multiSelectListPreference.icon.setTint(ContextCompat.getColor(it, R.color.iconSecondaryColor))
            multiSelectListPreference.setOnPreferenceChangeListener { preference, newValue ->
                if(newValue is HashSet<*>){
                    val board = BoardsManager.getActiveBoard(it)
                    board.visible_tags.clear()
                    board.visible_tags.addAll((newValue as HashSet<String>))
                    BoardsManager.saveAsCurrentBoards(it)
                }
                return@setOnPreferenceChangeListener true
            }
            this.preferenceScreen.addPreference(multiSelectListPreference);
        }

        val button = findPreference(getString(R.string.reset_sounds_key))
        button.setOnPreferenceClickListener {

            context?.let {
                val builder: AlertDialog.Builder = AlertDialog.Builder(it)

                builder.setTitle(getString(R.string.confirm))
                builder.setMessage(getString(R.string.sure_question))

                builder.setPositiveButton(
                    getString(R.string.yes),
                    DialogInterface.OnClickListener { dialog, which ->
                        dialog.dismiss()
                        context?.let {
                            val sm = SoundsManager(it)
                            sm.resetToTestSounds()
                            Toast.makeText(
                                it, getString(R.string.reset_successful),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })

                builder.setNegativeButton(
                    getString(R.string.no),
                    DialogInterface.OnClickListener { dialog, which ->
                        dialog.dismiss()
                    })

                val alert = builder.create()
                alert.show()
            }

            return@setOnPreferenceClickListener true
        }

        val manageBoardsButton = findPreference(getString(R.string.manage_boards_key))
        manageBoardsButton.setOnPreferenceClickListener {

            val intent = Intent(context, BoardListActivity::class.java)
            startActivity(intent)

            return@setOnPreferenceClickListener true
        }
    }

}