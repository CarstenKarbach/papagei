package de.karbach.papagei

import android.app.SearchManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class BoardListActivity: SingleFragmentActivity() {

    override fun createFragment(): Fragment {
        return BoardListFragment()
    }

    fun createDialog(uri: Uri){
        val dialogBuilder = AlertDialog.Builder(this).create()
        val inflater: LayoutInflater = this.getLayoutInflater()
        val dialogView = inflater.inflate(R.layout.name_dialog, null)

        val editText = dialogView.findViewById<EditText>(R.id.edt_comment)
        val button1 = dialogView.findViewById<Button>(R.id.buttonSubmit)
        val button2 = dialogView.findViewById<Button>(R.id.buttonCancel)

        button2.setOnClickListener{
            dialogBuilder.dismiss()
        }
        button1.setOnClickListener {
            val name = editText.text.toString()
            val bm = BoardsManager(this@BoardListActivity)
            val boardWithName = bm.getBoardByName(name)
            if (name == "" || boardWithName != null) {
                Toast.makeText(this@BoardListActivity, "Der Name ist ungültig oder schon in Benutzung.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            try {
                BoardsManager(this).importBoard(uri, name)
            }
            catch(e: Exception){
                Toast.makeText(this, "Beim Import ist ein Fehler aufgetreten.", Toast.LENGTH_SHORT).show()
                dialogBuilder.dismiss()
                return@setOnClickListener
            }
            val fm: FragmentManager = this.supportFragmentManager;
            var f:Fragment? = fm.findFragmentById(R.id.fragment_container)
            f?.let{
                if(f is BoardListFragment){
                    (f as BoardListFragment).notifyDataChanged()
                }
            }

            dialogBuilder.dismiss()
        }

        dialogBuilder.setView(dialogView)
        dialogBuilder.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(intent?.action == Intent.ACTION_SEND){
            (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
                createDialog(it)
            }
        }
    }

}