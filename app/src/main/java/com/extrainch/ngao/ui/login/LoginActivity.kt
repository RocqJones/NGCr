package com.extrainch.ngao.ui.login

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.extrainch.ngao.MainActivity
import com.extrainch.ngao.R
import com.extrainch.ngao.databinding.ActivityLoginBinding
import com.extrainch.ngao.databinding.DialogAlertBinding
import com.extrainch.ngao.patterns.MySingleton
import com.extrainch.ngao.utils.Constants
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class LoginActivity : AppCompatActivity() {

    private var binding : ActivityLoginBinding? = null

    var preferences: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null
    var SHARED_PREF_NAME = "ngao_pref"

    var password: String? = null

    var NATIONAL_ID = "NationalID"
    var nationalid: String? = null

    var PHONENUMBER = "phoneNumber"
    var phonenumber: String? = null

    var DEVICEID = "DeviceID"
    var deviceid: String? = null

    var IMEI = "Imei"
    var imei: String? = null

    var MACADDRESS = "MacAddress"
    var macaddress: String? = null

    var DEVICENAME = "DeviceName"
    var devicename: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        preferences = this.getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE)
        nationalid = preferences!!.getString(NATIONAL_ID, "NationalID")
        phonenumber = preferences!!.getString(PHONENUMBER, "phoneNumber")
        deviceid = preferences!!.getString(DEVICEID, "DeviceID")
        imei = preferences!!.getString(IMEI, "Imei")
        macaddress = preferences!!.getString(MACADDRESS, "MacAddress")
        devicename = preferences!!.getString(DEVICENAME, "DeviceName")

        binding!!.buttonOne.setOnClickListener {
            (binding!!.editPassword.text.toString().trim() + getString(
                R.string._1)).also { binding!!.editPassword.text = it }
        }

        binding!!.buttonTwo.setOnClickListener {
            (binding!!.editPassword.text.toString().trim() + getString(
                R.string._2)).also { binding!!.editPassword.text = it }
        }
        binding!!.buttonThree.setOnClickListener {
            (binding!!.editPassword.text.toString().trim() + getString(
                R.string._3)).also { binding!!.editPassword.text = it }
        }
        binding!!.buttonFour.setOnClickListener {
            (binding!!.editPassword.text.toString().trim() + getString(
                R.string._4)).also { binding!!.editPassword.text = it }
        }
        binding!!.buttonFive.setOnClickListener {
            (binding!!.editPassword.text.toString().trim() + getString(
                R.string._5)).also { binding!!.editPassword.text = it }
        }
        binding!!.buttonSix.setOnClickListener {
            (binding!!.editPassword.text.toString().trim() + getString(
                R.string._6)).also { binding!!.editPassword.text = it }
        }
        binding!!.buttonSeven.setOnClickListener {
            (binding!!.editPassword.text.toString().trim() + getString(
                R.string._7)).also { binding!!.editPassword.text = it }
        }
        binding!!.buttonEight.setOnClickListener {
            (binding!!.editPassword.text.toString().trim() + getString(
                R.string._8)).also { binding!!.editPassword.text = it }
        }
        binding!!.buttonNine.setOnClickListener {
            (binding!!.editPassword.text.toString().trim() + getString(
                R.string._9)).also { binding!!.editPassword.text = it }
        }
        binding!!.buttonZero.setOnClickListener {
            (binding!!.editPassword.text.toString().trim() + getString(
                R.string._0)).also { binding!!.editPassword.text = it }
        }

        binding!!.btnClear.setOnClickListener {
            var str: String = binding!!.editPassword.text.toString().trim()
            if (str.length > 1) {
                str = str.substring(0, str.length - 1)
                binding!!.editPassword.text = str
            } else if (str.length <= 1) {
                binding!!.editPassword.text = ""
            }
        }

        binding!!.btnOk.setOnClickListener {
            password = binding!!.editPassword.text.toString().trim()
            Log.d("pass", password!!)
            clientLogin(Constants.BASE_URL + "Security/ClientLogin")
        }

    }

    private fun clientLogin(url: String) {
        val jsonObject = JSONObject()

        try {
            jsonObject.put("NationalID", nationalid)
            jsonObject.put("PhoneNumber", phonenumber)
            jsonObject.put("DeviceID", deviceid)
            jsonObject.put("DeviceName", devicename)
            jsonObject.put("Imei", imei)
            jsonObject.put("MacAddress", macaddress)
            jsonObject.put("Password", password)
            Log.d("post login", "$jsonObject")
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val jsonObjectRequest : JsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            url, jsonObject, Response.Listener { response : JSONObject ->
                try {
                    Log.d("response", response.toString())

                    val objectData = JSONObject(response.toString())
                    if (objectData.getString("code").equals("200")) {
                        editor = preferences?.edit()
                        editor!!.putString("token", objectData.getString("data"))
                        editor!!.apply()

                        // from pref
                        val token: String? = preferences!!.getString("token", "token")
                        Log.d("token", token!!)

                        val i = Intent(this, MainActivity::class.java)
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(i)
                        finish()
                    } else {
                        loginFailureDialog(R.style.DialogAnimation_1, "Left - Right Animation!")
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

    private fun loginFailureDialog(animationSource: Int, s: String) {
        val dialog = Dialog(this)
        val dBinding: DialogAlertBinding = DialogAlertBinding.inflate(layoutInflater)
        val v: View = dBinding.root
        dialog.setContentView(v)
        dialog.setCancelable(false)

        dBinding.tvResponseId.text = getString(R.string.wrong_pin)

        dBinding.dialogButtonOK.setOnClickListener{
            dialog.dismiss()
            binding!!.editPassword.text = ""
        }

        dialog.window!!.attributes.windowAnimations = animationSource
        dialog.show()
        dialog.setCanceledOnTouchOutside(true)
        binding!!.editPassword.text = ""
    }
}