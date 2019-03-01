package com.isopodus.ccscontrol

import android.app.Activity
import android.content.res.Resources
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import khttp.post
import kotlinx.android.synthetic.main.fragment_settings.*
import org.json.JSONObject
import java.lang.Exception
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.*
import kotlin.concurrent.thread


class SettingsFragment : Fragment(), AdapterView.OnItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {

    private val host = "http://ccsystem.in/stat2/ccscontrol/"
    private var listener: MainActivityListener? = null

    @Suppress("OverridingDeprecatedMember", "DEPRECATION")
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
        val keysArray = arguments!!.getIntegerArrayList("keysArray")
        val spinner = view.findViewById(R.id.spinner) as Spinner

        val colors = ArrayList<Int>(countersArray.size)

        for(i in 0 until keysArray.size) {
            colors.add(R.color.colorRed)
            if(keysArray[i] == 2)
                colors[i] = R.color.colorGreen
        }

        val adapter = SettingsSpinnerAdapter(context!!, R.layout.spinner_state_item, countersArray, colors)
        adapter.notifyDataSetChanged()
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this

        val refresh = view.findViewById(R.id.refresh) as SwipeRefreshLayout
        refresh.setOnRefreshListener(this)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        listener!!.setOpenedMenu(R.id.nav_settings)
    }

    override fun onRefresh() {
        getSettings(spinner.getItemAtPosition(spinner.selectedItemPosition).toString())
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        //animate progress bar
        if(progressBar != null)
            progressBar.visibility = View.VISIBLE

        getSettings(spinner.getItemAtPosition(position).toString())
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    private fun getSettings(cid: String) {

        //get settings of the counter
        thread{
            try
            {
                val payload = mapOf("cid" to cid)
                val response = post(host + "getSettings.php", data = payload)

                if(response.text != "false") {
                    activity!!.runOnUiThread {
                        val jsonResponse = JSONObject(response.text)
                        val straitTimeString = jsonResponse.optString("straitTime")
                        val portionTimeString = jsonResponse.optString("portionTime")
                        val maxPortionTimeString = jsonResponse.optString("maxPortionTime")
                        val volumeString = jsonResponse.optString("volume")
                        val gss1String = jsonResponse.optString("gss1")
                        val gss1maxString = jsonResponse.optString("gss2")
                        val gss2String = jsonResponse.optString("gss1max")
                        val gss2maxString = jsonResponse.optString("gss2max")

                        //check for nulls and set text
                        if(
                            straitTime != null &&
                            straitTimeString != "null" &&
                            portionTimeString != "null" &&
                            maxPortionTimeString != "null" &&
                            volumeString != "null" &&
                            gss1String != "null" &&
                            gss1maxString != "null" &&
                            gss2String != "null" &&
                            gss2maxString != "null"
                        ) {
                            straitTime.setText(straitTimeString)
                            portionTime.setText(portionTimeString)
                            maxPortionTime.setText(maxPortionTimeString)
                            volume.setText(volumeString)
                            gss1.setText(gss1String)
                            gss2.setText(gss1maxString)
                            gss1max.setText(gss2String)
                            gss2max.setText(gss2maxString)
                        }
                        else {
                            straitTime.setText(0)
                            portionTime.setText(0)
                            maxPortionTime.setText(0)
                            volume.setText(0)
                            gss1.setText(0)
                            gss2.setText(0)
                            gss1max.setText(0)
                            gss2max.setText(0)
                        }

                        //make progress bar invisible
                        if(refresh != null)
                            refresh.isRefreshing = false
                        if(progressBar != null)
                            progressBar.visibility = View.INVISIBLE
                    }
                }
            } catch (e: Resources.NotFoundException) {
                activity!!.runOnUiThread {
                    Toast.makeText(
                        context,
                        getString(R.string.toast_server),
                        Toast.LENGTH_SHORT).show()
                    if(refresh != null)
                        refresh.isRefreshing = false
                }
            } catch (e: UnknownHostException) {
                activity!!.runOnUiThread {
                    Toast.makeText(
                        context,
                        getString(R.string.toast_network),
                        Toast.LENGTH_SHORT).show()
                    if(refresh != null)
                        refresh.isRefreshing = false
                }
            } catch (e: ConnectException) {
                activity!!.runOnUiThread {
                    Toast.makeText(
                        context,
                        getString(R.string.toast_network),
                        Toast.LENGTH_SHORT
                    ).show()
                    if(refresh != null)
                        refresh.isRefreshing = false
                }
            } catch (e: Exception) {
                Log.d("ERR", e.toString())
                if(refresh != null)
                    refresh.isRefreshing = false
            }
        }
    }
}