package com.isopodus.ccscontrol

//import android.view.MenuItem

interface MainActivityListener {
    //fun onNavigationItemSelected(item: MenuItem): Boolean
    fun openStateFragment(chosenCounter: String  = "krv000000" )
    fun getOpenedMenu(): Int
    fun setOpenedMenu(id: Int)
    fun setOverflowMenuButtonVisibility(visible: Int)
}