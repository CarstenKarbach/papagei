package de.karbach.papagei

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.Intent.getIntent
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.preference.MultiSelectListPreference
import androidx.preference.PreferenceFragmentCompat


class Settings : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

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
                                BoardsManager.clearAllBoards(it)
                                val sm = SoundsManager(it)
                                sm.resetToTestSounds()

                                activity?.finish()
                                val intent = Intent(context, SettingsActivity::class.java)
                                startActivity(intent)

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.setTitle(resources.getString(R.string.settings_title))
    }
}