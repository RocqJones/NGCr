package com.extrainch.ngao.ui.splash

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.extrainch.ngao.R
import com.extrainch.ngao.databinding.ActivitySplashBinding
import com.extrainch.ngao.databinding.DialogAlertBinding
import com.extrainch.ngao.databinding.DialogCustomizableBinding
import com.extrainch.ngao.patterns.MySingleton
import com.extrainch.ngao.ui.login.LoginActivity
import com.extrainch.ngao.ui.register.RegisterActivity
import com.extrainch.ngao.utils.Constants
import org.json.JSONException
import org.json.JSONObject
import java.net.NetworkInterface
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


class SplashActivity : AppCompatActivity() {

    private var binding : ActivitySplashBinding? = null

    private val MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0
    // var imeiCode = ""

    var preferences: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null
    var SHARED_PREF_NAME = "ngao_pref"

    var NATIONAL_ID = "NationalID"
    var NationalID: String? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        // hide the status bar and make the splash screen as a full screen activity
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // imei
        getDeviceIMEI()

        // mac address
        getMacAddress()

        // phone name
        getPhoneName()

        // device id. Don't use this it changes = 2de88172-dc13-41b0-ba49-eeaf7c307ba6
        val deviceId : String = UUID.randomUUID().toString()

        Log.d("phone data", "${getDeviceIMEI()}, ${getMacAddress()}, ${getPhoneName()}, $deviceId")
        editor = preferences!!.edit()
        editor!!.putString("deviceImei", getDeviceIMEI().toString())
        editor!!.putString("deviceMac", getMacAddress())
        editor!!.putString("deviceName", getPhoneName().toString())
        editor!!.putString("deviceId", deviceId)
        editor!!.apply()

        // prefs
        preferences = this.getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE)
        NationalID = preferences!!.getString(NATIONAL_ID, "NationalID")

        binding!!.getStarted.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.READ_PHONE_STATE
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                // READ_PHONE_STATE permission has not been granted.
                buildClientDetails(R.style.DialogAnimation_1, "Left - Right Animation!")
            } else {
                // send data to db
                val url: String = Constants.BASE_URL + "MobileClient/ClientDetail"
                sendDeviceDetails(url)
            }
        }
    }

    private fun sendDeviceDetails(url: String) {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("NationalID", "")
            jsonObject.put("ClientID", "")
            jsonObject.put("PhoneNumber", "")
            jsonObject.put("DeviceID", getDeviceIMEI())
            jsonObject.put("DeviceName", getPhoneName())
            jsonObject.put("Imei", getDeviceIMEI())
            jsonObject.put("MacAddress", getMacAddress())
            Log.d("clientDetails", "$jsonObject")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        HttpsTrustManager.allowAllSSL()

        val jsonObjectRequest : JsonObjectRequest = object :JsonObjectRequest(Method.POST,
            url, jsonObject, Response.Listener { response : JSONObject ->
                try {
                    Log.d("response", response.toString())

                    val jsnObject = JSONObject(response.toString())

                    if (jsnObject.getString("code").equals("200")) {
                        val data = JSONObject(jsnObject.getString("data"))

                        // write responses to shared prefs
                        editor = preferences!!.edit()
                        editor!!.putString("clientID", data.getString("clientID"))
                        editor!!.putString("ourBranchID", data.getString("ourBranchID"))
                        editor!!.putString("phoneNumber", data.getString("phoneNumber"))
                        editor!!.putString("mbdAccountID", data.getString("mbdAccountID"))
                        editor!!.putString("name", data.getString("name"))
                        editor!!.putString("loanAccountID", data.getString("loanAccountID"))
                        editor!!.putString("accountID", data.getString("accountID"))
                        editor!!.putString("reminder", data.getString("reminder"))
                        editor!!.apply()

                        // test access prefs
                        Log.d("reminder", preferences!!.getString("reminder", "reminder")!!)
                        popupDialog(R.style.DialogAnimation_1, "Left - Right Animation!")
                        intentToNext()
                    } else {
                        val r = Intent(this, RegisterActivity::class.java)
                        startActivity(r)
                        finish()
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

    private fun popupDialog(dialogAnimation: Int, s: String) {
        val dialog = Dialog(this)
        val dBinding: DialogCustomizableBinding = DialogCustomizableBinding.inflate(layoutInflater)
        val v: View = dBinding.root
        dialog.setContentView(v)
        dialog.setCancelable(false)

        dBinding.textOne.text = getString(R.string.successtxt)
        dBinding.textTwo.text = preferences!!.getString("reminder", "reminder")!!

        dialog.window!!.attributes.windowAnimations = dialogAnimation
        dialog.show()
        dialog.setCanceledOnTouchOutside(true)

        // set delay time in milliseconds
        Handler().postDelayed({
            val i = Intent(this, LoginActivity::class.java)
            startActivity(i)
            finish()
        }, 2000)
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

    @SuppressLint("HardwareIds")
    private fun getDeviceIMEI(): String? {
        var deviceUniqueIdentifier: String?
        this.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        deviceUniqueIdentifier = Settings.Secure.getString(
            applicationContext.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        if (null == deviceUniqueIdentifier || deviceUniqueIdentifier.isEmpty()) {
            deviceUniqueIdentifier =
                Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        }
        return deviceUniqueIdentifier
    }

    private fun getMacAddress(): String {
        try {
            val all: List<NetworkInterface> =
                Collections.list(NetworkInterface.getNetworkInterfaces())
            for (nif in all) {
                if (!nif.name.equals("wlan0", ignoreCase = true)) continue
                val macBytes = nif.hardwareAddress ?: return ""
                val res1 = StringBuilder()
                for (b in macBytes) {
                    // res1.append(Integer.toHexString(b & 0xFF) + ":");
                    res1.append(String.format("%02X:", b))
                }
                if (res1.isNotEmpty()) {
                    res1.deleteCharAt(res1.length - 1)
                }
                return res1.toString()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return "02:00:00:00:00:00"
    }

    private fun getPhoneName(): String? {
        return Build.MODEL
    }

    private fun buildClientDetails(animationSource: Int, s: String) {
        val dialog = Dialog(this)
        val dBinding: DialogAlertBinding = DialogAlertBinding.inflate(layoutInflater)
        val v: View = dBinding.root
        dialog.setContentView(v)
        dialog.setCancelable(false)

        dBinding.tvResponseId.text = getString(R.string.getstarted_dialog_txt)

        dBinding.dialogButtonOK.setOnClickListener{
            dialog.dismiss()
            requestReadPhoneStatePermission()
        }

        dialog.window!!.attributes.windowAnimations = animationSource
        dialog.show()
        dialog.setCanceledOnTouchOutside(true)
    }

    private fun requestReadPhoneStatePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_PHONE_STATE
            )
        ) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
        } else {
            // READ_PHONE_STATE permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_PHONE_STATE),
                this.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE
            )
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == this.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE) {
            // Received permission result for READ_PHONE_STATE permission.est.");
            // Check if the only required permission has been granted
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("text", "does nothing")
            }
        } else {
            buildClientDetails(R.style.DialogAnimation_1, "Left - Right Animation!")
        }
    }

    private fun intentToNext() {
        val i = Intent(this@SplashActivity, RegisterActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(i)
        finish()
    }

    class HttpsTrustManager : X509TrustManager {
        @Throws(CertificateException::class)
        override fun checkClientTrusted(
            x509Certificates: Array<X509Certificate>, s: String
        ) {
        }

        @Throws(CertificateException::class)
        override fun checkServerTrusted(
            x509Certificates: Array<X509Certificate>, s: String
        ) {
        }

        fun isClientTrusted(chain: Array<X509Certificate?>?): Boolean {
            return true
        }

        fun isServerTrusted(chain: Array<X509Certificate?>?): Boolean {
            return true
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return _AcceptedIssuers
        }

        companion object {
            private var trustManagers: Array<TrustManager>? = null
            private val _AcceptedIssuers = arrayOf<X509Certificate>()
            fun allowAllSSL() {
                HttpsURLConnection.setDefaultHostnameVerifier { arg0, arg1 -> true }
                var context: SSLContext? = null
                if (trustManagers == null) {
                    trustManagers = arrayOf(HttpsTrustManager())
                }
                try {
                    context = SSLContext.getInstance("TLS")
                    context.init(null, trustManagers, SecureRandom())
                } catch (e: NoSuchAlgorithmException) {
                    e.printStackTrace()
                } catch (e: KeyManagementException) {
                    e.printStackTrace()
                }
                HttpsURLConnection.setDefaultSSLSocketFactory(
                    context!!.socketFactory
                )
            }
        }
    }
}