package com.realworld.android.petsave.animalsnearyou.presentation.animaldetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realworld.android.petsave.animalsnearyou.presentation.animaldetails.model.mappers.UiAnimalDetailsMapper
import com.realworld.android.petsave.common.domain.model.animal.details.AnimalWithDetails
import com.realworld.android.petsave.common.domain.usecases.GetAnimalDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AnimalDetailsFragmentViewModel @Inject constructor(
    private val uiAnimalDetailsMapper: UiAnimalDetailsMapper,
    private val getAnimalDetails: GetAnimalDetails,
    private val compositeDisposable: CompositeDisposable
) : ViewModel() {

    val state: StateFlow<AnimalDetailsViewState> get() = _state.asStateFlow()
    private val _state: MutableStateFlow<AnimalDetailsViewState> =
        MutableStateFlow(AnimalDetailsViewState.Loading)

    fun handleEvent(event: AnimalDetailsEvent) {
        when (event) {
            is AnimalDetailsEvent.LoadAnimalDetails -> subscribeToAnimalDetails(event.animalId)
            is AnimalDetailsEvent.AdoptAnimal -> adoptAnimal()
        }
    }

    private fun subscribeToAnimalDetails(animalId: Long) {
        viewModelScope.launch {
            try {
                val animal = getAnimalDetails(animalId)
                delay(1000)
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

    private fun adoptAnimal() {
        compositeDisposable.add(
            Observable.timer(2L, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    (_state.value as AnimalDetailsViewState.AnimalDetails?)
                        ?.copy(adopted = true)
                        ?.let {
                            _state.value = it
                        }
                }
        )
    }

    private fun onFailure(failure: Throwable) {
        _state.update { AnimalDetailsViewState.Failure }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}