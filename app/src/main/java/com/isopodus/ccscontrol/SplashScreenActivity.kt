package com.isopodus.ccscontrol

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.animation.Animation
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import kotlinx.android.synthetic.main.activity_splash.*


class SplashScreenActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val fadeOut = AlphaAnimation(0f, 1f)
        fadeOut.interpolator = AccelerateInterpolator()
        fadeOut.duration = 1000

        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation) {
                val main = Intent(applicationContext, MainActivity::class.java)
                startActivity(main)
                finish()
            }
            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationStart(animation: Animation) {}
        })

        splashImage.startAnimation(fadeOut)
    }
}