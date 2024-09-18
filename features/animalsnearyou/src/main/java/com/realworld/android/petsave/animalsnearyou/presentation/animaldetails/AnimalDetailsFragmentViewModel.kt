package com.realworld.android.petsave.animalsnearyou.presentation.animaldetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realworld.android.petsave.animalsnearyou.presentation.animaldetails.model.mappers.UiAnimalDetailsMapper
import com.realworld.android.petsave.common.domain.model.animal.details.AnimalWithDetails
import com.realworld.android.petsave.common.domain.usecases.GetAnimalDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnimalDetailsFragmentViewModel @Inject constructor(
    private val uiAnimalDetailsMapper: UiAnimalDetailsMapper,
    private val getAnimalDetails: GetAnimalDetails
) : ViewModel() {

    private val _state: MutableStateFlow<AnimalDetailsViewState> =
        MutableStateFlow(AnimalDetailsViewState.Loading)

    val state: StateFlow<AnimalDetailsViewState> get() = _state.asStateFlow()

    fun handleEvent(event: AnimalDetailsEvent) {
        when (event) {
            is AnimalDetailsEvent.LoadAnimalDetails -> subscribeToAnimalDetails(event.animalId)
        }
    }

    private fun subscribeToAnimalDetails(animalId: Long) {
        viewModelScope.launch {
            try {
                val animal = getAnimalDetails(animalId)

                onAnimalsDetails(animal)
            } catch (t: Throwable) {
                onFailure(t)
            }
        }
    }

    private fun onAnimalsDetails(animal: AnimalWithDetails) {
        val animalDetails = uiAnimalDetailsMapper.mapToView(animal)
        _state.update { AnimalDetailsViewState.AnimalDetails(animalDetails) }
    }

    private fun onFailure(failure: Throwable) {
        _state.update { AnimalDetailsViewState.Failure }
    }
}