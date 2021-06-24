package com.extrainch.ngao.ui.register

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.extrainch.ngao.MainActivity
import com.extrainch.ngao.R
import com.extrainch.ngao.databinding.ActivityRegisterBinding
import com.extrainch.ngao.databinding.DialogAlertBinding
import com.extrainch.ngao.ui.register.viewmodel.ViewModelRegister
import com.extrainch.ngao.ui.splash.CheckUserActivity

class RegisterActivity : AppCompatActivity() {

    private var binding : ActivityRegisterBinding? = null

    private lateinit var url : String
    private lateinit var viewModel: ViewModelRegister

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        binding!!.backBtn.setOnClickListener {
            val i = Intent(this@RegisterActivity, CheckUserActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(i)
        }

        viewModel = ViewModelProvider(this).get(ViewModelRegister::class.java)
        viewModel.webUrl.observe(this, { url = it })

        binding!!.termsAndConditionsStmt.setOnClickListener {
            browserIntent(url)
        }

        binding!!.privacyPolicyStmt.setOnClickListener {
            browserIntent(url)
        }

        binding!!.create.setOnClickListener {
            if (binding!!.checkedPrivacyPolicy.isChecked && binding!!.checkedTermsAndC.isChecked) {
                // proceed to verification
                val j = Intent(this@RegisterActivity, MainActivity::class.java)
                startActivity(j)
            } else {
                verifyCheckBoxDialog(R.style.DialogAnimation_1, "Left - Right Animation!")
            }
        }
    }

    private fun verifyCheckBoxDialog(animationSource: Int, s: String) {
        val dialog = Dialog(this)
        val dBinding: DialogAlertBinding = DialogAlertBinding.inflate(layoutInflater)
        val v: View = dBinding.root
        dialog.setContentView(v)
        dialog.setCancelable(false)

        dBinding.tvResponseId.text = getString(R.string.register_dialog_txt)

        dBinding.dialogButtonOK.setOnClickListener{
            dialog.dismiss()
        }

        dialog.window!!.attributes.windowAnimations = animationSource
        dialog.show()
        dialog.setCanceledOnTouchOutside(true)
    }

    private fun browserIntent(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW)
        browserIntent.data = Uri.parse(url)
        startActivity(browserIntent)
    }
}