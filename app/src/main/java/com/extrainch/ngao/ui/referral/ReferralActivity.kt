package com.extrainch.ngao.ui.referral

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.Glide
import com.extrainch.ngao.MainActivity
import com.extrainch.ngao.R
import com.extrainch.ngao.databinding.ActivityReferralBinding
import com.extrainch.ngao.databinding.DialogAlertBinding
import com.extrainch.ngao.databinding.DialogSuccessBinding
import com.extrainch.ngao.patterns.MySingleton
import com.extrainch.ngao.utils.Constants
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class ReferralActivity : AppCompatActivity() {

    private var binding : ActivityReferralBinding? = null

    var preferences: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null
    var SHARED_PREF_NAME = "ngao_pref"

    var token: String? = null
    var name: String? = null
    var phoneNo: String? = null

    var PHONENUMBER = "phoneNumber"
    var NAME = "name"
    var TOKEN = "token"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReferralBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        preferences = this.getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE)
        token = preferences!!.getString(TOKEN, "token")
        name = preferences!!.getString("name", "name")
        phoneNo = preferences!!.getString(PHONENUMBER, "phoneNumber")

        binding!!.backBtn.setOnClickListener {
            val i = Intent(this, MainActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(i)
        }

        binding!!.create.setOnClickListener {
            customerReferral(Constants.BASE_URL + "MobileLoan/CustomerReferal")
        }
    }

    private fun customerReferral(url: String) {
        if (TextUtils.isEmpty(binding!!.fName.toString()) && TextUtils.isEmpty(binding!!.phoneNo.toString())) {
            emptyFieldsDialog(R.style.DialogAnimation_1, "Left - Right Animation!")
        } else {
            val nameG = binding!!.fName.text.toString().trim()
            val mobileG= binding!!.phoneNo.text.toString().trim()

            val jsonObject = JSONObject()

            try {
                jsonObject.put("RefereeName", name)
                jsonObject.put("RefereeMobile", phoneNo)
                jsonObject.put("ReferalName", nameG)
                jsonObject.put("ReferalMobile", mobileG)
                jsonObject.put("TokenCode", token)

                Log.d("post referral", "$jsonObject")
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            val jsonObjectRequest : JsonObjectRequest = object : JsonObjectRequest(
                Method.POST,
                url, jsonObject, Response.Listener { response : JSONObject ->
                    try {
                        Log.d("response ", response.toString())

                        val objectData = JSONObject(response.toString())
                        if (objectData.getString("code").equals("200")) {
                            showSuccessDialog(R.style.DialogAnimation_1, "Left - Right Animation!")
                        } else {
                            createFailureDialog(R.style.DialogAnimation_1, "Left - Right Animation!")
                        }
                    } catch (e : Exception) {
                        e.printStackTrace()
                    }

                }, Response.ErrorListener { error: VolleyError ->
                    Log.e("conn error", error.toString())
                    // dialog
                    connDialog(R.style.DialogAnimation_1, "Left - Right Animation!")
                }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params["Content-Type"] = "application/json"
                    //params["Authorization"] = "Bearer $token"
                    return params
                }

                override fun getBodyContentType(): String {
                    return "application/json"
                }
            }
            jsonObjectRequest.retryPolicy = DefaultRetryPolicy(60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

            MySingleton.getInstance(this.applicationContext)!!.addToRequestQueue(jsonObjectRequest)
        }
    }

    private fun createFailureDialog(dialogAnimation1: Int, s: String) {
        val dialog = Dialog(this)
        val dBinding: DialogAlertBinding = DialogAlertBinding.inflate(layoutInflater)
        val v: View = dBinding.root
        dialog.setContentView(v)
        dialog.setCancelable(false)

        dBinding.tvResponseId.text = getString(R.string.something_went_wrong)

        dBinding.dialogButtonOK.setOnClickListener{
            dialog.dismiss()
            binding!!.fName.text.clear()
            binding!!.phoneNo.text.clear()
        }

        dialog.window!!.attributes.windowAnimations = dialogAnimation1
        dialog.show()
        dialog.setCanceledOnTouchOutside(true)
        binding!!.fName.text.clear()
        binding!!.phoneNo.text.clear()
    }

    private fun connDialog(dialogAnimation1: Int, s: String) {
        val dialog = Dialog(this)
        val dBinding: DialogAlertBinding = DialogAlertBinding.inflate(layoutInflater)
        val v: View = dBinding.root
        dialog.setContentView(v)
        dialog.setCancelable(false)

        dBinding.tvResponseId.text = getString(R.string.conn_error)

        dBinding.dialogButtonOK.setOnClickListener{
            dialog.dismiss()
        }

        dialog.window!!.attributes.windowAnimations = dialogAnimation1
        dialog.show()
        dialog.setCanceledOnTouchOutside(true)
    }

    private fun emptyFieldsDialog(dialogAnimation: Int, s: String) {
        val dialog = Dialog(this)
        val dBinding: DialogAlertBinding = DialogAlertBinding.inflate(layoutInflater)
        val v: View = dBinding.root
        dialog.setContentView(v)
        dialog.setCancelable(false)

        dBinding.tvResponseId.text = getString(R.string.enter_fields)

        dBinding.dialogButtonOK.setOnClickListener{
            dialog.dismiss()
            binding!!.fName.text.clear()
            binding!!.phoneNo.text.clear()
        }

        dialog.window!!.attributes.windowAnimations = dialogAnimation
        dialog.show()
        dialog.setCanceledOnTouchOutside(true)
        binding!!.fName.text.clear()
        binding!!.phoneNo.text.clear()
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