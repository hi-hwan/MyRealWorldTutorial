package com.realworld.android.petsave.search.presentation

import com.realworld.android.petsave.common.presentation.Event
import com.realworld.android.petsave.common.presentation.model.UIAnimal

// Event 래퍼 클래스를 사용하여 각 필터마다 하나의 AutoCompleteTextView Adapter만 만든다는 것을 보장
data class SearchViewState(
    val noSearchQuery: Boolean = true,
    val searchResults: List<UIAnimal> = emptyList(),
    val ageFilterValues: Event<List<String>> = Event(emptyList()),
    val typeFilterValues: Event<List<String>> = Event(emptyList()),
    val searchingRemotely: Boolean = false,
    val noRemoteResults: Boolean = false,
    val failure: Event<Throwable>? = null
) {
    fun updateToReadyToSearch(ages: List<String>, types: List<String>): SearchViewState {
        return copy(
            ageFilterValues = Event(ages),
            typeFilterValues = Event(types)
        )
    }

    fun updateToNoSearchQuery(): SearchViewState {
        return copy(
            noSearchQuery = true,
            searchResults = emptyList(),
            noRemoteResults = false
        )
    }

    fun updateToSearching(): SearchViewState {
        return copy(
            noSearchQuery = false,
            searchingRemotely = false,
            noRemoteResults = false
        )
    }

    fun updateToSearchingRemotely(): SearchViewState {
        return copy(
            searchingRemotely = true,
            searchResults = emptyList()
        )
    }

    fun updateToHasSearchResults(animals: List<UIAnimal>): SearchViewState {
        return copy(
            noSearchQuery = false,
            searchResults = animals,
            searchingRemotely = false,
            noRemoteResults = false
        )
    }

    fun updateToNoResultsAvailable(): SearchViewState {
        return copy(searchingRemotely = false, noRemoteResults = true)
    }

    fun updateToHasFailure(throwable: Throwable): SearchViewState {
        return copy(failure = Event(throwable))
    }

    fun isInNoSearchResultsState(): Boolean {
        return noRemoteResults
    }
}
