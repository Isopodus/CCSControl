package com.isopodus.ccscontrol

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.opengl.Visibility
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    private val host = "http://ccsystem.in/stat2/ccscontrol/"
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

        return view
    }

    override fun onStart() {
        super.onStart()
        getInfo(Date())
    }

    fun getInfo(date : Date) {
        val jsonResponse = JSONArray()

        //get daily portions and straits on all counters
        thread{
            try
            {
                val payload = mapOf("date" to sdf.format(date),"user" to username)
                val response = khttp.post(host + "getInfo.php", data = payload)

                if(response.text != "false") {
                    val unsortedJson = JSONArray(response.text)

                    //sort received data by counter id
                    val jsonList = ArrayList<JSONObject>()
                    for (i in 0 until unsortedJson.length())
                        jsonList.add(unsortedJson.getJSONObject(i))
                    jsonList.sortWith(Comparator { a, b ->
                        var valA = String()
                        var valB = String()
                        try {
                            valA = a.get("counter") as String
                            valB = b.get("counter") as String
                        } catch (e: JSONException) {
                            //do something
                        }

                        valA.compareTo(valB)
                    })
                    for (i in 0 until unsortedJson.length())
                        jsonResponse.put(jsonList[i])

                    activity!!.runOnUiThread {
                        //remove old views if they exist and add new
                        if (view?.scrollLinearLayout != null) {
                            view!!.scrollLinearLayout.removeAllViews()

                            //create counter tiles
                            for (i in 0 until jsonResponse.length()) {
                                val counterTile =
                                    layoutInflater.inflate(R.layout.counter_tile, view!!.scrollLinearLayout, false)

                                //counter id
                                counterTile.counterId.text = jsonResponse.getJSONObject(i).optString("counter")

                                //portions and straits
                                if (jsonResponse.getJSONObject(i).getString("values") != "false") { //if got values
                                    counterTile.portionsCount.text =
                                            jsonResponse.getJSONObject(i).getJSONObject("values")
                                                .optString("portions") //set portions
                                    counterTile.straitsCount.text =
                                            jsonResponse.getJSONObject(i).getJSONObject("values")
                                                .optString("straits") //set straits
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
                                    jsonResponse.getJSONObject(i).getJSONObject("time").optString("sync")
                                )
                                counterTile.syncTime.text = sdfOut.format(sync)
                                counterTile.startTime.text = sdfOut.format(
                                    sdfIn.parse(
                                        jsonResponse.getJSONObject(i).getJSONObject("time").optString("start")
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
                                view!!.scrollLinearLayout.addView(counterTile)
                            }
                        }
                        progressBar.visibility = View.GONE
                    }
                }
            } catch (e: Resources.NotFoundException) {
                activity!!.runOnUiThread {
                    Toast.makeText(
                        context,
                        getString(R.string.toast_server),
                        Toast.LENGTH_SHORT).show()
                }
            } catch (e: UnknownHostException) {
                activity!!.runOnUiThread {
                    Toast.makeText(
                        context,
                        getString(R.string.toast_network),
                        Toast.LENGTH_SHORT).show()
                }
            } catch (e: ConnectException) {
                activity!!.runOnUiThread {
                    Toast.makeText(
                        context,
                        getString(R.string.toast_network),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.d("ERR", e.toString())
            }
        }
    }
}