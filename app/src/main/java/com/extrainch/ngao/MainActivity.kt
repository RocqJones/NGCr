package com.extrainch.ngao

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.extrainch.ngao.databinding.ActivityMainBinding
import com.extrainch.ngao.databinding.DialogAlertBinding
import com.extrainch.ngao.databinding.DialogCustomizableBinding
import com.extrainch.ngao.patterns.MySingleton
import com.extrainch.ngao.ui.loanrequest.RequestLoanActivity
import com.extrainch.ngao.ui.referral.ReferralActivity
import com.extrainch.ngao.ui.splash.SplashActivity
import com.extrainch.ngao.utils.Constants
import org.json.JSONException
import org.json.JSONObject
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity() {

    private var binding : ActivityMainBinding? = null

    var preferences: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null
    var SHARED_PREF_NAME = "ngao_pref"

    var TOKEN = "token"
    var token: String? = null

    var CLIENTID = "clientID"
    var clientId: String? = null

    var OURBRANCHID = "OurBranchID"
    var ourbranchid: String? = null

    var loanLimit: String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        binding!!.requestLoan.setOnClickListener {
            val i = Intent(this, RequestLoanActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(i)
        }

        preferences = this.getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE)
        token = preferences!!.getString(TOKEN, "token")
        clientId = preferences!!.getString(CLIENTID, "clientID")

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

        showLoanLimit(Constants.BASE_URL + "MobileLoan/GetLoanLimit")

        binding!!.loanLimiCard.setOnClickListener {
            ourbranchid = preferences!!.getString(OURBRANCHID, "OurBranchID")
            fetchClientPortFolio(Constants.BASE_URL + "MobileClient/FetchClientAccountBalances")
        }
    }

    private fun fetchClientPortFolio(url: String) {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("OurBranchID", ourbranchid)
            jsonObject.put("ClientID", clientId)
            jsonObject.put("TokenCode", token)

            Log.d("post referral", "$jsonObject")
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val jsonObjectRequest : JsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            url, jsonObject, Response.Listener { response : JSONObject ->
                try {
                    Log.d("response loanB", response.toString())

                    val objectData = JSONObject(response.toString())
                    if (objectData.getString("code").equals("200")) {

                        val data = JSONObject(objectData.getString("data"))
                        loanLimit = data.getString("clientID")
                        //popupDialog(R.style.DialogAnimation_1, "Left - Right Animation!")
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

    private fun showLoanLimit(url: String) {
        val jsonObject = JSONObject()

        try {
            jsonObject.put("ClientID", clientId)
            jsonObject.put("TokenCode", token)

            Log.d("post referral", "$jsonObject")
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val jsonObjectRequest : JsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            url, jsonObject, Response.Listener { response : JSONObject ->
                try {
                    Log.d("response lLimit", response.toString())

                    val objectData = JSONObject(response.toString())
                    if (objectData.getString("code").equals("200")) {
                        val data = JSONObject(objectData.getString("data"))
                        loanLimit = data.getString("loanLimit")

                        editor = preferences?.edit()
                        editor!!.putString("ourBranchID", data.getString("ourBranchID"))
                        editor!!.apply()

                        binding!!.loanLimit.text = loanLimit
                        popupDialog(R.style.DialogAnimation_1, "Left - Right Animation!")
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

    private fun popupDialog(dialogAnimation1: Int, s: String) {
        val dialog = Dialog(this)
        val dBinding: DialogCustomizableBinding = DialogCustomizableBinding.inflate(layoutInflater)
        val v: View = dBinding.root
        dialog.setContentView(v)
        dialog.setCancelable(false)

        dBinding.textOne.text = getString(R.string.successtxt)
        dBinding.textTwo.text = "Your loan limit " + loanLimit

        dBinding.dialogButtonOK.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window!!.attributes.windowAnimations = dialogAnimation1
        dialog.show()
        dialog.setCanceledOnTouchOutside(true)
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

    private fun createFailureDialog(dialogAnimation1: Int, s: String) {
        val dialog = Dialog(this)
        val dBinding: DialogAlertBinding = DialogAlertBinding.inflate(layoutInflater)
        val v: View = dBinding.root
        dialog.setContentView(v)
        dialog.setCancelable(false)

        dBinding.tvResponseId.text = getString(R.string.something_went_wrong)

        dBinding.dialogButtonOK.setOnClickListener{
            dialog.dismiss()
        }

        dialog.window!!.attributes.windowAnimations = dialogAnimation1
        dialog.show()
        dialog.setCanceledOnTouchOutside(true)
    }
}