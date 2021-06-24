package com.extrainch.ngao.patterns

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class MySingleton  {
    private var mySingleton: MySingleton? = null
    private var requestQueue: RequestQueue? = null
    private var c: Context? = null

    // object of class
    fun MySingleton(context: Context) {
        c = context
        requestQueue = getRequestQueue()
    }

    fun getRequestQueue(): RequestQueue? {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(c!!.applicationContext)
        }
        return requestQueue
    }

    // method that return instance of this class
    @Synchronized
    fun getInstance(context: Context): MySingleton? {
        if (mySingleton == null) {
            mySingleton = MySingleton()
        }
        return mySingleton
    }

    // method that accepts request
    fun <T> addToRequestQueue(request: Request<T>?) {
        requestQueue!!.add(request)
    }
}