package com.isopodus.ccscontrol

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner

class StateFragment : Fragment(), AdapterView.OnItemSelectedListener {
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        Log.w("spinner", parent!!.getItemAtPosition(position).toString())
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
       }

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
        spinner.onItemSelectedListener = this

        return view
    }
}