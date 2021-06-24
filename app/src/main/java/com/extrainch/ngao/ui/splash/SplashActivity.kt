package com.extrainch.ngao.ui.splash

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.extrainch.ngao.R
import com.extrainch.ngao.databinding.ActivitySplashBinding
import com.extrainch.ngao.databinding.DialogAlertBinding
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

        Log.d("phone data", "${getDeviceIMEI()}, ${getMacAddress()}, ${getPhoneName()}")

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
                val url: String = ""
                sendDeviceDetails(url)
            }
        }
    }

    private fun sendDeviceDetails(url: String) {
        // the intent will run inside post
        intentToNext()
        HttpsTrustManager.allowAllSSL()
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
        val myDevice = Build.MODEL
        return myDevice
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
        val i = Intent(this@SplashActivity, CheckUserActivity::class.java)
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
                    context!!.getSocketFactory()
                )
            }
        }
    }
}