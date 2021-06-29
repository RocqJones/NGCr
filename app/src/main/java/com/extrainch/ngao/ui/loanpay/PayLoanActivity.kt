package com.extrainch.ngao.ui.loanpay

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.extrainch.ngao.databinding.ActivityPayLoanBinding

class PayLoanActivity : AppCompatActivity() {

    private var binding : ActivityPayLoanBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPayLoanBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
    }
}