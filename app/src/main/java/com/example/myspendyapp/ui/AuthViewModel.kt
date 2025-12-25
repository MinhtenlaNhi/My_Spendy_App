package com.example.myspendyapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myspendyapp.data.db.AppDatabase
import com.example.myspendyapp.data.db.entity.User
import com.example.myspendyapp.data.preferences.SessionManager
import kotlinx.coroutines.launch

sealed class AuthResult {
    object Loading : AuthResult()
    data class Success(val message: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Idle : AuthResult()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val userDao = AppDatabase.getDatabase(application).userDao()
    private val sessionManager = SessionManager(application)

    private val _loginResult = MutableLiveData<AuthResult>(AuthResult.Idle)
    val loginResult: LiveData<AuthResult> = _loginResult

    private val _registerResult = MutableLiveData<AuthResult>(AuthResult.Idle)
    val registerResult: LiveData<AuthResult> = _registerResult

    private val _changePasswordResult = MutableLiveData<AuthResult>(AuthResult.Idle)
    val changePasswordResult: LiveData<AuthResult> = _changePasswordResult

    private val _resetPasswordResult = MutableLiveData<AuthResult>(AuthResult.Idle)
    val resetPasswordResult: LiveData<AuthResult> = _resetPasswordResult

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    fun login(username: String, password: String) {
        _loginResult.postValue(AuthResult.Loading)

        viewModelScope.launch {
            val user = userDao.findByUsername(username)
            if (user != null && user.password == password) {
                // Lưu phiên đăng nhập
                sessionManager.saveLoginSession(
                    userId = user.id,
                    username = user.username,
                    phone = user.phone,
                    email = user.email
                )
                _currentUser.postValue(user)
                _loginResult.postValue(AuthResult.Success("Đăng nhập thành công!"))
            } else {
                _loginResult.postValue(AuthResult.Error("Sai tên đăng nhập hoặc mật khẩu."))
            }
        }
    }

    @JvmOverloads
    fun register(username: String, phone: String, password: String, email: String? = null) {
        _registerResult.postValue(AuthResult.Loading)

        viewModelScope.launch {
            if (username.length < 5) {
                _registerResult.postValue(AuthResult.Error("Tên đăng nhập phải có ít nhất 5 ký tự."))
                return@launch
            }

            if (userDao.findByUsername(username) != null) {
                _registerResult.postValue(AuthResult.Error("Tên đăng nhập đã tồn tại."))
                return@launch
            }

            if (phone.isNotEmpty() && userDao.findByPhone(phone) != null) {
                _registerResult.postValue(AuthResult.Error("Số điện thoại đã được sử dụng."))
                return@launch
            }

            if (email != null && email.isNotEmpty() && userDao.findByEmail(email) != null) {
                _registerResult.postValue(AuthResult.Error("Email đã được sử dụng."))
                return@launch
            }

            if (password.length < 6) {
                _registerResult.postValue(AuthResult.Error("Mật khẩu phải có ít nhất 6 ký tự."))
                return@launch
            }

            // Save the new user to the database
            userDao.insert(User(
                username = username,
                phone = phone.ifEmpty { null },
                email = email,
                password = password
            ))
            _registerResult.postValue(AuthResult.Success("Đăng ký thành công! Vui lòng đăng nhập."))
        }
    }

    /**
     * Đăng xuất - xóa phiên đăng nhập
     */
    fun logout() {
        sessionManager.clearSession()
        _currentUser.postValue(null)
    }

    /**
     * Kiểm tra xem user đã đăng nhập chưa (từ session)
     */
    fun checkLoginSession() {
        viewModelScope.launch {
            if (sessionManager.isLoggedIn()) {
                val userId = sessionManager.getUserId()
                val user = userDao.findById(userId)
                if (user != null) {
                    _currentUser.postValue(user)
                } else {
                    // User không tồn tại trong DB, xóa session
                    sessionManager.clearSession()
                }
            }
        }
    }

    /**
     * Lấy thông tin user hiện tại từ session
     */
    fun getCurrentUserFromSession(): User? {
        return if (sessionManager.isLoggedIn()) {
            val userId = sessionManager.getUserId()
            // Trả về null, sẽ load từ DB trong checkLoginSession
            null
        } else {
            null
        }
    }

    fun resetLoginResult() {
        _loginResult.postValue(AuthResult.Idle)
    }

    fun resetRegisterResult() {
        _registerResult.postValue(AuthResult.Idle)
    }

    /**
     * Thay đổi mật khẩu
     */
    fun changePassword(currentPassword: String, newPassword: String) {
        _changePasswordResult.postValue(AuthResult.Loading)

        viewModelScope.launch {
            if (!sessionManager.isLoggedIn()) {
                _changePasswordResult.postValue(AuthResult.Error("Bạn chưa đăng nhập"))
                return@launch
            }

            val userId = sessionManager.getUserId()
            val user = userDao.findById(userId)

            if (user == null) {
                _changePasswordResult.postValue(AuthResult.Error("Không tìm thấy thông tin người dùng"))
                return@launch
            }

            // Kiểm tra mật khẩu hiện tại
            if (user.password != currentPassword) {
                _changePasswordResult.postValue(AuthResult.Error("Mật khẩu hiện tại không đúng"))
                return@launch
            }

            // Kiểm tra mật khẩu mới
            if (newPassword.length < 6) {
                _changePasswordResult.postValue(AuthResult.Error("Mật khẩu mới phải có ít nhất 6 ký tự"))
                return@launch
            }

            // Cập nhật mật khẩu
            val updatedUser = user.copy(password = newPassword)
            userDao.update(updatedUser)

            _changePasswordResult.postValue(AuthResult.Success("Đổi mật khẩu thành công!"))
        }
    }

    fun resetChangePasswordResult() {
        _changePasswordResult.postValue(AuthResult.Idle)
    }

    /**
     * Reset password - cập nhật mật khẩu dựa trên số điện thoại (dùng cho quên mật khẩu)
     */
    fun resetPassword(phoneNumber: String, newPassword: String) {
        _resetPasswordResult.postValue(AuthResult.Loading)

        viewModelScope.launch {
            if (phoneNumber.isEmpty()) {
                _resetPasswordResult.postValue(AuthResult.Error("Số điện thoại không được để trống"))
                return@launch
            }

            // Tìm user theo số điện thoại
            val user = userDao.findByPhone(phoneNumber)

            if (user == null) {
                _resetPasswordResult.postValue(AuthResult.Error("Không tìm thấy tài khoản với số điện thoại này"))
                return@launch
            }

            // Kiểm tra mật khẩu mới
            if (newPassword.length < 6) {
                _resetPasswordResult.postValue(AuthResult.Error("Mật khẩu phải có ít nhất 6 ký tự"))
                return@launch
            }

            // Cập nhật mật khẩu
            val updatedUser = user.copy(password = newPassword)
            userDao.update(updatedUser)

            _resetPasswordResult.postValue(AuthResult.Success("Đặt lại mật khẩu thành công!"))
        }
    }

    fun resetResetPasswordResult() {
        _resetPasswordResult.postValue(AuthResult.Idle)
    }
}
