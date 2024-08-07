package com.realworld.android.petsave.onboarding.presentation

sealed class OnboardingEvent {
    data class PostcodeChanged(val newPostcode: String) : OnboardingEvent()
    data class DistanceChanged(val newDistance: String) : OnboardingEvent()
    object SubmitButtonClicked : OnboardingEvent()
}
