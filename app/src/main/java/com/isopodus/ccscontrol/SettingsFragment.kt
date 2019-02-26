package com.isopodus.ccscontrol

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner

class SettingsFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private var listener: MainActivityListener? = null

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        if (activity is MainActivityListener) {
            listener = activity
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        val countersArray = arguments!!.getStringArrayList("countersArray")

        val spinner = view.findViewById(R.id.spinner) as Spinner
        val adapter = ArrayAdapter<String>(this.activity, R.layout.spinner_item, countersArray)
        adapter.setDropDownViewResource(R.layout.spinner_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        listener!!.setOpenedMenu(R.id.nav_settings)
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

}