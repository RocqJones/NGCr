package com.extrainch.ngao.ui.installment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.extrainch.ngao.databinding.ActivityInstallmentLoanBinding

class InstallmentLoanActivity : AppCompatActivity() {

    private var binding : ActivityInstallmentLoanBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInstallmentLoanBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
    }
}