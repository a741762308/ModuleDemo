package com.charles.route

import android.app.Application
import android.content.Context
import android.content.Intent


object RouteManager {
    private val sClassMap = HashMap<String, Class<out Any>>()

    fun init(context: Application) {

    }

    fun startActivity(context: Context, path: String) {
        sClassMap[path]?.run {
            context.startActivity(Intent(context, this))
        }
    }
}