package com.realworld.android.petsave.common.domain.usecases

import com.realworld.android.petsave.common.domain.model.animal.details.AnimalWithDetails
import com.realworld.android.petsave.common.domain.repositories.AnimalRepository
import com.realworld.android.petsave.common.utils.DispatchersProvider
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetAnimalDetails @Inject constructor(
    private val animalRepository: AnimalRepository,
    private val dispatchersProvider: DispatchersProvider
) {

    suspend operator fun invoke(animalId: Long): AnimalWithDetails {
        return withContext(dispatchersProvider.io()) {
            animalRepository.getAnimal(animalId)
        }
    }
}