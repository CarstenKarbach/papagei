package de.karbach.papagei

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.ArrayMap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.karbach.papagei.model.Sound

class IconSelectionFragment: Fragment() {

    interface IconSelectedCallback{
        fun iconSelected(icon: String)
    }

    var callback: IconSelectedCallback? = null
    var preselected: String? = null
    var color: Int = ColorHelper.defaultColor

    fun clearViewStates(recyclerView: RecyclerView){
        val childCount = recyclerView.getChildCount()
        for (i in 0..childCount-1) {
            val holder = recyclerView.getChildViewHolder(recyclerView.getChildAt(i));
            val viewHolder = holder as IconsAdapter.ViewHolder
            viewHolder.setActiveState(false)
        }
    }

    fun getIconsList(filter: String? = null) : ArrayMap<String,String>{
        val iconsList = ArrayMap<String,String>()
        val cfilter = filter?.toLowerCase()
        for(iconStr in resources.getStringArray(R.array.fa_icons)){
            val parts = iconStr.split("|")
            val id = parts[0]
            val icon = parts[1]
            if(cfilter == null || id.contains(cfilter)) {
                iconsList[id] = icon
            }
        }
        return iconsList
    }

    var iconSelectAdapter:IconsAdapter? = null

    fun hukAdapter(filter: String? = null, rootview: View){
        val recyclerView = rootview.findViewById<RecyclerView>(R.id.icons)
        val resolvedColor = ContextCompat.getColor(activity as Context, ColorHelper().nameToColor(color))
        val iconsList = getIconsList(filter)
        iconSelectAdapter = IconsAdapter(iconsList, object: IconsAdapter.ClickCallback {
            override fun clicked(icon: String) {
                clearViewStates(recyclerView)
            }
        }, R.color.colorActive, resolvedColor
        )
        preselected?.let{
            iconSelectAdapter?.activeIcon = it
        }
        recyclerView?.adapter = iconSelectAdapter
        iconSelectAdapter?.notifyDataSetChanged()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val result = inflater.inflate(R.layout.icon_select, container, false)

        val recyclerView = result.findViewById<RecyclerView>(R.id.icons)
        hukAdapter(null, result)
        recyclerView?.layoutManager = GridLayoutManager(activity, 4)

        val cancelButton = result.findViewById<Button>(R.id.cancel_button)
        cancelButton.setOnClickListener {
            activity?.setResult(Activity.RESULT_CANCELED)
            activity?.finish()
        }

        val okButton = result.findViewById<Button>(R.id.ok_button)
        okButton.setOnClickListener {
            val icon = iconSelectAdapter?.activeIcon
            if(icon == "" || icon == null){
                Toast.makeText(
                    activity as Activity, getString(R.string.please_select_icon),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            callback?.iconSelected(icon)
            val resultData = Intent()
            resultData.putExtra(IconSelectionActivity.EXTRA_PRESELECTED, icon)
            resultData.putExtra(IconSelectionActivity.EXTRA_COLOR, color)
            activity?.setResult(Activity.RESULT_OK, resultData)
            activity?.finish()
        }

        val filter = result.findViewById<EditText>(R.id.filter)
        filter.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val filterBy = s.toString()
                view?.let {
                    hukAdapter(filterBy, it)
                }
            }
        })

        val colorSelect = result.findViewById<Spinner>(R.id.choose_color)
        val iconColors = resources.getStringArray(R.array.icon_colors)
        val index = this.color
        if(index != -1) {
            colorSelect.setSelection(index)
        }
        colorSelect.setOnItemSelectedListener(object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                color = position
                val resolvedColor = ContextCompat.getColor(activity as Context, ColorHelper().nameToColor(color))
                iconSelectAdapter?.iconsColor = resolvedColor
                iconSelectAdapter?.notifyDataSetChanged()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        })

        return result
    }

}