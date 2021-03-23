package de.karbach.papagei.utils

import androidx.fragment.app.Fragment
import de.karbach.papagei.BoardsManager

fun Fragment.titleToActiveBoardName(){
    activity?.let{
        it.title = BoardsManager.getActiveBoard(it).name
    }
}