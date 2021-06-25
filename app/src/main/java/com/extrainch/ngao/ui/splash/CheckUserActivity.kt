package com.extrainch.ngao.ui.splash

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.extrainch.ngao.MainActivity
import com.extrainch.ngao.databinding.ActivityCheckUserBinding
import com.extrainch.ngao.ui.register.RegisterActivity


class CheckUserActivity : AppCompatActivity() {

    private var binding : ActivityCheckUserBinding? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckUserBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        binding!!.register.setOnClickListener {
            val i = Intent(this@CheckUserActivity, RegisterActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(i)
        }

        binding!!.proceed.setOnClickListener {
            val identificationNo = binding!!.phoneNo.text.toString().trim()
            if (identificationNo.isEmpty()) {
                Toast.makeText(this, "Phone No Required!", Toast.LENGTH_SHORT).show()
            } else {
                // proceed and check if the user exists
                val j = Intent(this@CheckUserActivity, MainActivity::class.java)
                j.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(j)
                finish()
            }
        }
    }
}