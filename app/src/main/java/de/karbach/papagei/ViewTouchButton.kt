package de.karbach.papagei

import android.content.Intent
import android.os.AsyncTask
import android.view.MotionEvent
import android.view.View

class ViewTouchButton(val onPressColor: Int, val defColor: Int): View.OnTouchListener{
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                v?.setBackgroundColor( onPressColor )
                return false
            }
            MotionEvent.ACTION_UP -> {
                val task = object : AsyncTask<Void, Void, Boolean>() {
                    override fun doInBackground(vararg params: Void?): Boolean {
                        Thread.sleep(100)
                        return true
                    }
                    override fun onPostExecute(result: Boolean?) {
                        super.onPostExecute(result)
                        v?.setBackgroundColor( defColor )
                    }
                }
                task.execute()
                return false
            }
            else -> return false
        }
    }
}