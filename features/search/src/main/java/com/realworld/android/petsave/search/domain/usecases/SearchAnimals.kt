package com.realworld.android.petsave.search.domain.usecases

import com.realworld.android.petsave.common.domain.repositories.AnimalRepository
import com.realworld.android.petsave.common.domain.model.search.SearchParameters
import com.realworld.android.petsave.common.domain.model.search.SearchResults
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.functions.Function3
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SearchAnimals @Inject constructor(
    private val animalRepository: AnimalRepository
) {
    companion object {
        private const val UI_EMPTY_VALUE = "Any"
    }

    operator fun invoke(
        querySubject: BehaviorSubject<String>,
        ageSubject: BehaviorSubject<String>,
        typeSubject: BehaviorSubject<String>
    ): Flowable<SearchResults> {
        val query = querySubject
            .debounce(500L, TimeUnit.MILLISECONDS)
            .map { it.trim() }
            .filter { it.length >= 2 || it.isEmpty() }
            .distinctUntilChanged()

        val age = ageSubject.replaceUIEmptyValue()
        val type = typeSubject.replaceUIEmptyValue()

        return Observable.combineLatest(query, age, type, combiningFunction)
            .toFlowable(BackpressureStrategy.LATEST)
            .filter { it.name.isNotEmpty() }
            .switchMap { parameters: SearchParameters ->
                animalRepository.searchCachedAnimalsBy(parameters)
            }
    }

    private val combiningFunction: Function3<String, String, String, SearchParameters>
        get() = Function3 { query, age, type -> SearchParameters(query, age, type) }

    private fun BehaviorSubject<String>.replaceUIEmptyValue(): Observable<String> {
        return map { if (it == UI_EMPTY_VALUE) "" else it }
    }
}