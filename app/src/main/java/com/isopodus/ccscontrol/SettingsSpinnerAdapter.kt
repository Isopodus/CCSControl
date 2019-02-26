package com.isopodus.ccscontrol

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ArrayAdapter



class SettingsSpinnerAdapter(context: Context, textViewResourceId: Int, objects: ArrayList<String>, private var colors: Array<Int>) :
    ArrayAdapter<String>(context, textViewResourceId, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v = super.getView(position, convertView, parent)
        (v as TextView).setTextColor(context!!.resources.getColorStateList(colors[position]))
        return v
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v = super.getDropDownView(position, convertView, parent)
        (v as TextView).setTextColor(context!!.resources.getColorStateList(colors[position]))
        return v
    }
}
