package com.extrainch.ngao.ui.referral

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.extrainch.ngao.MainActivity
import com.extrainch.ngao.R
import com.extrainch.ngao.databinding.ActivityReferralBinding
import com.extrainch.ngao.databinding.DialogSuccessBinding

class ReferralActivity : AppCompatActivity() {

    private var binding : ActivityReferralBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReferralBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        binding!!.backBtn.setOnClickListener {
            val i = Intent(this, MainActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(i)
        }

        binding!!.create.setOnClickListener {
            showSuccessDialog(R.style.DialogAnimation_1, "Left - Right Animation!")
        }
    }

    private fun showSuccessDialog(animationSource: Int, s: String) {
        val dialog = Dialog(this)
        val dBinding: DialogSuccessBinding = DialogSuccessBinding.inflate(layoutInflater)
        val v: View = dBinding.root
        dialog.setContentView(v)
        dialog.setCancelable(false)

        Glide.with(this).asGif().load(R.drawable.successgif).error(R.drawable.baseline_error_red_700_24dp)
            .into(dBinding.successGif)

        dialog.window!!.attributes.windowAnimations = animationSource
        dialog.show()
        dialog.setCanceledOnTouchOutside(true)

        // set delay time in milliseconds
        Handler().postDelayed({
            val i = Intent(this, MainActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(i)
            finish()
        }, 1700)
    }
}