package com.realworld.android.petsave.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realworld.android.petsave.common.utils.createExceptionHandler
import com.realworld.android.petsave.onboarding.R
import com.realworld.android.petsave.onboarding.domain.usecases.StoreOnboardingData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingFragmentViewModel @Inject constructor(
    private val storeOnboardingData: StoreOnboardingData
) : ViewModel() {

    companion object {
        private const val MAX_POSTCODE_LENGTH = 5
    }

    val viewState: StateFlow<OnboardingViewState> get() = _viewState
    val viewEffects: SharedFlow<OnboardingViewEffect> get() = _viewEffects

    private val _viewState = MutableStateFlow(OnboardingViewState())
    private val _viewEffects = MutableSharedFlow<OnboardingViewEffect>()

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            is OnboardingEvent.PostcodeChanged -> validateNewPostcodeValue(event.newPostcode)
            is OnboardingEvent.DistanceChanged -> validateNewDistanceValue(event.newDistance)
            is OnboardingEvent.SubmitButtonClicked -> wrapUpOnboarding()
        }
    }

    private fun validateNewPostcodeValue(newPostcode: String) {
        val validPostcode = newPostcode.length == MAX_POSTCODE_LENGTH

        val postcodeError = if (validPostcode || newPostcode.isEmpty()) {
            R.string.no_error
        } else {
            R.string.postcode_error
        }

        _viewState.update { oldState ->
            oldState.copy(
                postcode = newPostcode,
                postcodeError = postcodeError
            )
        }
    }

    private fun validateNewDistanceValue(newDistance: String) {
        val distanceError = when {
            newDistance.isNotEmpty() && newDistance.toInt() > 500 -> {
                R.string.distance_error
            }

            newDistance.isNotEmpty() && newDistance.toInt() == 0 -> {
                R.string.distance_error_cannot_be_zero
            }

            else -> {
                R.string.no_error
            }
        }

        _viewState.update { oldState ->
            oldState.copy(
                distance = newDistance,
                distanceError = distanceError
            )
        }
    }

    private fun wrapUpOnboarding() {
        val errorMessage = "Failed to store onboarding data"
        val exceptionHandler = viewModelScope.createExceptionHandler(errorMessage) { onFailure(it) }
        val (postcode, distance) = viewState.value

        viewModelScope.launch(exceptionHandler) {
            storeOnboardingData(postcode, distance)
            _viewEffects.emit(OnboardingViewEffect.NavigateToAnimalsNearYou)
        }
    }

    private fun onFailure(throwable: Throwable) {
        // TODO: Handle failures
    }
}
