package com.example.taskmanager.viewmodel

import android.net.Uri
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.data.model.User
import com.example.taskmanager.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        checkAuthStatus()
    }

    fun checkAuthStatus() {
        val uid = repository.currentUserId
        if (uid != null) {
            _isLoggedIn.value = true
            loadUserProfile(uid)
        } else {
            _isLoggedIn.value = false
            _currentUser.value = null
        }
    }

    private fun loadUserProfile(uid: String) {
        viewModelScope.launch {
            val user = repository.getUserProfile(uid)
            _currentUser.value = user ?: User(uid = uid, name = "User")
        }
    }

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Email and password cannot be empty."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.login(email.trim(), pass)
            _isLoading.value = false
            result.onSuccess { user ->
                _currentUser.value = user
                _isLoggedIn.value = true
            }.onFailure { exception ->
                _errorMessage.value = exception.localizedMessage ?: "Invalid email or password."
            }
        }
    }

    fun register(name: String, email: String, pass: String, confirmPass: String) {
        if (name.isBlank()) {
            _errorMessage.value = "Full Name is required."
            return
        }
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _errorMessage.value = "Please enter a valid email address."
            return
        }
        if (pass.isBlank() || pass.length < 6) {
            _errorMessage.value = "Password must be at least 6 characters."
            return
        }
        if (pass != confirmPass) {
            _errorMessage.value = "Passwords do not match."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.register(name.trim(), email.trim(), pass)
            _isLoading.value = false
            result.onSuccess { user ->
                _currentUser.value = user
                _isLoggedIn.value = true
            }.onFailure { exception ->
                _errorMessage.value = exception.localizedMessage ?: "Unable to create account."
            }
        }
    }

    fun uploadProfilePhoto(imageUri: Uri) {
        val uid = _currentUser.value?.uid ?: repository.currentUserId ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.uploadProfilePhoto(uid, imageUri)
            _isLoading.value = false
            result.onSuccess { photoUrl ->
                val updated = _currentUser.value?.copy(photoUrl = photoUrl) ?: User(
                    uid = uid,
                    photoUrl = photoUrl
                )
                _currentUser.value = updated
            }
        }
    }

    fun logout() {
        repository.logout()
        _currentUser.value = null
        _isLoggedIn.value = false
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
