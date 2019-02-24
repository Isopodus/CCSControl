package com.isopodus.ccscontrol

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner

class StateFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_state, container, false)
        val countersArray = arguments!!.getStringArrayList("countersArray")

        val spinner = view.findViewById(R.id.spinner) as Spinner
        val adapter = ArrayAdapter<String>(this.activity, R.layout.spinner_item, countersArray)
        adapter.setDropDownViewResource(R.layout.spinner_item)
        spinner.adapter = adapter

        return view
    }
}