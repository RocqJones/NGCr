package com.extrainch.ngao.ui.register

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.extrainch.ngao.databinding.ActivityRegisterBinding
import com.extrainch.ngao.ui.splash.CheckUserActivity

class RegisterActivity : AppCompatActivity() {
    private var binding : ActivityRegisterBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        binding!!.backBtn.setOnClickListener {
            val i = Intent(this@RegisterActivity, CheckUserActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(i)
        }
    }
}