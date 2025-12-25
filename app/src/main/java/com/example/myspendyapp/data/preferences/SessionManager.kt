package com.example.myspendyapp.data.preferences

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = prefs.edit()

    companion object {
        private const val PREF_NAME = "MySpendyApp_Session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USERNAME = "username"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_PHONE = "phone"
        private const val KEY_EMAIL = "email"
    }

    /**
     * Lưu thông tin đăng nhập
     */
    fun saveLoginSession(userId: Long, username: String, phone: String? = null, email: String? = null) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putLong(KEY_USER_ID, userId)
        editor.putString(KEY_USERNAME, username)
        editor.putString(KEY_PHONE, phone)
        editor.putString(KEY_EMAIL, email)
        editor.apply()
    }

    /**
     * Xóa phiên đăng nhập (logout)
     */
    fun clearSession() {
        editor.clear()
        editor.apply()
    }

    /**
     * Kiểm tra xem user đã đăng nhập chưa
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * Lấy username của user hiện tại
     */
    fun getUsername(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }

    /**
     * Lấy user ID của user hiện tại
     */
    fun getUserId(): Long {
        return prefs.getLong(KEY_USER_ID, -1L)
    }

    /**
     * Lấy phone của user hiện tại
     */
    fun getPhone(): String? {
        return prefs.getString(KEY_PHONE, null)
    }

    /**
     * Lấy email của user hiện tại
     */
    fun getEmail(): String? {
        return prefs.getString(KEY_EMAIL, null)
    }
}



