package com.isopodus.ccscontrol

import android.app.Activity
import android.content.res.Resources
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import khttp.post
import kotlinx.android.synthetic.main.fragment_state.*
import org.json.JSONObject
import java.lang.Exception
import java.net.ConnectException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class StateFragment : Fragment(), AdapterView.OnItemSelectedListener {
    private val host = "http://ccsystem.in/stat2/ccscontrol/"
    private lateinit var sdf: SimpleDateFormat
    private lateinit var sdfIn: SimpleDateFormat
    private lateinit var sdfOut: SimpleDateFormat

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
        sdf = SimpleDateFormat("yyyy-MM-dd")
        sdfIn = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        sdfOut = SimpleDateFormat("HH:mm:ss dd-MM-yyyy")
        val view = inflater.inflate(R.layout.fragment_state, container, false)
        val countersArray = arguments!!.getStringArrayList("countersArray")

        val spinner = view.findViewById(R.id.spinner) as Spinner
        val adapter = ArrayAdapter<String>(this.activity, R.layout.spinner_state_item, countersArray)
        adapter.setDropDownViewResource(R.layout.spinner_state_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this

        spinner.setSelection(adapter.getPosition((arguments!!.getString("chosenCounter"))))

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        listener!!.setOpenedMenu(R.id.nav_state)
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        thread{
            try
            {
                val cid = parent!!.getItemAtPosition(position).toString()

                val payload = mapOf("cid" to cid, "date" to sdf.format(Date()))
                val response = post(host + "getStatus.php", data = payload)

                activity!!.runOnUiThread {
                    if (response.text != "false") {
                        val jsonStatus = JSONObject(response.text)

                        //if there is data
                        if( jsonStatus.optString("values") != "false" &&
                            jsonStatus.optString("values") != "null" &&
                            jsonStatus.optString("status") != "false" &&
                            jsonStatus.optString("status") != "null" &&
                                portionsCount != null) {

                            //values
                            if (jsonStatus.getJSONObject("values").optString("portions") == "null")
                                portionsCount.text = "0"
                            else
                                portionsCount.text = jsonStatus.getJSONObject("values").optString("portions")

                            if (jsonStatus.getJSONObject("values").optString("straits") == "null")
                                straitsCount.text = "0"
                            else
                                straitsCount.text = jsonStatus.getJSONObject("values").optString("straits")

                            //time
                            var sync = sdfOut.parse("00:00:00 01-01-2019")
                            if (jsonStatus.getJSONObject("status").optString("syncTime") != "null") {
                                sync = sdfIn.parse(jsonStatus.getJSONObject("status").optString("syncTime"))
                                syncTime.text = sdfOut.format(sync)
                                val start = sdfIn.parse(jsonStatus.getJSONObject("status").optString("startupTime"))
                                startupTime.text = sdfOut.format(start)
                            } else {
                                syncTime.text = getString(R.string.state_null_time)
                                startupTime.text = getString(R.string.state_null_time)
                            }

                            //temp and humidity
                            temp.text = getString(
                                R.string.state_celsius,
                                jsonStatus.getJSONObject("status").optString("temp").toInt()
                            )
                            humid.text = getString(
                                R.string.state_percent,
                                jsonStatus.getJSONObject("status").optString("humid").toInt()
                            )

                            //memory
                            if (jsonStatus.getJSONObject("status").optString("sdStatus") != "0")
                                memoryState.text = getString(R.string.state_server_ok)
                            else
                                memoryState.text = getString(R.string.state_server_err)

                            //server
                            //get difference in minutes
                            val diff = (Date().time - sync.time) / 1000.0 / 60.0
                            if (diff > 2.0) {
                                statusImage.setImageResource(R.drawable.status_no)
                                serverState.text = getString(R.string.state_server_err)
                            } else {
                                statusImage.setImageResource(R.drawable.status_ok)
                                serverState.text = getString(R.string.state_server_ok)
                            }
                        }
                        else
                        {
                            portionsCount.text = "0"
                            straitsCount.text = "0"
                            syncTime.text = getString(R.string.state_null_time)
                            startupTime.text = getString(R.string.state_null_time)
                            temp.text = getString(R.string.state_celsius, 0)
                            humid.text = getString(R.string.state_percent, 0)
                            memoryState.text = getString(R.string.state_server_err)
                            serverState.text = getString(R.string.state_server_err)
                            statusImage.setImageResource(R.drawable.status_no)
                        }
                    }
                }
            }
            catch (e: Resources.NotFoundException) {
                activity!!.runOnUiThread {
                    Toast.makeText(
                        context,
                        getString(R.string.toast_server),
                        Toast.LENGTH_SHORT).show()
                }
            }
            catch (e: UnknownHostException) {
                activity!!.runOnUiThread {
                    Toast.makeText(
                        context,
                        getString(R.string.toast_network),
                        Toast.LENGTH_SHORT).show()
                }
            }
            catch (e: ConnectException) {
                activity!!.runOnUiThread {
                    Toast.makeText(
                        context,
                        getString(R.string.toast_network),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            catch (e: Exception) {
                Log.d("ERR", e.toString())
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }
}