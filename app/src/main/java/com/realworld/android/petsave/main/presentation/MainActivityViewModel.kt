package com.realworld.android.petsave.main.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realworld.android.petsave.common.utils.createExceptionHandler
import com.realworld.android.petsave.main.domain.usecases.OnboardingIsComplete
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.realworld.android.petsave.animalsnearyou.R as animalsNearYouR
import com.realworld.android.petsave.onboarding.R as onboardingR

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val onboardingIsComplete: OnboardingIsComplete
) : ViewModel() {

    private val _viewEffect = MutableSharedFlow<MainActivityViewEffect>()
    val viewEffect: SharedFlow<MainActivityViewEffect> get() = _viewEffect

    private val _isLoggedIn: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoggedIn get() = _isLoggedIn.asStateFlow()

    fun setIsLoggedIn(loggedIn: Boolean) {
        _isLoggedIn.value = loggedIn
    }

    fun onEvent(event: MainActivityEvent) {
        when (event) {
            is MainActivityEvent.DefineStartDestination -> defineStartDestination()
        }
    }

    private fun defineStartDestination() {
        val errorMessage = "Failed to check if onboarding is complete"
        val exceptionHandler = viewModelScope.createExceptionHandler(errorMessage) { onFailure(it) }

        viewModelScope.launch(exceptionHandler) {
            val destination = if (onboardingIsComplete()) {
                animalsNearYouR.id.nav_animalsnearyou
            } else {
                onboardingR.id.nav_onboarding
            }

            _viewEffect.emit(MainActivityViewEffect.SetStartDestination(destination))
        }
    }

    private fun onFailure(throwable: Throwable) {
        // TODO: Handle failures
    }
}
