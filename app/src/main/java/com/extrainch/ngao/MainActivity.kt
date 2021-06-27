package com.extrainch.ngao

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.extrainch.ngao.databinding.ActivityMainBinding
import com.extrainch.ngao.ui.referral.ReferralActivity
import com.extrainch.ngao.ui.splash.SplashActivity
import java.time.Instant
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private var binding : ActivityMainBinding? = null

    var preferences: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null
    var SHARED_PREF_NAME = "ngao_pref"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        preferences = this.getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE)
        if (preferences!!.contains("name")) {
            binding!!.fullName.text = preferences!!.getString("name", "name")
        } else {
            val x = Intent(this, SplashActivity::class.java)
            x.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(x)
        }

        // set greetings
        val currentTime = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        Log.d("time", currentTime)
        val ct: List<String> = currentTime.split("T")
        val currentT = ct[1]
        val greetings = arrayOf("Good morning,", "Good afternoon,", "Good evening", "Good night,", "Hello,")
        if (currentT >= "00:00:00.000Z" && currentT <= "11:59:59.000Z") {
            binding!!.greetings.text = greetings[0]
        } else if (currentT >= "12:00:00.000Z" && currentT <= "17:59:59.999Z") {
            binding!!.greetings.text = greetings[1]
        } else if (currentT >= "18:00:00.000Z" && currentT <= "21:59:59.999Z") {
            binding!!.greetings.text = greetings[2]
        } else if (currentT >= "22:00:00.000Z" && currentT <= "23:59:59.999Z") {
            binding!!.greetings.text = greetings[3]
        } else {
            binding!!.greetings.text = greetings[4]
        }

        binding!!.referralCard.setOnClickListener {
            val p = Intent(this@MainActivity, ReferralActivity::class.java)
            startActivity(p)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val editor = preferences!!.edit()
        editor.remove("token")
        editor.apply()
    }
}