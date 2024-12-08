package com.realworld.android.petsave.animalsnearyou.presentation.animaldetails

import com.realworld.android.petsave.animalsnearyou.presentation.animaldetails.model.UIAnimalDetailed

sealed class AnimalDetailsViewState {
    data object Loading : AnimalDetailsViewState()

    data class AnimalDetails(
        val animal: UIAnimalDetailed,
        val adopted: Boolean = false
    ) : AnimalDetailsViewState()

    data object Failure : AnimalDetailsViewState()
}