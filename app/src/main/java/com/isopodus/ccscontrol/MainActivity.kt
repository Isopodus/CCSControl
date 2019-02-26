package com.isopodus.ccscontrol

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import khttp.post
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import org.json.JSONArray
import java.lang.Exception
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.*
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, MainActivityListener  {
    private val host = "http://ccsystem.in/stat2/ccscontrol/"

    lateinit var sp: SharedPreferences
    private var countersArray = ArrayList<String>()
    private var isActive = true
    private var openedMenu = R.id.nav_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        //load preferences
        sp = getSharedPreferences("SP", Context.MODE_PRIVATE)

        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        //make drawer drag zone wider
        val dragger = drawer_layout.javaClass.getDeclaredField("mLeftDragger")
        dragger.isAccessible = true
        val draggerObj = dragger.get(drawer_layout)
        val mEdgeSize = draggerObj.javaClass.getDeclaredField("mEdgeSize")
        mEdgeSize.isAccessible = true
        val edge = mEdgeSize.getInt(draggerObj)
        mEdgeSize.setInt(draggerObj, edge * 5)

        //set listener
        nav_view.setNavigationItemSelectedListener(this)

        //open info fragment by default
        nav_view.setCheckedItem(R.id.nav_home)
        onNavigationItemSelected(nav_view.menu.findItem(R.id.nav_home))

        //check login
        if(!sp.getBoolean("LOGIN", false))
            openLoginActivity()

        //set username
        nav_view.getHeaderView(0).usernameView.text = sp.getString("USERNAME", "null")

        //-- get data --//
        updateData()

        //refresh every 30 sec
        scheduledRefresh()
    }

    override fun getOpenedMenu(): Int {
        return openedMenu
    }

    override fun setOpenedMenu(id: Int) {
        openedMenu = id
        nav_view.setCheckedItem(openedMenu)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        try {
            when (item.itemId) {
                R.id.nav_home -> {
                    openInfoFragment()
                }
                R.id.nav_state -> {
                    openStateFragment()
                }
                R.id.nav_settings -> {
                    openSettingsFragment()
                }
                R.id.nav_send -> {
                    val emailIntent = Intent(Intent.ACTION_SENDTO)

                    emailIntent.data = Uri.parse("mailto: coffeecountersystem@gmail.com")
                    emailIntent.putExtra(
                        Intent.EXTRA_SUBJECT,
                        getString(R.string.email_message1) + sp.getString("USERNAME", "null"))
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "")

                    try {
                        startActivity(Intent.createChooser(emailIntent,  getString(R.string.email_email)))
                    } catch (ex: android.content.ActivityNotFoundException) { }
                }
                R.id.nav_logout -> {
                    sp.edit().putBoolean("LOGIN", false).apply()
                    sp.edit().putString("USERNAME", "null").apply()
                    openLoginActivity()
                }
            }

            drawer_layout.closeDrawer(GravityCompat.START)
        }
        catch(e:Exception)
        {
            e.printStackTrace()
        }
        return true
    }

    private fun openLoginActivity(){
        val i = Intent(this, LoginActivity::class.java)
        startActivity(i)
        finish()
    }
    private fun openInfoFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        val fragment = InfoFragment()

        transaction.replace(R.id.fragment_container, fragment,"infoFragment")
        transaction.addToBackStack(null)
        transaction.commit()

        openedMenu =  R.id.nav_home
    }
    override fun openStateFragment(chosenCounter: String) {
        val transaction = supportFragmentManager.beginTransaction()
        val fragment = StateFragment()

        //put preloaded counters list
        val bundle = Bundle()
        bundle.putStringArrayList("countersArray", countersArray)
        bundle.putString("chosenCounter", chosenCounter)
        fragment.arguments = bundle

        transaction.replace(R.id.fragment_container, fragment,"stateFragment")
        transaction.addToBackStack(null)
        transaction.commit()

        openedMenu =  R.id.nav_state
    }
    private fun openSettingsFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        val fragment = SettingsFragment()

        //put preloaded counters list
        val bundle = Bundle()
        bundle.putStringArrayList("countersArray", countersArray)
        fragment.arguments = bundle

        transaction.replace(R.id.fragment_container, fragment,"settingsFragment")
        transaction.addToBackStack(null)
        transaction.commit()

        openedMenu =  R.id.nav_settings
    }

    private fun updateData(){
        val transaction = supportFragmentManager.beginTransaction()
        countersArray = getCounters()

        //refresh info fragment
        val oldFragInfo = supportFragmentManager.findFragmentByTag("infoFragment") as?  InfoFragment
        if(oldFragInfo != null && oldFragInfo.isVisible)
            oldFragInfo.getInfo(Date())

        /*//refresh settings fragment
        val oldFragSettings = supportFragmentManager.findFragmentByTag("settingsFragment")
        if(oldFragSettings != null && oldFragSettings.isVisible) {
            openSettingsFragment()
        }*/

        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun getCounters() : ArrayList<String> {
        val countersArray = ArrayList<String>()
        thread{
            try
            {
                val username = sp.getString("USERNAME", "null")

                val payload = mapOf("user" to username)
                val response = post(host + "getCounters.php", data = payload)

                val jsonCounters = JSONArray(response.text)

                for (i in 0 until jsonCounters.length())
                {
                    countersArray.add(jsonCounters.getJSONObject(i).optString("counter"))
                }
                countersArray.sort()
            }
            catch (e: Resources.NotFoundException) {
                runOnUiThread {
                    Toast.makeText(
                        this,
                        getString(R.string.toast_server),
                        Toast.LENGTH_SHORT).show()
                }
            }
            catch (e: UnknownHostException) {
                runOnUiThread {
                    Toast.makeText(
                        this,
                        getString(R.string.toast_network),
                        Toast.LENGTH_SHORT).show()
                }
            }
            catch (e: ConnectException) {
                runOnUiThread {
                    Toast.makeText(
                        this,
                        getString(R.string.toast_network),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            catch (e: Exception) {
                Log.d("ERR", e.toString())
            }
        }
        return countersArray
    }

    //refresh tiles every 30 seconds
    private fun scheduledRefresh() {
        if(isActive) {
            Handler().postDelayed(object : Runnable {
                override fun run() {
                    if(isActive) {
                        updateData()
                        Handler().postDelayed(this, 30000)
                    }
                }
            }, 30000)
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = run {
        super.onOptionsItemSelected(item)
    }

    public override fun onResume() {
        super.onResume()
        isActive = true
    }

    public override fun onPause() {
        super.onPause()
        isActive = false
    }
    public override fun onStop() {
        super.onStop()
        isActive = false
    }
    public override fun onDestroy() {
        super.onDestroy()
        isActive = false
    }
}
