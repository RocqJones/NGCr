package com.extrainch.ngao.ui.loanrequest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.extrainch.ngao.databinding.ActivityRequestLoanBinding

class RequestLoanActivity : AppCompatActivity() {

    private var binding : ActivityRequestLoanBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestLoanBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

    }
}