package com.example.chatappstream.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatappstream.util.Constants
import dagger.hilt.android.internal.lifecycle.HiltViewModelMap
import dagger.hilt.android.lifecycle.HiltViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val client: ChatClient
) : ViewModel() {

    private val _loginEvent = MutableSharedFlow<LogInEvent>()
    val loginEvent = _loginEvent.asSharedFlow()

    private val _loadingState = MutableLiveData<UiLoadingState>()
    val loadingState: LiveData<UiLoadingState>
        get() = _loadingState


    private fun isValidUserName(userName: String): Boolean {
        return userName.length > Constants.MIN_USERNAME_LENGTH
    }

    fun loginUser(userName: String, token: String? = null) {
        val trimmedUserName = userName.trim()
        viewModelScope.launch {
            if (isValidUserName(trimmedUserName) && token != null) {// means user is registered
                loginRegisteredUser(trimmedUserName, token)
            } else if (isValidUserName(trimmedUserName) && token == null) {
                loginGuestUser(trimmedUserName)
            } else {
                _loginEvent.emit(LogInEvent.ErrorInputTooShort)

            }
        }

    }

    private fun loginRegisteredUser(userName: String, token: String) {
        val user = User(id = userName, name = userName)
        //connect user with server and authenticate

        _loadingState.value = UiLoadingState.Loading
        client.connectUser(user = user, token = token).enqueue { result ->
            _loadingState.value = UiLoadingState.NotLoading

            if (result.isSuccess) {
                viewModelScope.launch {
                    _loginEvent.emit(LogInEvent.Success)
                }
            } else {
                viewModelScope.launch {
                    _loginEvent.emit(
                        LogInEvent.ErrorLogIn(
                            result.error().message ?: "An unexpected error occur"
                        )
                    )
                }

            }

        }
    }

    private fun loginGuestUser(userName: String) {
        _loadingState.value = UiLoadingState.Loading

        client.connectGuestUser(userId = userName, username = userName).enqueue { result ->
            _loadingState.value = UiLoadingState.NotLoading

            if (result.isSuccess) {
                viewModelScope.launch {
                    _loginEvent.emit(LogInEvent.Success)
                }
            } else {
                viewModelScope.launch {
                    _loginEvent.emit(
                        LogInEvent.ErrorLogIn(
                            result.error().message ?: "An unexpected error occur"
                        )
                    )
                }

            }
        }

    }

    sealed class LogInEvent {
        object ErrorInputTooShort : LogInEvent()
        data class ErrorLogIn(val error: String) : LogInEvent()
        object Success : LogInEvent()
    }

    sealed class UiLoadingState {
        object Loading : UiLoadingState()
        object NotLoading : UiLoadingState()
    }
}