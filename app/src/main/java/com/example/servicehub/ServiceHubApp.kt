package com.example.servicehub

import android.app.Application
import com.example.servicehub.cart.CartManager
import com.example.servicehub.session.UserSession

class ServiceHubApp : Application() {
    override fun onCreate() {
        super.onCreate()
        UserSession.init(this)
        CartManager.init(this)
    }
}
