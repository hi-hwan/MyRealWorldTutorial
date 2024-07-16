package com.realworld.android.petsave.main.presentation

import androidx.annotation.IdRes

sealed class MainActivityViewEffect {
    data class SetStartDestination(@IdRes val destination: Int) : MainActivityViewEffect()
}
