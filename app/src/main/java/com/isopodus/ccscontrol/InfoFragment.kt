package com.isopodus.ccscontrol

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import kotlinx.android.synthetic.main.counter_tile.view.*
import kotlinx.android.synthetic.main.fragment_info.*
import kotlinx.android.synthetic.main.fragment_info.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.net.ConnectException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread
import org.json.JSONException

class InfoFragment : Fragment() {

    private lateinit var sp: SharedPreferences
    private lateinit var username: String
    private lateinit var sdf: SimpleDateFormat
    private lateinit var sdfIn: SimpleDateFormat
    private lateinit var sdfOut: SimpleDateFormat

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_info, container, false)
        sdf = SimpleDateFormat("yyyy-MM-dd")
        sdfIn = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        sdfOut = SimpleDateFormat("HH:mm:ss dd-MM-yyyy")

        //load preferences
        sp = activity!!.getSharedPreferences("SP", Context.MODE_PRIVATE)
        username = sp.getString("USERNAME", "null")

        if(arguments != null)
            setInfo(JSONArray(arguments!!.getString("countersData")), view.scrollLinearLayout)

        return view
    }

    fun setInfo(data : JSONArray, scroll : LinearLayout?) {

        //remove old views if they exist and add new
        if (scroll != null) {
            //view.scrollLinearLayout.removeAllViews()

            //create counter tiles
            for (i in 0 until data.length()) {
                val counterTile =
                    layoutInflater.inflate(R.layout.counter_tile, scroll, false)

                //counter id
                counterTile.counterId.text = data.getJSONObject(i).getString("counter")

                //portions and straits
                if (data.getJSONObject(i).getString("values") != "false") { //if got values
                    counterTile.portionsCount.text =
                            data.getJSONObject(i).getJSONObject("values")
                                .getString("portions") //set portions
                    counterTile.straitsCount.text =
                            data.getJSONObject(i).getJSONObject("values")
                                .getString("straits") //set straits
                    if (counterTile.portionsCount.text == "null")
                        counterTile.portionsCount.text = "0"
                    if (counterTile.straitsCount.text == "null")
                        counterTile.straitsCount.text = "0"
                } else {
                    counterTile.portionsCount.text = "0"
                    counterTile.straitsCount.text = "0"
                }

                //sync and startup time
                val sync = sdfIn.parse(
                    data.getJSONObject(i).getJSONObject("time").getString("sync")
                )
                counterTile.syncTime.text = sdfOut.format(sync)
                counterTile.startTime.text = sdfOut.format(
                    sdfIn.parse(
                        data.getJSONObject(i).getJSONObject("time").getString("start")
                    )
                )

                //status
                //get difference in minutes
                val diff = (Date().time - sync.time) / 1000.0 / 60.0
                if (diff > 2.0) {
                    counterTile.statusImage.setImageResource(R.drawable.status_no)
                    counterTile.status.text = getString(R.string.info_status_err)
                } else {
                    counterTile.statusImage.setImageResource(R.drawable.status_ok)
                    counterTile.status.text = getString(R.string.info_status_ok)
                }
                scroll.addView(counterTile)
            }
        }
    }
}