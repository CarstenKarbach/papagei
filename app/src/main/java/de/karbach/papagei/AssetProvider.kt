package de.karbach.papagei

import android.content.ContentProvider
import android.content.ContentValues
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.net.Uri
import android.content.res.AssetManager
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.util.Log
import java.io.FileNotFoundException
import java.io.IOException


class AssetProvider:ContentProvider() {
    override fun insert(p0: Uri, p1: ContentValues?): Uri? {
        return null
    }

    override fun query(
        p0: Uri,
        p1: Array<out String>?,
        p2: String?,
        p3: Array<out String>?,
        p4: String?
    ): Cursor? {
        return null
    }

    override fun onCreate(): Boolean {
        return false
    }

    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<out String>?): Int {
        return 0
    }

    override fun delete(p0: Uri, p1: String?, p2: Array<out String>?): Int {
        return 0
    }

    override fun getType(p0: Uri): String? {
        return null
    }

    override fun openAssetFile(uri: Uri, mode: String): AssetFileDescriptor? {
        val am = context!!.assets
        val file_name = Uri.decode(uri.toString().replace("content://de.karbach.papagei.assetprovider/", ""))
        var afd: AssetFileDescriptor? = null
        try {
            afd = am.openFd(file_name)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return afd//super.openAssetFile(uri, mode);
    }
}