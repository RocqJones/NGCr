package com.extrainch.ngao.ui.splash

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.extrainch.ngao.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private var binding : ActivitySplashBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding!!.root)


        // hide the status bar and make the splash screen as a full screen activity
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        binding!!.getStarted.setOnClickListener {
            val i = Intent(this@SplashActivity, CheckUserActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(i)
            finish()
        }
    }
}