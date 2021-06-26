package com.extrainch.ngao.ui.register

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.extrainch.ngao.R
import com.extrainch.ngao.databinding.ActivityRegisterBinding
import com.extrainch.ngao.databinding.DialogAlertBinding
import com.extrainch.ngao.patterns.MySingleton
import com.extrainch.ngao.service.ReceiveSMS
import com.extrainch.ngao.ui.login.LoginActivity
import com.extrainch.ngao.ui.register.viewmodel.ViewModelRegister
import com.extrainch.ngao.ui.splash.CheckUserActivity
import com.extrainch.ngao.utils.Constants
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private var binding : ActivityRegisterBinding? = null

    var preferences: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null
    var SHARED_PREF_NAME = "ngao_pref"

    var idNumber: String? = null
    var phoneNo: String? = null
    private lateinit var url : String
    private lateinit var viewModel: ViewModelRegister

    private val MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        requestSmsPermissions()

        requestStoragePermission()

        preferences = this.getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE)

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
                idNumber = binding!!.idNumber.text.toString().trim()
                phoneNo = binding!!.phoneNumber.text.toString().trim()
                // proceed to registration
                val url: String = Constants.BASE_URL + "Security/ClientRegister"
                clientRegistration(url)
            } else if(TextUtils.isEmpty(binding!!.phoneNumber.toString()) && TextUtils.isEmpty(
                    binding!!.idNumber.toString()
                )) {
                Toast.makeText(this, "Enter all fields!", Toast.LENGTH_SHORT).show()
            } else {
                verifyCheckBoxDialog(R.style.DialogAnimation_1, "Left - Right Animation!")
            }
        }
    }

    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_PHONE_STATE
            )
        ) {
            AlertDialog.Builder(this)
                .setTitle("Permission Needed")
                .setMessage("Permission is needed to access files from your device...")
                .setPositiveButton(
                    "OK"
                ) { _, _ ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_PHONE_STATE),
                        this.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE
                    )
                }
                .setNegativeButton(
                    "Cancel"
                ) { dialog, _ -> dialog.dismiss() }.create().show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                this.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE
            )
        }
    }

    private fun requestSmsPermissions() {
        val smsPermission = Manifest.permission.RECEIVE_SMS
        val grant = ContextCompat.checkSelfPermission(this, smsPermission)
        //check if read SMS permission is granted or not
        if (grant != PackageManager.PERMISSION_GRANTED) {
            val permissionList = arrayOfNulls<String>(1)
            permissionList[0] = smsPermission
            ActivityCompat.requestPermissions(this, permissionList, 1)
        }
    }

    private fun clientRegistration(url: String) {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("NationalID", idNumber)
            jsonObject.put("PhoneNumber", phoneNo)
            jsonObject.put("DeviceID", preferences!!.getString("DeviceID", "DeviceID")!!)
            jsonObject.put("DeviceName", preferences!!.getString("DeviceName", "DeviceName")!!)
            jsonObject.put("Imei", preferences!!.getString("Imei", "Imei")!!)
            jsonObject.put("MacAddress", preferences!!.getString("MacAddress", "MacAddress")!!)
            Log.d("post clientDetails", "$jsonObject")
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val jsonObjectRequest : JsonObjectRequest = object : JsonObjectRequest(Method.POST,
            url, jsonObject, Response.Listener { response : JSONObject ->
                try {
                    Log.d("response", response.toString())

                    val jsnObject = JSONObject(response.toString())

                    if (jsnObject.getString("code").equals("500")) {
                        val msg = JSONObject(jsnObject.getString("msg")).toString()
                        warnDialog(msg, R.style.DialogAnimation_1, "Left - Right Animation!")
                    } else if (jsnObject.getString("code").equals("200")){
                        val data = JSONObject(jsnObject.getString("data"))
                        val phoneNumber = data.getString(data.getString("phoneNumber"))

                        ReceiveSMS().setEditText(binding!!.edtVerificationCode)

                        editor = preferences!!.edit()
                        editor!!.putString("NationalID", binding!!.idNumber.toString())
                        editor!!.putString("clientID", data.getString("clientID"))
                        editor!!.putString("ourBranchID", data.getString("ourBranchID"))
                        editor!!.putString("phoneNumber", phoneNumber)
                        editor!!.putString("mbdAccountID", data.getString("mbdAccountID"))
                        editor!!.putString("name", data.getString("name"))
                        editor!!.putString("loanAccountID", data.getString("loanAccountID"))
                        editor!!.putString("accountID", data.getString("accountID"))
                        editor!!.putString("reminder", data.getString("reminder"))
                        editor!!.apply()

                        enableDisableFields()
                        warnDialog(
                            data.getString("remarks").toString(),
                            R.style.DialogAnimation_1,
                            "Left - Right Animation!"
                        )
                    } else {
                        val dialog = Dialog(this)
                        val dBinding: DialogAlertBinding = DialogAlertBinding.inflate(layoutInflater)
                        val v: View = dBinding.root
                        dialog.setContentView(v)
                        dialog.setCancelable(false)

                        dBinding.tvResponseId.text = getString(R.string.verify_number)

                        dBinding.dialogButtonOK.setOnClickListener{
                            dialog.dismiss()
                            verifyCode("https://192.168.1.11:8090/SupremeApp/api/Security/ClientVerifyCode")
                        }

                        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation_1
                        dialog.show()
                        dialog.setCanceledOnTouchOutside(true)
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

    private fun verifyCode(s: String) {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("NationalID", preferences!!.getString("idNumber", "idNumber"))
            jsonObject.put("ClientID", preferences!!.getString("clientID", "clientID"))
            jsonObject.put("PhoneNumber", preferences!!.getString("phoneNumber", "phoneNumber"))
            jsonObject.put("DeviceID", preferences!!.getString("DeviceID", "DeviceID"))
            jsonObject.put("DeviceName", preferences!!.getString("DeviceName", "DeviceName"))
            jsonObject.put("Imei", preferences!!.getString("Imei", "Imei"))
            jsonObject.put("MacAddress", preferences!!.getString("MacAddress", "MacAddress"))
            jsonObject.put("VerificationCode", binding!!.edtVerificationCode)
            jsonObject.put("Password", binding!!.edtPassword.toString())

            Log.d("post verify", "$jsonObject")
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val jsonObjectRequest : JsonObjectRequest = object : JsonObjectRequest(Method.POST,
            s, jsonObject, Response.Listener { response : JSONObject ->
                try {
                    Log.d("response", response.toString())

                    val objRes = JSONObject(response.toString())

                    if (objRes.getString("code").equals("200")) {
                        editor = preferences!!.edit()
                        editor!!.putString("clientID", objRes.getString("data"))
                        editor!!.apply()

                        updateClientPassword(Constants.BASE_URL + "Security/UpdateClientPassword")
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

    private fun updateClientPassword(url: String) {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("NationalID", preferences!!.getString("idNumber", "idNumber"))
            jsonObject.put("ClientID", preferences!!.getString("clientID", "clientID"))
            jsonObject.put("PhoneNumber", preferences!!.getString("phoneNumber", "phoneNumber"))
            jsonObject.put("DeviceID", preferences!!.getString("DeviceID", "DeviceID"))
            jsonObject.put("DeviceName", preferences!!.getString("DeviceName", "DeviceName"))
            jsonObject.put("Imei", preferences!!.getString("Imei", "Imei"))
            jsonObject.put("MacAddress", preferences!!.getString("MacAddress", "MacAddress"))
            jsonObject.put("VerificationCode", binding!!.edtVerificationCode)
            jsonObject.put("Password", binding!!.edtPassword.toString())
            Log.d("post update pass", "$jsonObject")
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val jsonObjectRequest : JsonObjectRequest = object : JsonObjectRequest(Method.POST,
            url, jsonObject, Response.Listener { response : JSONObject ->
                try {
                    Log.d("response", response.toString())

                    val objRes = JSONObject(response.toString())

                    if (objRes.getString("code").equals("200")) {
                        // channels
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val mNotificationManager =
                                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                            val importance = NotificationManager.IMPORTANCE_HIGH
                            val mChannel = NotificationChannel(
                                Constants.CHANNEL_ID,
                                Constants.CHANNEL_NAME,
                                importance
                            )
                            mChannel.description = Constants.CHANNEL_DESCRIPTION
                            mChannel.enableLights(true)
                            mChannel.lightColor = Color.RED
                            mChannel.enableVibration(true)
                            mChannel.vibrationPattern =
                                longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                            mNotificationManager.createNotificationChannel(mChannel)
                        }

                        val j = Intent(this, LoginActivity::class.java)
                        startActivity(j)
                        finish()
                    } else if (objRes.getString("code").equals("500")) {
                        enableDisableFields()
                        val msg = JSONObject(objRes.getString("msg")).toString()
                        warnDialog(msg, R.style.DialogAnimation_1, "Left - Right Animation!")
                    } else {
                        val msg = JSONObject(objRes.getString("msg")).toString()
                        warnDialog(msg, R.style.DialogAnimation_1, "Left - Right Animation!")
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

    private fun enableDisableFields() {
        binding!!.firstView.visibility = View.GONE
        binding!!.secondView.visibility = View.VISIBLE
        binding!!.tvTxtDetails.text = phoneNo

        object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding!!.tvTimer.text = "" + millisUntilFinished / 1000
                //here you can have your logic to set text to edittext
            }

            override fun onFinish() {
                binding!!.tvTimer.text = "RESEND"
            }
        }.start()
    }

    private fun warnDialog(msg: String, dialoganimation1: Int, s: String) {
        val dialog = Dialog(this)
        val dBinding: DialogAlertBinding = DialogAlertBinding.inflate(layoutInflater)
        val v: View = dBinding.root
        dialog.setContentView(v)
        dialog.setCancelable(false)

        dBinding.tvResponseId.text = msg

        dBinding.dialogButtonOK.setOnClickListener{
            dialog.dismiss()
        }

        dialog.window!!.attributes.windowAnimations = dialoganimation1
        dialog.show()
        dialog.setCanceledOnTouchOutside(true)
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
            permissionPhone()
        }

        dialog.window!!.attributes.windowAnimations = animationSource
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

    private fun permissionPhone() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_PHONE_STATE
                )
            ) {
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_PHONE_STATE),
                    this.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE
                )
            }
        } else {
            requestStoragePermission()
        }
    }

    private fun browserIntent(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW)
        browserIntent.data = Uri.parse(url)
        startActivity(browserIntent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == this.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Thanks for enabling the permission", Toast.LENGTH_SHORT)
                    .show()

                //do something permission is allowed here....
            } else {
                Toast.makeText(this, "Please allow the Permission", Toast.LENGTH_SHORT).show()
            }
        }
    }
}