package com.extrainch.ngao.patterns

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class MySingleton private constructor(private var c: Context) {
    private var requestQueue: RequestQueue?
    fun getRequestQueue(): RequestQueue? {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(c.applicationContext)
        }
        return requestQueue
    }

    // method that accepts request
    fun <T> addToRequestQueue(request: Request<T>?) {
        requestQueue!!.add(request)
    }

    companion object {
        private var mySingleton: MySingleton? = null

        // method that return instance of this class
        @Synchronized
        fun getInstance(context: Context): MySingleton? {
            if (mySingleton == null) {
                mySingleton = MySingleton(context)
            }
            return mySingleton
        }
    }

    // object of class
    init {
        requestQueue = getRequestQueue()
    }
}