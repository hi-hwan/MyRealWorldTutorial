package com.realworld.android.petsave.main.domain.usecases

import com.realworld.android.petsave.common.domain.repositories.AnimalRepository
import com.realworld.android.petsave.common.utils.DispatchersProvider
import kotlinx.coroutines.withContext
import javax.inject.Inject

class OnboardingIsComplete @Inject constructor(
    private val repository: AnimalRepository,
    private val dispatchersProvider: DispatchersProvider
) {
    suspend operator fun invoke() = withContext(dispatchersProvider.io()) {
        repository.onboardingIsComplete()
    }
}
