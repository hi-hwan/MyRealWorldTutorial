package com.realworld.android.petsave.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realworld.android.logging.Logger
import com.realworld.android.petsave.common.domain.model.NoMoreAnimalsException
import com.realworld.android.petsave.common.domain.model.animal.Animal
import com.realworld.android.petsave.common.domain.model.pagination.Pagination
import com.realworld.android.petsave.common.presentation.model.mappers.UiAnimalMapper
import com.realworld.android.petsave.common.utils.createExceptionHandler
import com.realworld.android.petsave.common.domain.model.search.SearchParameters
import com.realworld.android.petsave.common.domain.model.search.SearchResults
import com.realworld.android.petsave.search.domain.usecases.GetSearchFilters
import com.realworld.android.petsave.search.domain.usecases.SearchAnimals
import com.realworld.android.petsave.search.domain.usecases.SearchAnimalsRemotely
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchFragmentViewModel @Inject constructor(
    private val uiAnimalMapper: UiAnimalMapper,
    private val getSearchFilters: GetSearchFilters,
    private val searchAnimals: SearchAnimals,
    private val searchAnimalsRemotely: SearchAnimalsRemotely,
    private val compositeDisposable: CompositeDisposable
) : ViewModel() {

    private val _state = MutableStateFlow(SearchViewState())
    private val querySubject = BehaviorSubject.create<String>()
    private val ageSubject = BehaviorSubject.createDefault("")
    private val typeSubject = BehaviorSubject.createDefault("")

    private var runningJobs = mutableListOf<Job>()
    private var isLastPage = false
    private var currentPage = 0

    val state: StateFlow<SearchViewState> = _state.asStateFlow()

    fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.PrepareForSearch -> prepareForSearch()
            else -> onSearchParametersUpdate(event)
        }
    }

    private fun onSearchParametersUpdate(event: SearchEvent) {
        runningJobs.map { it.cancel() }

        resetStateIfNoRemoteResults()

        when (event) {
            is SearchEvent.QueryInput -> updateQuery(event.input)
            is SearchEvent.AgeValueSelected -> updateAgeValue(event.age)
            is SearchEvent.TypeValueSelected -> updateTypeValue(event.type)
            else -> Logger.d("Wrong SearchEvent in onSearchParametersUpdates!")
        }
    }

    private fun resetStateIfNoRemoteResults() {
        if (state.value.isInNoSearchResultsState()) {
            _state.value = state.value.updateToSearching()
        }
    }

    private fun prepareForSearch() {
        loadFilterValues()
        setupSearchSubscription()
    }

    private fun setupSearchSubscription() {
        searchAnimals(querySubject, ageSubject, typeSubject)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { onSearchResults(it) },
                { onFailure(it) }
            )
            .addTo(compositeDisposable)
    }

    private fun onSearchResults(searchResults: SearchResults) {
        val (animals, searchParameters) = searchResults

        if (animals.isEmpty()) {
            onEmptyCacheResults(searchParameters)
        } else {
            onAnimalList(animals)
        }
    }

    private fun onEmptyCacheResults(searchParameters: SearchParameters) {
        _state.update { oldState ->
            oldState.updateToSearchingRemotely()
        }
        searchRemotely(searchParameters)
    }

    private fun searchRemotely(searchParameters: SearchParameters) {
        val exceptionHandler = createExceptionHandler(message = "Failed to search remotely.")

        val job = viewModelScope.launch(exceptionHandler) {
            Logger.d("Searching remotely...")
            val pagination = searchAnimalsRemotely(
                ++currentPage,
                searchParameters,
            )

            onPaginationInfoObtained(pagination)
        }
        runningJobs.add(job)

        job.invokeOnCompletion {
            it?.printStackTrace()
            runningJobs.remove(job)
        }
    }

    private fun updateQuery(input: String) {
        resetPagination()

        querySubject.onNext(input)

        if (input.isEmpty()) {
            setNoSearchQueryState()
        } else {
            setSearchingState()
        }
    }

    private fun updateAgeValue(age: String) {
        ageSubject.onNext(age)
    }

    private fun updateTypeValue(type: String) {
        typeSubject.onNext(type)
    }

    private fun createExceptionHandler(message: String): CoroutineExceptionHandler {
        return viewModelScope.createExceptionHandler(message) {
            onFailure(it)
        }
    }

    private fun setSearchingState() {
        _state.update { oldState -> oldState.updateToSearching() }
    }

    private fun setNoSearchQueryState() {
        _state.update { oldState -> oldState.updateToNoSearchQuery() }
    }

    private fun onAnimalList(animals: List<Animal>) {
        _state.update { oldState ->
            oldState.updateToHasSearchResults(animals.map { uiAnimalMapper.mapToView(it) })
        }
    }

    private fun resetPagination() {
        currentPage = 0
    }

    private fun onPaginationInfoObtained(pagination: Pagination) {
        currentPage = pagination.currentPage
    }

    private fun onFailure(throwable: Throwable) {
        _state.update { oldState ->
            if (throwable is NoMoreAnimalsException) {
                oldState.updateToNoResultsAvailable()
            } else {
                oldState.updateToHasFailure(throwable)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    private fun loadFilterValues() {
        val exceptionHandler = createExceptionHandler(
            message = "Failed to get filter values!"
        )

        viewModelScope.launch(exceptionHandler) {
            val (ages, types) = getSearchFilters()
            updateStateWithFilterValues(ages, types)
        }
    }

    private fun updateStateWithFilterValues(
        ages: List<String>,
        types: List<String>
    ) {
        _state.update { oldState ->
            oldState.updateToReadyToSearch(ages, types)
        }
    }
}
