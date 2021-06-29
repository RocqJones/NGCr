package com.extrainch.ngao.ui.loanadvanced

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.extrainch.ngao.databinding.ActivityAdvancedLoanBinding

class AdvancedLoanActivity : AppCompatActivity() {

    private var binding : ActivityAdvancedLoanBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdvancedLoanBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
    }
}