package com.example.servicehub.session

import android.content.Context
import android.content.SharedPreferences

object UserSession {

    private const val PREFS  = "user_session"
    private const val K_CID  = "company_id"
    private const val K_PHONE = "phone"
    private const val K_FLAG  = "login_flag"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    var companyId: String
        get() = prefs?.getString(K_CID, "") ?: ""
        set(value) { prefs?.edit()?.putString(K_CID, value)?.apply() }

    var phone: String
        get() = prefs?.getString(K_PHONE, "") ?: ""
        set(value) { prefs?.edit()?.putString(K_PHONE, value)?.apply() }

    // "1", "0", or null (null = never logged in)
    var loginFlag: String?
        get() = prefs?.getString(K_FLAG, null)
        set(value) {
            val ed = prefs?.edit() ?: return
            if (value != null) ed.putString(K_FLAG, value) else ed.remove(K_FLAG)
            ed.apply()
        }

    fun clear() {
        prefs?.edit()?.remove(K_CID)?.remove(K_PHONE)?.remove(K_FLAG)?.apply()
    }
}
