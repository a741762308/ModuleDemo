package com.charles.module.app

import android.app.Application
import com.charles.route.RouteManager

class AppContext : Application() {
    override fun onCreate() {
        super.onCreate()
        RouteManager.init(this)
    }
}