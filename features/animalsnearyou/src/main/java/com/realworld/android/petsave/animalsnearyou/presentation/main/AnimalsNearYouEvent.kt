package com.realworld.android.petsave.animalsnearyou.presentation.main

sealed class AnimalsNearYouEvent {
    object RequestInitialAnimalsList: AnimalsNearYouEvent()
    object RequestMoreAnimals: AnimalsNearYouEvent()
}