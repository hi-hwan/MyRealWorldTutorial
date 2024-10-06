package com.realworld.android.petsave.main.di

import com.realworld.android.petsave.common.data.api.PetFinderApi
import com.realworld.android.petsave.common.data.cache.Cache
import com.realworld.android.petsave.common.data.preferences.Preferences
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// [SingletonComponent]만 가능하여, [ActivityRetainedComponent]에 있는
// [AnimalRepository]와 [DispatcherProvider]는 여기서 주입할 수 없다.
@EntryPoint
@InstallIn(SingletonComponent::class)
interface SharingModuleDependencies {
    fun petFinderApi(): PetFinderApi
    fun cache(): Cache
    fun preferences(): Preferences
}